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

import com.canto.cumulus.GUID;
import com.canto.cumulus.Item;
import com.canto.cumulus.ItemCollection;
import com.canto.cumulus.fieldvalue.AssetReference;
import dk.kb.cumulus.CumulusRecord;
import dk.kb.ds.cumulus.export.FieldMapper;
import dk.kb.ds.cumulus.export.YAML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts String content by either copying verbatim or splitting on newline.
 * Newline splitting is handled by {@link Converter}.
 */
public class StringConverter extends Converter {
    private static final Logger log = LoggerFactory.getLogger(StringConverter.class);

    /**
     * If specified, the Cumulus value regexp matched against the {@code pattern} and replaced with {@code replacement}
     * before being verified added. If the incoming values does not match, the value is not added.
     */
    public static final String CONF_PATTERN = "pattern";
    public static final String CONF_REPLACEMENT = "replacement";

    private Pattern pattern;
    private String replacement;

    public static void register() {
        ConverterFactory.registerCreator("string", StringConverter::new);
    }

    public StringConverter(YAML config) {
        super(config);
        pattern = config.containsKey(CONF_PATTERN) ? Pattern.compile(config.getString(CONF_PATTERN)) : null;
        replacement = config.getString(CONF_REPLACEMENT, null);
        if (pattern != null) {
            log.debug("");
            if (replacement == null) {
                throw new IllegalArgumentException(
                    "A pattern '" + pattern.pattern() + "' was specified, but a replacement is missing");
            }
        }
    }

    @Override
    public void convertImpl(CumulusRecord record, List<FieldMapper.FieldValue> resultList) {
        switch (sourceType) {
            case string: {
                addValue(convertImpl(getAsString(record)), resultList);
                break;
            }
//            case assetReference: {
//                AssetReference ar = record.getAssetReference(source);
//                addValue(convertImpl(ar.getDisplayString()), resultList);
//                break;
//            }
            case assetReference/*Rendition*/: {
                GUID guid = record.getGUID(source);
                if (guid != null){
                    AssetReference ar = getRenditionAssetReference(record, guid);
                    addValue(convertImpl(ar != null ? ar.getDisplayString() : null), resultList);
                    break;
                }
                addValue(null, resultList);
                break;
            }
            default: throw new UnsupportedOperationException("The source type '" + sourceType + "' is not supported");
        }
    }

    private AssetReference getRenditionAssetReference(CumulusRecord record, GUID guid) {
        GUID rendition_name_guid = null;
        GUID rendition_state_guid = null;
        final int FINISHED_STATE_ID = 3; //Possible to retrieve it somewhere??
        final String JP2_RENDITION_NAME = "JPEG2000";
        AssetReference ar = null;

        ItemCollection renditions = record.getTableValue(guid);
        if (renditions != null) {
            for (com.canto.cumulus.FieldDefinition fd : renditions.getLayout()) {
                if ("Rendition Name".equals(fd.getName())) rendition_name_guid = fd.getFieldUID();
                if ("State".equals(fd.getName())) rendition_state_guid = fd.getFieldUID();
            }
            String query = String.format(Locale.ROOT, "%s == \"%s\" && %s == \":ID:%d\"",
                rendition_name_guid, JP2_RENDITION_NAME,
                rendition_state_guid, FINISHED_STATE_ID);
            renditions.find(query, null, null, null);
            if (renditions.getItemCount() == 1) {
                Item rendition = renditions.iterator().next();
                if (rendition.hasValue(GUID.UID_REC_ASSET_REFERENCE)) {
                    ar = rendition.getAssetReferenceValue(GUID.UID_REC_ASSET_REFERENCE);
                }
            }
        }
        return ar;
    }

    String convertImpl(String input) {
        return getMatchedAndReplaced(pattern, replacement, input);
    }

    String getMatchedAndReplaced(Pattern pattern, String replacement, String value) {
        if (value == null || pattern == null) {
            return value;
        }
        Matcher matcher = pattern.matcher(value);
        if (!matcher.matches()) {
            log.debug("The matching of pattern and value failed:");
            log.debug("Pattern: {}", pattern);
            log.debug("Value: {}", value);
            return null;
        }
        return replacement == null ? value : matcher.replaceAll(replacement);
    }
}
