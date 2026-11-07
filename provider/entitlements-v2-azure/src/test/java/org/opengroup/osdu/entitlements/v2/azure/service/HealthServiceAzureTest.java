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

package org.opengroup.osdu.entitlements.v2.azure.service;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opengroup.osdu.azure.cache.IRedisClientFactory;
import org.opengroup.osdu.core.common.cache.IRedisCache;
import org.opengroup.osdu.core.common.cache.RedisCache;
import org.opengroup.osdu.entitlements.v2.azure.config.CacheConfig;
import org.opengroup.osdu.entitlements.v2.model.ParentReferences;
import org.opengroup.osdu.entitlements.v2.service.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import redis.embedded.RedisServer;

@SpringBootTest
@RunWith(SpringRunner.class)
public class HealthServiceAzureTest {

    @Autowired
    private HealthService healthService;

    @MockitoBean
    private CacheConfig cacheConfig;

    @MockitoBean
    private IRedisClientFactory redisClientFactory;

    @MockitoSpyBean
    private IRedisCache<String, ParentReferences> redisGroupCache;

    private static RedisServer redisServer;
    private static RedisCache<String, ParentReferences> redisCache;

    @BeforeClass
    public static void setupClass() {
        redisServer = RedisServer.builder().port(7000).build();
        redisServer.start();
        redisCache = new RedisCache<String, ParentReferences>("localhost", 7000, 3600, String.class, ParentReferences.class);
    }

    @AfterClass
    public static void end() {
        redisServer.stop();
    }

    @Before
    public void setup() {
        Mockito.when(redisClientFactory.getClient(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(redisCache);
    }

    @Test
    public void shouldSucceedInHealthCheck() {
        healthService.performHealthCheck();
        Mockito.verify(redisGroupCache).get("some-key");
    }
}