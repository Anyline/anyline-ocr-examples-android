package io.anyline.examples.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

import static at.nineyards.anyline.reporter.ReportingService.PREFS_NAME;

/**
 * Simple {@link SharedPreferences} manager
 * <p/>
 * Manages the preferences (reporting), saves the "awarded points" for scanning,
 * checks if help has already been displayed
 */
public class Preferences {
    private static final String PREF_SCAN_COUNTER = "scan_counter";
    private static final String PREF_REPORTING_ENABLED = "reporting_enabled";
    private static final String SHARED_PREFS_ANYLINE = "AnylinePreferences";
    private static final String PREF_EMAIL = "user_email";
    private static final String PREFS_ONBOARDING_INCOMPLETE = "onboarding_complete";
    private static final String PREFS_ONBOARDING_INCOMPLETE_a = "onboarding_completee";
    private static final String PREF_UUID = "uuid";
    private static final String PREF_INTERCOM_USER_ID = "uuid_intercom_user_id";

    private static Preferences instance;

    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    private final SharedPreferences reportingPreferences;

    private Preferences(Context context) {
        preferences = context.getSharedPreferences(
                SHARED_PREFS_ANYLINE, Context.MODE_PRIVATE);

        reportingPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public static Preferences getInstance(Context context) {
        if (instance == null) {
            instance = new Preferences(context);
        }
        return instance;
    }

    public int getSharedPreferenceScanCounter() {
        return preferences.getInt(PREF_SCAN_COUNTER, 0);
    }

    public void setSharedPreferencesScanCounter(int count) {
        editor.putInt(PREF_SCAN_COUNTER, count);
        editor.commit();
    }

    public boolean isReportingEnabled() {
        return preferences.getBoolean(PREF_REPORTING_ENABLED, true);
    }

    public void setReportingEnabled(boolean enabled) {
        editor.putBoolean(PREF_REPORTING_ENABLED, enabled);
        editor.commit();
    }

    public boolean isModuleOpenendFirstTime(String module) {
        return preferences.getBoolean(module, true);
    }

    public void setModuleHasBeenExplained(String module) {
        editor.putBoolean(module, false);
        editor.commit();
    }

    public boolean hasEmailAddressStored() {
        if (!preferences.getString(PREF_EMAIL, "").isEmpty()) {
            return true;
        }
        return false;
    }

    public String getStoredEmailAddress() {
        return preferences.getString(PREF_EMAIL, "");
    }

    public void setStoredEmailAddress(String storedEmailAddress) {
        editor.putString(PREF_EMAIL, storedEmailAddress);
        editor.commit();
    }

    public boolean isOpeningForFirstTime() {
        return preferences.getBoolean(PREFS_ONBOARDING_INCOMPLETE_a, true);
    }

    public void setOnboardingCompleted() {
        editor.putBoolean(PREFS_ONBOARDING_INCOMPLETE_a, false);
        editor.commit();
    }


    public String getIntercomUserId() {
        return preferences.getString(PREF_INTERCOM_USER_ID, null);
    }

    public void setIntercomUserId(String user) {
        editor.putString(PREF_INTERCOM_USER_ID, user);
        editor.commit();
    }

    public String getUuid() {
        String uuidPrefs = reportingPreferences.getString(PREF_UUID, "");

        //first installation -> create new uuid
        if (uuidPrefs.isEmpty()) {
            uuidPrefs = UUID.randomUUID().toString();
            final SharedPreferences.Editor reportingEditor = reportingPreferences.edit();
            reportingEditor.putString(PREF_UUID, uuidPrefs);
            reportingEditor.commit();

        }
        return uuidPrefs;
    }

}
