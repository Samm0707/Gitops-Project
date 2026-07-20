package com.XAMMER.HRMS.service;

import com.XAMMER.HRMS.model.Holiday;
import com.XAMMER.HRMS.repository.HolidayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class HolidayServiceImpl implements HolidayService {

    private final HolidayRepository holidayRepository;

    @Autowired
    public HolidayServiceImpl(HolidayRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    @Override
    public List<Holiday> getAllHolidays() {
        return holidayRepository.findAll();
    }

    @Override
    public Holiday getHolidayById(Long id) {
        return holidayRepository.findById(id).orElse(null);
    }

    @Override
    public void saveHoliday(Holiday holiday) {
        holidayRepository.save(holiday);
    }

    @Override
    public void deleteHoliday(Long id) {
        holidayRepository.deleteById(id);
    }
    @Override
    public List<Holiday> getUpcomingHolidays() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        LocalDate todayPlusOneMonth = today.plus(12, ChronoUnit.MONTHS);

        List<Holiday> upcoming = holidayRepository.findUpcomingHolidays(today).stream()
                .filter(holiday -> !holiday.getDate().isAfter(todayPlusOneMonth))
                .toList();
        System.out.println("Holidays fetched using custom query (>= today): " + upcoming);
        System.out.println("Upcoming holidays within the next month: " + upcoming);
        return upcoming;
    }
}