//  Copyright © Microsoft Corporation
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.opengroup.osdu.entitlements.v2.util;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

public class AzureTokenServiceTest {

    @Test
    public void shouldPreferUniqueNameForV2Token() {
        String token = bearer("{\"ver\":\"2.0\",\"unique_name\":\"User@example.test\","
                + "\"oid\":\"object-id\",\"azp\":\"client-id\"}");

        assertEquals("User@example.test", AzureTokenService.callerId(token));
    }

    @Test
    public void shouldPreferUniqueNameForV1Token() {
        String token = bearer("{\"ver\":\"1.0\",\"unique_name\":\"User@example.test\","
                + "\"oid\":\"object-id\",\"appid\":\"client-id\",\"upn\":\"other@example.test\"}");

        assertEquals("User@example.test", AzureTokenService.callerId(token));
    }

    @Test
    public void shouldPreferObjectIdForV2TokenWithNullUniqueName() {
        String token = bearer("{\"ver\":\"2.0\",\"unique_name\":null,"
                + "\"oid\":\"object-id\",\"azp\":\"client-id\"}");

        assertEquals("object-id", AzureTokenService.callerId(token));
    }

    @Test
    public void shouldUseAuthorizedPartyForV2TokenWithNullObjectId() {
        String token = bearer("{\"ver\":\"2.0\",\"oid\":null,\"azp\":\"client-id\"}");

        assertEquals("client-id", AzureTokenService.callerId(token));
    }

    @Test
    public void shouldUseApplicationIdForV1ApplicationToken() {
        String token = bearer("{\"ver\":\"1.0\",\"oid\":\"object-id\","
                + "\"appid\":\"client-id\",\"upn\":\"user@example.test\"}");

        assertEquals("client-id", AzureTokenService.callerId(token));
    }

    @Test
    public void shouldUseUpnForV1TokenWithoutApplicationId() {
        String token = bearer("{\"ver\":\"1.0\",\"oid\":\"object-id\","
                + "\"appid\":null,\"upn\":\"user@example.test\"}");

        assertEquals("user@example.test", AzureTokenService.callerId(token));
    }

    @Test
    public void shouldTreatMissingVersionAsV1() {
        String token = bearer("{\"oid\":\"object-id\",\"appid\":\"client-id\"}");

        assertEquals("client-id", AzureTokenService.callerId(token));
    }

    @Test
    public void shouldRejectV2TokenWithOnlyV1CallerClaims() {
        assertNoCaller("{\"ver\":\"2.0\",\"appid\":\"client-id\",\"upn\":\"user@example.test\"}");
    }

    @Test
    public void shouldRequireObjectIdForV1ApplicationClaim() {
        assertNoCaller("{\"ver\":\"1.0\",\"appid\":\"client-id\"}");
    }

    @Test
    public void shouldRejectTokenWithoutCallerClaims() {
        assertNoCaller("{\"ver\":\"2.0\",\"unique_name\":null,\"oid\":null,\"azp\":null}");
    }

    @Test
    public void shouldRejectTokenWithoutPayload() {
        assertMalformedToken("not-a-jwt");
    }

    @Test
    public void shouldRejectInvalidBase64Payload() {
        assertMalformedToken("e30.***.signature");
    }

    @Test
    public void shouldRejectInvalidJsonPayload() {
        assertMalformedToken(bearer("not-json"));
    }

    private static void assertNoCaller(String claims) {
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> AzureTokenService.callerId(bearer(claims)));

        assertEquals("INTEGRATION_TESTER_ACCESS_TOKEN names no caller", error.getMessage());
    }

    private static void assertMalformedToken(String token) {
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> AzureTokenService.callerId(token));

        assertEquals("INTEGRATION_TESTER_ACCESS_TOKEN is not a JWT", error.getMessage());
        assertNotNull(error.getCause());
    }

    private static String bearer(String claims) {
        // Synthetic payloads exercise claim selection, not signature validation.
        return "e30." + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(claims.getBytes(StandardCharsets.UTF_8)) + ".signature";
    }
}
