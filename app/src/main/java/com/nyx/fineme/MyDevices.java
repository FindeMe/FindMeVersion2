package com.nyx.fineme;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.nyx.fineme.adapters.DevicesAdapter;
import com.nyx.fineme.helper.APIUrl;
import com.nyx.fineme.helper.BackgroundServices;
import com.nyx.fineme.helper.PostAction;
import com.nyx.fineme.helper.SharedPrefManager;
import com.nyx.fineme.models.DeviceRow;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class MyDevices extends Activity {


    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }
    String pref_index="";
    ArrayList<DeviceRow> d;
    DevicesAdapter a;
    void refresh(){
        if(d!=null) {
            d.clear();
            a.notifyDataSetChanged();
            ;
        }
    findViewById(R.id.loading).setVisibility(View.VISIBLE);
    findViewById(R.id.no_items).setVisibility(View.GONE);

    BackgroundServices.getInstance(this)
            .setBaseUrl(APIUrl.SERVER)
            .addPostParam("service" ,"my_devices")
            .addPostParam("id" , SharedPrefManager.getInstance(this).getUserKey(SharedPrefManager.KEY_ID))
            .addPostParam("token" , SharedPrefManager.getInstance(this).getUserKey(SharedPrefManager.KEY_TOKEN))
            .CallPost(new PostAction() {
                @Override
                public void whenFinished(String status, String response) throws JSONException {
                    if(new JSONObject(response).getInt("status")==1) {
                        JSONArray arr = new JSONObject(response).getJSONArray("data");
                        if(arr.length()==0)
                            findViewById(R.id.no_items).setVisibility(View.VISIBLE);
                        else {
                           d = new ArrayList<>();
                            for (int i = 0; i < arr.length(); i++)
                                d.add(new DeviceRow(arr.getJSONObject(i).getString("id"),
                                        arr.getJSONObject(i).getString("address"),
                                        arr.getJSONObject(i).getString("info"),
                                        arr.getJSONObject(i).getInt("min_dist"),
                                        arr.getJSONObject(i).getString("pic") ,
                                        arr.getJSONObject(i).getString("family")));
                            RecyclerView rec = (RecyclerView) findViewById(R.id.list);
                            rec.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            a = new DevicesAdapter(d ,MyDevices.this) {
                                @Override
                                public void editPic(int position) {

pref_index = d.get(position).id;



                                }
                            };
                            rec.setAdapter(a);

                        }
                        findViewById(R.id.loading).setVisibility(View.GONE);
                    }else{
                        Toast.makeText(MyDevices.this, new JSONObject(response).getString("message"), Toast.LENGTH_SHORT).show();
                    }
                }
            });
}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_devices);
        if(FirebaseMessaging.getInstance()!=null)
        FirebaseMessaging.getInstance().subscribeToTopic("user_"+
                SharedPrefManager.getInstance(MyDevices.this).getUserID());
//        findViewById(R.id.go_logout).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SharedPrefManager.getInstance(MyDevices.this).logout();
//                Intent i = new Intent(MyDevices.this ,SplashScreen.class);
//                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(i);
//                finish();
//            }
//        });
        findViewById(R.id.go_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MyDevices.this ,MainActivity.class);
                startActivity(i);
            }
        });    findViewById(R.id.go_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MyDevices.this ,ProfileActivity.class);
                startActivity(i);
            }
        });
        findViewById(R.id.go_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(d!=null) {
                    d.clear();
                    a.notifyDataSetChanged();
                    ;
                }
                findViewById(R.id.loading).setVisibility(View.VISIBLE);
                findViewById(R.id.no_items).setVisibility(View.GONE);

                BackgroundServices.getInstance(MyDevices.this)
                        .setBaseUrl(APIUrl.SERVER)
                        .addPostParam("service" ,"search")
                        .addPostParam("id" , SharedPrefManager.getInstance(getApplicationContext()).getUserKey(SharedPrefManager.KEY_ID))
                        .addPostParam("query" , ((EditText)findViewById(R.id.search_input)).getText().toString().trim())
                        .addPostParam("token" , SharedPrefManager.getInstance(getApplicationContext()).getUserKey(SharedPrefManager.KEY_TOKEN))
                        .CallPost(new PostAction() {
                            @Override
                            public void whenFinished(String status, String response) throws JSONException {
                                if(new JSONObject(response).getInt("status")==1) {
                                    JSONArray arr = new JSONObject(response).getJSONArray("data");
                                    if(arr.length()==0)
                                        findViewById(R.id.no_items).setVisibility(View.VISIBLE);
                                    else {
                                        d = new ArrayList<>();
                                        for (int i = 0; i < arr.length(); i++)
                                            d.add(new DeviceRow(arr.getJSONObject(i).getString("id"),
                                                    arr.getJSONObject(i).getString("address"),
                                                    arr.getJSONObject(i).getString("info"),
                                                    arr.getJSONObject(i).getInt("min_dist"),
                                                    arr.getJSONObject(i).getString("pic") ,
                                                    arr.getJSONObject(i).getString("family")));
                                        RecyclerView rec = (RecyclerView) findViewById(R.id.list);
                                        rec.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                        a = new DevicesAdapter(d ,MyDevices.this) {
                                            @Override
                                            public void editPic(int position) {

                                                pref_index = d.get(position).id;



                                            }
                                        };
                                        rec.setAdapter(a);

                                    }
                                    findViewById(R.id.loading).setVisibility(View.GONE);
                                }else{
                                    Toast.makeText(MyDevices.this, new JSONObject(response).getString("message"), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

    }


    public static final int REQUEST_IMAGE = 100;










    String imagePath = "";
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getParcelableExtra("path");
                Toast.makeText(this, "تم التعديل بنجاح", Toast.LENGTH_SHORT).show();
                    imagePath = uri.getPath();
//                    d.get(pref_index).pic = imagePath;
//                    a.notifyDataSetChanged();
                BackgroundServices.getInstance(MyDevices.this)
                        .setBaseUrl(APIUrl.SERVER)
                        .addPostParam("id" , pref_index)
                        .addPostParam("service" ,"edit_pic")
                        .setFile("pic" ,new File(imagePath))
                        .CallPost(new PostAction() {
                            @Override
                            public void whenFinished(String status, String response) throws JSONException {
                                Log.d("kkkkk" ,response);
                             }
                        });

            }
        }
    }
}
