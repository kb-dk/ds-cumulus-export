package dk.kb.ds.cumulus.export;

import com.canto.cumulus.fieldvalue.AssetReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
class CumulusRecordMockTest {

    @Test
    public void testGetString() {
        CumulusRecordMock record = new CumulusRecordMock("asset", "foo");
        String value = record.getFieldValue("asset");
        assertNotNull(value, "There should be a String value for field 'asset'");
        assertEquals("foo", value, "The value for the field 'asset' should be as expected");
    }

    /**
     * The AssetReference is special as it is tricky to mock.
     */
    @Test
    public void testGetAssetReference() {
        CumulusRecordMock record = new CumulusRecordMock("asset", "foo");
        AssetReference ar = record.getAssetReference("asset");
        assertNotNull(ar, "There should be an asset reference for field 'asset'");
        assertEquals("foo", ar.getDisplayString(), "The display string for the AssetReference should be as expected");
    }


}
