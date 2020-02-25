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

import dk.kb.util.YAML;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extension of {@link StringConverter} that calls an external web service and adds the result.
 */
public class ExternalConverter extends StringConverter {
    private static final Logger log = LoggerFactory.getLogger(StringConverter.class);

    /**
     * The URL for the external service. In the URL, {@code $1} will be replaced with the String delivered
     * by the super-class. {@code $1} is mandatory in the URL.
     */
    public static final String CONF_EXTERNAL_SERVICE = "externalService";
    /**
     * If specified, the result from the external service will be treated as JSON and the element at the given
     * path will be extracted and added as the result. If not defined, the result from the external service
     * will be passed on as-is in the chain.
     */
    // TODO: Implement this througn kb.util
    //    public static final String CONF_JSON_PATH = "jsonPath";

    /**
     * If specified, the value delivered the external service (or by the JSON path {@link #CONF_JSON_PATH}
     * will be regexp matched and replaced before being added. If not specified, the value will be passes
     * unmodified.
     */
    public static final String CONF_EXT_PATTERN = "extPattern";
    public static final String CONF_EXT_REPLACEMENT = "extReplacement";

    private final String externalService;
    private final Pattern extPattern;
    private final String extReplacement;

    public static void register() {
        ConverterFactory.registerCreator("external", ExternalConverter::new);
    }

    public ExternalConverter(YAML config) {
        super(config);
        externalService = config.getString(CONF_EXTERNAL_SERVICE);
        if (!externalService.contains("$1")) {
            throw new IllegalArgumentException(String.format(
                Locale.ENGLISH, "The %s was '%s' but should contain the marker $1",
                CONF_EXTERNAL_SERVICE, externalService));
        }
        extPattern = config.containsKey(CONF_EXT_PATTERN) ?
            Pattern.compile(config.getString(CONF_EXT_PATTERN)) : null;
        extReplacement = config.getString(CONF_EXT_REPLACEMENT, null);
        HttpURLConnection.setFollowRedirects(false);
    }

    @Override
    String convertImpl(String input) {
        final String superValue = super.convertImpl(input);

        if (superValue == null) {
            return null;
        }
        // TODO: What about encoding?
        String stringURL = externalService.replace("$1", superValue);
        URL url;
        try {
            url = new URL(stringURL);
        } catch (MalformedURLException e) {
            log.warn("Unable to parse '" + stringURL + "' as an URL", e);
            return null;
        }
        log.debug("Calling external service with {}", url);
        String content;
        try {
            content = IOUtils.toString(url, "utf-8");
        } catch (IOException e) {
            log.warn("IOException while reading content of '" + url + "'", e);
            return null;
        }

        if (extPattern == null) {
            return content;
        }
        Matcher matcher = extPattern.matcher(content);
        if (!matcher.matches()) {
            log.warn("Unable to match pattern '{}' on input from URL '{}': '{}'",
                     extPattern.pattern(), url, content.length() > 203 ? content.substring(0, 200) + "..." : content);
            return null;
        }

        return extReplacement == null ? content : matcher.replaceAll(extReplacement);
    }


}
