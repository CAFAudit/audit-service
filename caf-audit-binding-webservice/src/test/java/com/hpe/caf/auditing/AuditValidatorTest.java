/*
 * Copyright 2015-2024 Open Text.
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
package com.hpe.caf.auditing;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AuditValidatorTest
{

    @Test
    public void testValidateString_Success()
    {
        AuditValidator.validateString("param1", "test", 1, 5);
    }

    @Test
    public void testValidateString_Failure_StringFieldTooLong()
    {
        Assertions.assertThrows(AuditValidatorException.class, () ->
                AuditValidator.validateString("param1", "test", -1, 3));
    }

    @Test
    public void testValidateString_Failure__StringFieldTooShort()
    {
        Assertions.assertThrows(AuditValidatorException.class, () ->
                AuditValidator.validateString("param1", "test", 5, -1));
    }

}
