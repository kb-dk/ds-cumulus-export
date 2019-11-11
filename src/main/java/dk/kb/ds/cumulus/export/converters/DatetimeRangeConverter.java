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
import dk.kb.ds.cumulus.export.CalendarUtils;
import dk.kb.ds.cumulus.export.FieldMapper;
import dk.kb.ds.cumulus.export.YAML;

import java.util.List;

/**
 * Converts datetime into Solr DateRangeField, which supports arbitrary precision and ranges:
 * {@code 2019-11-08}, {@code 2019-11-08T13:50:05Z}, {@code 2019-11 TO 2019-12-05}.
 * @see <a href="https://lucene.apache.org/solr/8_3_0/solr-core/org/apache/solr/schema/DateRangeField.html">DateRangeField JavaDoc</a>
 */
public class DatetimeRangeConverter extends Converter {

    public static void register() {
        ConverterFactory.registerCreator("datetimeRange", DatetimeRangeConverter::new);
    }

    public DatetimeRangeConverter(YAML config) {
        super(config);
    }

    @Override
    public void convertImpl(CumulusRecord record, List<FieldMapper.FieldValue> resultList) throws IllegalStateException {
        convertImpl(getAsString(record), resultList);
    }

    private void convertImpl(String datetimeStr, List<FieldMapper.FieldValue> resultList) {
        if (datetimeStr == null) {
            return;
        }
        addValue(CalendarUtils.getUTCTimeRange(datetimeStr), resultList);
    }
}
