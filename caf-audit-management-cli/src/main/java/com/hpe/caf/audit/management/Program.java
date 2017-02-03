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
