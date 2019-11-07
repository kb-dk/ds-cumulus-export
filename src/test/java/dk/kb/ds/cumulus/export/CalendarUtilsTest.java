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
    void testGetUTCTime() {
        final String[][] TESTS = new String[][] {
            {"2019", "2019-01-01T00:00:00Z"},
            {"2019-10", "2019-10-01T00:00:00Z"},
            {"2019.10", "2019-10-01T00:00:00Z"},
            {"1897-1901", null},
            {"2019-10-30", "2019-10-30T00:00:00Z"},
            {"2019-20", null},
            {"2019-13-30", null}
        };

        for (String[] test: TESTS) {
            assertEquals(test[1], CalendarUtils.getUTCTime(test[0]),
                         "The input 'test[0]' should yield the expected datetime");
        }
    }

    @Test
    void testGetUTCTimeRange() {
        final String[][] TESTS = new String[][]{
            {"2019", "2019"},
            {"2019-10", "2019-10"},
            {"2019.10", "2019-10"},
            {"2111-12", "2111-12"},
            {"2111-13", "2111 TO 2113"},
            {"2000-01", "2000-01"},
            {"2019-10-30", "2019-10-30"},
            {"2019.10.30", "2019-10-30"},
            {"2019/10/30", "2019-10-30"},
            {"2019-13-30", null},
            {"1969.05?", null},
            {"1838 eller 1898", null},
            {"2019-10-32", null},
            {"1897-1901", "1897 TO 1901"},
            {"188-?", null}
        };

        for (String[] test : TESTS) {
            assertEquals(test[1], CalendarUtils.getUTCTimeRange(test[0]),
                         "The input 'test[0]' should yield the expected datetime");
        }
    }

    void getDateTime() {
        assertEquals("1981-01-01T00:00:00.000+01:00", CalendarUtils.getDateTime("yyyy", "1981"));

    }
}
