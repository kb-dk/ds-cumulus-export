package dk.kb.ds.cumulus.export;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for calendar issues.
 */
public class CalendarUtils {
    private static final Logger log = LoggerFactory.getLogger(CalendarUtils.class);

    /** constructor to prevent instantiation of utility class. */
    protected CalendarUtils() {}

    /** A single instance of the DatatypeFactory to prevent overlap from recreating it too often.*/
    private static DatatypeFactory factory = null;
    private static final String FALSE = "false";

    /**
     * ISO8601 representation with second granularity and Zulu time. Compatible with Solr datetime.
     */
    static final DateTimeFormatter ISO8601_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    /**
     * Parse the given datetime String leniently, by iterating multiple formats until a result is derived.
     * @param datetime uncontrolled datetime representation.
     * @return ISO-8601 representation of the given datetime padded to seconds if possible, else null.
     */
    public static String getUTCTime(String datetime) {
        return getUTCTime(datetime, true);
    }
    /**
     * Parse the given datetime String leniently, by iterating multiple formats until a result is derived.
     * @param datetime     uncontrolled datetime representation.
     * @param padToSeconds if true, the result will be padded to seconds, i.e. if the extracted datetime is '2019-10-29'
     *                     then the result will be '2019-10-29T00:00:00Z'.
     * @return ISO-8601 representation of the given datetime if possible, else null.
     */
    public static String getUTCTime(String datetime, boolean padToSeconds) {
        if (datetime == null || datetime.isEmpty()) {
            return null;
        }

        String parsed;
        if ((parsed = parseFullWritten(datetime)) != null ||
            (parsed = parseYear(datetime)) != null ||
            (parsed = parseYearMonth(datetime)) != null
            // (parsed = parseWithAnotherMethod(datetime)) != null // TODO: Extend here
            ) {
            return ensurePadding(parsed, padToSeconds);
        }
        log.debug("Unable to parse datetime '{}'", datetime);
        return null;
    }

    private static final String PADDING = "0000-01-01T00:00:00Z";
    /**
     * Pads a given input to second granularity and with Zulu time designation.
     * @param datetime     a partial ISO8601 datetime.
     * @param padToSeconds if true, the input will be padded to seconds, else it will be returned unchanged.
     * @return datetime padded as requested.
     */
    private static String ensurePadding(String datetime, boolean padToSeconds) {
        if (!padToSeconds) {
            return datetime;
        }
        return datetime + PADDING.substring(datetime.length());
    }

    static final DateTimeFormatter WRITTEN_PARSER = DateTimeFormatter.ofPattern("ccc LLL dd HH:mm:ss zzz yyy");
    /**
     * Parses inputs with full date and time in written format.
     * @param datetime written time representation, e.g. 'Mon Jul 29 16:10:29 CEST 2019'.
     * @return ISO-8601 representation of the given datetime if possible, else null.
     */
    private static String parseFullWritten(String datetime) {
        try {
            LocalDateTime createdDateFormatted = LocalDateTime.parse(datetime, WRITTEN_PARSER);
            LocalDateTime createdDateUTC = createdDateFormatted.atZone(ZoneId.of("Europe/Copenhagen"))
                .withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
            return createdDateUTC.format(ISO8601_FORMATTER);
        } catch (Exception e) {
            log.trace("parseFullWritten({}) failed parsing", datetime);
        }
        return null;
    }

    private final static Pattern YEAR_PATTERN = Pattern.compile("^([0-9]{4})$");
    /**
     * Parses inputs consisting of exactly 4 digits.
     * @param datetime year, represented as 4 digits.
     * @return Truncated ISO-8601 representation of the given datetime if possible, else null.
     */
    private static String parseYear(String datetime) {
        if (YEAR_PATTERN.matcher(datetime).matches()) {
            return datetime;
        }
        log.trace("parseYear({}) failed parsing", datetime);
        return null;
    }

