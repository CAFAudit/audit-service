package com.github.cafaudit.auditmonkey;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MonkeyFactoryTest
{

    @Test
    public void shouldReturnStandardMonkey() {
        Monkey monkey = MonkeyFactory.selectMonkey(AuditMonkeyConstants.STANDARD_MONKEY);
        assertTrue(monkey instanceof StandardMonkey);
    }
    
    @Test
    public void shouldReturnRandomMonkey() {
        Monkey monkey = MonkeyFactory.selectMonkey(AuditMonkeyConstants.RANDOM_MONKEY);
        assertTrue(monkey instanceof RandomMonkey);
    }
    
    @Test
    public void shouldReturnNull() {
        Monkey monkey = MonkeyFactory.selectMonkey("blah");
        assertNull(monkey);
    }    
}
