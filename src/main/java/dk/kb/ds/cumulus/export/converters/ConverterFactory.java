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
import java.util.stream.Collectors;

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
     * The sub-YAML containing all the conversion maps.
     */
    public static final String CONF_MAPS = "maps";

    /**
     * If mapName is not specified in {@link #build(String, String)} this name will be used.
     */
    public static final String DEFAULT_MAP = "default";

    // YAML keys
    public static final String CONF_CONVERTERS = "converters";
    public static final String CONF_CUMULUS = "cumulus";
    public static final String CONF_SOLR = "solr";

    /**
     * The supported converters, represented by their creators.
     * Implementations of {@link Converter} should call {@link #registerCreator} to register.
     */
    private static final Map<String, Function<YAML, Converter>> creators = new HashMap<>();
    /**
     * Find all classes implementing {@link Converter} and makes them register in this factory.
     */
    static {
        log.debug("Scanning for implementations of class 'Converter'");
        Reflections reflections = new Reflections("dk"); // The prefix ("scan URLs") must not be empty!?
        Set<Class<? extends Converter>> classes = reflections.getSubTypesOf(Converter.class);
        if (classes.isEmpty()) {
            log.error("No implementations of class 'Converter' located. It is highly improbably that the " +
                      "DS Cumulus Exporter will function");
        } else {
            log.info("Registering {} implementations of class 'Converter'", classes.size());
        }
        for (Class<? extends Converter> c: classes) {
            try {
                c.getMethod("register").invoke(null);
            } catch (Exception e) {
                throw new Error(
                    "Cannot invoke 'register' method on Converter-implementing class '" + c.getName() +
                    "'. This is likely because a new Converter has been added and the author forgot to add the" +
                    "'register' method");
            }
        }
    }

    /**
     * Creates a Map of {@link Converter}s from the given configuration.
     * @param converterConfig a YAML file specifying conversions.
     *                        See ds-cumulus-export-default-mapping.yml in the test folder for a sample setup.
     * @param mapName the name of the map in the given converterConfig to use. Default is "default".
     * @return a list with Converters as specified in converterConfig.
     * @throws IOException if the configuration could not be resolved/fetched.
     */
    public static List<Converter> build(String converterConfig, String mapName) throws IOException {
        if (mapName == null) {
            mapName = DEFAULT_MAP;
        }
        YAML baseYAML = YAML.resolveConfig(converterConfig, Configuration.CONF_ROOT);
        YAML mapYAML = baseYAML.getSubMap(CONF_MAPS + "." + mapName);
        if (mapYAML == null) {
            throw new IOException("Unable to locate map '" + mapName + "'");
        }
        return build(mapYAML);
    }

    /**
     * Creates a Map of {@link Converter}s from the given configuration.
     * @param mapConfig the conversions.
     * @return a list with Converters as specified in mapConfig.
     */
    public static List<Converter> build(YAML mapConfig) {
        List<YAML> converterConfigs = mapConfig.getYAMLList(CONF_CONVERTERS);
        log.debug("Creating {} mappings from provided converter configurations", converterConfigs.size());

        return converterConfigs.stream().
            map(ConverterFactory::buildConverter).
            collect(Collectors.toList());
    }

    public static Converter buildConverter(YAML converterConfig) {
        final String destType = converterConfig.getString("destType", Converter.DEFAULT_DEST_TYPE);
        Function<YAML, Converter> creator = creators.get(destType);
        if (creator == null) {
            throw new IllegalArgumentException("Unsupported destination type '" + destType + "'");
        }
        return creator.apply(converterConfig);
    }

    /**
     * Callback for {@link Converter} implementations to register at this factory.
     * @param destType the destination type for the Converter
     * @param cc a Converter Creator
     */
    public static void registerCreator(String destType, Function<YAML, Converter> cc) {
        if (creators.containsKey(destType)) {
            throw new IllegalArgumentException(
                "Received a ConverterCreator with destination type '" + destType + "', but a ConverterCreator" +
                " for that type is already registered. Existing creator: " + creators.get(destType) +
                ", new creator: " + cc);
        }
        log.info("Registering ConverterCreator for destination type '" + destType + "'");
        creators.put(destType, cc);
    }

}
