package com.XAMMER.HRMS.service;

import com.XAMMER.HRMS.model.Holiday;
import java.util.List;

public interface HolidayService {
    List<Holiday> getAllHolidays();
    Holiday getHolidayById(Long id);
    void saveHoliday(Holiday holiday);
    void deleteHoliday(Long id);
    List<Holiday> getUpcomingHolidays();
}