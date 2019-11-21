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

import dk.kb.ds.cumulus.export.CumulusRecordMock;
import dk.kb.ds.cumulus.export.DSAsserts;
import dk.kb.ds.cumulus.export.FieldMapper;
import dk.kb.ds.cumulus.export.YAML;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class IntegerConverterTest {

    @Test
    public void testInteger() {
        final AtomicInteger getIntCalledCounter = new AtomicInteger(0);

        Map<String, Object> conf = new LinkedHashMap<>();
        conf.put(Converter.CONF_SOURCE, "myInt");
        conf.put(Converter.CONF_DEST, "myIntDest");
        conf.put(Converter.CONF_DEST_TYPE, "integer");
        Converter converter = ConverterFactory.buildConverter(new YAML(conf));

        CumulusRecordMock record = new CumulusRecordMock("myInt", "123") {
            // We want to see if the Integer-returning method is called when requesting the value for the myInt field
            @Override
            public Integer getFieldIntValue(String fieldname) {
                getIntCalledCounter.incrementAndGet();
                return super.getFieldIntValue(fieldname);
            }
        };
        FieldMapper.FieldValues fieldValues = new FieldMapper.FieldValues();
        converter.convert(record, fieldValues);

        DSAsserts.assertFieldValues(
            fieldValues,
            "myIntDest", "123");

        assertEquals(1, getIntCalledCounter.get(),
                     "The getFieldIntValue should be called the expected number of times");
    }
}
