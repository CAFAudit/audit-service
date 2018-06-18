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

import java.util.Date;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.cafaudit.AuditLog;
import com.hpe.caf.auditing.AuditChannel;

public class DemoMonkey implements Monkey, Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger(DemoMonkey.class);

    private static final Random GENERATOR = new Random();
    
    private AuditChannel channel;
    private MonkeyConfig monkeyConfig;
    private BlockingQueue<Integer> queue;
    /*
     * Setting up variables for the random generation of Audit Events
     */
    private String[] auditEvents = {"createDocument", "createUser", "deleteDocument", "deleteUser", "login", "logout", "moveDocument", "readDocument"};
    private String[] tenantIds = {"00001", "00002", "00003", "00004", "00005", "00006", "00007", "00008"};
    private String[] workstations = {"laptop-001", "terminal-002", "pc-hpe-003", "apple-hpe-004"};
    private String[] directories = {"/docs/company/important", "/docs/company/secrect", "/docs/public/", "/docs/private"};
    private User[] users = new User[8];
    private Doc[] docs = new Doc[8];

    /**
     * Constructor for DemoMonkey. Constructor sets the channel and the configuration, populates the
     * queue of work for the Monkey and creates the array of user and document objects for the demo
     * audit events.
     * 
     * @param channel Channel to send audit events
     * @param monkeyConfig Configuration for the Monkey
     */
    public DemoMonkey(AuditChannel channel, MonkeyConfig monkeyConfig)
    {
        this.channel = channel;
        this.monkeyConfig = monkeyConfig;
        this.queue = MonkeyFactory.populateQueue(monkeyConfig);
        initialiseUserArray();
        initialiseDocArray();
    }

    @Override
    public void execute(AuditChannel channel, MonkeyConfig monkeyConfig) throws Exception
    {
        while (queue.size() > 0) {
            int i = queue.take();
            User user = (User)selectRandom(users);
            Doc doc = (Doc)selectRandom(docs);
            LOG.debug("Sending Audit Event [" + i + "]");
            
            switch ((String)selectRandom(auditEvents)) {
                case "createDocument":
                    AuditLog.auditCreateDocument(channel, (String)selectRandom(tenantIds), user.getUserId(), monkeyConfig.getCorrelationId(), doc.getDocId(), doc.getTitle(), doc.getFileType(), randomDate(), user.getUsername());
                    break;
                case "createUser":
                    AuditLog.auditCreateUser(channel,(String)selectRandom(tenantIds), user.getUserId(), monkeyConfig.getCorrelationId(), user.getUsername(), user.getEmailAddress(), randomDate());
                    break;
                case "deleteDocument":
                    AuditLog.auditDeleteDocument(channel, (String)selectRandom(tenantIds), user.getUserId(), monkeyConfig.getCorrelationId(), doc.getDocId(), doc.getTitle(), doc.getFileType(), randomDate(), user.getUsername());
                    break;
                case "deleteUser":
                    AuditLog.auditDeleteUser(channel, (String)selectRandom(tenantIds), user.getUserId(), monkeyConfig.getCorrelationId(), user.getUsername(), randomDate());
                    break;
                case "login":
                    AuditLog.auditLogin(channel, (String)selectRandom(tenantIds), user.getUserId(), monkeyConfig.getCorrelationId(), user.getUsername(), (String)selectRandom(workstations), randomDate());
                    break;
                case "logout":
                    AuditLog.auditLogout(channel, (String)selectRandom(tenantIds), user.getUserId(), monkeyConfig.getCorrelationId(), user.getUsername(), (String)selectRandom(workstations), randomDate());
                    break;
                case "moveDocument":
                    AuditLog.auditMoveDocument(channel, (String)selectRandom(tenantIds), user.getUserId(), monkeyConfig.getCorrelationId(), doc.getDocId(), doc.getTitle(), doc.getFileType(), randomDate(), user.getUsername(), (String)selectRandom(directories), (String)selectRandom(directories));
                    break;
                case "readDocument":
                    AuditLog.auditReadDocument(channel, (String)selectRandom(tenantIds), user.getUserId(), monkeyConfig.getCorrelationId(), doc.getDocId(), doc.getTitle(), doc.getFileType(), randomDate(), user.getUsername());
                    break;
                default:
                    LOG.error("Impossible selection of unknown Audit Event, there must be a Monkey in the works!");
                    break;
            }
        }
    }

    @Override
    public void run()
    {
        try {
            execute(channel, monkeyConfig);
        } catch (Exception e) {
            LOG.error("Error executing a multi-threaded version of the DemoMonkey, " + e);
        }
    }

    /**
     * Populate users array with 8 pre-configured users 
     */
    private void initialiseUserArray()
    {
        users[0] = new User("1234-0001", "aorange", "aorange@email.com");
        users[1] = new User("1234-0002", "bblack", "bblack@email.com");
        users[2] = new User("1234-0003", "cwhite", "cwhite@email.com");
        users[3] = new User("1234-0004", "dpurple", "dpurple@email.com");
        users[4] = new User("1234-0005", "eyellow", "eyellow@email.com");
        users[5] = new User("1234-0006", "fred", "fred@email.com");
        users[6] = new User("1234-0007", "ggreen", "ggreen@email.com");
        users[7] = new User("1234-0008", "hblue", "hblue@email.com");
    }
    
    /**
     * Populate docs array with 8 pre-configured docs
     */
    private void initialiseDocArray()
    {
        docs[0] = new Doc(1234500001, "A New Hope", "pdf");
        docs[1] = new Doc(1234500002, "Empire Strikes Back", "msg");
        docs[2] = new Doc(1234500003, "Return of the Jedi", "txt");
        docs[3] = new Doc(1234500004, "The Hobbit", "pdf");
        docs[4] = new Doc(1234500005, "Fellowship of the Ring", "txt");
        docs[5] = new Doc(1234500006, "Two Towers", "msg");
        docs[6] = new Doc(1234500007, "Return of the King", "pdf");
        docs[7] = new Doc(1234500008, "Macbeth", "txt");
    }    

    /**
     * Randomly select a value from a give Object[] array
     * 
     * @param array Object array to perform random selection upon
     * @return Randomly select Object from provided array
     */
    protected Object selectRandom(Object[] array)
    {
        int randomIndex = GENERATOR.nextInt(array.length);
        int randomNumber = GENERATOR.nextInt(array.length / 2);
        int randomOperation = GENERATOR.nextInt(2);
        if (randomOperation == 1) {
            randomIndex = randomIndex + randomNumber;
            if (randomIndex >= array.length) {
                randomIndex = array.length - 1;
            }
        }
        else if (randomOperation == 0) {
            randomIndex = randomIndex - randomNumber;
            if (randomIndex < 0) {
                randomIndex = 0;
            }
        }
        return array[randomIndex];
    }

    /**
     * Generates a random Date between today and one move previously
     * 
     * @return Randomly generated date
     */
    protected Date randomDate()
    {
        LocalDate now = LocalDate.now();
        LocalDate past = now.minusMonths(1);
        LocalDate randomDate = new LocalDate(ThreadLocalRandom.current().nextLong(past.toDate().getTime(), now.toDate().getTime()));
        return randomDate.toDate();
    }
    
    /**
     * Private inner class.
     * Class represents a basic document object holding the document's Id, title and file type.
     */
    private class Doc
    {

        private long docId;
        private String title;
        private String fileType;

        public Doc(long docId, String title, String fileType)
        {
            this.docId = docId;
            this.title = title;
            this.fileType = fileType;
        }

        /**
         * @return the docId
         */
        public long getDocId()
        {
            return docId;
        }

        /**
         * @return the title
         */
        public String getTitle()
        {
            return title;
        }

        /**
         * @return the fileType
         */
        public String getFileType()
        {
            return fileType;
        }
    }

    /**
     * Private inner class.
     * Class represents a basic user object holding the user's Id, username and email address.
     */
    private class User
    {

        private String userId;
        private String username;
        private String emailAddress;

        public User(String userId, String username, String emailAddress)
        {
            this.userId = userId;
            this.username = username;
            this.emailAddress = emailAddress;
        }

        /**
         * @return the userId
         */
        public String getUserId()
        {
            return userId;
        }

        /**
         * @return the username
         */
        public String getUsername()
        {
            return username;
        }

        /**
         * @return the emailAddress
         */
        public String getEmailAddress()
        {
            return emailAddress;
        }
    }

}
