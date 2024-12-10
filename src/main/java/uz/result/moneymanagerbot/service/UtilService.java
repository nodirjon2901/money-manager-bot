package uz.result.moneymanagerbot.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class UtilService {

    public String getCurrentExchangeRate() {
        try {
            String url = "https://cbu.uz/oz/arkhiv-kursov-valyut/json/";
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONArray jsonResponse = new JSONArray(response.toString());
            for (int i = 0; i < jsonResponse.length(); i++) {
                JSONObject currency = jsonResponse.getJSONObject(i);
                if (currency.getString("Ccy").equals("USD")) {
                    double exchangeRate = currency.getDouble("Rate");
                    return String.format("Нал валюта (1 USD = %.2f UZS)", exchangeRate);
                }
            }

            return "Нал валюта (курс topilmadi)";
        } catch (Exception e) {
            e.printStackTrace();
            return "Нал валюта (ошибка получения курса)";
        }
    }

    public Double getCurrentExchangeRateSumma() {
        try {
            String url = "https://cbu.uz/oz/arkhiv-kursov-valyut/json/";
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONArray jsonResponse = new JSONArray(response.toString());
            for (int i = 0; i < jsonResponse.length(); i++) {
                JSONObject currency = jsonResponse.getJSONObject(i);
                if (currency.getString("Ccy").equals("USD")) {
                    return currency.getDouble("Rate");
                }
            }

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
