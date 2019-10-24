package dk.kb.ds.cumulus.export;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CalendarUtilsTest {

    @Test
    void getXmlGregorianCalendar() {
    }

    @Test
    void getCurrentDate() {
    }

    @Test
    void getDateTime() {
        assertEquals("1981-01-01T00:00:00.000+01:00", CalendarUtils.getDateTime("yyyy", "1981"));

        assertEquals("1981-01-01T00:00:00.000+01:00", CalendarUtils.getDateTime("yyyy", "1981-1985"));
    }
}
