package io.anyline.examples.ocr;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import org.json.JSONObject;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import androidx.annotation.NonNull;


public class SerialNumberPreferences {
    private static final String TAG = SerialNumberPreferences.class.getSimpleName();


    /**********************************************************************************************************************
     P R E F E R E N C E S
     *********************************************************************************************************************/

    private static final String SHARED_PREFS_ANYLINE = "AnylinePreferences";

    private static final String PREFNAME_CUTOUT_RATIO_WIDTH = "cutoutRatioWidth";
    private static final String PREFNAME_CUTOUT_MAX_WIDTH = "cutoutWidth";
    private static final String PREFNAME_CUTOUT_CORNER_RADIUS = "cutoutCornerRadius";
    private static final String PREFNAME_CUTOUT_ALIGN = "cutoutAlign";
    private static final String PREFNAME_BASIC_LENGTH_FROM = "basicLengthFrom";
    private static final String PREFNAME_BASIC_LENGTH_TO = "basicLengthTo";
    private static final String PREFNAME_BASIC_TYPE = "basicType";
    private static final String PREFNAME_BASIC_EXCLUDE = "basicExclude";
    private static final String PREFNAME_ADVANCED_REGEX = "advancedRegex";
    private static final String PREFNAME_USE_BASIC_CHARACTERS = "useBasicCharacters";

    private static final int PREF_CUTOUT_RATIO_WIDTH_DEFAULT = 5;
    private static final int PREF_CUTOUT_MAX_WIDTH_DEFAULT = 80;
    private static final int PREF_CUTOUT_CORNER_RADIUS_DEFAULT = 5;
    private static final ScanAreaAlignment PREF_CUTOUT_ALIGN_DEFAULT = ScanAreaAlignment.CENTER;
    private static final int PREF_BASIC_LENGTH_FROM_DEFAULT = 5;
    private static final int PREF_BASIC_LENGTH_TO_DEFAULT = 20;
    private static final ScanType PREF_BASIC_TYPE_DEFAULT = ScanType.NUMBERS_LETTERS;
    private static final String PREF_BASIC_EXCLUDE_DEFAULT = "";
    private static final String PREF_ADVANCED_REGEX_DEFAULT = "[A-Z0-9]{4,}";
    private static final Boolean PREF_USE_BASIC_CHARACTERS_DEFAULT = true;

    private static final int CUTOUT_MAX_HEIGHT = 100;


    private static SerialNumberPreferences instance;

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;


    public enum ScanAreaAlignment {
        TOP_HALF(0),
        CENTER(1),
        BOTTOM_HALF(2);

        private int mValue;

        ScanAreaAlignment(int value) { // Constructor
            this.mValue = value;
        }

        public int id() {
            return mValue; // Return enum index
        }

        public static ScanAreaAlignment fromId(int value) {
            for (ScanAreaAlignment scanAreaAlignment : values()) {
                if (scanAreaAlignment.mValue == value) {
                    return scanAreaAlignment;
                }
            }
            return CENTER; // should never happen
        }
    }


    public enum ScanType {
        NUMBERS_LETTERS(0),
        NUMBERS(1),
        LETTERS(2);

        private int mValue;

        ScanType(int value) { // Constructor
            this.mValue = value;
        }

        public int id() {
            return mValue; // Return enum index
        }

        public static ScanType fromId(int value) {
            for (ScanType scanType : values()) {
                if (scanType.mValue == value) {
                    return scanType;
                }
            }
            return NUMBERS_LETTERS; // should never happen
        }
    }

    private String allowedChars;
    private String regex;


