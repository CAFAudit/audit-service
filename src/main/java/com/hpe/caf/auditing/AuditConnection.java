package com.hpe.caf.auditing;

import java.io.IOException;

public interface AuditConnection extends AutoCloseable
{
    /**
     * Create a new audit channel.
     *
     * @return a new audit channel descriptor, or null if none is available
     * @throws IOException if an I/O problem is encountered
     */
    AuditChannel createChannel() throws IOException;
}
