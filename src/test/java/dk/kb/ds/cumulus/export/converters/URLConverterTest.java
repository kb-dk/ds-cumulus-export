package dk.kb.ds.cumulus.export.converters;

import dk.kb.ds.cumulus.export.CumulusRecordMock;
import dk.kb.ds.cumulus.export.DSAsserts;
import dk.kb.ds.cumulus.export.FieldMapper;
import dk.kb.ds.cumulus.export.YAML;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

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
class URLConverterTest {
    /**
     * This test relies on the current (2019-11-18) behaviour of https://kb.dk/ which does not return HTTP 200,
     * while http://www.kb.dk/ does return HTTP 200. This is unreliable and should be mocked instead.
     */
    @Test
    public void testVerify() {
        Map<String, Object> conf = new LinkedHashMap<>();
        conf.put(Converter.CONF_SOURCE, "mySource");
        conf.put(Converter.CONF_DEST, "myURL");
        conf.put(Converter.CONF_DEST_TYPE, "url");

        {
            Converter converter = new URLConverter(new YAML(conf));
            CumulusRecordMock record = new CumulusRecordMock("mySource", "https://kb.dk/");
            FieldMapper.FieldValues fieldValues = new FieldMapper.FieldValues();
            converter.convert(record, fieldValues);

            assertTrue(fieldValues.isEmpty(),"There should be no FieldValues produced, but there were " + fieldValues);
        }

        conf.put(URLConverter.CONF_VERIFY_PATTERN, "^https?://(?:www[.])?(.+)");
        conf.put(URLConverter.CONF_VERIFY_REPLACEMENT, "http://www.$1");

        {
            Converter converter = new URLConverter(new YAML(conf));
            CumulusRecordMock record = new CumulusRecordMock("mySource", "https://kb.dk/");
            FieldMapper.FieldValues fieldValues = new FieldMapper.FieldValues();
            converter.convert(record, fieldValues);

            DSAsserts.assertFieldValues(
                fieldValues,
                "myURL", "https://kb.dk/");
        }
    }
}
