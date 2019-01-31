package com.saeed.projects.googlemapsplacestutorial.gui;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.saeed.projects.googlemapsplacestutorial.R;

public class MainActivity extends AppCompatActivity
{

    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(pingGoogleServices()){
            init();
        }

    }

    private void init(){
        Button btnMaps = (Button) findViewById(R.id.btnInitMaps);
        btnMaps.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent mapIntent = new Intent(MainActivity.this, MapActivity.class);

                Bundle b = new Bundle();
                b.putInt("mapOption", 1); //Your id
                mapIntent.putExtras(b); //Put your id to your next Intent

                startActivity(mapIntent);
            }
        });

        Button btnLocateMe= (Button) findViewById(R.id.btnLocateMe);
        btnLocateMe.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent mapIntent = new Intent(MainActivity.this, MapActivity.class);

                Bundle b = new Bundle();
                b.putInt("mapOption", 2); //Your id
                mapIntent.putExtras(b); //Put your id to your next Intent

                startActivity(mapIntent);
            }
        });

        Button btnSearch = (Button) findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent mapIntent = new Intent(MainActivity.this, MapActivity.class);

                Bundle b = new Bundle();
                b.putInt("mapOption", 3); //Your id
                mapIntent.putExtras(b); //Put your id to your next Intent

                startActivity(mapIntent);
            }
        });
    }

    public boolean pingGoogleServices(){
        Log.d(TAG, " - pingGoogleServices - Checking google services version.");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if(available == ConnectionResult.SUCCESS){
            // services working fine
            Log.d(TAG, "pingGoogleServices: Google Play Services working fine");
            return true;
        } else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            Log.d(TAG, "pingGoogleServices: An error, that can be fixed");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "We can't make map request", Toast.LENGTH_LONG).show();
        }

        return false;
    }
}
