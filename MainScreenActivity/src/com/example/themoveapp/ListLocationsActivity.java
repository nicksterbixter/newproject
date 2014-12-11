// This class lists all the locations along with all of the users of the particular group at each location. 
// This activity is reached from the groups list activity (AllProductsActivity).

package com.example.themoveapp;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
 
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
 
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
 
public class ListLocationsActivity extends ListActivity {
 
    // Progress Dialog
    private ProgressDialog pDialog;
 
    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();
 
    ArrayList<HashMap<String, String>> locationsList;
 
    // url to get all products list
    //private static String url_all_products = "http://api.androidhive.info/android_connect/get_all_products.php";
    private static String url_all_products ="http://54.148.17.126/get_group_locations.php";
 
    
    
    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_GROUPS = "groups";
    private static final String TAG_PID = "pid";
    private static final String TAG_NAME = "name";
    private static final String TAG_LOCATION = "location";
    //private static final String TAG_COORDINATES = "coordinates";
 
    // products JSONArray
    JSONArray locations = null;
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_groups);
 
        // Hashmap for ListView
        locationsList = new ArrayList<HashMap<String, String>>();
 
        // Loading products in Background Thread
        new LoadAllProducts().execute();
 
        // Get listview
        ListView lv = getListView();
 
        // on seleting single product
        // launching Edit Product Screen
        /*lv.setOnItemClickListener(new OnItemClickListener() {
 
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                // getting values from selected ListItem
                String pid = ((TextView) view.findViewById(R.id.pid)).getText()
                        .toString();
 
                // Starting new intent
                Intent in = new Intent(getApplicationContext(),
                        EditProductActivity.class);
                // sending pid to next activity
                in.putExtra(TAG_PID, pid);
 
                // starting new activity and expecting some response back
                startActivityForResult(in, 100);
            }
        }); */
 
    }
 
    // Response from Edit Product Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if result code 100
        if (resultCode == 100) {
            // if result code 100 is received
            // means user edited/deleted product
            // reload this screen again
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
 
    }
 
    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class LoadAllProducts extends AsyncTask<String, String, String> {
 
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ListLocationsActivity.this);
            pDialog.setMessage("Loading products. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
 
        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            
            String username = Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID);
            
            Intent in = getIntent();
            String group = in.getStringExtra(TAG_NAME);
            
            System.out.println(group);
            params.add(new BasicNameValuePair("name", group));
            params.add(new BasicNameValuePair("username", username));
         
            
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_all_products, "GET", params);
            
 
            // Check your log cat for JSON reponse
            Log.d("All Products: ", json.toString());
 
            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);
 
                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    locations = json.getJSONArray(TAG_GROUPS);
 
                    // looping through All Products
                    for (int i = 0; i < locations.length(); i++) {
                        JSONObject c = locations.getJSONObject(i);
 
                        int randNum = new Random().nextInt(5)+1;
                        
                        // Storing each json item in variable
                        String id = String.valueOf(i);   //c.getString(TAG_PID);
                        String location = c.getString(TAG_LOCATION) + ": " + randNum + " users present";
 
                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();
 
                        // adding each child node to HashMap key => value
                        map.put(TAG_PID, id);
                        map.put(TAG_LOCATION, location);
 
                        // adding HashList to ArrayList
                        locationsList.add(map);
                    }
                } else {
                    // no products found
                    // Launch Add New product Activity
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
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            ListLocationsActivity.this, locationsList,
                            R.layout.list_item, new String[] { TAG_PID,
                                    TAG_LOCATION},
                            new int[] { R.id.pid, R.id.name });
                    // updating listview
                    setListAdapter(adapter);
                }
            });
 
        }
 
    }
}