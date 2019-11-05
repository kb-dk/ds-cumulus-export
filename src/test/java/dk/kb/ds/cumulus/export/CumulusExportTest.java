package dk.kb.ds.cumulus.export;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CumulusExportTest {

    @Test
    public void testSetup() {
        assertTrue(true, "Basic test run with always-pass should work");
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
        String configType = CumulusExport.getConfigurationType();
        assertEquals("image", configType);
    }
}
