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

import java.util.List;
import java.util.Objects;

/**
 * Abstract field → field converter with helper methods. Implementations handle the actual conversion.
 */
public abstract class Converter {
    public enum SOURCE_TYPE {string, assetReference}
    public enum DEST_TYPE {verbatim, int32, int64, bool, float32, float64, datetime, datetimeRange, pattern}

    public final String source;
    public final SOURCE_TYPE sourceType; // Default is string
    public final String destination;
    public final String fallbackDestination;
    public final DEST_TYPE destinationType;
    public final boolean required;

    /**
     * @param source              the source field name.
     * @param sourceType          the source data type.
     * @param destination         the destination field.
     * @param fallbackDestination the destination field to use if the data could not be converted.
     *                            this will always be the verbatim source value. fallbackDestination can be null.
     * @param destinationType     the destination data type.
     * @param required            if true, an exception will be thrown if the source field is not present.
     */
    public Converter(String source, SOURCE_TYPE sourceType,
                     String destination, String fallbackDestination, DEST_TYPE destinationType,
                     boolean required) {
        this.source = source;
        this.sourceType = sourceType == null ? SOURCE_TYPE.string : sourceType;
        this.destination = destination;
        this.fallbackDestination = fallbackDestination;
        this.destinationType = destinationType;
        this.required = required;
    }

    /**
     * Extract the content of the {@link #source} field from the record, process it and
     * add the result(s) to fieldValues.
     * @param record     a Cumulus record.
     * @param resultList the destination for the processed values.
     * @throws IllegalArgumentException if the combination of input and processing was not valid.
     * @throws IllegalStateException if the {@link #source} was required but not present in the record.
     */
    public abstract void convert(CumulusRecord record, List<FieldMapper.FieldValue> resultList)
        throws IllegalArgumentException, IllegalStateException;

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
                value = record.getFieldValueOrNull(source);
                break;
            }
            case assetReference: {
                AssetReference assetReference = record.getAssetReference(source);
                if (assetReference == null) {
                    break;
                }
                // TODO: Test that the displayString is usable
                value = assetReference.getDisplayString();
                break;
            }
            default: value = null;
        }
        if (required && value == null) {
            throw new IllegalStateException(
                "The required field '" + sourceType + "' was not present in the record");
        }
        return value;
    }

    /**
     * Add the given values to the resultList if the values are not null.
     * @param values to add to the resultList.
     * @param resultList the values will be added as {@link #destination}-value pairs to this list.
     */
    protected void addValues(List<String> values, List<FieldMapper.FieldValue> resultList) {
        if (values == null) {
            return;
        }
        values.stream().filter(Objects::nonNull).forEach(value -> addValue(value, resultList));
    }
    /**
     * Add the given value to the resultList if the value is not null.
     * @param resultList the value will be added as a {@link #destination}-value pair to this list.
     * @param value a value to add to the resultList.
     */
    protected void addValue(String value, List<FieldMapper.FieldValue> resultList) {
        addValue(value, false, resultList);
    }
    /**
     * Add the given value to the resultList if the value is not null.
     * @param value a value to add to the resultList.
     * @param resultList the value will be added as a {@link #destination}-value pair to this list.
     * @param linebreakIsMulti if true, the value will be split on linebreaks and each line added as a separate
     *                         pair to resultList.
     */
    protected void addValue(String value, boolean linebreakIsMulti, List<FieldMapper.FieldValue> resultList) {
        if (value != null) {
            if (linebreakIsMulti) {
                for (String line : value.split("\\r?\\n")) {
                    resultList.add(new FieldMapper.FieldValue(destination, line));
                }
            } else {
                resultList.add(new FieldMapper.FieldValue(destination, value));
            }

        }
    }
}
