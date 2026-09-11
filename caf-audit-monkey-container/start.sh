#!/bin/sh
#
# Copyright 2015-2026 Open Text.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


cd /maven
if [ "$OTEL_JAVAAGENT_ENABLED" = "true" ]
then
  export OTEL_SERVICE_NAME=audit-monkey
fi
java $(${OTEL_GET_JAVA_TOOL_OPTIONS}) $CAF_AUDIT_MONKEY_JAVA_OPTS -cp "*" com.github.cafaudit.auditmonkey.AuditMonkey
