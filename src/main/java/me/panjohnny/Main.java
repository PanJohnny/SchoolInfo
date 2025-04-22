package me.panjohnny;

import me.panjohnny.services.BakalariService;
import me.panjohnny.services.StravaService;

public class Main {
    private static Window w;
    public static void main(String[] args) {
        Configurator configurator = new Configurator();
        w = new Window(configurator);

        BakalariService bakalariService = new BakalariService(configurator);
        try {
            bakalariService.login();
            var data = bakalariService.getData();
            w.displayData(data.component1(), data.component2());
        } catch (Exception e) {
            System.out.println("Failed to login to Bakalari: " + e.getMessage());
            throw new RuntimeException(e);
        }

        StravaService stravaService = new StravaService(configurator);
        try {
            stravaService.login();
            var data = stravaService.getData();
            w.appendData("###", "###");
            for (int i = 0; i < data.component1().length; i++) {
                String l = data.component1()[i];
                String r = null;
                if (i < data.component2().length) {
                    r = data.component2()[i];
                }

                w.appendData(l, r);

            }
        } catch (Exception e) {
            System.out.println("Failed to login to Strava: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}