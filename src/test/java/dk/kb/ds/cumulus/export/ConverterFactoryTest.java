package dk.kb.ds.cumulus.export;

import dk.kb.ds.cumulus.export.converters.Converter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
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
class ConverterFactoryTest {

    @Test
    public void testDefaultFactory() throws IOException {
        Map<String, Converter> converters =
            ConverterFactory.build("ds-cumulus-export-default-mapping.yml", null);
        assertEquals(3, converters.size(), "There should be the right number of converters");
    }

}
