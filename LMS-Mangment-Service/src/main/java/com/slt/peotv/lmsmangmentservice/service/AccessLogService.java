package com.slt.peotv.lmsmangmentservice.service;

import com.slt.peotv.lmsmangmentservice.entity.AccessLog.AccessLogEntity;

import java.sql.Time;
import java.text.ParseException;

public interface AccessLogService {
    public void main() throws ParseException;
    public void processLogEntry(AccessLogEntity log) throws ParseException;
    public void processLogEntry() throws ParseException;
    public Time parseTime(String timeString) throws ParseException;
}
