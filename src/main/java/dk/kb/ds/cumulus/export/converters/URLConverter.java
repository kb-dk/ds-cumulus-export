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

import dk.kb.ds.cumulus.export.YAML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Extension of {@link StringConverter} that expects a URL to be generated, then verifies if the URL works.
 */
public class URLConverter extends StringConverter {
    private static final Logger log = LoggerFactory.getLogger(StringConverter.class);

    /**
     * If specified, the value delivered by the super-class {@link StringConverter} will be regexp matched and
     * replaced before being verified. This derived value is only used for verification: If the verification
     * passes, the super-class delivered value will be passed on.
     */
    public static final String CONF_VERIFY_PATTERN = "verifyPattern";
    public static final String CONF_VERIFY_REPLACEMENT = "verifyReplacement";

    /**
     * If {@code verifyURL: true} is defined in the setup, the result from the super class {@link StringConverter}
     * will be verified. If the server at the end of the URL does not respond with a HTTP code 200 (OK), null will
     * be returned.
     */
    public static final String CONF_VERIFY_URL = "verifyURL";
    public static final boolean DEFAULT_VERIFY_URL = true;

    private final boolean verifyURL;
    private final Pattern verifyPattern;
    private final String verifyReplacement;

    public static void register() {
        ConverterFactory.registerCreator("url", URLConverter::new);
    }

    public URLConverter(YAML config) {
        super(config);
        verifyURL = config.getBoolean(CONF_VERIFY_URL, DEFAULT_VERIFY_URL);
        verifyPattern = config.containsKey(CONF_VERIFY_PATTERN) ?
            Pattern.compile(config.getString(CONF_VERIFY_PATTERN)) : null;
        verifyReplacement = config.getString(CONF_VERIFY_REPLACEMENT, null);
        HttpURLConnection.setFollowRedirects(false);
    }

    @Override
    String convertImpl(String input) {
        final String url = super.convertImpl(input);

        if (url == null || !verifyURL) {
            return input;
        }
        boolean ok = resolves(url);
        if (ok) {
            log.debug("Server responds HTTP 200 (OK) for '" + url + "' derived from '" + input + "'");
            return input;
        }
        log.warn("No resource available for '" + url + "' derived from '" + input + "'");
        return null;
    }

    /**
     * Optionally performs regexp-replace with setup from {@link #CONF_VERIFY_PATTERN} and
     * {@link #CONF_VERIFY_REPLACEMENT}, then checks server response for the generated URL.
     * @param url an URL to verify.
     * @return true if the servere responded HTTP 200 (OK) for the URL.
     */
    private boolean resolves(String url) {
        final String adjustedURL = getMatchedAndReplaced(verifyPattern, verifyReplacement, url);
        if (adjustedURL == null) {
            return false;
        }
        HttpURLConnection con;
        try {
            con = (HttpURLConnection) new URL(adjustedURL).openConnection();
        } catch (IOException e) {
            log.warn("Unable to open connection to '" + adjustedURL + "' derived from incoming URL '" + url + "'", e);
            return false;
        }
        try {
            con.setRequestMethod("HEAD");
            return con.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (ProtocolException e) {
            log.warn("Unable to set request method 'HEAD' for '" + adjustedURL + "' derived from incoming URL '" +
                     url + "'", e);
        } catch (IOException e) {
            log.warn("Unable to get response code for '" + adjustedURL + "' derived from incoming URL '" +
                     url + "'", e);
        } finally {
            con.disconnect();
        }
        return false;
    }

}
