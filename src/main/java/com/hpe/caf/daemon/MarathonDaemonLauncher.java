package com.hpe.caf.daemon;

import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.MarathonClient;
import mesosphere.marathon.client.model.v2.*;
import mesosphere.marathon.client.utils.MarathonException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MarathonDaemonLauncher implements DaemonLauncher {

    private static String marathonEndpoint;

    private static final String ERR_MSG_MARATHON_HOST_URL_NOT_SPECIFIED = "The Marathon host has not been specified.";
    private static final String ERR_MSG_MARATHON_FAILED_TO_LAUNCH_SCHEDULER = "Failed to launch scheduler";

    private static final String MARATHON_SCHEDULER_GROUPS_ID = "caf-audit-schedulers";

    private static final Logger LOG = LoggerFactory.getLogger(MarathonDaemonLauncher.class);

    public MarathonDaemonLauncher(String marathonEndpoint) {
        this.marathonEndpoint = marathonEndpoint;
    }

    @Override
    public void launch(
            final String id,
            final String image,
            final String[] args,
            final String marathonCPUs,
            final String marathonMem,
            final String marathonInstances,
            final String[] marathonURIs,
            final String marathonContainerType,
            final String marathonContainerNetwork,
            final String marathonDockerForcePullImage
    ) throws Exception {
        LOG.info("launch: Launching Scheduler via Marathon...");

        if(StringUtils.isNotEmpty(marathonEndpoint)){

            try {
                final Marathon marathon = MarathonClient.getInstance(marathonEndpoint);

                //  Check if audit scheduler group already exists. MarathonException with 404 will be thrown if it does
                //  not exist.
                Group auditSchedulerGroup;
                try {
                    auditSchedulerGroup = marathon.getGroup(MARATHON_SCHEDULER_GROUPS_ID);

                } catch (MarathonException me) {
                    //  Use 404 error as group existence check.
                    if (404 == me.getStatus()) {
                        //  Create new group as it does not yet exist.
                        final Group newAuditSchedulerGroup = new Group();
                        newAuditSchedulerGroup.setId(MARATHON_SCHEDULER_GROUPS_ID);
                        marathon.createGroup(newAuditSchedulerGroup);
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

                List<String> uris = Arrays.asList(marathonURIs);
                auditManagementApp.setUris(uris);

                Container auditManagementContainer = new Container();
                auditManagementContainer.setType(marathonContainerType);

                Docker auditManagementDocker = new Docker();
                auditManagementDocker.setImage(image);
                auditManagementDocker.setNetwork(marathonContainerNetwork);
                auditManagementDocker.setForcePullImage(Boolean.parseBoolean(marathonDockerForcePullImage));

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
}

