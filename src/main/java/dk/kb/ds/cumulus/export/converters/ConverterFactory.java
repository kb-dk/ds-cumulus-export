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
package dk.kb.ds.cumulus.export.converters;

import dk.kb.ds.cumulus.export.Configuration;
import dk.kb.ds.cumulus.export.FieldMapper;
import dk.kb.ds.cumulus.export.YAML;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

/**
 * Produces a Map of {@link Converter}s based on a YAML configuration.
 *
 * Note: Implementations of {@link Converter} should self-register by calling
 *       {@link ConverterFactory#registerCreator(String, Function)}.
 *       See {@link StringConverter} for an example.
 */
public class ConverterFactory {
    private static final Logger log = LoggerFactory.getLogger(FieldMapper.class);

    /**
     * If mapName is not specified in {@link #build(String, String)} this name will be used.
     */
    public static final String YAML_DEFAULT_MAP = "default";

    // YAML keys
    public static final String YAML_CONVERTERS = "converters";
    public static final String YAML_CUMULUS = "cumulus";
    public static final String YAML_SOLR = "solr";

    /**
     * The supported converters, represented by their creators.
     * Implementations of {@link Converter} should call {@link #registerCreator} to register.
     */
    private static final Map<String, Function<YAML, Converter>> creators = new HashMap<>();
    /**
     * Find all classes implementing {@link Converter} and makes them register in this factory.
     */
    static {
        Reflections reflections = new Reflections("");
        Set<Class<? extends Converter>> classes = reflections.getSubTypesOf(Converter.class);
        for (Class<? extends Converter> c: classes) {
            try {
                c.getMethod("register").invoke(null);
            } catch (Exception e) {
                throw new Error(
                    "Cannot invoke 'register' method on Converter-implementing class '" + c.getName() + "'");
            }
        }
    }

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
            mapName = YAML_DEFAULT_MAP;
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
        List<YAML> converterConfigs = mapConfig.getYAMLList(YAML_CONVERTERS);
        log.debug("Got {} converter configurations", converterConfigs.size());

        Map<String, Converter> converters = new HashMap<>(converterConfigs.size());
        converterConfigs.stream().
            map(ConverterFactory::buildConverter).
            forEach(conv -> converters.put(conv.source, conv));
        return converters;
    }

    private static Converter buildConverter(YAML converterConfig) {
        final String destType = converterConfig.getString("destType");
        Function<YAML, Converter> creator = creators.get(destType);
        if (creator == null) {
            throw new IllegalArgumentException("Unsupported destination type '" + destType + "'");
        }
        return creator.apply(converterConfig);
    }

    public static void registerCreator(String destType, Function<YAML, Converter> cc) {
        if (creators.containsKey(destType)) {
            throw new IllegalArgumentException(
                "Received a ConverterCreator with destination type '" + destType + "', but a ConverterCreator" +
                " for that type is already registered. Existing creator: " + creators.get(destType) +
                ", new creator: " + cc);
        }
        log.info("Registering ConverterCreator for destination type " + destType);
        creators.put(destType, cc);
    }

}
