package com.hf.webflux.hfai.utils;

import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Util {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    public static String getNowDateFormat() {
        return getNowDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public static String getNowDateFormat(String pattern) {
        return LocalDateTime.now().atZone(ZONE_ID).format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String getPrice(String fsym, String tsyms) {
        String urlTemplate = "https://min-api.cryptocompare.com/data/price?fsym={fsym}&tsyms={tsyms}";
        String urlString= urlTemplate.replace("{fsym}", fsym).replace("{tsyms}", tsyms);
        return fetchDataFromUrl(urlString);
    }
    public static String fetchDataFromUrl(String urlString) {
        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            } else {
                System.out.println("Failed to fetch data. Response code: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response.toString();
    }

}
