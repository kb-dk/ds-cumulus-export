package dk.kb.ds.cumulus.export;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CalendarUtilsTest {

    @Test
    void testGetUTCTimeWritten() {
        //  Standard time: Zulu time is 1 hour ahead
        assertEquals("2017-11-09T14:47:04Z", CalendarUtils.getUTCTime("Thu Nov 09 15:47:04 CET 2017"));

        // Daylight saving time: Zulu time is 2 hours ahead
        assertEquals("2019-10-04T08:05:10Z", CalendarUtils.getUTCTime("Fri Oct 04 10:05:10 CET 2019"));

        assertEquals("2018-05-23T07:54:00Z", CalendarUtils.getUTCTime("Wed May 23 09:54:00 CEST 2018"));
    }

    @Test
    void testGetUTCTimePadded() {
        final String[][] TESTS = new String[][] {
            {"2019", "2019-01-01T00:00:00Z"},
            {"2019-10", "2019-10-01T00:00:00Z"},
            {"2019.10", "2019-10-01T00:00:00Z"}
        };

        for (String[] test: TESTS) {
            assertEquals(test[1], CalendarUtils.getUTCTime(test[0]),
                         "The input 'test[0]' should yield the expected datetime");
        }
    }

    @Test
    void testGetUTCTimeNonPadded() {
        final String[][] TESTS = new String[][]{
            {"2019", "2019"},
            {"2019-10", "2019-10"},
            {"2019.10", "2019-10"}
        };

        for (String[] test : TESTS) {
            assertEquals(test[1], CalendarUtils.getUTCTime(test[0], false),
                         "The input 'test[0]' should yield the expected datetime");
        }
    }

    void getDateTime() {
        assertEquals("1981-01-01T00:00:00.000+01:00", CalendarUtils.getDateTime("yyyy", "1981"));

    }

    @Test
    void convertDatetimeFormat() {
        assertEquals("1978-08-30", CalendarUtils.convertDatetimeFormat("1978.08.30"));
        assertEquals("1864-04-30", CalendarUtils.convertDatetimeFormat("1864-04-30"));
        assertEquals("1947-03", CalendarUtils.convertDatetimeFormat("1947.03"));
        assertEquals("1980", CalendarUtils.convertDatetimeFormat("1980"));
        assertEquals("1969-05", CalendarUtils.convertDatetimeFormat("1969.05?"));
        assertEquals("Midt af 1900-tallet", CalendarUtils.convertDatetimeFormat("Midt af 1900-tallet"));
        assertEquals("Ukendt", CalendarUtils.convertDatetimeFormat("Ukendt"));
        assertEquals("u.å.", CalendarUtils.convertDatetimeFormat("u.å."));
//        assertEquals("1972 TO 1979", CalendarUtils.convertDatetimeFormat("1972-1979"));
//        assertEquals("??", CalendarUtils.convertDatetimeFormat("1911-12"));
//        assertEquals("1897 TO 1898", CalendarUtils.convertDatetimeFormat("1897-98"));
//        assertEquals("1957", CalendarUtils.convertDatetimeFormat("1957-583/2"));
//        assertEquals("198-?", CalendarUtils.convertDatetimeFormat("198-?"));
//        assertEquals("??", CalendarUtils.convertDatetimeFormat("30.10. 1971.10.30"));
//        assertEquals("??", CalendarUtils.convertDatetimeFormat("1972.12.31/1973.01"));
//        assertEquals("??", CalendarUtils.convertDatetimeFormat("1838 eller 1898"));
//        assertEquals("??", CalendarUtils.convertDatetimeFormat("ca. 1966"));
//        assertEquals("??", CalendarUtils.convertDatetimeFormat("1990erne"));
//        assertEquals("??", CalendarUtils.convertDatetimeFormat("1962.08.26? / 1963.03.12? / 1964.02.22?"));
//        assertEquals("??", CalendarUtils.convertDatetimeFormat("Sen 1800-talet - tidlig 1900-tallet"));
    }
}
