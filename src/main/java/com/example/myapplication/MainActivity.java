package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};

    static final int SCAN_BARCODE = 1;
    static final int GET_AUTH = 2;


    private String encodedAuth = null;


    //check json path before .read
    public boolean pathIsValid(Object jsonString,String path){
        try {
            String value = JsonPath.read(jsonString, path).toString();
        } catch(PathNotFoundException e) {
            return false;
        }
        return true;
    }

    public void login(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, GET_AUTH);
    }
    //onclick method for get ci attribs button
    public void getDataInsight(View view)
    {

            Log.i("Info", "Button pressed");
            CheckBox checkBoxCIType = (CheckBox) findViewById(R.id.checkBoxCIType);
            requestInsightData(checkBoxCIType.isChecked(), encodedAuth);



    }

    public void requestInsightData(boolean isOldCI, String authData){

        EditText editTextCI = (EditText) findViewById(R.id.editTextCI);
        final TextView textViewLabel = (TextView) findViewById(R.id.textViewLabel);
        final TextView textViewId = (TextView) findViewById(R.id.textViewId);
        final TextView textViewOwner = (TextView) findViewById(R.id.textViewOwner);
        final TextView textViewStatus = (TextView) findViewById(R.id.textViewStatus);
        final TextView textViewName = (TextView) findViewById(R.id.textViewName);
        final TextView textViewOldCI = (TextView) findViewById(R.id.textViewOldCI);
        final TextView textViewResponsible = (TextView) findViewById(R.id.textViewResponsible);
        final TextView textViewSelflink = (TextView) findViewById(R.id.textViewSelflink);
        final TextView textViewLocation = (TextView) findViewById(R.id.textViewLocation);

        Toast.makeText(this, "Requesting data", Toast.LENGTH_LONG).show();

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String urlInsight = null;
        if(isOldCI){
            urlInsight = "https://sd.nexign.com/rest/insight/1.0/iql/objects?objectSchemaId=3&iql=%22CI%20Old%20Number%22%20endswith%20" + editTextCI.getText().toString() + "&resultPerPage=1";}
        else {urlInsight = "https://sd.nexign.com/rest/insight/1.0/iql/objects?objectSchemaId=3&iql=objectId=" + editTextCI.getText().toString() + "&resultPerPage=1";};

        // Request a string response from the provided URL.

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, urlInsight, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response
                    ) {
                        try {

                            String owner = null;
                            String ownerPath = "$.objectEntries.[0].attributes.[9].objectAttributeValues.[0].displayValue";
                            String label = null;
                            String labelPath = "$.objectEntries.[0].label";
                            String insightId = null;
                            String insightIdPath = "$.objectEntries.[0].id";
                            String status = null;
                            String statusPath = "$.objectEntries.[0].attributes.[20].objectAttributeValues.[0].displayValue";
                            String name = null;
                            String namePath = "$.objectEntries.[0].attributes.[1].objectAttributeValues.[0].displayValue";
                            String oldCINum = null;
                            String oldCINumPath = "$.objectEntries.[0].attributes.[7].objectAttributeValues.[0].displayValue";
                            String responsible = null;
                            String responsiblePath = "$.objectEntries.[0].attributes.[8].objectAttributeValues.[0].displayValue";
                            String selfLink = null;
                            String selfLinkPath = "$.objectEntries.[0]._links.self";
                            String location = null;
                            String locationPath = "$.objectEntries.[0].attributes.[14].objectAttributeValues.[0].displayValue";


                            Configuration jsonPathConf = Configuration.defaultConfiguration();
                            jsonPathConf.addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);

                            Object jsonResponse = jsonPathConf.jsonProvider().parse(response.toString());

                            if(pathIsValid(jsonResponse,labelPath)){
                                label = JsonPath.read(jsonResponse, labelPath);}
                            if(pathIsValid(jsonResponse,insightIdPath)){
                                insightId = JsonPath.read(jsonResponse, insightIdPath).toString();}
                            if(pathIsValid(jsonResponse,ownerPath)){
                                owner = JsonPath.read(jsonResponse, ownerPath);}
                            if(pathIsValid(jsonResponse,statusPath)){
                                status = JsonPath.read(jsonResponse, statusPath);}
                            if(pathIsValid(jsonResponse,namePath)){
                                name = JsonPath.read(jsonResponse, namePath);}
                            if(pathIsValid(jsonResponse,oldCINumPath)){
                                oldCINum = JsonPath.read(jsonResponse, oldCINumPath);}
                            if(pathIsValid(jsonResponse,responsiblePath)){
                                responsible = JsonPath.read(jsonResponse, responsiblePath);}
                            if(pathIsValid(jsonResponse,selfLinkPath)){
                                selfLink = JsonPath.read(jsonResponse, selfLinkPath);}
                            if(pathIsValid(jsonResponse,locationPath)){
                                location = JsonPath.read(jsonResponse, locationPath);}



                            textViewLabel.setText("Label: " + label);
                            textViewId.setText("Id: " + insightId);
                            textViewOwner.setText("Owner: " + owner);
                            textViewStatus.setText("Status: " + status);
                            textViewName.setText("Name: " + name);
                            textViewOldCI.setText("Old CI Number: " + oldCINum);
                            textViewResponsible.setText("Responsible: " + responsible);
                            textViewSelflink.setText(selfLink);
                            textViewLocation.setText("Location: " + location);


                        } catch (PathNotFoundException e) {Log.i(null,"Path not found");}

                        Log.i(null, "Got response");
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                textViewLabel.setText("API response error " + error.toString());
                Log.i("error string", error.toString());
            }



        }) {

            //request Headers for api
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                //temp test auth
                //String encodedString = Base64.encodeToString(String.format("%s:%s", "***", "***").getBytes(), Base64.NO_WRAP);
                params.put("Authorization", "Basic " + authData);
                return params;
            }

        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void scanBarcode(View view)
    {
        Intent intent = new Intent(this, BarcodeScanningActivity.class);
        startActivityForResult(intent, SCAN_BARCODE);

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);

        if (savedInstanceState != null) {

            encodedAuth = savedInstanceState.getString("STATE_AUTH");}

        if(encodedAuth==null){
            login();
        }




    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("STATE_AUTH", encodedAuth);



        super.onSaveInstanceState(savedInstanceState);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == SCAN_BARCODE) {
            boolean isOldCI;
            if (resultCode == RESULT_OK) {
                CheckBox checkBoxCIType = (CheckBox) findViewById(R.id.checkBoxCIType);
                String requestBarcode = null;
                String rawBarcode = data.getStringExtra("BARCODE_RAW_VALUE");
                if (rawBarcode!=null) {
                    EditText editTextCI = (EditText) findViewById(R.id.editTextCI);
                    //Toast.makeText(this, rawBarcode, Toast.LENGTH_SHORT).show();
                    if(rawBarcode.startsWith("https://sd.nexign.com/secure")) {
                        requestBarcode = rawBarcode.substring(51);
                        isOldCI = false;
                    }
                    else {
                        requestBarcode=rawBarcode;
                        isOldCI = true;

                    }
                    checkBoxCIType.setChecked(isOldCI);
                    editTextCI.setText(requestBarcode);
                    requestInsightData(isOldCI,encodedAuth);
                }
            }
        }
        if (requestCode == GET_AUTH){
            if (resultCode == RESULT_OK){
                encodedAuth = data.getStringExtra("AUTHENTICATION_STRING");
            }
        }
        super.onActivityResult(requestCode,resultCode,data);
    }
}
