package com.getspreebie.mohau.travelmantics;

import android.graphics.drawable.Drawable;

public class Settings {
    private Drawable drawable; // Used to store a Deal's Image when passing from DealListActivity to DealActivity
    private Boolean adminLevelAccess = true; // By default, set the app to have admin level access privileges
    private static Settings instance;

    private Settings() {}

    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }


    public Boolean getAdminLevelAccess() {
        return adminLevelAccess;
    }

    public void setAdminLevelAccess(Boolean adminLevelAccess) {
        this.adminLevelAccess = adminLevelAccess;
    }
}
