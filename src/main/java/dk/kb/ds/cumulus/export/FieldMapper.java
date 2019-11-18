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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

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
     * @throws IOException if the f
     */
    public FieldMapper() throws IOException {
        final String convResource = Configuration.getYAML().getString(
            Configuration.CONF_CONVERSION_SETUP, Configuration.DEFAULT_CONVERSION_SETUP);
        final String convMap = Configuration.getYAML().getString(
            Configuration.CONF_CONVERSION_MAP, Configuration.DEFAULT_CONVERSION_MAP);
        log.info("Loading conversion setup from resource " + convResource + " with map " + convMap);
        converters = ConverterFactory.build(convResource, convMap);
        log.debug("Loading successful for conversion setup from resource " + convResource + " with map " + convMap);
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
         * Created a {@code <doc>} under the given rootElement and fills it with the {@link FieldValue}s
         * {@code <field name="field">value</field>}.
         * @param rootElement the generated doc-element will be added here.
         */
        public void toDoc(Element rootElement) {
            final Document base = rootElement.getOwnerDocument();

            Element docElement = base.createElement("doc");
            rootElement.appendChild(docElement);

            forEach(fv -> fv.toDoc(docElement));
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            stream().limit(20).forEach(fv -> {
                if (sb.length() != 0) {
                    sb.append(", ");
                }
                sb.append(fv.field).append(":").
                    append(fv.value.length() > 50 ? fv.value.substring(0, 50) + "..." : fv.value);
            });
            if (size() > 20) {
                sb.append(", ...(").append(size() - 20).append(" field-values more)");
            }
            return "FieldValues(" + sb.toString() + ")";
        }
    }

    /**
     * Single field value pair.
     */
    public static class FieldValue {
        public final String field;
        public final String value;

        public FieldValue(String field, String value) {
            this.field = field;
            this.value = value;
        }

        /**
         * Adds the contained field and value to the given solrDoc as {@code <field name="field">value</field>}.
         * @param solrDoc where to add the field and value.
         */
        public void toDoc(Element solrDoc) {
            final Document base = solrDoc.getOwnerDocument();

            Element fieldElement = base.createElement("field");
            solrDoc.appendChild(fieldElement);
            fieldElement.setAttribute("name", field);
            fieldElement.appendChild(base.createTextNode(value));
        }

        @Override
        public String toString() {
            return "FieldValue(" + "field='" + field + "', value='" + value + "')";
        }
    }

}
