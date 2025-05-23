package me.panjohnny.baka4j.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.panjohnny.baka4j.BakaClient;
import me.panjohnny.baka4j.util.AuthException;
import me.panjohnny.baka4j.util.ReqParameters;
import okhttp3.*;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class BakaClientImpl implements BakaClient  {
    private String token;
    private String refreshToken;
    private final String url;

    private long expiresIn;

    private final OkHttpClient httpClient;

    public static MediaType FORM = MediaType.get("application/x-www-form-urlencoded; charset=utf-8");

    private final System.Logger logger = System.getLogger(this.getClass().getName());

    public BakaClientImpl(String url) {
        this(url, new OkHttpClient());
    }

    public BakaClientImpl(String url, OkHttpClient client) {
        this.httpClient = client;
        this.url = url;
    }

    @Override
    public void authorize(String username, String password) throws AuthException {
        Request request = post("/api/login", new ReqParameters("client_id=ANDR&grant_type=password").set("username", username).set("password", password)).build();
        try (Response response = httpClient.newCall(request).execute()) {
            extractToken(response);
        } catch (IOException e) {
            logger.log(System.Logger.Level.ERROR, "Failed to authorize client");
            throw new AuthException(e);
        }
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public String getRefreshToken() {
        return refreshToken;
    }

    @Override
    public void refresh() throws AuthException {
        Request request = post("/api/login", new ReqParameters("client_id=ANDR&grant_type=refresh_token").set("refresh_token", refreshToken)).build();
        try (Response response = httpClient.newCall(request).execute()) {
            extractToken(response);
        } catch (IOException e) {
            logger.log(System.Logger.Level.ERROR, "Failed to refresh access token");
            throw new AuthException(e);
        }
    }

    private void extractToken(Response response) throws IOException {
        if (response.code() != 200) {
            throw new IllegalArgumentException(Objects.requireNonNull(response.body()).string());
        }
        JsonObject json = JsonParser.parseString(Objects.requireNonNull(response.body()).string()).getAsJsonObject();
        refreshToken = json.get("refresh_token").getAsString();
        token = json.get("access_token").getAsString();
        expiresIn = System.currentTimeMillis() + json.get("expires_in").getAsLong() * 1000;
    }

    @Override
    public OkHttpClient getOkHttpClient() {
        return httpClient;
    }

    @Override
    public String getSchoolURL() {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    @Override
    public long getExpires() {
        return expiresIn;
    }

    protected Request.Builder get(String endpoint, ReqParameters parameters) {
        Request.Builder builder = new Request.Builder().header("Content-Type", "application/x-www-form-urlencoded").get();
        String u = getSchoolURL() + endpoint;
        if (parameters != null) {
            u += "?" + parameters;
        }

        if (token != null) {
            // add authorization header
            builder.header("Authorization", "Bearer " + token);
        }

        return builder.url(u);
    }

    @SuppressWarnings("SameParameterValue")
    protected Request.Builder post(String endpoint, ReqParameters parameters) {
        Request.Builder builder = new Request.Builder().header("Content-Type", "application/x-www-form-urlencoded");
        String u = getSchoolURL() + endpoint;

        if (token != null) {
            // add authorization header
            builder.header("Authorization", "Bearer " + token);
        }

        return builder.url(u).post(RequestBody.create(parameters.toString(), FORM));
    }



    protected Request.Builder get(String endpoint) {
        return get(endpoint, null);
    }

    @Override
    public void authorize(String token, String refreshToken, long expires) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expires;
    }

    private Timer timer;
    @Override
    public void enableRefreshJobs() {
        if (timer == null) {
            timer = new Timer("RefreshJobTimer");
            logger.log(System.Logger.Level.INFO, "Refresh jobs enabled");
            new RefreshJob(0).cancel();
        }
    }

    @Override
    public void disableRefreshJobs() {
        timer.purge();
        timer = null;
        logger.log(System.Logger.Level.INFO, "Refresh jobs disabled");
    }

    private class RefreshJob extends TimerTask {
        private final int i;

        private RefreshJob(int i) {
            this.i = i+1;
        }

        @Override
        public void run() {
            try {
                refresh();
                timer.schedule(new RefreshJob(i), new Date(getExpires() - 5000));
                logger.log(System.Logger.Level.INFO, "Refresh job {0} finished!", i);
            } catch (AuthException e) {
                logger.log(System.Logger.Level.ERROR, "Refresh job failed, stopping refresh jobs");
            }
        }
    }

    public JsonElement getJson(String endpoint, ReqParameters parameters) throws IOException {
        Request request = get(endpoint, parameters).build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.code() != 200) {
                throw new IllegalArgumentException(Objects.requireNonNull(response.body()).string());
            }
            return JsonParser.parseString(Objects.requireNonNull(response.body()).string());
        }
    }
}
