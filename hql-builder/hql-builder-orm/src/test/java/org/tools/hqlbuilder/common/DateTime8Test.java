package org.tools.hqlbuilder.common;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.Assert;
import org.junit.Test;

public class DateTime8Test {
    @Test
    public void test() {
        try {
            LocalDateTime t = LocalDateTime.now();
            Thread.sleep(500);
            Duration duration = Duration.between(t, t.plusHours(2).plusMinutes(35).plusSeconds(18).plusNanos(1234567890l));
            Assert.assertEquals("2h35m19" + DateTime8.getDecimalSeperator() + "234s", DateTime8.print(duration));
        } catch (Exception ex) {
            Assert.fail("" + ex);
        }
    }
}
