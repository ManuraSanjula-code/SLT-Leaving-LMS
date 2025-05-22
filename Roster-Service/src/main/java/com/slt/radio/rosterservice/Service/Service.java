package com.slt.radio.rosterservice.Service;

import com.slt.radio.rosterservice.Repo.RosterRepository;
import com.slt.radio.rosterservice.Repo.ShiftRosterRepository;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Service
public class Service {
    @Autowired
    private RosterRepository rosterRepository;
    @Autowired
    private ShiftRosterRepository shiftRosterRepository;

    public static String getMonthName(int month) {
        String[] monthNames = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };

        // Adjust for 0-based array index
        return monthNames[month - 1];
    }

    public void delete(String date, boolean swap) {
        String[] dateParts = date.split("-");

        // Convert each part to integer
        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]);
        int day = Integer.parseInt(dateParts[2]);

        if(swap){
            rosterRepository.deleteByYearAndMonth(year, month);
        }else{
            System.out.println("Deleting shift roster for date: " + year + " -------- "+ getMonthName(month));
            System.out.println(shiftRosterRepository.findByYearAndMonth(year, getMonthName(month)).size());
            shiftRosterRepository.deleteByYearAndMonth(year, getMonthName(month));
        }

        System.out.println("Deleting roster for date: " + date);
    }
}
