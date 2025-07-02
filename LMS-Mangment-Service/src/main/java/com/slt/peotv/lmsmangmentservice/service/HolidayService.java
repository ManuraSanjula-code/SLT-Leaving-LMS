package com.slt.peotv.lmsmangmentservice.service;

import com.slt.peotv.lmsmangmentservice.model.dto.HolidayDTO;
import com.slt.peotv.lmsmangmentservice.model.req.HolidayReq;

import java.time.LocalDate;
import java.util.List;

public interface HolidayService {
    List<HolidayDTO> getHolidays(int year);
    HolidayDTO getHoliday(LocalDate holidayDate);
    void updateHoliday(LocalDate holidayDate, HolidayDTO holidayDTO);
    void updateHoliday(Long id, HolidayReq holidayDTO);
    void saveHoliday(HolidayReq holidayReq);
    void deleteHoliday(Long id);
}
