
package moccacino.raspbiathome;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.app.Activity;
import android.view.View;

public class MainActivity extends AppCompatActivity implements Observer {

    private static final int RESULT_SETTINGS = 1;

    EditText etResponse;
    TextView tvIsConnected;
    Spinner spinner;

    MenuItem menuItem;

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    List<List<String>> listDataHeaderSummary;
    HashMap<String, List<String>> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean debugSwitch = sharedPref.getBoolean("pref_debug_switch", false);

        SharedPreferences.OnSharedPreferenceChangeListener listener =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                        // listener implementation
                        Boolean debugSwitch = prefs.getBoolean("pref_debug_switch", false);
                        etResponse.setVisibility(debugSwitch ? View.VISIBLE : View.INVISIBLE);
                    }
                };
        sharedPref.registerOnSharedPreferenceChangeListener(listener);

        Toolbar appToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(appToolbar);


        // get reference to the views
        etResponse = (EditText) findViewById(R.id.etResponse);
        etResponse.setVisibility(debugSwitch ? View.VISIBLE : View.GONE);

        tvIsConnected = (TextView) findViewById(R.id.tvIsConnected);
        spinner = (Spinner) findViewById(R.id.spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.ips_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);


        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.lvExp);
        expListView.setVisibility(debugSwitch ? View.GONE : View.VISIBLE);
        // preparing list data
        listDataHeader = Arrays.asList(getResources().getStringArray(R.array.categories));
        listDataHeaderSummary = new ArrayList<List<String>>();
        for (int i = 0; i <= listDataHeader.size() - 1; i++) {
            listDataHeaderSummary.add(0, new ArrayList<String>());
            listDataHeaderSummary.get(0).add("<no data>");
        }

        listDataChild = new HashMap<String, List<String>>();
        initChildLists();

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataHeaderSummary, listDataChild);
        // setting list adapter
        expListView.setAdapter(listAdapter);
    }

    @Override
    public void onPause() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        for (int i = 0; i <= listDataHeaderSummary.size() - 1; i++) {
            editor.putString("SUMMARY_" + String.valueOf(i), listAdapter.getHeaderSummary(i));
            editor.commit();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        for (int i = 0; i <= listDataHeaderSummary.size() - 1; i++) {
            String savedState = sharedPref.getString("SUMMARY_" + String.valueOf(i), "");
            List tempHeaderSummary = new ArrayList<String>();
            tempHeaderSummary.add(savedState);
            listAdapter.setHeaderSummary(i, tempHeaderSummary, false);
        }
    }

    public void perform_updates() {
        // check if you are connected or not
        if (isConnected()) {
            tvIsConnected.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
            tvIsConnected.setText("You are connected");
            etResponse.setText("");
        } else {
            tvIsConnected.setText("You are NOT connected");
        }

        initChildLists();

        // call AsynTask to perform network operation on separate thread
        String baseUrl = "http://" + spinner.getSelectedItem().toString() + "/";

        //clear the summary before performing REST calls
        for (int i = 0; i <= listDataHeader.size() - 1; i++) {
            listAdapter.setHeaderSummary(i, new ArrayList<String>(), false);
        }
        new HttpAsyncTask(MainActivity.this, "get_out_temp").execute(baseUrl);
        new HttpAsyncTask(MainActivity.this, "get_in_temp").execute(baseUrl);
        new HttpAsyncTask(MainActivity.this, "get_humidity").execute(baseUrl);
        new HttpAsyncTask(MainActivity.this, "get_pressure").execute(baseUrl);
        new HttpAsyncTask(MainActivity.this, "get_windows").execute(baseUrl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_items, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(intent, RESULT_SETTINGS);
                break;
            case R.id.action_refresh:
                menuItem = item;
                menuItem.setActionView(R.layout.progressbar);
                menuItem.expandActionView();
                perform_updates();
                break;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SETTINGS:
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                etResponse.setVisibility(sharedPref.getBoolean("pref_debug_switch", false) ? View.VISIBLE : View.GONE);
                expListView.setVisibility(sharedPref.getBoolean("pref_debug_switch", false) ? View.GONE : View.VISIBLE);
                break;
        }
    }


    public void initChildLists() {

        for (int i = 0; i <= listDataHeader.size() - 1; i++) {
            List<String> emptyList = new ArrayList<String>();
            listDataChild.put(listDataHeader.get(i), emptyList);
        }
    }

    public boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }


    public void receiveUpdates(String title, HashMap<String, String> updates, Boolean has_error) {
        //populate lists based
        List<String> listToAppend = null;
        List<String> summaryToUpdate = new ArrayList<String>();
        int indexListToUpdate = -1;
        switch (title) {
            case "get_out_temp":
                indexListToUpdate = 0;
                break;
            case "get_in_temp":
                indexListToUpdate = 0;
                break;
            case "get_humidity":
                indexListToUpdate = 1;
                break;
            case "get_pressure":
                indexListToUpdate = 2;
                break;
            case "get_windows":
                indexListToUpdate = 3;
                break;
            default:
                etResponse.append("\nERROR: unable to handle rest call: " + title + "\n");
                etResponse.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                return;
        }

        //print debug information
        String toPrintString = "";

        for (HashMap.Entry<String, String> entry : updates.entrySet()) {
            String pairString = entry.getKey() + ": " + entry.getValue() + "\n";
            if (!entry.getKey().equals("id"))
                toPrintString = toPrintString + pairString;
            etResponse.append(pairString);
            if (indexListToUpdate >= 0) {
                //update for floats and format to have 1 decimal
                if (entry.getKey().equals("value")) {
                    Float floatValue = Float.parseFloat(entry.getValue());
                    String formatedValue = String.format("%.1f", floatValue);
                    summaryToUpdate.add(" " + formatedValue);
                    listAdapter.setHeaderSummary(indexListToUpdate, summaryToUpdate, true);
                }
                //update for windows and display label
                if (entry.getKey().equals("state")) {
                    String formatedValue = (entry.getValue() == "0") ? "Closed" : "Open";
                    summaryToUpdate.add("  " + formatedValue);
                    listAdapter.setHeaderSummary(indexListToUpdate, summaryToUpdate, true);
                }
            }
        }
        etResponse.append("\n");
        //remove trailing \n
        listToAppend = listDataChild.get(listDataHeader.get(indexListToUpdate));
        listToAppend.add(toPrintString.substring(0, toPrintString.length() - 1));
        listAdapter.notifyDataSetChanged();

        //update progress bar
        menuItem.collapseActionView();
        menuItem.setActionView(null);

    }


}