package dk.kb.ds.cumulus.export;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class ConfigurationTest {

    @Test
    public void testConfigurationLoad() {
        // Pretty simple: If it loads without throwing an exception, all is fine
        Configuration.instance();
    }

    @Test
    public void testCumulusConfigurationLoad() {
        assertNotNull(Configuration.getCumulusConf(), "The cumulus configuration should exist");
    }

}
