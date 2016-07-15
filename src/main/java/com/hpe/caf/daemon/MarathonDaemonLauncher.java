package com.hpe.caf.daemon;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hpe.caf.services.audit.api.AppConfig;
import java.lang.reflect.Type;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.MarathonClient;
import mesosphere.marathon.client.model.v2.*;
import mesosphere.marathon.client.utils.MarathonException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public final class MarathonDaemonLauncher implements DaemonLauncher {

    private final AppConfig properties;

    private static final String ERR_MSG_MARATHON_HOST_URL_NOT_SPECIFIED = "The Marathon host has not been specified.";
    private static final String ERR_MSG_MARATHON_FAILED_TO_LAUNCH_SCHEDULER = "Failed to launch scheduler";

    private static final String MARATHON_SCHEDULER_GROUPS_ID = "caf-audit-schedulers";

    private static final Logger LOG = LoggerFactory.getLogger(MarathonDaemonLauncher.class);

    public MarathonDaemonLauncher(final AppConfig properties) {
        this.properties = properties;
    }

    @Override
    public void launch(
        final String id,
        final String image,
        final String[] args
    ) throws Exception {

        //  Get Marathon specific properties.
        String marathonEndpoint = properties.getMarathonUrl();
        String marathonCPUs = properties.getMarathonCPUs();
        String marathonMem = properties.getMarathonMem();
        String marathonInstances = "1";
        String marathonConstraints = properties.getMarathonConstraints();
        String marathonContainerType = "DOCKER";
        String marathonContainerDockerCredentials = properties.getMarathonContainerDockerCredentials();
        String marathonContainerDockerNetwork = properties.getMarathonContainerDockerNetwork();
        String marathonContainerDockerForcePullImage = properties.getMarathonContainerDockerForcePullImage();

        LOG.info("launch: Launching Scheduler via Marathon...");

        if(StringUtils.isNotEmpty(marathonEndpoint)){

            try {
                final Marathon marathon = MarathonClient.getInstance(marathonEndpoint);

                //  Check if audit scheduler group already exists. MarathonException with 404 will be thrown if it does
                //  not exist.
                Group auditSchedulerGroup;
                try {
                    //  Create new group.
                    auditSchedulerGroup = new Group();
                    auditSchedulerGroup.setId(MARATHON_SCHEDULER_GROUPS_ID);

                    LOG.debug("launch: Creating scheduler group...");
                    marathon.createGroup(auditSchedulerGroup);

                } catch (MarathonException me) {

                    //  Use 409 to trap and ignore conflict errors where the group already exists.
                    if (409 == me.getStatus() && me.getMessage().contains("Conflict")) {
                        LOG.debug("launch: Scheduler group already exists...");
                    }
                    else {
                        throw me;
                    }
                }

                final App auditManagementApp = new App();
                auditManagementApp.setId("/" + MARATHON_SCHEDULER_GROUPS_ID + "/" + id);
                auditManagementApp.setArgs(Arrays.asList(args));
                auditManagementApp.setCpus(Double.parseDouble(marathonCPUs));
                auditManagementApp.setMem(Double.parseDouble(marathonMem));
                auditManagementApp.setInstances(Integer.parseInt(marathonInstances));
                auditManagementApp.setConstraints(parseConstraints(marathonConstraints));

                List<String> uris = Arrays.asList(marathonContainerDockerCredentials);
                auditManagementApp.setUris(uris);

                Container auditManagementContainer = new Container();
                auditManagementContainer.setType(marathonContainerType);

                Docker auditManagementDocker = new Docker();
                auditManagementDocker.setImage(image);
                auditManagementDocker.setNetwork(marathonContainerDockerNetwork);
                auditManagementDocker.setForcePullImage(Boolean.parseBoolean(marathonContainerDockerForcePullImage));

                auditManagementContainer.setDocker(auditManagementDocker);

                auditManagementApp.setContainer(auditManagementContainer);

                marathon.createApp(auditManagementApp);

                LOG.info("launch: Scheduler launched via Marathon...");

            } catch (Exception Ex) {
                LOG.error("launch: Error - '{}'", ERR_MSG_MARATHON_FAILED_TO_LAUNCH_SCHEDULER);
                throw new Exception(ERR_MSG_MARATHON_FAILED_TO_LAUNCH_SCHEDULER + " : " + Ex);
            }
        }
        else
        {
            LOG.error("launch: Error - '{}'", ERR_MSG_MARATHON_HOST_URL_NOT_SPECIFIED);
            throw new Exception(ERR_MSG_MARATHON_HOST_URL_NOT_SPECIFIED);
        }

    }

    /**
     * Parses the specified JSON-encoded Marathon constraints string.
     * @param constraintsString The string to be parsed.
     * @return The constraints as a Java list of list of strings.
     */
    private static List<List<String>> parseConstraints(
        final String constraintsString
    ) {
        // Check whether a constraint is actually specified
        if (StringUtils.isEmpty(constraintsString)) {
            return null;
        }

        // Construct an object for parsing the JSON-encoded constraints string
        final Gson gson = new Gson();

        // Parse the constraints string and return it
        final Type listOfStringListsType = new TypeToken<List<List<String>>>(){}.getType();

        return gson.fromJson(constraintsString, listOfStringListsType);
    }
}

