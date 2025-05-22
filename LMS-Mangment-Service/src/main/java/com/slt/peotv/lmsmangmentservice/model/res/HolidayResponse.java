package com.slt.peotv.lmsmangmentservice.model.res;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HolidayResponse {
    public List<Holiday> holidays;
}
