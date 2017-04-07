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
package com.hpe.caf.audit.management;

import java.util.Arrays;

public final class Program {
    public static void main(String[] args) throws Exception {

        // Check that the command argument has been specified
        if (args.length == 0) {
            System.err.println("Command not specified");
            System.exit(1);
        }

        // Get the command argument and the rest of the arguments
        final String command = args[0];
        final String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);

        // Emulate the standard vkconfig arguments -
        // obviously we can add new commands for custom behaviour
        switch(command) {
            case "scheduler":
                com.vertica.solutions.kafka.cli.SchedulerConfigurationCLI.main(commandArgs);
                return;
            case "topic":
                com.vertica.solutions.kafka.cli.TopicConfigurationCLI.main(commandArgs);
                return;
            case "launch":
                com.vertica.solutions.kafka.Launcher.main(commandArgs);
                return;
            default:
                System.err.println("Command not recognised: " + command);
                System.exit(1);
        }
    }
}
