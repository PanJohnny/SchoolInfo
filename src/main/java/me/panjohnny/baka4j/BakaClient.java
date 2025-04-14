package me.panjohnny.baka4j;

import me.panjohnny.baka4j.impl.BakaClientImpl;
import me.panjohnny.baka4j.util.AuthException;
import okhttp3.OkHttpClient;

public interface BakaClient  {
    static BakaClientImpl getInstance(String url) {
        return new BakaClientImpl(url);
    }

    /**
     * Authorises the client
     */
    void authorize(String username, String password) throws AuthException;

    /**
     * @return token that the client holds if authorised
     */
    String getToken();
    /**
     * @return refresh token that the client holds if authorised
     */
    String getRefreshToken();

    /**
     * Refreshes the token using refresh token
     */
    void refresh() throws AuthException;

    /**
     * @return okhttp client that this object uses
     */
    OkHttpClient getOkHttpClient();

    /**
     * @return Specified school url
     */
    String getSchoolURL();

    /**
     * @return time in millis (System.currentTimeMillis()) that the access token expires
     */
    long getExpires();

    /**
     * Authorize client with tokens and expires time
     * @param token token
     * @param refreshToken refresh token
     * @param expires time when the token expires (System.currentTimeMillis())
     */
    void authorize(String token, String refreshToken, long expires);

    /**
     * Enables jobs that refresh token in new thread periodically
     */
    void enableRefreshJobs();

    /**
     * Disables jobs that refresh token in new thread periodically
     */
    void disableRefreshJobs();
}
