package dk.kb.ds.cumulus.export;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for calendar issues.
 */
public class CalendarUtils {
    private static final Logger log = LoggerFactory.getLogger(CalendarUtils.class);

    /** constructor to prevent instantiation of utility class. */
    protected CalendarUtils() {}

    private static final String DATE_RANGE_PATTERN = "[%s TO %s]";
    private static final String DASH = "-";

    /**
     * ISO8601 representation with second granularity and Zulu time. Compatible with Solr datetime.
     */
    private static final DateTimeFormatter ISO8601_FORMATTER =  DateTimeFormatter.
        ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT);

    /**
     * Parse the given datetime String leniently, by iterating multiple formats until a result is derived.
     * @param datetime uncontrolled datetime representation.
     * @return time range representation of the given datetime if possible, else null.
     */
    public static String getUTCTimeRange(String datetime) {
        if (datetime == null || datetime.isEmpty()) {
            return null;
        }

        String parsed;
        if ((parsed = parseFullWritten(datetime)) != null ||
            (parsed = parseYear(datetime)) != null ||
            (parsed = parseYearMonth(datetime)) != null ||
            (parsed = parseYearMonthDay(datetime)) != null ||
            (parsed = parseYearToYear(datetime)) != null
        ) {
            return parsed;
        }
        log.debug("Unable to parse datetime '{}'", datetime);
        return null;
    }

    /**
     * Parse the given datetime String leniently, by iterating multiple formats until a result is derived.
     * @param datetime     uncontrolled datetime representation.
     *  the result will be padded to seconds, i.e. if the extracted datetime is '2019-10-29'
     *  then the result will be '2019-10-29T00:00:00Z'.
     * @return ISO-8601 representation of the given datetime if possible, else null.
     */
    public static String getUTCTime(String datetime) {
        if (datetime == null || datetime.isEmpty()) {
            return null;
        }

        String parsed;
        if ((parsed = parseFullWritten(datetime)) != null ||
            (parsed = parseYear(datetime)) != null ||
            (parsed = parseYearMonth(datetime)) != null ||
            (parsed = parseYearMonthDay(datetime)) != null
            ) {
            return ensurePadding(parsed);
        }
        log.debug("Unable to parse datetime '{}'", datetime);
        return null;
    }


    private static final String PADDING = "0000-01-01T00:00:00Z";
    /**
     * Pads a given input to second granularity and with Zulu time designation.
     * @param datetime     a partial ISO8601 datetime.
     * @return datetime padded to seconds.
     */
    private static String ensurePadding(String datetime) {
        return datetime + PADDING.substring(datetime.length());
    }

    private static final DateTimeFormatter WRITTEN_PARSER = DateTimeFormatter.
        ofPattern("ccc LLL dd HH:mm:ss zzz yyyy", Locale.UK); // UK as we use ccc and LLL
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

    private final static Pattern YEAR_MONTH_PATTERN = Pattern.compile("^([0-9]{4}).(0[1-9]|1[012])$");
    /**
     * Parses inputs consisting of exactly 4 digits (year), a non-digit character and 2 digits (month).
     * @param datetime year.month, e.g. '2019-10'
     * @return Truncated ISO-8601 representation of the given datetime if possible, else null.
     */
    private static String parseYearMonth(String datetime) {
        Matcher ymMatcher = YEAR_MONTH_PATTERN.matcher(datetime);
        if (ymMatcher.matches()) {
            return ymMatcher.group(1) + DASH + ymMatcher.group(2);
        }
        log.trace("parseYearMonth({}) failed parsing", datetime);
        return null;
    }

    private final static Pattern YEAR_MONTH_DAY_PATTERN = Pattern.compile("^([0-9]{4}).(0[1-9]|1[012]).(0[1-9]|[12][0-9]|3[01])$");
    /**
     * Parses inputs consisting of exactly 4 digits (year), a non-digit character, 2 digits (month, 01-12), a non-digit character
     * and 2 digits (day, 01-31).
     * @param datetime year.month.day, e.g. '2019-10-31'.
     * @return Truncated ISO-8601 representation of the given datetime if possible, else null.
     */
    private static String parseYearMonthDay(String datetime) {
        Matcher ymdMatcher = YEAR_MONTH_DAY_PATTERN.matcher(datetime);
        if (ymdMatcher.matches()) {
            return ymdMatcher.group(1)
                + DASH + ymdMatcher.group(2)
                + DASH + ymdMatcher.group(3);
        }
        log.trace("parseYearMonthDay({}) failed parsing", datetime);
        return null;
    }

    private final static Pattern YEAR_TO_YEAR = Pattern.compile("^([0-9]{4}).([0-9]{4})$");
    private final static Pattern YEAR_RANGE_PATTERN = Pattern.compile("^([0-9]{4}).(1[3-9]|[20-99]})$");
    /**
     * Parses inputs consisting of 4 digits (year), a non-digit character and 4 digits (year)
     * or inputs of 4 digits (year), a non-digit character, two digits (year) in the range 13-99
     * @param datetime year-year, e.g. '2019-2020' or 1912-13
     * @return Truncated ISO-8601 representation of the given datetime range, else null.
     */
    private static String parseYearToYear(String datetime) {
        Matcher yyMatcher = YEAR_TO_YEAR.matcher(datetime);
        if (yyMatcher.matches()) {
            return String.format(Locale.ROOT, DATE_RANGE_PATTERN, yyMatcher.group(1), yyMatcher.group(2));
        }
        yyMatcher = YEAR_RANGE_PATTERN.matcher(datetime);
        if (yyMatcher.matches()){
            return String.format(Locale.ROOT, DATE_RANGE_PATTERN,
                                 yyMatcher.group(1), yyMatcher.group(1).substring(0,2) + yyMatcher.group(2));
        }

        log.trace("parseYearToYear({}) failed parsing", datetime);
        return null;
    }
}