    @SuppressLint("CommitPrefEdits")
    private SerialNumberPreferences(Context context) {
        prefs = context.getSharedPreferences(SHARED_PREFS_ANYLINE, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }


    public static SerialNumberPreferences getInstance(Context context) {
        if (instance == null) {
            instance = new SerialNumberPreferences(context);
        }
        return instance;
    }


    protected int getPrefCutoutRatioWidth() {
        return prefs.getInt(PREFNAME_CUTOUT_RATIO_WIDTH, PREF_CUTOUT_RATIO_WIDTH_DEFAULT);
    }

    protected int getPrefCutoutMaxWidth() {
        return prefs.getInt(PREFNAME_CUTOUT_MAX_WIDTH, PREF_CUTOUT_MAX_WIDTH_DEFAULT);
    }

    protected int getPrefCutoutCornerRadius() {
        return prefs.getInt(PREFNAME_CUTOUT_CORNER_RADIUS, PREF_CUTOUT_CORNER_RADIUS_DEFAULT);
    }

    protected ScanAreaAlignment getPrefCutoutAlign() {
        return ScanAreaAlignment.fromId(prefs.getInt(PREFNAME_CUTOUT_ALIGN, PREF_CUTOUT_ALIGN_DEFAULT.id()));
    }

    protected int getPrefBasicLengthFrom() {
        return prefs.getInt(PREFNAME_BASIC_LENGTH_FROM, PREF_BASIC_LENGTH_FROM_DEFAULT);
    }

    protected int getPrefBasicLengthTo() {
        return prefs.getInt(PREFNAME_BASIC_LENGTH_TO, PREF_BASIC_LENGTH_TO_DEFAULT);
    }

    protected ScanType getPrefBasicType() {
        return ScanType.fromId(prefs.getInt(PREFNAME_BASIC_TYPE, PREF_BASIC_TYPE_DEFAULT.id()));
    }

    protected String getPrefBasicExclude() {
        return prefs.getString(PREFNAME_BASIC_EXCLUDE, PREF_BASIC_EXCLUDE_DEFAULT);
    }

    protected String getPrefAdvancedRegex() {
        return prefs.getString(PREFNAME_ADVANCED_REGEX, PREF_ADVANCED_REGEX_DEFAULT);
    }

    protected Boolean getPrefUseBasicCharacters() {
        return prefs.getBoolean(PREFNAME_USE_BASIC_CHARACTERS, PREF_USE_BASIC_CHARACTERS_DEFAULT);
    }


    protected void putDefaultPrefCutoutRatioWidth() {
        editor.putInt(PREFNAME_CUTOUT_RATIO_WIDTH, PREF_CUTOUT_RATIO_WIDTH_DEFAULT);
        editor.apply();
    }

    protected void putDefaultPrefCutoutMaxWidth() {
        editor.putInt(PREFNAME_CUTOUT_MAX_WIDTH, PREF_CUTOUT_MAX_WIDTH_DEFAULT);
        editor.apply();
    }

    protected void putDefaultPrefCutoutCornerRadius() {
        editor.putInt(PREFNAME_CUTOUT_CORNER_RADIUS, PREF_CUTOUT_CORNER_RADIUS_DEFAULT);
        editor.apply();
    }

    protected void putDefaultPrefCutoutAlign() {
        editor.putInt(PREFNAME_CUTOUT_ALIGN, PREF_CUTOUT_ALIGN_DEFAULT.id());
        editor.apply();
    }

    protected void putDefaultPrefBasicLengthFrom() {
        editor.putInt(PREFNAME_BASIC_LENGTH_FROM, PREF_BASIC_LENGTH_FROM_DEFAULT);
        editor.apply();
    }

    protected void putDefaultPrefBasicLengthTo() {
        editor.putInt(PREFNAME_BASIC_LENGTH_TO, PREF_BASIC_LENGTH_TO_DEFAULT);
        editor.apply();
    }

    protected void putDefaultPrefBasicType() {
        editor.putInt(PREFNAME_BASIC_TYPE, PREF_BASIC_TYPE_DEFAULT.id());
        editor.apply();
    }

    protected void putDefaultPrefBasicExclude() {
        editor.putString(PREFNAME_BASIC_EXCLUDE, PREF_BASIC_EXCLUDE_DEFAULT);
        editor.apply();
    }

    protected void putDefaultPrefAdvancedRegex() {
        editor.putString(PREFNAME_ADVANCED_REGEX, PREF_ADVANCED_REGEX_DEFAULT);
        editor.apply();
    }

    protected void putDefaultPrefUseBasicCharacters() {
        editor.putBoolean(PREFNAME_USE_BASIC_CHARACTERS, PREF_USE_BASIC_CHARACTERS_DEFAULT);
        editor.apply();
    }


    protected void putPrefCutoutRatioWidth(int value) {
        editor.putInt(PREFNAME_CUTOUT_RATIO_WIDTH, value);
        editor.apply();
    }

    protected void putPrefCutoutMaxWidth(int value) {
        editor.putInt(PREFNAME_CUTOUT_MAX_WIDTH, value);
        editor.apply();
    }

    protected void putPrefCutoutCornerRadius(int value) {
        editor.putInt(PREFNAME_CUTOUT_CORNER_RADIUS, value);
        editor.apply();
    }

    protected void putPrefCutoutAlign(ScanAreaAlignment value) {
        editor.putInt(PREFNAME_CUTOUT_ALIGN, value.id());
        editor.apply();
    }

    protected void putPrefBasicLengthFrom(int value) {
        editor.putInt(PREFNAME_BASIC_LENGTH_FROM, value);
        editor.apply();
    }

    protected void putPrefBasicLengthTo(int value) {
        editor.putInt(PREFNAME_BASIC_LENGTH_TO, value);
        editor.apply();
    }

    protected void putPrefBasicType(ScanType value) {
        editor.putInt(PREFNAME_BASIC_TYPE, value.id());
        editor.apply();
    }

    protected void putPrefBasicExclude(String value) {
        editor.putString(PREFNAME_BASIC_EXCLUDE, value);
        editor.apply();
    }

    protected void putPrefAdvancedRegex(String value) {
        editor.putString(PREFNAME_ADVANCED_REGEX, value);
        editor.apply();
    }

    protected void putPrefUseBasicCharacters(Boolean value) {
        editor.putBoolean(PREFNAME_USE_BASIC_CHARACTERS, value);
        editor.apply();
    }


    protected void putDefaultPrefsScanArea() {
        putDefaultPrefCutoutRatioWidth();
        putDefaultPrefCutoutMaxWidth();
        putDefaultPrefCutoutCornerRadius();
        putDefaultPrefCutoutAlign();
    }


    protected void putDefaultPrefsBasic() {
        putDefaultPrefBasicLengthFrom();
        putDefaultPrefBasicLengthTo();
        putDefaultPrefBasicType();
        putDefaultPrefBasicExclude();
        putDefaultPrefUseBasicCharacters();
    }


    protected void putDefaultPrefsAdvanced() {
        putDefaultPrefAdvancedRegex();
    }


    private void initRegexAllowedChars() {

        int prefBasicLengthFrom = getPrefBasicLengthFrom();
        int prefBasicLengthTo = getPrefBasicLengthTo();
        SerialNumberPreferences.ScanType prefBasicType = getPrefBasicType();

        String prefBasicExclude = getPrefBasicExclude();
        String prefAdvancedRegex = getPrefAdvancedRegex();
        Boolean prefUseBasicCharacters = getPrefUseBasicCharacters();

        if (prefAdvancedRegex.length() > 0) {
            try {
                Pattern.compile(prefAdvancedRegex);
            } catch (PatternSyntaxException e) {
                prefUseBasicCharacters = true;
            }
        }

        if (prefUseBasicCharacters) {
            if (prefBasicType == SerialNumberPreferences.ScanType.NUMBERS_LETTERS) {
                allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
                regex = "[A-Z0-9]{" + prefBasicLengthFrom + "," + prefBasicLengthTo + "}";
            } else if (prefBasicType == SerialNumberPreferences.ScanType.NUMBERS) {
                allowedChars = "0123456789";
                regex = "[0-9]{" + prefBasicLengthFrom + "," + prefBasicLengthTo + "}";
            } else { // letters only
                allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
                regex = "[A-Z]{" + prefBasicLengthFrom + "," + prefBasicLengthTo + "}";
            }
            // exclude characters from allowed characters:
            for (int i = 0; i < prefBasicExclude.length(); i++) {
                String s = prefBasicExclude.substring(i, i+1);
                allowedChars = allowedChars.replaceAll(s, "");
            }
        } else {
            regex = prefAdvancedRegex;
            allowedChars = "";
        }
    }


    protected String getRegex() {
        initRegexAllowedChars();
        return this.regex;
    }


    protected String getAllowedChars() {
        initRegexAllowedChars();
        return this.allowedChars;
    }


    @NonNull
    public static Point getDisplayDimensions(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = null;
        if (wm != null) {
            display = wm.getDefaultDisplay();
        }

        DisplayMetrics metrics = new DisplayMetrics();
        if (display != null) {
            display.getMetrics(metrics);
        }
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        // find out if status bar has already been subtracted from screenHeight
        if (display != null) {
            display.getRealMetrics(metrics);
        }
        int physicalHeight = metrics.heightPixels;
        int statusBarHeight = getStatusBarHeight(context);
        int navigationBarHeight = getNavigationBarHeight(context);
        int heightDelta = physicalHeight - screenHeight;
        if (heightDelta == 0 || heightDelta == navigationBarHeight) {
            screenHeight -= statusBarHeight;
        }

        Log.i (TAG, "height, width getDimens: " + screenHeight + " " + screenWidth);

        return new Point(screenWidth, screenHeight);
    }

    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return (resourceId > 0) ? resources.getDimensionPixelSize(resourceId) : 0;
    }

