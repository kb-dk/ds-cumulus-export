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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class FieldMapper {
    private static final Logger log = LoggerFactory.getLogger(FieldMapper.class);

    private final Map<String, Converter> converters;

    public FieldMapper() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Convert the given record according to the setup.
     * @param record a Cumulus record.
     * @return a list for field-value pairs.
     * @throws IllegalArgumentException if a combination of input and processing was not valid.
     * @throws IllegalStateException if a required field was not present in the record.
     */
    public List<FieldValue> convert(CumulusRecord record) {
        List<FieldValue> resultList = new ArrayList<>();
        for (Converter converter: converters.values()) {
            converter.convert(record, resultList);
        }
        log.trace("Produced {} fieldValues for the given record", resultList.size());
        return resultList;
    }

    /**
     * Simple pair of field name and value.
     */
    public static class FieldValue {
        public final String field;
        public final String value;

        public FieldValue(String field, String value) {
            this.field = field;
            this.value = value;
        }
    }

}
