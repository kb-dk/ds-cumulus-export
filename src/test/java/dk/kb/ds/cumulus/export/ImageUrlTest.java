package dk.kb.ds.cumulus.export;

import org.junit.jupiter.api.Assertions;
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
class ImageUrlTest {

    @Test
    void testValid() {
        String[][] TESTS = {
            {
                "cumulus-core-test-01:/Depot/DAM/test/Samlingsbilleder/0000/375/526/KE030219.tif",
                "https://kb-images.kb.dk/Depot/DAM/test/Samlingsbilleder/0000/375/526/KE030219/full/full/0/native.jpg"
            },
            {
                "cumulus-elsewhere:/Samlingsbilleder/Foo/KE030219.tif",
                "https://kb-images.kb.dk/Samlingsbilleder/Foo/KE030219/full/full/0/native.jpg"
            }
        };

        for (String[] test: TESTS) {
            assertEquals(test[1], ImageUrl.makeUrl(test[0]));
        }
    }

    @Test
    void testInvalid() {
        String[] INVALIDS = {
                null,
                "cumulus-elsewhere:/Samlingsbilleder/Foo/KE030219.png",
                "missingcolon/Samlingsbilleder/Foo/KE030219.tif"
        };

        for (String invalid: INVALIDS) {
            try {
                assertNull(ImageUrl.makeUrl(invalid));
            } catch (Exception e) {
                // Exceptions are okay too, but we should decide on the right action
            }
        }
    }
}
