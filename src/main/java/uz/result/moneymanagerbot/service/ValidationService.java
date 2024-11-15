package uz.result.moneymanagerbot.service;

import org.springframework.stereotype.Service;

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

}
