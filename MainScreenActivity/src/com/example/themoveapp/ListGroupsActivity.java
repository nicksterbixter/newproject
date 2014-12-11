package com.example.themoveapp;
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
 
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
 
import android.provider.Settings.Secure; 
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
 
public class ListGroupsActivity extends ListActivity {
 
    // Progress Dialog
    private ProgressDialog pDialog;
 
    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();
 
    ArrayList<HashMap<String, String>> groupsList;

     // android.provider.Settings.Secure.getString(getContentResolver(),  android.provider.Settings.Secure.ANDROID_ID); 
 
    // url to get all groups list
    private static String url_all_groups = "http://54.148.17.126/get_all_groups.php";
 
    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_GROUPS = "groups";
    private static final String TAG_PID = "pid";
    private static final String TAG_NAME = "name";
 
    // groups JSONArray
    JSONArray groups = null;
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_groups);
        
        
 
        // Hashmap for ListView
        groupsList = new ArrayList<HashMap<String, String>>();
 
        // Loading groups in Background Thread
        new LoadAllGroups().execute();
 
        // Get listview
        ListView lv = getListView();
 
        // on seleting single group
        // launching Locations Screen
        lv.setOnItemClickListener(new OnItemClickListener() {
 
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                // getting values from selected ListItem
                String pid = ((TextView) view.findViewById(R.id.pid)).getText()
                        .toString();
                String name = ((TextView) view.findViewById(R.id.name)).getText()
                .toString();
 
                // Starting new intent
                Intent in = new Intent(getApplicationContext(),
                        ListLocationsActivity.class);
                // sending pid to next activity
                in.putExtra(TAG_PID, pid);
                in.putExtra(TAG_NAME, name);
 
                // starting new activity and expecting some response back
                startActivityForResult(in, 100);
            }
        });
 
    }
 
    // Response from Edit Group Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if result code 100
        if (resultCode == 100) {
            // if result code 100 is received
            // means user edited/deleted group
            // reload this screen again
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
 
    }
 
    /**
     * Background Async Task to Load all group by making HTTP Request
     * */
    class LoadAllGroups extends AsyncTask<String, String, String> {
 
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ListGroupsActivity.this);
            pDialog.setMessage("Loading groups. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
 
        /**
         * getting All groups from url
         * */
        protected String doInBackground(String... args) {

        	String username = Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID);
            System.err.println(username);
            //String username = android_id;

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            //params.add(new BasicNameValuePair("username", username));
 
            // getting JSON Object
            // Note that create groups url accepts POST method
            //JSONObject json = jParser.makeHttpRequest(url_create_group,
            //        "POST", params);

            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_all_groups, "GET", params);
 
            // Check your log cat for JSON reponse
            Log.d("All Groups: ", json.toString());
 
            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);
 
                if (success == 1) {
                    // groups found
                    // Getting Array of Groups
                    groups = json.getJSONArray(TAG_GROUPS);
 
                    // looping through All Groups
                    for (int i = 0; i < groups.length(); i++) {
                        JSONObject c = groups.getJSONObject(i);
 
                        // Storing each json item in variable
                        String id = String.valueOf(i);//c.getString(TAG_PID);
                        String name = c.getString(TAG_NAME);
 
                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();
 
                        // adding each child node to HashMap key => value
                        map.put(TAG_PID, id);
                        map.put(TAG_NAME, name);
 
                        // adding HashList to ArrayList
                        groupsList.add(map);
                    }
                } else {
                    // no groups found
                    // Launch Add New groups Activity
                    Intent i = new Intent(getApplicationContext(),
                            NewGroupActivity.class);
                    // Closing all previous activities
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
 
            return null;
        }
 
        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all groups
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            ListGroupsActivity.this, groupsList,
                            R.layout.list_item, new String[] { TAG_PID,
                                    TAG_NAME},
                            new int[] { R.id.pid, R.id.name });
                    // updating listview
                    setListAdapter(adapter);
                }
            });
 
        }
 
    }
}