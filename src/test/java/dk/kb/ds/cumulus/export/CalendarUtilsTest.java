package dk.kb.ds.cumulus.export;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CalendarUtilsTest {

    @Test
    void getDateTime() {
        assertEquals("1981-01-01T00:00:00.000+01:00", CalendarUtils.getDateTime("yyyy", "1981"));

    }

    @Test
    void convertDatetimeFormat() {
        assertEquals("1978-08-30", CalendarUtils.convertDatetimeFormat("1978.08.30"));
        assertEquals("1864-04-30", CalendarUtils.convertDatetimeFormat("1864-04-30"));
        assertEquals("1947-03", CalendarUtils.convertDatetimeFormat("1947.03"));
        assertEquals("1980", CalendarUtils.convertDatetimeFormat("1980"));
        assertEquals("Midt af 1900-tallet", CalendarUtils.convertDatetimeFormat("Midt af 1900-tallet"));
        assertEquals("Ukendt", CalendarUtils.convertDatetimeFormat("Ukendt"));
//        assertEquals("1957", CalendarUtils.convertDatetimeFormat("1957-583/2"));
//        assertEquals("198-?", CalendarUtils.convertDatetimeFormat("198-?"));
//        assertEquals("198-?", CalendarUtils.convertDatetimeFormat("30.10. 1971.10.30"));
//        assertEquals("198-?", CalendarUtils.convertDatetimeFormat("1972.12.31/1973.01"));
//        assertEquals("1972 TO 1979", CalendarUtils.convertDatetimeFormat("1972-1979"));
        assertEquals("1897 TO 1898", CalendarUtils.convertDatetimeFormat("1897-98"));
//        assertEquals("??", CalendarUtils.convertDatetimeFormat("1838 eller 1898"));
//        assertEquals("??", CalendarUtils.convertDatetimeFormat("ca. 1966"));
//        assertEquals("??", CalendarUtils.convertDatetimeFormat("1990erne"));
    }
}
