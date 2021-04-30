package io.anyline.examples.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import static io.anyline.reporter.ReportingService.PREFS_NAME;

/**
 * Simple {@link SharedPreferences} manager
 * <p/>
 * Manages the preferences (reporting), saves the "awarded points" for scanning,
 * checks if help has already been displayed
 */
public class Preferences {
    private static final String PREF_SCAN_COUNTER = "scan_counter";
    private static final String PREF_UUID = "uuid";
    private static final String SHARED_PREFS_ANYLINE = "AnylinePreferences";
    private static final String PREF_EMAIL = "user_email";
    private static final String PREFS_FIRST_START = "first_start";
    private static final String SHARED_PREFS_EMAIL_FACE_AUTHENTICATION = "email_face_authentication_collected";

    public static final String PREFS_PROJECT_NAME = "projectName";
    public static final String PREFS_API_KEY = "apiKey";
    public static final String PREFS_STAGE = "staging";
    public static final String SHARED_PREFS_ANYLINE_EXAMPLES = "prefs_examples_app";
    public static final String PREFS_LOGIN_STATUS = "is_logged_in";
    public static final String PREFS_PROJECT_TITLE = "projectTitle";
    public static final String PREFS_CUTOUT_CONFIG = "cutoutConfig";
    public static final String PREFS_CUSTOMER_ID = "customerId";
    public static final String PREFS_API_URL = "apiUrl";
    public static final String PREFS_ASSET_ID = "assetId";
    public static final String PREFS_ANYLINE_VERSION = "anylineVersion";


    public static final String PREFS_LOCK_UPDATE_CHECK_UNTIL = "lockUpdateCheckUntil";

    private static final int LOCK_DAYS = 2;
    private static Preferences instance;

    private final SharedPreferences preferences;
    private final SharedPreferences reportingPreferences;

    private Preferences(Context context) {
        preferences = context.getSharedPreferences(
                SHARED_PREFS_ANYLINE, Context.MODE_PRIVATE);

        reportingPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
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
        preferences.edit()
                .putInt(PREF_SCAN_COUNTER, count)
                .apply();
    }

    public boolean isLockedUpdate() {
        String lockedUntil = preferences.getString(PREFS_LOCK_UPDATE_CHECK_UNTIL, "");
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String getCurrentDateTime = sdf.format(c.getTime());
        return getCurrentDateTime.compareTo(lockedUntil) <= 0;
    }

    public void setLockedUpdate() {
        Calendar c = Calendar.getInstance();
        // ask user after xx days to update app
        c.add(Calendar.DATE, LOCK_DAYS);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String getLockedUntilDateTime = sdf.format(c.getTime());

        preferences.edit()
                .putString(PREFS_LOCK_UPDATE_CHECK_UNTIL, getLockedUntilDateTime)
                .apply();
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
        preferences.edit()
                .putString(PREF_EMAIL, storedEmailAddress)
                .apply();
    }

    public boolean isFirstStart() {
        return preferences.getBoolean(PREFS_FIRST_START, true);
    }


    public void setFirstStartFinished() {
        preferences.edit()
                .putBoolean(PREFS_FIRST_START, false)
                .apply();
    }

    public void setEmailForFaceAuthenticationCollected() {
        preferences.edit()
                .putBoolean(SHARED_PREFS_EMAIL_FACE_AUTHENTICATION, true)
                .apply();
    }

    public boolean wasEmailForFaceAuthenticationCollected() {
        return preferences.contains(SHARED_PREFS_EMAIL_FACE_AUTHENTICATION);
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
