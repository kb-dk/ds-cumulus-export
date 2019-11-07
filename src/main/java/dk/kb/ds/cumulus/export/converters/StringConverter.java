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
import dk.kb.ds.cumulus.export.YAML;

import java.util.List;

/**
 * Converts String content by either copying verbatim or splitting on newline.
 */
public class StringConverter extends Converter {
    private final boolean lineBreakIsMulti;

    public StringConverter(YAML config) {
        this(config.getString("source"), SOURCE_TYPE.valueOf(config.getString("sourceType")),
             config.getString("dest"), config.getBoolean("required", false),
             config.getBoolean("lineBreakIsMulti", false));
    }

    public StringConverter(String source, SOURCE_TYPE sourceType,
                           String destination,
                           boolean required, boolean linebreakIsMulti) {
        super(source, sourceType, destination, null, DEST_TYPE.verbatim, required);
        this.lineBreakIsMulti = linebreakIsMulti;
    }

    @Override
    public void convert(CumulusRecord record, List<FieldMapper.FieldValue> resultList) throws IllegalStateException {
        addValue(getAsString(record), lineBreakIsMulti, resultList);
    }
}
