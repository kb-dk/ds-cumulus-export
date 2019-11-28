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
import dk.kb.ds.cumulus.export.converters.ConverterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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
    /** The Cumulus collection */
    public static final String CONF_CUMULUS_COLLECTION = "collection";
    /** The output file for solr */
    public static final String CONF_OUTPUT_FILE = "outputfile";
    /** The type of object to get info from */
    public static final String CONF_TYPE = "type";
    /** For testing purposes */
    public static final String MAXRECORDS = "maxrecords";

    /**
     * The name of the conversion setup used by {@link dk.kb.ds.cumulus.export.converters.ConverterFactory}.
     * If this is not defined, the fallback is {@code ds-cumulus-export-default-mapping.yml}.
     */
    public static final String CONF_CONVERSION_SETUP = "conversionSetup";
    public static final String DEFAULT_CONVERSION_SETUP = "ds-cumulus-export-default-mapping.yml";
    /**
     * The name of the conversion map to use.
     * If this is not defined, the fallback is {@code default}.
     */
    public static final String CONF_CONVERSION_MAP = "conversionMap";
    public static final String DEFAULT_CONVERSION_MAP = ConverterFactory.DEFAULT_MAP;

    /** Whether Cumulus should have write access. */
    protected static final boolean CUMULUS_WRITE_ACCESS = false;

    private static Configuration instance = null;
    private YAML confMap;
    private String maxRecords;

    /** The configuration for Cumulus.*/
    protected final CumulusConfiguration cumulusConf;
    private final String outputFile;
    private final String collection;
    private final String type;
    /**
     * Loads the DS Cumulus Export YAML configuration file from classpath or user home.
     * @throws IOException is the configuration could not be located or retrieved.
     */
    private Configuration() throws IOException {
        confMap = YAML.resolveConfig(DEFAULT_CONF_FILE, CONF_ROOT);

        this.cumulusConf = loadCumulusConfiguration(confMap.getSubMap(CONF_CUMULUS));

        this.collection = getString(confMap, CONF_CUMULUS_COLLECTION);
        this.outputFile = getString(confMap, CONF_OUTPUT_FILE);
        this.type = getString(confMap, CONF_TYPE);
        this.maxRecords = getString(confMap, MAXRECORDS);
    }

    private String getString(YAML map, String confElement) {
        ArgumentCheck.checkTrue(map.containsKey(confElement), "Missing configuration element '" + confElement + "'");
        log.info("Processing configuration element '" + confElement + "'");
        return map.getString(confElement);
    }

    public static CumulusConfiguration getCumulusConf() {
        return instance().cumulusConf;
    }

    public static String getCollection(){
        return instance().collection;
    }
    public static String getOutputFile(){
        return instance().outputFile;
    }
    public static String getType(){
        return instance().type;
    }
    public static Integer getMaxRecords() throws Exception {
        try {
            return Integer.parseInt(instance().maxRecords);
        } catch (NumberFormatException e) {
            String m = "Value of maxrecords must be an integer";
            log.warn(m, e);
            throw new Exception(m, e);
        }
    }
    /**
     * @return the underlying map holding the configuration.
     */
    public static YAML getYAML() {
        return instance().confMap;
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
    protected CumulusConfiguration loadCumulusConfiguration(YAML map) {
        for (String element: new String[]{
            CONF_CUMULUS_SERVER, CONF_CUMULUS_USERNAME, CONF_CUMULUS_PASSWORD, CONF_CUMULUS_CATALOG}) {
            ArgumentCheck.checkTrue(map.containsKey(element), "Missing Cumulus element '" + element + "'");
        }

        return new CumulusConfiguration(
            CUMULUS_WRITE_ACCESS,
            map.getString(CONF_CUMULUS_SERVER),
            map.getString(CONF_CUMULUS_USERNAME),
            map.getString(CONF_CUMULUS_PASSWORD),
            map.getList(CONF_CUMULUS_CATALOG));
    }
}
