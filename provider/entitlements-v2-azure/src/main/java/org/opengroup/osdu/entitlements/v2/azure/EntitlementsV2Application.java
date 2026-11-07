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

package org.opengroup.osdu.entitlements.v2.azure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan(basePackages = {
        "org.opengroup.osdu.core",
        "org.opengroup.osdu.azure",
        "org.opengroup.osdu.entitlements.v2"
}, excludeFilters = {
        // core-lib-azure 4.x extends a Boot 3 actuator class that Boot 4 moved; matched by name so the class is never loaded.
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org\\.opengroup\\.osdu\\.azure\\.health\\.AzureHealthEndpointWebExtension")
})
@SpringBootApplication
@EnableAsync
@EnableScheduling
@PropertySource("classpath:swagger.properties")
public class EntitlementsV2Application {
    public static void main(String[] args) {
        Class<?>[] sources = new Class<?>[] { EntitlementsV2Application.class };
        SpringApplication.run(sources, args);
    }
}
