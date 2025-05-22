package com.slt.peotv.lmsmangmentservice.model.res;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DateWrapper {
    public String iso;
}
