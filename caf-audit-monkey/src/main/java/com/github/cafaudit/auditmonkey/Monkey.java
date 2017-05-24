package com.github.cafaudit.auditmonkey;

import com.hpe.caf.auditing.AuditChannel;

public interface Monkey
{

    public void execute(AuditChannel channel, AuditMonkeyConfig monkeyConfig) throws Exception;
    
}
