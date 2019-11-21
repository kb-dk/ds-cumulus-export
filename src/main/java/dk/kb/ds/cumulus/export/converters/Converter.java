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

import com.canto.cumulus.fieldvalue.AssetReference;
import dk.kb.cumulus.CumulusRecord;
import dk.kb.ds.cumulus.export.FieldMapper;
import dk.kb.ds.cumulus.export.YAML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * Abstract field → field converter with helper methods. Implementations handle the actual conversion.
 */
public abstract class Converter {
    private static final Logger log = LoggerFactory.getLogger(Converter.class);

    // YAML keys
    public static final String CONF_SOURCE =                 "source";
    public static final String CONF_SOURCE_TYPE =            "sourceType";

    public static final String CONF_DEST =                   "dest";
    public static final String CONF_FALLBACK_DEST =          "fallbackDest";
    public static final String CONF_DEST_TYPE =              "destType";
    public static final String DEFAULT_DEST_TYPE =           "string";

    public static final String  CONF_REQUIRED =              "required";
    public static final boolean DEFAULT_REQUIRED =            false;
    public static final String  CONF_LINE_BREAK_IS_MULTI =   "lineBreakIsMulti";
    public static final boolean DEFAULT_LINE_BREAK_IS_MULTI = true;


    public enum SOURCE_TYPE {
        string, integer, longType { }, assetReference;

        // We cannot use "long" as enum (it is a reserved word), so we need to handle from- and to-String
        public static SOURCE_TYPE getEnum(String value) {
            return "long".equals(value) ? longType : valueOf(value);
        }
        @Override
        public String toString() {
            return this == longType ? "long" : super.toString();
        }
    }

    public final String source;
    public final SOURCE_TYPE sourceType; // Default is string
    public final String destination;
    public final String fallbackDestination;
    public final String destinationType; // Default is string
    public final boolean required;
    public final boolean linebreakIsMulti;

    /**
     * Fills in basic attributed as specified in the config.
     * @param config configuration for a Converter implementation.
     */
    public Converter(YAML config) {
        this(config.getString(CONF_SOURCE),
             config.containsKey(CONF_SOURCE_TYPE) ? SOURCE_TYPE.getEnum(config.getString(CONF_SOURCE_TYPE)) : null,
             config.getString(CONF_DEST), config.getString(CONF_FALLBACK_DEST),
             config.getString(CONF_DEST_TYPE, DEFAULT_DEST_TYPE),
             config.getBoolean(CONF_REQUIRED, DEFAULT_REQUIRED),
             config.getBoolean(CONF_LINE_BREAK_IS_MULTI, DEFAULT_LINE_BREAK_IS_MULTI)
             );
    }

    /**
     * @param source              the source field name.
     * @param sourceType          the source data type.
     * @param destination         the destination field.
     * @param fallbackDestination the destination field to use if the data could not be converted.
     *                            this will always be the verbatim source value. fallbackDestination can be null.
     * @param destinationType     the destination data type.
     * @param required            if true, an exception will be thrown if the source field is not present.
     * @param lineBreakIsMulti    if true, calls to {@link #addValue} with values containing linebreaks will result in
     *                            multiple additions to the output, one per line.
     */
    public Converter(String source, SOURCE_TYPE sourceType,
                     String destination, String fallbackDestination, String destinationType,
                     Boolean required, Boolean lineBreakIsMulti) {
        this.source = source;
        this.sourceType = sourceType == null ? getDefaultSourceType() : sourceType;
        this.destination = destination;
        this.fallbackDestination = fallbackDestination;
        this.destinationType = destinationType == null ? DEFAULT_DEST_TYPE : destinationType;
        this.required = required == null ? DEFAULT_REQUIRED : required;
        this.linebreakIsMulti = lineBreakIsMulti == null ? DEFAULT_LINE_BREAK_IS_MULTI : lineBreakIsMulti;
    }

    /**
     * Override this in implementing classes if needed.
     * @return the sourceType to use if none is given in the setup.
     */
    protected SOURCE_TYPE getDefaultSourceType() {
        return SOURCE_TYPE.string;
    }

    /**
     * Extract the content of the {@link #source} field from the record, process it and add the result(s) to
     * fieldValues. The use of {@link #fallbackDestination} is handled by the abstract {@Converter}: Implementations
     * should simply do nothing if the value could not be added.
     * @param record     a Cumulus record.
     * @param resultList the destination for the processed values.
     * @throws IllegalArgumentException if the combination of input and processing was not valid.
     */
    abstract void convertImpl(CumulusRecord record, List<FieldMapper.FieldValue> resultList)
        throws IllegalArgumentException;

