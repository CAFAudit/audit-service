/*
 * Copyright 2015-2023 Open Text.
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

import java.text.MessageFormat;

final class AuditValidator
{

    /**
     * Validates the field length and throws an appropriate exception if min or max constraints are exceeded.
     */
    public static void validateString(String fieldName, String fieldValue, int minLength, int maxLength)
    {
        Integer fieldLength = fieldValue.length();

        if (minLength != -1) {
            if (fieldLength < minLength) {
                throw new AuditValidatorException(MessageFormat.format("Field name {0} is too short, minimum is {1} characters.", fieldName, minLength));
            }
        }

        if (maxLength != -1) {
            if (fieldLength > maxLength) {
                throw new AuditValidatorException(MessageFormat.format("Field name {0} is too long, maximum is {1} characters.", fieldName, maxLength));
            }
        }
    }

}
