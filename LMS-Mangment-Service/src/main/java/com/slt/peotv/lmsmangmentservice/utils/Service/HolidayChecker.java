package com.slt.peotv.lmsmangmentservice.utils.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slt.peotv.lmsmangmentservice.model.res.Holiday;
import com.slt.peotv.lmsmangmentservice.model.res.HolidayApiResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.ZonedDateTime;

public class HolidayChecker {

    private static final String API_KEY = "pUaTY2zfZxShAJYbmH5FkbrIj9J5Lsby"; // ← Replace with your Calendarific API key
    private static final String COUNTRY = "LK"; // Sri Lanka

    public static boolean isTodayGovHoliday() throws IOException, InterruptedException {
        LocalDate today = LocalDate.now();
        int year = today.getYear();

        String url = String.format(
                "https://calendarific.com/api/v2/holidays?api_key=%s&country=%s&year=%d",
                API_KEY, COUNTRY, year
        );

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper objectMapper = new ObjectMapper();
        HolidayApiResponse apiResponse = objectMapper.readValue(response.body(), HolidayApiResponse.class);

        if (apiResponse != null && apiResponse.response != null && apiResponse.response.holidays != null) {
            for (Holiday holiday : apiResponse.response.holidays) {
                if (holiday.date != null && holiday.date.iso != null) {

                    LocalDate holidayDate;
                    String isoDateString = holiday.date.iso;

                    if (isoDateString.contains("T")) {

                        ZonedDateTime zonedDateTime = ZonedDateTime.parse(isoDateString);
                        holidayDate = zonedDateTime.toLocalDate();
                    } else {
                        holidayDate = LocalDate.parse(isoDateString);
                    }

                    if (holidayDate.equals(today)) {
                        System.out.println("🎉 Holiday: " + holiday.name);
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
