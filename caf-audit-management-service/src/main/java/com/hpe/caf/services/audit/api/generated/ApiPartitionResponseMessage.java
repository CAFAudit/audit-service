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
package com.hpe.caf.services.audit.api.generated;

/**
 * Created by CS on 14/04/2016.
 */
public class ApiPartitionResponseMessage {

    private int partitionsAdded;
    private String message;

    public ApiPartitionResponseMessage(){}

    public ApiPartitionResponseMessage(int partitionsAdded) {
        this.partitionsAdded = partitionsAdded;
    }

    public ApiPartitionResponseMessage(String message) {
        this.message = message;
    }

    public ApiPartitionResponseMessage(int partitionsAdded, String message) {
        this.partitionsAdded = partitionsAdded;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getPartitionsAdded() {
        return partitionsAdded;
    }

    public void setPartitionsAdded(int partitionsAdded) {
        this.partitionsAdded = partitionsAdded;
    }
}
