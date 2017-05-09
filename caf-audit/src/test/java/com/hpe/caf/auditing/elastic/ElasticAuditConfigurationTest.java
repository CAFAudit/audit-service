/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development LP.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hpe.caf.auditing.elastic;

import org.junit.Assert;
import org.junit.Test;

public class ElasticAuditConfigurationTest {

    @Test
    public void testGetClusterName() throws Exception {
        ElasticAuditConfiguration config = new ElasticAuditConfiguration();
        String defaultClusterName = config.getClusterName();
        Assert.assertEquals("elasticsearch-cluster", defaultClusterName);
    }

}
