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

import dk.kb.ds.cumulus.export.converters.Converter;
import dk.kb.ds.cumulus.export.converters.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class ConverterFactory {
    private static final Logger log = LoggerFactory.getLogger(FieldMapper.class);

    /**
     * If mapName is not specified in {@link #build(String, String)} this name will be used.
     */
    public static final String DEFAULT_MAP = "default";

    // YAML keys below
    public static final String CONVERTERS = "converters";
    public static final String CUMULUS = "cumulus";
    public static final String SOLR = "solr";

    /**
     * Creates a Map of {@link Converter}s from the given configuration.
     * @param converterConfig a YAML file specifying conversions.
     *                        See ds-cumulus-export-default-mapping.yml in the test folder for a sample setup.
     * @param mapName the name of the map in the given converterConfig to use.
     * @return a map with Converters as specified in converterConfig.
     * @throws IOException if the configuration could not be resolved/fetched.
     */
    public static Map<String, Converter> build(String converterConfig, String mapName) throws IOException {
        if (mapName == null) {
            mapName = DEFAULT_MAP;
        }
        YAML baseYAML = YAML.resolveConfig(converterConfig, Configuration.CONF_ROOT);
        YAML mapYAML = baseYAML.getSubMap("maps." + mapName);
        if (mapYAML == null) {
            throw new IOException("Unable to locate map '" + mapName + "'");
        }
        return build(mapYAML);
    }

    /**
     * Creates a Map of {@link Converter}s from the given configuration.
     * @param mapConfig the conversions.
     * @return a map with Converters as specified in mapConfig.
     */
    public static Map<String, Converter> build(YAML mapConfig) {
        List<YAML> converterConfigs = mapConfig.getYAMLList(CONVERTERS);
        log.debug("Got {} converter configurations", converterConfigs.size());

        Map<String, Converter> converters = new HashMap<>(converterConfigs.size());
        converterConfigs.stream().
            map(ConverterFactory::buildConverter).
            forEach(conv -> converters.put(conv.source, conv));
        return converters;
    }

    private static Converter buildConverter(YAML converterConfig) {
        final String destType = converterConfig.getString("destType");
        switch (destType) {
            case "verbatim" : return new StringConverter(converterConfig);
            default: throw new IllegalArgumentException("No converter for destType '" + destType + "'");
        }
    }
}
