package dk.kb.ds.cumulus.export;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CumulusExportTest {

    @Test
    public void testSetup() {
        assertTrue(true, "Basic test run with always-pass should work");
    }

    @Test
    public void testGetUTCTime() {
        //  Standard time: Zulu time is 1 hour ahead
        assertEquals("2017-11-09T14:47:04Z", CumulusExport.getUTCTime("Thu Nov 09 15:47:04 CET 2017"));

        // Daylight saving time: Zulu time is 2 hours ahead
        assertEquals("2019-10-04T08:05:10Z", CumulusExport.getUTCTime("Fri Oct 04 10:05:10 CET 2019"));

        //
        assertEquals("2018-05-23T07:54:00Z", CumulusExport.getUTCTime("Wed May 23 09:54:00 CEST 2018"));
    }

    @Test
    public void testConvertCollectionToSolrFormat() {
        // Remove special chars, replace space with underscore
        assertEquals("Samlings_billeder", CumulusExport.convertCollectionToSolrFormat("[S+am¤%lings bil>led\"er@£€¡![=?"));

        // Keep literals
        assertEquals("æøåöéñäÄÖ", CumulusExport.convertCollectionToSolrFormat("æøåöéñäÄÖ"));
    }

    @Test
    public void testGetConfigurationType() {
        // How to make test data for config type?
//        String configType = CumulusExport.getConfigurationType();
//        assertEquals("image", configType);
    }
}
