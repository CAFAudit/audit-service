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

import com.hpe.caf.api.Configuration;

import java.util.List;

@Configuration
public class ElasticAuditConfiguration {

    public ElasticAuditConfiguration() {

    }

    private List<String> hostNames;

    private int port;

    private String clusterName;

    public List<String> getHostNames() {
        return hostNames;
    }

    public void setHostnames(List<String> hostNames) {
        this.hostNames = hostNames;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getClusterName() {
        if (clusterName ==  null) {
            //  Default cluster name.
            clusterName = "elasticsearch";
        }
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
}
