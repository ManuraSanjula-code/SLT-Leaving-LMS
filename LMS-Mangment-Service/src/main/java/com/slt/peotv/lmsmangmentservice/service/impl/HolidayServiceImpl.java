package com.slt.peotv.lmsmangmentservice.service.impl;

import com.slt.peotv.lmsmangmentservice.entity.Holiday;
import com.slt.peotv.lmsmangmentservice.model.dto.HolidayDTO;
import com.slt.peotv.lmsmangmentservice.model.req.HolidayReq;
import com.slt.peotv.lmsmangmentservice.repository.HolidayRepository;
import com.slt.peotv.lmsmangmentservice.service.HolidayService;
import com.slt.peotv.lmsmangmentservice.utils.service.LMSUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class HolidayServiceImpl implements HolidayService {
    @Autowired
    private HolidayRepository holidayRepository;
    @Autowired
    private LMSUtils lmsUtils;
    @Override
    public List<HolidayDTO> getHolidays(int year) {
        return holidayRepository.findByYear(year).stream().map(lmsUtils::maoHolidayToDTO).toList();
    }

    @Override
    public HolidayDTO getHoliday(LocalDate holidayDate) {
        return null;
    }

    @Override
    public void updateHoliday(LocalDate holidayDate, HolidayDTO holidayDTO) {

    }

    @Override
    public void updateHoliday(Long id, HolidayReq holidayReq) {
        Optional<Holiday> holidayOptional = holidayRepository.findById(id);
        if(holidayOptional.isPresent()) {
            Holiday holiday = holidayOptional.get();

            if(holidayReq.getHolidayDate() != null)
                holiday.setHolidayDate(holidayReq.getHolidayDate());

            if(holidayReq.getDescription() != null)
                holiday.setDescription(holidayReq.getDescription());

            holiday.setRecurring(holidayReq.isRecurring());

            if(holidayReq.getCreatedAt() != null)
                holiday.setCreatedAt(holidayReq.getCreatedAt());

            holidayRepository.save(holiday);
        }
    }

    @Override
    public void saveHoliday(HolidayReq holidayReq) {
        holidayRepository.save(lmsUtils.mapReqoHoliday(holidayReq));
    }

    @Override
    public void deleteHoliday(Long id) {
        holidayRepository.deleteById(id);
    }
}
