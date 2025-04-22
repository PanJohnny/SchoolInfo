package me.panjohnny.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.panjohnny.Configurator;
import okhttp3.*;
import kotlin.Pair;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class StravaService extends Service {

    private static final String LOGIN_URL = "https://app.strava.cz/api/login";
    private static final String FOOD_URL = "https://app.strava.cz/api/objednavky";
    private String sid;
    private final OkHttpClient httpClient;
    private static final MediaType TEXT_PLAIN = MediaType.get("text/plain;charset=UTF-8");

    public StravaService(Configurator config) {
        super(config);
        this.httpClient = new OkHttpClient();
    }

    @Override
    public void login() throws Exception {
        JsonObject credentials = new JsonObject();
        credentials.addProperty("cislo", "0837");
        credentials.addProperty("jmeno", config.getStravaUsername());
        credentials.addProperty("heslo", config.getStravaPassword());
        credentials.addProperty("zustatPrihlasen", true);
        credentials.addProperty("environment", "W");
        credentials.addProperty("lang", "CZ");

        RequestBody body = RequestBody.create(credentials.toString(), TEXT_PLAIN);
        Request request = new Request.Builder()
                .url(LOGIN_URL)
                .post(body)
                .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:131.0) Gecko/20100101 Firefox/131.0")
                .header("Accept", "*/*")
                .header("Accept-Language", "cs,sk;q=0.8,en-US;q=0.5,en;q=0.3")
                .header("Content-Type", "application/plain;charset=UTF-8")
                .header("Origin", "https://app.strava.cz")
                .header("Referer", "https://app.strava.cz/prihlasit-se?jidelna")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            JsonObject jsonResponse = JsonParser.parseString(response.body().string()).getAsJsonObject();
            sid = jsonResponse.get("sid").getAsString();
        }
    }

    @Override
    public Pair<String[], String[]> getData() throws Exception {
        if (sid == null) {
            throw new IllegalStateException("Not logged in. Call login() first.");
        }

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("cislo", "0837");
        requestBody.addProperty("sid", sid);
        requestBody.addProperty("s5url", "https://wss52.strava.cz/WSStravne5/WSStravne5.svc");
        requestBody.addProperty("lang", "CZ");
        requestBody.addProperty("podminka", "");
        requestBody.addProperty("ignoreCert", "false");

        RequestBody body = RequestBody.create(requestBody.toString(), TEXT_PLAIN);
        Request request = new Request.Builder()
                .url(FOOD_URL)
                .post(body)
                .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:131.0) Gecko/20100101 Firefox/131.0")
                .header("Accept", "*/*")
                .header("Accept-Language", "cs,sk;q=0.8,en-US;q=0.5,en;q=0.3")
                .header("Content-Type", "text/plain;charset=UTF-8")
                .header("Origin", "https://app.strava.cz")
                .header("Referer", "https://app.strava.cz/prihlasit-se?jidelna")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            JsonObject jsonResponse = JsonParser.parseString(response.body().string()).getAsJsonObject();
            JsonArray foodData = jsonResponse.getAsJsonArray("table0");
            return processFoodData(foodData);
        }
    }

    private Pair<String[], String[]> processFoodData(JsonArray foodData) {
        Calendar today = Calendar.getInstance();
        // If it is after 16:10 go to the next day, or if it is a weekend, go to the next monday
        if (today.get(Calendar.HOUR_OF_DAY) > 16 || (today.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || today.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)) {
            int daysToAdd = (Calendar.SATURDAY - today.get(Calendar.DAY_OF_WEEK) + 2) % 7;
            if (daysToAdd == 0) daysToAdd = 7;
            today.add(Calendar.DAY_OF_MONTH, daysToAdd);
        }

        String todayDate = new SimpleDateFormat("dd.MM.yyyy").format(Date.from(today.toInstant()));

        String orderedFood = null;
        String soup = null;
        String extra = null;

        for (int i = 0; i < foodData.size(); i++) {
            JsonObject food = foodData.get(i).getAsJsonObject();
            String date = food.get("datum").getAsString();

            if (date.equals(todayDate)) {
                String foodType = food.get("druh_popis").getAsString();
                String foodName = formatFoodName(food.get("nazev").getAsString());

                if ("Polévka".equals(foodType)) {
                    soup = foodName;
                } else if ("Doplněk".equals(foodType)) {
                    extra = foodName;
                }

                if (food.get("pocet").getAsInt() != 0) {
                    orderedFood = foodName;
                }
            }
        }

        ArrayList<String> left = new ArrayList<>();
        ArrayList<String> right = new ArrayList<>();

        left.add("Polévka");
        right.add(soup != null ? soup : "Není k dispozici");

        left.add("Hlavní jídlo");
        right.add(orderedFood != null ? orderedFood : "Není objednáno");

        if (extra != null) {
            left.add("Doplněk");
            right.add(extra);
        }

        return new Pair<>(left.toArray(new String[0]), right.toArray(new String[0]));
    }

    private String formatFoodName(String foodName) {
        foodName = foodName.trim().replace(" ,", ",");
        if (foodName.contains("BZL")) {
            // trim the string to remove  BZL and stuff after it
            foodName = foodName.substring(0, foodName.indexOf("BZL")).trim();
        }
        return Character.toUpperCase(foodName.charAt(0)) + foodName.substring(1);
    }
}