    private final static Pattern YEAR_MONTH_PATTERN = Pattern.compile("^([0-9]{4}).([0-9]{2})$");
    /**
     * Parses inputs consisting of exactly 4 digits (year), a non-digit character and 2 digits (month).
     * @param datetime year.month, e.g. '2019-10'.
     * @return Truncated ISO-8601 representation of the given datetime if possible, else null.
     */
    private static String parseYearMonth(String datetime) {
        Matcher ymMatcher = YEAR_MONTH_PATTERN.matcher(datetime);
        if (ymMatcher.matches()) {
            return ymMatcher.group(1) + "-" + ymMatcher.group(2);
        }
        log.trace("parseYearMonth({}) failed parsing", datetime);
        return null;
    }

    /**
     * Turns a date into a XMLGregorianCalendar.
     *
     * @param date The date.
     * @return The XMLGregorianCalendar.
     */
    public static XMLGregorianCalendar getXmlGregorianCalendar(Date date) {
        try {
            if(factory == null) {
                factory = DatatypeFactory.newInstance();
            }

            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(date);
            return factory.newXMLGregorianCalendar(gc);
        } catch (Exception e) {
            IllegalStateException res = new IllegalStateException("Could not create XML date for the date '"
                + date + "'.", e);
            throw res;
        }
    }

    /**
     * @return The current date in the XML date format.
     */
    public static String getCurrentDate() {
        return getXmlGregorianCalendar(new Date()).toString();
    }

    /**
     * Retrieves a date in the XML format, which needs to be transformed from another given format.
     * @param format The format of the given date.
     * @param dateString The given date to transform.
     * @return The given date in the XML date format.
     */
    public static String getDateTime(String format, String dateString) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        try {
            Date date = formatter.parse(dateString);
            return getXmlGregorianCalendar(date).toString();
        } catch (ParseException e) {
            try {
                SimpleDateFormat formatter2 = new SimpleDateFormat(format, Locale.US);
                Date date2 = formatter2.parse(dateString);
                return getXmlGregorianCalendar(date2).toString();
            } catch (ParseException e2) {
                return FALSE;
//                IllegalStateException res = new IllegalStateException("Cannot parse date '" + dateString
//                    + "' in format '" + format + "'. " + "Caught exceptions: " + e + " , " + e2, e2);
//                throw res;
            }
        }
    }

    /**
     * Converts gregorian date .
     * @param datetimeFromCumulus The originating date from Cumulus.
     * @return The date in solr date_range format or text value ??
     */

    public static String convertDatetimeFormat(String datetimeFromCumulus) throws DateTimeException {

        String[] patternList = {"yyyy-MM-dd", "yyyy-MM", "yyyy"};
        String pattern;

        String gregorianDate = getDateTime(patternList[0], datetimeFromCumulus);
        pattern = patternList[0];

        if (gregorianDate.equals(FALSE)){
            pattern = patternList[0];
            gregorianDate = getDateTime("yyyy.MM.dd", datetimeFromCumulus);
        }
        if (gregorianDate.equals(FALSE)){
            pattern = patternList[1];
            gregorianDate = getDateTime("yyyy.MM", datetimeFromCumulus);
        }
        if (gregorianDate.equals(FALSE)){
            pattern = patternList[1];
            gregorianDate = getDateTime(pattern, datetimeFromCumulus);

        }
        if (gregorianDate.equals(FALSE)){
            pattern = patternList[2];
            gregorianDate = getDateTime(pattern, datetimeFromCumulus);
        }
        if (gregorianDate.equals(FALSE)){
            return datetimeFromCumulus;
            //Return the string value of År, i.e. no appropriate date format found
//            TODO: How should this be handled by solr
        }

        // Convert to solr date range format

        LocalDateTime createdDateFormatted = LocalDateTime.parse(gregorianDate, DateTimeFormatter.ISO_DATE_TIME);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(pattern);

        final String format = createdDateFormatted.format(timeFormatter);

        return format;
    }

}
