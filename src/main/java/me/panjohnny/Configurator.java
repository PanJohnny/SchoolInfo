package me.panjohnny;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;

public class Configurator {
    public static final String CONFIG_FILE = "config.properties";
    private final Properties properties;

    public Configurator() {
        properties = new Properties();
        try {
            properties.load(new FileReader(CONFIG_FILE));
        } catch (Exception e) {
            System.err.println("Error loading configuration file: " + e.getMessage());
            System.out.println("Creating default configuration file...");
            setDefaults();
            store();
            System.out.println("Fill in the configuration file and run the program again.");
            System.exit(0);
        }
    }

    public void store() {
        try {
            properties.store(new FileWriter(CONFIG_FILE), "Configuration");
        } catch (Exception e) {
            System.err.println("Error storing configuration file: " + e.getMessage());
            System.err.println("This is a critical error, please check your file permissions.");
            System.err.println("The program will now exit.");
            throw new RuntimeException("Failed to store configuration file", e);
        }
    }

    public String getBakalariUsername() {
        return properties.getProperty("bakalari.username");
    }

    public String getBakalariPassword() {
        return properties.getProperty("bakalari.password");
    }

    public String getBakalariUrl() {
        return properties.getProperty("bakalari.url");
    }

    public String getStravaUsername() {
        return properties.getProperty("strava.username");
    }

    public String getStravaPassword() {
        return properties.getProperty("strava.password");
    }

    public void setDefaults() {
        properties.setProperty("bakalari.username", "bakaláři uživatel");
        properties.setProperty("bakalari.password", "heslo");
        properties.setProperty("bakalari.url", "https://gymlit.bakalari.cz/bakaweb");
        properties.setProperty("strava.username", "uživatel strava");
        properties.setProperty("strava.password", "heslo");
    }
}
