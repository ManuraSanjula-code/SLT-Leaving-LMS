package com.slt.peotv.lmsmangmentservice.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomDateDeserializer extends JsonDeserializer<Date> {
    private static final String[] DATE_FORMATS = {
            "yyyy-MM-dd'T'HH:mm:ss.SSSX",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "EEE, dd MMM yyyy HH:mm:ss zzz",
            "yyyy-MM-dd"
    };

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateStr = p.getText();

        // Remove any "+" in front of the year and normalize the year
        if (dateStr.startsWith("+")) {
            dateStr = dateStr.substring(1);
            // If year has more than 4 digits, normalize it
            int firstDash = dateStr.indexOf('-');
            if (firstDash > 4) {
                String year = dateStr.substring(0, firstDash);
                // Take only the last 4 digits of the year
                year = year.substring(year.length() - 4);
                dateStr = year + dateStr.substring(firstDash);
            }
        }

        // Try each date format
        for (String format : DATE_FORMATS) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(format);
                dateFormat.setLenient(false);
                return dateFormat.parse(dateStr);
            } catch (ParseException e) {
                // Try next format
            }
        }

        // If all formats fail, let Jackson try its default handling
        return new DateDeserializers.DateDeserializer().deserialize(p, ctxt);
    }
}