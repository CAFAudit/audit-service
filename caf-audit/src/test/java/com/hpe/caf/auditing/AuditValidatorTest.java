package com.hpe.caf.auditing;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AuditValidatorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testValidateString_Success() {
        AuditValidator.validateString("param1","test",1,5);
    }

    @Test
    public void testValidateString_Failure_StringFieldTooLong() {

        thrown.expect(AuditValidatorException.class);
        thrown.expectMessage("is too long");

        AuditValidator.validateString("param1","test",-1,3);
    }

    @Test
    public void testValidateString_Failure__StringFieldTooShort() {

        thrown.expect(AuditValidatorException.class);
        thrown.expectMessage("is too short");

        AuditValidator.validateString("param1","test",5,-1);
    }

}