    /**
     * Extract the content of the {@link #source} field from the record, process it and
     * add the result(s) to fieldValues.
     * @param record     a Cumulus record.
     * @param fieldValues the destination for the processed values.
     * @throws IllegalArgumentException if the combination of input and processing was not valid.
     * @throws IllegalStateException if the {@link #source} was required but not present in the record.
     */
    public void convert(CumulusRecord record, FieldMapper.FieldValues fieldValues)
        throws IllegalArgumentException, IllegalStateException {
        final int beforeSize = fieldValues.size();
        convertImpl(record, fieldValues);
        if (beforeSize != fieldValues.size()) {
            return;
        }
        if (required) {
            throw new IllegalStateException(
                "The required field '" + source + "' should result in at least 1 output field, but did not");
        }
        String sourceValue = getAsString(record);
        if (fallbackDestination != null && !fallbackDestination.isEmpty() &&
            sourceValue != null && !sourceValue.isEmpty()) {
            log.debug("Could not derive a value for primary destination field '{}' for value '{}' " +
                      "with destination type '{}', copying verbatim to fallback field '{}'",
                      destination, sourceValue, destinationType, fallbackDestination);
            fieldValues.add(new FieldMapper.FieldValue(fallbackDestination, sourceValue));
        }
    }

    /**
     * Helper method for implementing classes.
     * Extract the content of the {@link #source} field from the record as a String, regardless of actual type.
     * @param record a Cumulus record.
     * @return the content of {@link #source} as a String.
     * @throws IllegalStateException if the {@link #source} was required but not present in the record.
     */
    protected String getAsString(CumulusRecord record) {
        String value = null;
        switch (sourceType) {
            case string: {
                value = record.getFieldValueForNonStringField(source);
                break;
            }
            case integer: {
                Integer intValue = record.getFieldIntValue(source);
                value = intValue == null ? null : intValue.toString();
                break;
            }
            case longType: {
                Long longValue = record.getFieldLongValue(source);
                value = longValue == null ? null : longValue.toString();
                break;
            }
            case assetReference: {
                AssetReference assetReference = record.getAssetReference(source);
                if (assetReference == null) {
                    break;
                }
                // Old code: record.getAssetReference("Asset Reference").getPart(0).getDisplayString()
                value = assetReference.getDisplayString();
                break;
            }
            default: value = null;
        }
        if (required && value == null) {
            throw new IllegalStateException(
                "The required field '" + source + "' was not present in the record");
        }
        return value;
    }

    /**
     * Helper method for implementing classes.
     * Extract the content of the {@link #source} field from the record as the stated {@link #CONF_SOURCE_TYPE}.
     * @param record a Cumulus record.
     * @return the content of {@link #source}.
     * @throws IllegalStateException if the {@link #source} was required but not present in the record as the
     * stated type.
     */
    protected Object getValue(CumulusRecord record) {
        Object value;
        switch (sourceType) {
            case string: {
                value = record.getFieldValue(source);
                break;
            }
            case integer: {
                value = record.getFieldIntValue(source);
                break;
            }
            case longType: {
                value = record.getFieldLongValue(source);
                break;
            }
            case assetReference: {
                value = record.getAssetReference(source);
                break;
            }
            default: value = null;
        }
        if (required && value == null) {
            throw new IllegalStateException(
                "The required field '" + source + "' was not present in the record");
        }
        return value;
    }

    /**
     * Add the given values to the resultList if the values are not null.
     * {@link Object#toString()} will be called on each value before adding to resultList.
     * @param values to add to the resultList.
     * @param resultList the values will be added as {@link #destination}-value pairs to this list.
     */
    void addValues(List<Object> values, List<FieldMapper.FieldValue> resultList) {
        if (values == null) {
            return;
        }
        values.stream().filter(Objects::nonNull).forEach(value -> addValue(value, resultList));
    }
    /**
     * Add the given value to the resultList if the value is not null.
     * {@link Object#toString()} will be called on the value before adding to resultList.
     * Note: If {@link #linebreakIsMulti} is trie, values with multiple lines will be split into lines and each line
     * will be added separately.
     * @param value a value to add to the resultList.
     * @param resultList the value will be added as a {@link #destination}-value pair to this list.
     */
    void addValue(Object value, List<FieldMapper.FieldValue> resultList) {
        if (value == null) {
            return;
        }
        if (linebreakIsMulti) {
            for (String line : value.toString().split("\\r?\\n")) {
                resultList.add(new FieldMapper.FieldValue(destination, line));
            }
        } else {
            resultList.add(new FieldMapper.FieldValue(destination, value.toString()));
        }
    }

    /**
     * "Override" this in implementing classes and call something like
     * {@code ConverterFactory.registerCreator("string", StringConverter::new);}
     */
    public static void register() {
        throw new UnsupportedOperationException(
            "The static method 'register' must be defined in all implementations of Converter");
    }
}
