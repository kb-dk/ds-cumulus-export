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

import com.canto.cumulus.CumulusSession;
import com.canto.cumulus.GUID;
import com.canto.cumulus.fieldvalue.AssetReference;
import dk.kb.cumulus.CumulusRecord;
import dk.kb.cumulus.field.Field;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import sun.misc.Unsafe;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Used for unit testing {@link FieldMapper} and friends.
 */
public class CumulusRecordMock extends CumulusRecord {
    private static final Logger log = LoggerFactory.getLogger(FieldMapper.class);

    public Map<String, String> content = new HashMap<>();

    public CumulusRecordMock() {
        super(null, null);
    }

    public CumulusRecordMock(String... keyValues) {
        super(null, null);
        if (keyValues.length % 2 == 1) {
            throw new IllegalArgumentException(
                "There must be an even number of keyValues, but there was " + keyValues.length);
        }
        for (int i = 0 ; i < keyValues.length ; i+=2) {
            content.put(keyValues[i], keyValues[i+1]);
        }
    }

    public String put(String s, String s2) {
        return content.put(s, s2);
    }

    public void putAll(Map<? extends String, ? extends String> map) {
        content.putAll(map);
    }

    public void clear() {
        content.clear();
    }

    /** ****************************** Overrides of CumulusRecord **************************** */

    @Override
    public String getUUID() {
        return content.get("GUID");
    }

    @Override
    public String getFieldValue(String fieldname) {
        return content.get(fieldname);
    }

    @Override
    public String getFieldValueOrNull(String fieldname) {
        return content.getOrDefault(fieldname, null);
    }

    @Override
    public void setStringValueInField(String fieldName, String value) {
        put(fieldName, value);
    }

    @Override
    public Long getFieldLongValue(String fieldname) {
        return Long.valueOf(content.get(fieldname));
    }

    @Override
    public Integer getFieldIntValue(String fieldname) {
        return Integer.valueOf(content.get(fieldname));
    }

    @Override
    public AssetReference getAssetReference(String fieldname) {
        AssetReferenceMock ar = (AssetReferenceMock)arInstantiator.newInstance();
        ar.setDisplayString(getFieldValue(fieldname));
        return ar;
    }
    // The mess below is because the AssetReference does not have an empty constructor
    Objenesis objenesis = new ObjenesisStd();
    ObjectInstantiator arInstantiator = objenesis.getInstantiatorOf(AssetReferenceMock.class);
    private class AssetReferenceMock extends AssetReference {
        private String displayString = null;
        public AssetReferenceMock(CumulusSession session, String pathNameOrXML, String assetHandlingSet) {
            super(session, pathNameOrXML, assetHandlingSet);
            throw new IllegalStateException(
                "The constructor should never be called. Only for use with ObjectInstantiator");
        }

        public void setDisplayString(String displayString) {
            this.displayString = displayString;
        }

        @Override
        public String getDisplayString() {
            return displayString;
        }
    }

    @Override
    public Collection<Integer> getCategories() {
        throw new UnsupportedOperationException("Not yet implemented in the CumulurRecord mocker");
    }

    @Override
    public String getFieldValueForNonStringField(String fieldname) {
        return content.get(fieldname);
    }

    @Override
    public File getFile() {
        throw new UnsupportedOperationException("Not yet implemented in the CumulurRecord mocker");
    }

    @Override
    public void setNewAssetReference(File f) {
        throw new UnsupportedOperationException("Not yet implemented in the CumulurRecord mocker");
    }

    @Override
    public void updateAssetReference() {
        throw new UnsupportedOperationException("Not yet implemented in the CumulurRecord mocker");
    }

    @Override
    public void writeFieldMetadata(OutputStream out) throws ParserConfigurationException, TransformerException {
        throw new UnsupportedOperationException("Not yet implemented in the CumulurRecord mocker");
    }

    @Override
    protected void addCumulusFieldToMetadataOutput(Field f, Document doc, Element rootElement) {
        throw new UnsupportedOperationException("Not yet implemented in the CumulurRecord mocker");
    }

    @Override
    protected String[] getValues(String value) {
        throw new UnsupportedOperationException("Not yet implemented in the CumulurRecord mocker");
    }

    @Override
    public void validateFieldsHasValue(Collection<String> requiredFields) {
        throw new UnsupportedOperationException("Not yet implemented in the CumulurRecord mocker");
    }

    @Override
    public void validateFieldsExists(Collection<String> requiredFields) {
        throw new UnsupportedOperationException("Not yet implemented in the CumulurRecord mocker");
    }

    @Override
    public void setStringEnumValueForField(String fieldName, String value) {
        throw new UnsupportedOperationException("Not yet implemented in the CumulurRecord mocker");
    }

    @Override
    protected void setStringValueInField(GUID fieldGuid, String value) {
        throw new UnsupportedOperationException("Not yet implemented in the CumulurRecord mocker");
    }

    @Override
    public void setDateValueInField(String fieldName, Date dateValue) {
        throw new UnsupportedOperationException("Not yet implemented in the CumulurRecord mocker");
    }

    @Override
    public void setBooleanValueInField(String fieldName, Boolean value) {
        throw new UnsupportedOperationException("Not yet implemented in the CumulurRecord mocker");
    }

    @Override
    public boolean isMasterAsset() {
        throw new UnsupportedOperationException("Not yet implemented in the CumulurRecord mocker");
    }

    @Override
    public boolean isSubAsset() {
        throw new UnsupportedOperationException("Not yet implemented in the CumulurRecord mocker");
    }

    @Override
    public void addMasterAsset(CumulusRecord record) {
        throw new UnsupportedOperationException("Not yet implemented in the CumulurRecord mocker");
    }

    @Override
    public void addSubAsset(CumulusRecord record) {
        throw new UnsupportedOperationException("Not yet implemented in the CumulurRecord mocker");
    }

    @Override
    public void createRelationToRecord(CumulusRecord record, String fieldName, GUID relation) {
        throw new UnsupportedOperationException("Not yet implemented in the CumulurRecord mocker");
    }
}
