package uz.result.moneymanagerbot.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
public class ValidationService {

    public boolean isValidDouble(String input) {
        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isValidDateRange(String dateRange) {
        if (dateRange == null || !dateRange.contains("/")) {
            return false;
        }

        String[] dates = dateRange.split("/");
        if (dates.length != 2) {
            return false;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try {
            LocalDate startDate = LocalDate.parse(dates[0], formatter);
            LocalDate endDate = LocalDate.parse(dates[1], formatter);

            return !startDate.isAfter(endDate);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public boolean isValidLong(String input) {
        try {
            Long.parseLong(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isValidDateTime(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        try {
            LocalDateTime.parse(input, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

}
