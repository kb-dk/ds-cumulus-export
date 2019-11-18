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

class FieldMapperTest {

    @Test
    public void testBasicMapping() throws IOException {
        CumulusRecordMock record = new CumulusRecordMock(
            "guid", "Uid:dk:kb:doms:2007-01/b29e6d60-717e-11e0-82d7-002185371280",
            "Titel", "myTitle",
            "År", "2019-11-11",
            "Item Creation Date", "Fri Oct 04 10:05:10 CET 2019",
            "Categories", "toys\nanimals",
            "Emneord", "Old wars\nNew orders",
            "Ophav", "H.C. Andersen\nGrimm E. Ulv",
            "Copyright", "Custom License"
        );
        FieldMapper mapper = new FieldMapper();
        FieldMapper.FieldValues fieldValues = mapper.apply(record);

        DSAsserts.assertFieldValues(
            fieldValues,
            "id", "ds_billedsamling_b29e6d60-717e-11e0-82d7-002185371280",
            "title", "myTitle",
            "datetime", "2019-11-11",
            "created_date", "2019-10-04T08:05:10Z",
            "license", "Custom License"
        );

        DSAsserts.assertMultiFieldValues(
            fieldValues,
            "keyword",
            "toys", "animals"
        );
        DSAsserts.assertMultiFieldValues(
            fieldValues,
            "subject",
            "Old wars", "New orders"
        );
        DSAsserts.assertMultiFieldValues(
            fieldValues,
            "author",
            "H.C. Andersen", "Grimm E. Ulv"
        );
    }

}
