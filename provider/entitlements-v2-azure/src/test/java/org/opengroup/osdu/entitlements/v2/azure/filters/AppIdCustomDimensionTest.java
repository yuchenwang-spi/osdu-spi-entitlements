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

package org.opengroup.osdu.entitlements.v2.azure.filters;

import tools.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.web.internal.RequestTelemetryContext;
import com.microsoft.applicationinsights.web.internal.ThreadContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.entitlements.v2.api.AddMemberApi;
import org.opengroup.osdu.entitlements.v2.auth.AuthorizationService;
import org.opengroup.osdu.entitlements.v2.azure.AzureAppProperties;
import org.opengroup.osdu.entitlements.v2.azure.config.CacheConfig;
import org.opengroup.osdu.entitlements.v2.azure.configuration.TestComponentScan;
import org.opengroup.osdu.entitlements.v2.model.Role;
import org.opengroup.osdu.entitlements.v2.model.addmember.AddMemberDto;
import org.opengroup.osdu.entitlements.v2.service.AddMemberService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestComponentScan.class)
@WebMvcTest(controllers = AddMemberApi.class)
public class AppIdCustomDimensionTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private AddMemberService service;
    @MockitoBean
    private JaxRsDpsLog logger;
    @MockitoBean
    private ITenantFactory tenantFactory;
    @MockitoBean
    private AuthorizationService authService;
    @MockitoBean
    private CacheConfig cacheConfig;
    @MockitoBean
    private AzureAppProperties azureAppProperties;

    @Test
    public void shouldMatchExpectedHttpRequest() throws Exception {
        final TenantInfo tenantInfo = new TenantInfo();
        tenantInfo.setDataPartitionId("common");
        tenantInfo.setServiceAccount("internal-service-account");
        Mockito.when(tenantFactory.listTenantInfo()).thenReturn(Collections.singletonList(tenantInfo));
        Mockito.when(tenantFactory.getTenantInfo("common")).thenReturn(tenantInfo);
        Mockito.when(authService.isCurrentUserAuthorized(ArgumentMatchers.any(), ArgumentMatchers.any(String[].class))).thenReturn(true);
        Mockito.when(azureAppProperties.getDomain()).thenReturn("contoso.com");
        String appIdValue = "app-id-value";
        performRequest(appIdValue);
        Assert.assertTrue(MDC.getCopyOfContextMap().entrySet().stream()
                .anyMatch(entry -> DpsHeaders.APP_ID.equals(entry.getKey()) && appIdValue.equals(entry.getValue())));
    }

    private void performRequest(String appId) throws Exception {
        AddMemberDto dto = new AddMemberDto("a@common.com", Role.OWNER);
        String group = "service.viewers.users@common.contoso.com";
        try (MockedStatic<ThreadContext> contextMockedStatic = Mockito.mockStatic(ThreadContext.class)) {
            contextMockedStatic.when(ThreadContext::getRequestTelemetryContext).thenReturn(new RequestTelemetryContext(System.currentTimeMillis()));
            mockMvc.perform(MockMvcRequestBuilders.post("/groups/{group_email}/members", group)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8.toString())
                            .header(DpsHeaders.AUTHORIZATION, "Bearer token")
                            .header(DpsHeaders.DATA_PARTITION_ID, "common")
                            .header(DpsHeaders.USER_ID, "a@b.com")
                            .header(DpsHeaders.APP_ID, appId)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        }
    }
}
