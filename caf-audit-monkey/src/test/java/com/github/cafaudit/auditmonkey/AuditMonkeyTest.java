/*
 * Copyright 2015-2018 Micro Focus or one of its affiliates.
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
package com.github.cafaudit.auditmonkey;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import com.hpe.caf.auditing.AuditChannel;

public class AuditMonkeyTest
{
    private AuditChannel channel;
    private StandardMonkey standardMonkey;
    
    @Before
    public void setUp() {
        
        channel = mock(AuditChannel.class);
        standardMonkey = mock(StandardMonkey.class);
        
    }

    @Test
    public void shouldHaveMultiThreadsAreCreated() throws Exception {
        AuditMonkey.runTheMonkey(channel, standardMonkey, 10);
        verify(standardMonkey, times(10)).run();
    }
    
}
