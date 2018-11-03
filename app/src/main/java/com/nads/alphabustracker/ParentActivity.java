package com.nads.alphabustracker;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class ParentActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FirebaseMessaging.getInstance().subscribeToTopic("location");
        Button signout = (Button)findViewById(R.id.button5);
        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent5 = new Intent(ParentActivity.this, AlphaLoginActivity.class);
                intent5.putExtra("finish", true);
                intent5.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent5);
                finish();
            }

        });
    }
    private class DownloadStateReceiver extends BroadcastReceiver {
        // Prevents instantiation
        private DownloadStateReceiver() {
        }
       // BroadcastReceiver receiver = new BroadcastReceiver() {
            final TextView textView5 = (TextView) findViewById(R.id.textView5);
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    String value = intent.getStringExtra("score");
                    textView5.setText(value);
                    Toast.makeText(getApplicationContext(), "there's no error", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "there's error", Toast.LENGTH_LONG).show();
                }
            }
        //};

    }
   @Override
   public void onStart(){
    super.onStart();
       DownloadStateReceiver mDownloadStateReceiver =
               new DownloadStateReceiver();

       LocalBroadcastManager.getInstance(this).registerReceiver(mDownloadStateReceiver,
               new IntentFilter(Constants.MESSAGING_EVENT));
   }
    @Override
    public void onStop() {
        super.onStop();
        DownloadStateReceiver mDownloadStateReceiver =
                new DownloadStateReceiver();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mDownloadStateReceiver);
    }

}