    public static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        return (resourceId > 0) ? resources.getDimensionPixelSize(resourceId) : 0;
    }

    public static float convertPixelsToDp(float px, Context context) {
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }


    protected JSONObject updateJsonFromSettings(Context context, JSONObject jo) {
        int prefCutoutRatioWidth = getPrefCutoutRatioWidth();
        int prefCutoutMaxWidth = getPrefCutoutMaxWidth();
        int prefCutoutCornerRadius = getPrefCutoutCornerRadius();
        SerialNumberPreferences.ScanAreaAlignment prefCutoutAlign = getPrefCutoutAlign();

        // assumptions for setting the corner radius:
        // scan area fills the whole display, except status bar and navigation bar

        // get the width and the height of the scan area:
        Point dimensions = getDisplayDimensions(context);
        int scanViewWidthPx = dimensions.x;
        int scanViewHeightPx = dimensions.y;

        double maxScanAreaWidthPx = scanViewWidthPx * prefCutoutMaxWidth * 0.01;
        double maxScanAreaHeightPx = scanViewHeightPx * CUTOUT_MAX_HEIGHT * 0.01;
        double maxCutoutRatio = maxScanAreaWidthPx / maxScanAreaHeightPx;

        double cutoutHeightPx;
        if (maxCutoutRatio >= prefCutoutRatioWidth) { // preferred cutout is higher than max. possible cutout
            cutoutHeightPx = maxScanAreaHeightPx;
        } else { // max possible cutout is higher than preferred cutout
            cutoutHeightPx = maxScanAreaWidthPx / prefCutoutRatioWidth;
        }

        double cornerRadiusPx = cutoutHeightPx * prefCutoutCornerRadius / 200; // = / 2 (=half of the height) / 100 (=percentage)
        float cornerRadiusDp = convertPixelsToDp ((float)cornerRadiusPx, context);

        try {
            JSONObject viewPluginJO = jo.getJSONObject("viewPlugin");
            JSONObject cutoutConfigJO = viewPluginJO.getJSONObject("cutoutConfig");
            JSONObject ratioFromSizeJO = cutoutConfigJO.getJSONObject("ratioFromSize");

            if (prefCutoutAlign == SerialNumberPreferences.ScanAreaAlignment.TOP_HALF) {
                cutoutConfigJO.put("alignment", "top_half");
            } else if (prefCutoutAlign == SerialNumberPreferences.ScanAreaAlignment.CENTER) {
                cutoutConfigJO.put("alignment", "center");
            } else if (prefCutoutAlign == SerialNumberPreferences.ScanAreaAlignment.BOTTOM_HALF) {
                cutoutConfigJO.put("alignment", "bottom_half");
            }
            cutoutConfigJO.put("maxWidthPercent", prefCutoutMaxWidth + "%");
            cutoutConfigJO.put("maxHeightPercent", CUTOUT_MAX_HEIGHT + "%");
            cutoutConfigJO.put("cornerRadius", (int)cornerRadiusDp);

            ratioFromSizeJO.put("width", prefCutoutRatioWidth);
            ratioFromSizeJO.put("height", 1);

        } catch (Exception e) {
            Log.i(TAG, "Exception Json: " + e);
        }
        return jo;
    }

}
