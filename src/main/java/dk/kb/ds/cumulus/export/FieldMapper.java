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

import dk.kb.cumulus.CumulusRecord;
import dk.kb.ds.cumulus.export.converters.Converter;
import dk.kb.ds.cumulus.export.converters.ConverterFactory;
import dk.kb.util.Resolver;
import dk.kb.util.YAML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static dk.kb.ds.cumulus.export.CumulusExport.INDENTATION;
import static dk.kb.ds.cumulus.export.CumulusExport.NEWLINE;

/**
 * Extracts selected fields from {@link CumulusRecord}s and provides a list of {@link FieldValue}s with the
 * content of these fields. Depending on setup, the content will be modified during this process.
 */
public class FieldMapper implements Function<CumulusRecord, FieldMapper.FieldValues> {
    private static final Logger log = LoggerFactory.getLogger(FieldMapper.class);

    private final Map<String, String> staticFields = new LinkedHashMap<>();
    private final List<Converter> converters;

    /**
     * Loads a {@link ConverterFactory} setup, as specified in the base configuration, and constructs a field mapper.
     * Note that the setup is merged with the overall setup for ds-cumulus-export, allowing for separation of
     * environment (in the ds-cumulus-setup YAML file) and behaviout (in the ConverterFactory YAML file) through
     * the YAML alias mechanism.
     * @throws IOException if the configuration could not be loaded.
     */
    public FieldMapper() throws IOException {
        final String convResource = Configuration.getYAML().getString(
            Configuration.CONF_CONVERSION_SETUP, Configuration.DEFAULT_CONVERSION_SETUP);
        final String convMap = Configuration.getYAML().getString(
            Configuration.CONF_CONVERSION_MAP, Configuration.DEFAULT_CONVERSION_MAP);
        log.info("Loading conversion setup with base {} from resource {} with map {}",
                 Configuration.instance().getConfFile(), convResource, convMap);

        // We need to remove the "digisam:"-part from the resourceFile in order to use the 2 YAML files as one
        YAML fieldYAML = getBaseMergedYaml(convResource);

        converters = ConverterFactory.build(fieldYAML, convMap);
        log.info("Loading successful for setup with base {} from resource {} with map {}",
                 Configuration.instance().getConfFile(), convResource, convMap);
    }

    /**
     * Merges the given YAML resource with the YAML for the base {@link Configuration}, removing the "digisam:"-line
     * from the yamlResource (if present) to avoid duplicate key conflicts.
     * @param yamlResource a YAML.
     * @return a merged YAML from Configuration base and yamlResource.
     * @throws IOException if the loading failed.
     */
    static YAML getBaseMergedYaml(String yamlResource) throws IOException {
        YAML fieldYAML;
        try (InputStream baseStream = Resolver.resolveStream(Configuration.instance().getConfFile());
             InputStream resourceStream = new ByteArrayInputStream(
                 Resolver.resolveUTF8String(yamlResource).replaceAll("^digisam:.*", "").getBytes("utf-8"));
             InputStream mergeStream = new SequenceInputStream(baseStream, resourceStream)) {
            fieldYAML = YAML.parse(mergeStream).getSubMap(Configuration.CONF_ROOT);
        }
        return fieldYAML;
    }

    /**
     * Static field-values will be added to all outputs when {@link #apply(CumulusRecord)} is called.
     * @param field a static field, such as {@code collection}.
     * @param value a static value, such as {@code Samlingsbilleder}-
     */
    public void putStatic(String field, String value) {
       staticFields.put(field, value);
    }

    /**
     * Applies the configured {@link Converter}s to the given record.
     * Note: Field-values added with {@link #putStatic(String, String)} will also be added.
     * If a record cannot be processed by any reason, processing errors will be logged and null returned.
     * @param record a Cumulus record.
     * @return a list of field-value pairs or null if processing errors occured.
     * @throws IllegalArgumentException if a combination of input and processing was not valid.
     * @throws IllegalStateException if a required field was not present in the record.
     */
    @Override
    public FieldValues apply(CumulusRecord record) {
        FieldValues fieldValues = new FieldValues();
        try {
            converters.forEach(c -> c.convert(record, fieldValues));
        } catch (IllegalArgumentException|IllegalStateException e) {
            log.warn("Unable to process Cumulus record. Extracted so far: " + fieldValues, e);
            return null;
        }
        log.trace("Produced {} fieldValues for the given record", fieldValues.size());
        staticFields.forEach((f, v) -> fieldValues.add(new FieldValue(f, v)));
        log.trace("Added {} static fieldValues to the given record", staticFields.size());
        return fieldValues;
    }

    /**
     * Representation of all field value pairs for a document.
     */
    public static class FieldValues extends ArrayList<FieldValue> {
        /**
         * Created a {@code <stream>} and fills it with the {@link FieldValue}s
         * {@code <field name="field">value</field>}.
         * @param xml the XML stream.
         */


        public void toXML(XMLStreamWriter xml){
            try {
                xml.writeCharacters(INDENTATION);
                xml.writeStartElement("doc");
                xml.writeCharacters(NEWLINE);
                forEach(fv -> fv.toXML(xml));
                xml.writeCharacters(INDENTATION);
                xml.writeEndElement(); // doc
                xml.writeCharacters(NEWLINE);
            } catch (XMLStreamException e) {
                log.error("Error during XML building", e);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Single field value pair.
     */
    public static class FieldValue {
        public final String field;
        public final String value;

        public FieldValue(String field, String value) {
            if (field == null) {
                throw new IllegalArgumentException("field was null (with value '" + value + "'), which is not allowed");
            }
            if (value == null) {
                throw new IllegalArgumentException("value was null (with field '" + field + "'), which is not allowed");
            }
            this.field = field;
            this.value = value;
        }

        /**
         * Adds the contained field and value to the xml stream {@code <field name="field">value</field>}.
         * @param xml the xml stream where the field and value goes.
         */
           public void toXML(XMLStreamWriter xml) {

               try {
                   xml.writeCharacters(INDENTATION);
                   xml.writeCharacters(INDENTATION);
                   xml.writeStartElement("field");
                   xml.writeAttribute("name", field);
                   xml.writeCharacters(value);
                   xml.writeEndElement(); // field
                   xml.writeCharacters(NEWLINE);
               } catch (XMLStreamException e) {
                   log.error("Error during XML building, Field: {}, Value: {}, Exception: {}",field,value,e);
                   throw new RuntimeException(e);
               }
           }
        }
}
