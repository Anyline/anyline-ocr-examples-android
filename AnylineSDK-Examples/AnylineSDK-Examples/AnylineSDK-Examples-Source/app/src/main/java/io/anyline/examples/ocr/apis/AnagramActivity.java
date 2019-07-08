package io.anyline.examples.ocr.apis;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import at.nineyards.anyline.modules.AnylineBaseModuleView;
import io.anyline.examples.ScanningConfigurationActivity;
import io.anyline.examples.R;
import io.anyline.examples.ScanModuleEnum;

public class AnagramActivity extends ScanningConfigurationActivity implements RequestListener {

    public final String ANAGRAMICA_API = "http://www.anagramica.com/all/";

    public static final String SCRABBLE_INPUT = "SCRABBLE_INPUT";
    private static final String WORD_SUGGESTIONS = "WORD_SUGGESTIONS";

    private TextView textError;
    private LinearLayout layout;
    private ArrayList<String> results = null;
    private String input;
    private LinearLayout scrabbleTiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_no_menu);
        getLayoutInflater().inflate(R.layout.activity_scrabble_anagram, (ViewGroup) findViewById(R.id.placeholder));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textError = (TextView) findViewById(R.id.error_msg);
        layout = (LinearLayout) findViewById(R.id.content_layout);
        scrabbleTiles = (LinearLayout) findViewById(R.id.original_tiles);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                input = extras.getString(SCRABBLE_INPUT, "").trim();

            }
        } else {
            input = savedInstanceState.getString(SCRABBLE_INPUT);
            results = savedInstanceState.getStringArrayList(WORD_SUGGESTIONS);
        }

        setupScanResult();
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(SCRABBLE_INPUT, input);
        savedInstanceState.putStringArrayList(WORD_SUGGESTIONS, results);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (results != null) {
            layout.removeAllViews();
            scrabbleTiles.removeAllViews();
            showScrabbleAnagramResults();
        } else if (input != null && !input.isEmpty()) {
            new RequestTask(ANAGRAMICA_API + input, this).
                    executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        //show the user's scrabble tiles (= the input)
        if (input != null) {
            for (int j = 0; j < input.length(); j++) {
                char c = input.charAt(j);
                addLetterToLayout(scrabbleTiles, c);
            }
        }
    }


    @Override
    public void onResult(JSONObject json) {
        if (isFinishing()) {
            return;
        }

        if (json == null) {
            textError.setVisibility(View.VISIBLE);
            return;
        }

        textError.setVisibility(View.GONE);
        try {
            results = new ArrayList<>();
            JSONArray array = json.getJSONArray("all");

            for (int i = 0; i < array.length(); i++) {
                String s = array.getString(i);
                if (s.length() > 1) {
                    results.add(s);
                }
            }
            //sort: because we will display the words in descending order
            // with regard to the scrabble-points
            Collections.sort(results, new Comparator<String>() {
                @Override
                public int compare(String lhs, String rhs) {
                    if (calcScrabblePoints(lhs) > calcScrabblePoints(rhs)) {
                        return -1;
                    }
                    if (calcScrabblePoints(lhs) < calcScrabblePoints(rhs)) {
                        return 1;
                    }
                    return 0;
                }
            });

            showScrabbleAnagramResults();
        } catch (JSONException e) {
            textError.setVisibility(View.VISIBLE);
        }
    }

    private void showScrabbleAnagramResults() {
        if (results == null || results.size() == 0) {
            textError.setVisibility(View.VISIBLE);
            return;
        }
        textError.setVisibility(View.GONE);
        layout.removeAllViews();
        //only show top 10 results
        int count = results.size() > 10 ? 10 : results.size();


        int points = 0;
        for (int i = 0; i < count; i++) {
            int calcPoints = calcScrabblePoints(results.get(i));
            if (points != calcPoints) {
                points = calcPoints;
                addPointHeaderToLayout(points);
            }

            LinearLayout tmp = createWordResultLayout();
            for (int j = 0; j < results.get(i).length(); j++) {
                char c = results.get(i).charAt(j);
                addLetterToLayout(tmp, c);
            }
        }
    }

    private int calcScrabbleValueForCharacter(char c) {
        c = Character.toUpperCase(c);

        //https://en.wikipedia.org/wiki/Scrabble_letter_distributions#English
        if (c == 'E' || c == 'A' || c == 'I' || c == 'O' || c == 'N' || c == 'R' || c == 'T'
                || c == 'L' || c == 'S' || c == 'U') {
            return 1;
        }
        if (c == 'D' || c == 'G') {
            return 2;
        }
        if (c == 'B' || c == 'C' || c == 'M' || c == 'P') {
            return 3;
        }
        if (c == 'F' || c == 'H' || c == 'V' || c == 'W' || c == 'Y') {
            return 4;
        }
        if (c == 'K') {
            return 5;
        }
        if (c == 'J' || c == 'X') {
            return 8;
        }
        if (c == 'Q' || c == 'Z') {
            return 10;
        }
        //should not happen
        return 0;
    }


    private int calcScrabblePoints(String string) {
        int points = 0;
        for (int i = 0; i < string.length(); i++) {
            points += calcScrabbleValueForCharacter(string.charAt(i));
        }
        return points;
    }

    private void addPointHeaderToLayout(int points) {
        LinearLayout root = (LinearLayout) getLayoutInflater().
                inflate(R.layout.scrabble_points_header, null);
        ((TextView) root.findViewById(R.id.points_header)).setText(
                getResources().getString(R.string.scrabble_points, points));
        layout.addView(root);
    }

    private LinearLayout createWordResultLayout() {
        LinearLayout tmp = new LinearLayout(this);
        tmp.setOrientation(LinearLayout.HORIZONTAL);
        tmp.setLayoutParams(new LinearLayout.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.addView(tmp);
        return tmp;
    }

    private void addLetterToLayout(LinearLayout parent, char c) {

        View scrabbleComponent = getLayoutInflater().
                inflate(R.layout.scrabble_component, parent, false);
        ((TextView) scrabbleComponent.findViewById(R.id.scrabble_letter)).
                setText(Character.toString(c).toUpperCase());

        ((TextView) scrabbleComponent.findViewById(R.id.scrabble_point)).
                setText("" + calcScrabbleValueForCharacter(c));

        parent.addView(scrabbleComponent);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.activity_close_translate);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return false;
    }

    public void goBack(View view) {
        this.onBackPressed();
    }

    @Override
    protected AnylineBaseModuleView getScanView() {
        return null;
    }

    @Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return null;
    }
}
