/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.salesforce.api.utils;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

/**
 * Utility class for working with DateTime fields.
 */
public abstract class DateTimeUtils {

    private static final Pattern BAD_TZ_PATTERN = Pattern.compile("[+-][0-9]{4}+$");

    private static final DateTimeFormatter ISO_8601_FORMATTER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("yyyy-MM-dd")
            .appendLiteral('T')
            .appendPattern("HH:mm:ss[.SSS]")
            .appendOffset("+HH:MM", "Z")
            .toFormatter();

    public static String formatDateTime(ZonedDateTime dateTime) throws DateTimeException {
        return ISO_8601_FORMATTER.format(dateTime);
    }

    public static ZonedDateTime parseDateTime(String dateTimeStr) throws DateTimeParseException {
        return ISO_8601_FORMATTER.parse(normalizeDateTime(dateTimeStr), ZonedDateTime::from);
    }

    private static String normalizeDateTime(String dateTimeAsString) {
        if (BAD_TZ_PATTERN.matcher(dateTimeAsString).find()) {
            int splitAt = dateTimeAsString.length() - 2;
            dateTimeAsString = dateTimeAsString.substring(0, splitAt) + ":" + dateTimeAsString.substring(splitAt);
        }
        return dateTimeAsString;
    }
    
    public static String formatDate(LocalDate date) {
        return ISO_LOCAL_DATE.format(date);
    }
    
    public static LocalDate parseDate(String date) {
        return ISO_LOCAL_DATE.parse(date,LocalDate::from);
    }
        
    public static String formatTime(OffsetTime time) {
        // Sets the timezone as UTC for the time before sending to salesforce
        return ISO_LOCAL_TIME.format(time.withOffsetSameInstant(ZoneOffset.UTC).toLocalTime());
    }
    
    public static OffsetTime parseTime(String time) {
        // Sets the timezone as UTC for the time which comes from salesforce
        return OffsetTime.of(ISO_LOCAL_TIME.parse(time, LocalTime::from), ZoneOffset.UTC);
    }
 
}
