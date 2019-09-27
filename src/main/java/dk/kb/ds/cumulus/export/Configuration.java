/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package dk.kb.ds.cumulus.export;

import dk.kb.cumulus.config.CumulusConfiguration;
import dk.kb.cumulus.utils.ArgumentCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central configuration for the DS Cumulus Exporter.
 *
 * The configuration file ds-cumulus-export.yml must be a YAML in the following format:
 * digisam:
 *   cumulus:
 *     server: $ URL for the Cumulus server
 *     username: $ Cumulus user name
 *     password: $ Cumulus user password
 *     catalog: $ List of Cumulus Catalogs
 *       - cat1
 *       - cat2
 *       - ...
 */
// See https://github.com/Det-Kongelige-Bibliotek/ccs/blob/master/src/main/java/dk/kb/ccs/conf/Configuration.java
@SuppressWarnings("WeakerAccess")
public class Configuration {
    private static final Logger log = LoggerFactory.getLogger(Configuration.class);

    public static final String DEFAULT_CONF_FILE = "ds-cumulus-export.yml";


    /** The root element of the YAML configuration.*/
    public static final String CONF_ROOT = "digisam";
    /** Cumulus node-element.*/
    public static final String CONF_CUMULUS = "cumulus";
    /** The cumulus server url leaf-element.*/
    public static final String CONF_CUMULUS_SERVER = "server";
    /** The cumulus server username leaf-element.*/
    public static final String CONF_CUMULUS_USERNAME = "username";
    /** The cumulus server password leaf-element.*/
    public static final String CONF_CUMULUS_PASSWORD = "password";
    /** The cumulus catalogs array leaf-element.*/
    public static final String CONF_CUMULUS_CATALOG = "catalog";

    /** Whether Cumulus should have write access. */
    protected static final boolean CUMULUS_WRITE_ACCESS = false;

    private static Configuration instance = null;
    private LinkedHashMap<String, Object> confMap = null;
    /** The configuration for Cumulus.*/
    protected final CumulusConfiguration cumulusConf;
    /**
     * Loads the DS Cumulus Export YAML configuration file from classpath or user home.
     * @throws IOException is the configuration could not be located or retrieved.
     */
    private Configuration() throws IOException {

        // TODO: This should be changed to use JNDI
        log.debug("Looking for '" + DEFAULT_CONF_FILE + "' on the classpath");
        URL configURL = Thread.currentThread().getContextClassLoader().getResource(DEFAULT_CONF_FILE);
        if (configURL ==  null) {
            log.debug("Looking for '" + DEFAULT_CONF_FILE + "' on the user home path");
            Path configPath = Path.of(System.getProperty("user.home"), DEFAULT_CONF_FILE);
            if (!configPath.toFile().exists()) {
                String message = "Unable to locate '" + DEFAULT_CONF_FILE + "' on the classpath or in user.home, " +
                                 "unable to continue";
                log.error(message);
                throw new IOException(message);
            }
            configURL = configPath.toUri().toURL();
        }

        Object raw;
        try (InputStream configStream = configURL.openStream()) {
            raw = new Yaml().load(configStream);
            if(!(raw instanceof LinkedHashMap)) {
                throw new IllegalArgumentException("The config resource '" + configURL
                        + "' does not contain a valid DS Cumulus Export configuration.");
            }
        } catch (IOException e) {
            throw new IOException(
                "Exception trying to load the DS Cumulus Export configuration from '" + configURL + "'");
        }

        LinkedHashMap<String, Object> rootMap = (LinkedHashMap<String, Object>) raw;
        ArgumentCheck.checkTrue(rootMap.containsKey(CONF_ROOT),
                                "Configuration must contain the '" + CONF_ROOT + "' element.");
        confMap = (LinkedHashMap<String, Object>) rootMap.get(CONF_ROOT);
        this.cumulusConf = loadCumulusConfiguration((Map<String, Object>) confMap.get(CONF_CUMULUS));
    }

    public static CumulusConfiguration getCumulusConf() {
        return instance().cumulusConf;
    }

    public static synchronized Configuration instance() {
        if (instance == null) {
            try {
                instance = new Configuration();
            } catch (IOException e) {
                String message = "Exception retrieving the DS Cumulus Export configuration";
                log.error(message, e);
                // We do this as runtime as we access the instance-method a lot
                throw new RuntimeException(message, e);
            }
        }
        return instance;
    }

    /**
     * Method for extracting the Cumulus configuration from the YAML map.
     * @param map The map with the Cumulus configuration elements.
     * @return The Cumulus configuration.
     */
    protected CumulusConfiguration loadCumulusConfiguration(Map<String, Object> map) {
        for (String element: new String[]{
            CONF_CUMULUS_SERVER, CONF_CUMULUS_USERNAME, CONF_CUMULUS_PASSWORD, CONF_CUMULUS_CATALOG}) {
            ArgumentCheck.checkTrue(map.containsKey(element), "Missing Cumulus element '" + element + "'");
        }

        return new CumulusConfiguration(
            CUMULUS_WRITE_ACCESS, (
            String) map.get(CONF_CUMULUS_SERVER),
            (String) map.get(CONF_CUMULUS_USERNAME),
            (String) map.get(CONF_CUMULUS_PASSWORD),
            (List<String>) map.get(CONF_CUMULUS_CATALOG));
    }
}
