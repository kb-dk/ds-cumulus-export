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

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FieldMapperTest {

    @Test
    public void testBasicMapping() throws IOException {
        CumulusRecordMock record = new CumulusRecordMock(
            "Titel", "myTitle",
            "År", "2019-11-11"
        );
        FieldMapper mapper = new FieldMapper();
        FieldMapper.FieldValues fieldValues = mapper.apply(record);
        assertContent(fieldValues,
                      "title", "myTitle",
                      "datetime", "2019-11-11");
    }

    /**
     * Helper for checking that conversion yielded the expected result-
     * @param fieldValues the converted key-values.
     * @param expectedValues the expected key-values.
     */
    private void assertContent(FieldMapper.FieldValues fieldValues, String... expectedValues) {
        if (expectedValues.length % 2 == 1) {
            throw new IllegalArgumentException("The number of keyValues should be even but was " + expectedValues.length);
        }
        out:
        for (int i = 0 ; i < expectedValues.length ; i+=2) {
            for (FieldMapper.FieldValue fv: fieldValues) {
                if (fv.field.equals(expectedValues[i])) {
                    assertEquals(expectedValues[i+1], fv.value,
                                 "The field " + expectedValues[i] + " should contain the right value");
                    continue out;
                }
            }
            fail("There should be a fieldValue with key '" + expectedValues[i] + "'");
        }
    }
}
