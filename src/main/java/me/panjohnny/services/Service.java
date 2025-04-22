package me.panjohnny.services;

import me.panjohnny.Configurator;
import kotlin.Pair;

public abstract class Service {
    protected Configurator config;
    public Service(Configurator config) {
        this.config = config;
    }

    public abstract void login() throws Exception;

    public abstract Pair<String[], String[]> getData() throws Exception;

    public boolean shouldRefresh() {
        return true;
    }
}
