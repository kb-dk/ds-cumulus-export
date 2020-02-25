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

import dk.kb.cumulus.CumulusRecord;
import dk.kb.ds.cumulus.export.FieldMapper;
import dk.kb.util.YAML;

import java.util.List;

/**
 * Copies Integers directly. Unless overridden, the Cumulus {@code sourceType} is {@code integer} and will be retrieved
 * as such. Set {@code sourceType} to {@code string} for lenient source format.
 */
public class LongConverter extends Converter {

    public static void register() {
        ConverterFactory.registerCreator("long", LongConverter::new);
    }

    public LongConverter(YAML config) {
        super(config);
    }

    @Override
    public void convertImpl(CumulusRecord record, List<FieldMapper.FieldValue> resultList) throws IllegalStateException {
        addValue(getValue(record), resultList);
    }

    @Override
    protected SOURCE_TYPE getDefaultSourceType() {
        return SOURCE_TYPE.longType;
    }
}
