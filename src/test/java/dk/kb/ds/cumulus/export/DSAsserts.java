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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * custom assertions for easier unit testing.
 */
public class DSAsserts {

    /**
     * Helper for checking that conversion yielded the expected result.
     * @param fieldValues the converted key-values.
     * @param expectedFieldValues the expected key-values.
     */
    public static void assertFieldValues(FieldMapper.FieldValues fieldValues, String... expectedFieldValues) {
        if (expectedFieldValues.length % 2 == 1) {
            throw new IllegalArgumentException(
                "The number of extectedFieldValues should be even but was " + expectedFieldValues.length);
        }
        out:
        for (int i = 0 ; i < expectedFieldValues.length ; i+=2) {
            for (FieldMapper.FieldValue fv: fieldValues) {
                if (fv.field.equals(expectedFieldValues[i])) {
                    assertEquals(expectedFieldValues[i+1], fv.value,
                                 "The field " + expectedFieldValues[i] + " should contain the right value");
                    continue out;
                }
            }
            fail("There should be a fieldValue with key '" + expectedFieldValues[i] + "'");
        }
    }

    /**
     * Helper for checking that conversion yielded the expected multi-valued result.
     * @param fieldValues the converted key-values.
     * @param expectedKey the key for the field to test.
     * @param expectedValues the values that should be in the field.
     */
    public static void assertMultiFieldValues(
        FieldMapper.FieldValues fieldValues, String expectedKey, String... expectedValues) {
        List<String> pool = new ArrayList<>(Arrays.asList(expectedValues));
        for (FieldMapper.FieldValue fv: fieldValues) {
            if (fv.field.equals(expectedKey)) {
                assertTrue(pool.remove(fv.value), "The value '" + fv.value + "' should be in the expectedValues");
            }
        }
        if (!pool.isEmpty()) {
            fail("The following expectedValues could not be located: " + pool);
        }
    }
}
