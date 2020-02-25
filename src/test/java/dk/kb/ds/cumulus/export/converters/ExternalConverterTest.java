package dk.kb.ds.cumulus.export.converters;

import dk.kb.ds.cumulus.export.CumulusRecordMock;
import dk.kb.ds.cumulus.export.FieldMapper;
import dk.kb.util.YAML;

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
class ExternalConverterTest {

    // TODO: We need to mock the service before having this as a unit test. The result is in resources/bs000001.dhash.json
    public void testLocalService() {
        final String IMAGE = "http://kb-images.kb.dk/DAMJP2/online_master_arkiv/non-archival/KOB/bs_kistebilleder-2/bs000001/full/!345,2555/0/native.jpg";

        Map<String, Object> conf = new LinkedHashMap<>();
        conf.put(Converter.CONF_SOURCE, "mySource");
        conf.put(Converter.CONF_DEST, "myHash");
        conf.put(Converter.CONF_DEST_TYPE, "external");

        conf.put(ExternalConverter.CONF_EXTERNAL_SERVICE,
                 "http://localhost:8080/ds-image-analysis/api/imageDHash/imgURL/?imgURL=$1");

        // {"message":"20,Simple,890940 30,Simple,520707519 42,Simple,2565263978941 56,Simple
        conf.put(ExternalConverter.CONF_EXT_PATTERN, "\\{\"message\":\"20,([a-zA-Z]+),([0-9]+) ([0-9]+).*");
        conf.put(ExternalConverter.CONF_EXT_REPLACEMENT, "$1$3_$2");

        {
            Converter converter = ConverterFactory.buildConverter(new YAML(conf));
            CumulusRecordMock record = new CumulusRecordMock("mySource", IMAGE);
            FieldMapper.FieldValues fieldValues = new FieldMapper.FieldValues();
            converter.convert(record, fieldValues);
            assertFalse(fieldValues.isEmpty(), "There should be produced at least 1 FieldValue");

            String hash = fieldValues.get(0).value;
            assertEquals("Simple30_890940", hash, "The hash from the extraction should be as expected");
        }


    }

}
