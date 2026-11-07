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

package org.opengroup.osdu.entitlements.v2.azure.config;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Task scheduler dedicated to Gremlin token refresh and retry work.
 */
@Component
public class GremlinTaskScheduler extends ThreadPoolTaskScheduler {

    @PostConstruct
    public void initialize() {
        setPoolSize(1);
        setThreadNamePrefix("gremlin-refresh-");
        setAwaitTerminationSeconds(30);
        setWaitForTasksToCompleteOnShutdown(true);
        super.initialize();
    }
}
