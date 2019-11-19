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

class StringConverterTest {

    @Test
    public void testLinebreaks() {
        Map<String, Object> conf = new LinkedHashMap<>();
        conf.put(Converter.CONF_SOURCE, "mySource");
        conf.put(Converter.CONF_DEST, "myDest");
        conf.put(Converter.CONF_DEST_TYPE, "string");
        conf.put(Converter.CONF_LINE_BREAK_IS_MULTI, false);
        {
            StringConverter single = new StringConverter(new YAML(conf));
            CumulusRecordMock record = new CumulusRecordMock("mySource", "foo\nbar");
            FieldMapper.FieldValues singleFV = new FieldMapper.FieldValues();
            single.convert(record, singleFV);

            DSAsserts.assertFieldValues(
                singleFV,
                "myDest", "foo\nbar");
        }
        {
            conf.put(Converter.CONF_LINE_BREAK_IS_MULTI, true);
            StringConverter multi = new StringConverter(new YAML(conf));
            CumulusRecordMock record = new CumulusRecordMock("mySource", "foo\nbar");
            FieldMapper.FieldValues multiFV = new FieldMapper.FieldValues();
            multi.convert(record, multiFV);

            DSAsserts.assertMultiFieldValues(multiFV, "myDest", "foo", "bar");
        }
    }

    @Test
    public void testAssetReferenceSource() {
        Map<String, Object> conf = new LinkedHashMap<>();
        conf.put(Converter.CONF_SOURCE, "mySource");
        conf.put(Converter.CONF_SOURCE_TYPE, Converter.SOURCE_TYPE.assetReference);
        conf.put(Converter.CONF_DEST, "myDest");
        conf.put(Converter.CONF_DEST_TYPE, "string");

        StringConverter single = new StringConverter(new YAML(conf));
        CumulusRecordMock record = new CumulusRecordMock("mySource", "foo");
        FieldMapper.FieldValues singleFV = new FieldMapper.FieldValues();
        single.convert(record, singleFV);

        DSAsserts.assertFieldValues(
            singleFV,
            "myDest", "foo");
    }

    @Test
    public void testIDConversion() {
        Map<String, Object> conf = new LinkedHashMap<>();
        conf.put(Converter.CONF_SOURCE, "guid");
        conf.put(Converter.CONF_DEST, "id");
        conf.put(Converter.CONF_DEST_TYPE, "string");
        conf.put(Converter.CONF_REQUIRED, "true");
        conf.put(StringConverter.CONF_PATTERN, ".+");
        conf.put(StringConverter.CONF_REPLACEMENT, "ds_billedsamling_$0");

        StringConverter converter = new StringConverter(new YAML(conf));
        CumulusRecordMock record = new CumulusRecordMock("guid", "foo");
        FieldMapper.FieldValues fieldValues = new FieldMapper.FieldValues();
        converter.convert(record, fieldValues);

        DSAsserts.assertFieldValues(
            fieldValues,
            "id", "ds_billedsamling_foo");
    }

    @Test
    public void testPatternGroups() {
        final String INPUT = "foo23bar88baz!";
        final String EXPECTED = "2388!";

        Map<String, Object> conf = new LinkedHashMap<>();
        conf.put(Converter.CONF_SOURCE, "mySource");
        conf.put(Converter.CONF_DEST, "myDest");
        conf.put(Converter.CONF_DEST_TYPE, "string");
        conf.put(Converter.CONF_REQUIRED, "true");
        conf.put(StringConverter.CONF_PATTERN, "[^0-9]*([0-9]+)[^0-9]*([0-9]+)[a-z]*([^0-9a-z]+).*");
        conf.put(StringConverter.CONF_REPLACEMENT, "$1$2$3");

        StringConverter converter = new StringConverter(new YAML(conf));
        CumulusRecordMock record = new CumulusRecordMock("mySource", INPUT);
        FieldMapper.FieldValues fieldValues = new FieldMapper.FieldValues();
        converter.convert(record, fieldValues);

        DSAsserts.assertFieldValues(
            fieldValues,
            "myDest", EXPECTED);
    }
}
