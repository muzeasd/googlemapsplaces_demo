package com.saeed.projects.googlemapsplacestutorial.gui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.PendingResults;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.saeed.projects.googlemapsplacestutorial.adapters.CustomWindowInfoAdapter;
import com.saeed.projects.googlemapsplacestutorial.adapters.PlaceAutocompleteAdapter;
import com.saeed.projects.googlemapsplacestutorial.R;
import com.saeed.projects.googlemapsplacestutorial.models.PlaceInfo;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener
{
    private static final String TAG = "MapActivity";
    private static boolean isPermissionsGranted = false;

    private static final int INTERNET_PERMISSION_CODE = 1001;
    private static final int FINE_LOCATION_PERMISSION_CODE = 1000;
    private static final int NETWORK_STATE_PERMISSION_CODE = 1002;

    private static final float ZOOM_LEVEL_CITY = 10f;
    private static final float ZOOM_LEVEL_STREET = 15f;
    private static final int PLACE_PICK_REQUEST = 1;

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProvider;
//    GeoDataClient googleApiClient;
    GoogleApiClient googleApiClient;
    PlaceAutocompleteAdapter mPlaceAutoCompleteAdapter;

    private static int mapOption = -1;
    MarkerOptions markerOptions;
    Marker mMarker;
    LatLng latLng;



    AutoCompleteTextView mSearchBox;
    ImageView imgViewLocateMe, imgViewMarkerInfo, imgViewPlacePicker;
    RelativeLayout layoutSearchBox;

    PlaceInfo mPlaceInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mSearchBox = (AutoCompleteTextView) findViewById(R.id.txtSearchBox);
        mSearchBox.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int actiondId, KeyEvent keyEvent)
            {
                if(actiondId == EditorInfo.IME_ACTION_SEARCH ||
                        actiondId == EditorInfo.IME_ACTION_DONE ||
                        keyEvent.getAction() == KeyEvent.ACTION_DOWN ||
                        keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    geoLocate();

                }
                return false;
            }
        });

        imgViewLocateMe = (ImageView) findViewById(R.id.ic_locate_me);
        imgViewLocateMe.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                locateMyDevice();
            }
        });

        imgViewMarkerInfo = (ImageView) findViewById(R.id.ic_marker_info);
        imgViewMarkerInfo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "onClick: MarkerInfo");
                if(mMarker != null){
                    try{
                        if(mMarker.isInfoWindowShown())
                            mMarker.hideInfoWindow();
                        else {
                            mMarker.showInfoWindow();
                        }
                    } catch (Exception ex){
                        Log.d(TAG, "onClick: NULL pointer ex" + ex.getMessage());
                    }
                }
            }
        });

        imgViewPlacePicker = (ImageView) findViewById(R.id.ic_map);
        imgViewPlacePicker.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "onClick: imgViewPlacePicker");

                PlacePicker.IntentBuilder placeBuilder = new PlacePicker.IntentBuilder();
                try{
                    startActivityForResult(placeBuilder.build(MapActivity.this), PLACE_PICK_REQUEST);
                }catch (Exception ex) {

                }

            }
        });

        layoutSearchBox = (RelativeLayout) findViewById(R.id.layoutSearchBox);

        getPermissions();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if(requestCode == PLACE_PICK_REQUEST){
            if(resultCode == RESULT_OK){
                Place place = PlacePicker.getPlace(MapActivity.this, data);
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(googleApiClient, place.getId());
                placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            }
        }
    }

    private void geoLocate(){
        Log.d(TAG, "geoLocate: start");

        String searchString = mSearchBox.getText().toString();
        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> addressList = new ArrayList<>();
        try{

            addressList = geocoder.getFromLocationName(searchString, 5);
            if(addressList.size() > 0){
                Address address = addressList.get(0);
                Log.d(TAG, "geoLocate: " + address.toString());

                latLng = new LatLng(address.getLatitude(), address.getLongitude());
                markerOptions = new MarkerOptions().position(latLng).title(address.getLocality());
                Toast.makeText(MapActivity.this, "Location Found", Toast.LENGTH_LONG).show();
                MoveCameraToLocation(latLng, ZOOM_LEVEL_CITY, markerOptions);
            }
        }catch (Exception ex){
            Log.d(TAG, "geoLocate: " + ex.getMessage());
        }
    }

    private void loadCorrespondingMap()
    {
        mapOption = getIntent().getExtras().getInt("mapOption");

        if (mapOption == 1) {
            initMap();
            layoutSearchBox.setVisibility(View.GONE);
            imgViewMarkerInfo.setVisibility(View.GONE);
            imgViewPlacePicker.setVisibility(View.GONE);
        } else if (mapOption == 2) {
            locateMyDevice();
            layoutSearchBox.setVisibility(View.GONE);
            imgViewMarkerInfo.setVisibility(View.GONE);
            imgViewPlacePicker.setVisibility(View.GONE);
        } else if (mapOption == 3) {
            locateMyDevice();
        }
    }

    private void locateMyDevice()
    {
        try {

            Log.d(TAG, "LocateMe():");

            mFusedLocationProvider = LocationServices.getFusedLocationProviderClient(this);
            try {
                final Task taskLocation = mFusedLocationProvider.getLastLocation();
                taskLocation.addOnCompleteListener(new OnCompleteListener()
                {
                    @Override
                    public void onComplete(@NonNull Task task)
                    {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: success");
                            Location currentLocation = (Location) task.getResult();
                            deviceLocationListener.onLocationChanged(currentLocation);
                        }
                    }
                });

            } catch (SecurityException se) {
                Log.d(TAG, "LocateMe: ERROR" + se.getMessage());
            }

        } catch (SecurityException se) {
            Log.d(TAG, "LocateMe: SecurityException {" + se.getMessage() + "}");
        }
    }

    LocationListener deviceLocationListener = new LocationListener()
    {
        @Override
        public void onLocationChanged(Location location)
        {
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
            markerOptions = new MarkerOptions().position(latLng).title("My Device");

            initMap();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle)
        {

        }

        @Override
        public void onProviderEnabled(String s)
        {

        }

        @Override
        public void onProviderDisabled(String s)
        {

        }
    };

    private void MoveCameraToLocation(LatLng latLng, float zoomLevel, MarkerOptions markerOptions)
    {
        try {

            mMarker = mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
            mMap.setInfoWindowAdapter(new CustomWindowInfoAdapter(MapActivity.this));
            hideSoftKeyboard();


        } catch (Exception ex){
            Log.d(TAG, "MoveCameraToLocation: "+ ex.getMessage());
        }
    }

    OnMapReadyCallback onMapReadyCallback = new OnMapReadyCallback()
    {
        @Override
        public void onMapReady(GoogleMap googleMap)
        {
            try {
                Log.d(TAG, "onMapReady: called");

                MapActivity.this.mMap = googleMap;
                mMap.getUiSettings().setMyLocationButtonEnabled(false);

                if (mapOption == 1) {

                    latLng = new LatLng(31.520370, 74.358749);
                    markerOptions = new MarkerOptions().position(latLng).title("Lahore");
                    MoveCameraToLocation(latLng, ZOOM_LEVEL_CITY, markerOptions);

                } else if (mapOption == 2) {

                    markerOptions = new MarkerOptions().position(latLng).title("My Device");
                    if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                    mMap.setMyLocationEnabled(true);
                    MoveCameraToLocation(latLng, ZOOM_LEVEL_STREET, markerOptions);
                } else if (mapOption == 3) {

//                    googleApiClient = Places.getGeoDataClient(MapActivity.this, null);
                    googleApiClient = new GoogleApiClient
                            .Builder(MapActivity.this)
                            .addApi(Places.GEO_DATA_API)
                            .addApi(Places.PLACE_DETECTION_API)
                            .enableAutoManage(MapActivity.this, MapActivity.this)
                            .build();

                    LatLngBounds latLngBounds = new LatLngBounds(new LatLng(-40, -168), new LatLng(71, 136));
                    mPlaceAutoCompleteAdapter = new PlaceAutocompleteAdapter(getBaseContext(), googleApiClient, latLngBounds, null);

                    markerOptions = new MarkerOptions().position(latLng).title("My Device");
                    if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                    mMap.setMyLocationEnabled(true);
                    MoveCameraToLocation(latLng, ZOOM_LEVEL_STREET, markerOptions);

                    mSearchBox.setAdapter(mPlaceAutoCompleteAdapter);
                    mSearchBox.setOnItemClickListener(adapterViewClickListener);
                }

            }catch (Exception ex){

            }
        }
    };

    private void initMap(){
        Log.d(TAG, "initMap: start");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(onMapReadyCallback);
    }

    private boolean getPermissions(){

        Log.d(TAG, "getPermissions: getPermissions() Called");

        String[] permissions = {Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE};

        if(ContextCompat.checkSelfPermission(MapActivity.this, permissions[0]) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(MapActivity.this, permissions[1]) == PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "getPermissions: permissions already granted");
                isPermissionsGranted = true;
                loadCorrespondingMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, INTERNET_PERMISSION_CODE);
            }
        }else {
            ActivityCompat.requestPermissions(this, permissions, FINE_LOCATION_PERMISSION_CODE);
        }

        Log.d(TAG, "getPermissions: permissions failed");
        return false;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case INTERNET_PERMISSION_CODE:
            {
                if(grantResults.length>0) {
                    for (int index = 0; index < grantResults.length; index++) {
                        if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                            isPermissionsGranted = false;
                            return;
                        }
                    }
                    isPermissionsGranted = true;
                }
            }
            case FINE_LOCATION_PERMISSION_CODE:
            {
                if(grantResults.length>0) {
                    for (int index = 0; index < grantResults.length; index++) {
                        if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                            isPermissionsGranted = false;
                            return;
                        }
                    }
                    isPermissionsGranted = true;
                }
            }
        }

        if(isPermissionsGranted){
            loadCorrespondingMap();
        } else Log.d(TAG, "getPermissions: permissions failed");
    }

    private void hideSoftKeyboard() {
//        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchBox.getWindowToken(), 0);

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {

    }

    private AdapterView.OnItemClickListener adapterViewClickListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
        {
            hideSoftKeyboard();

            final AutocompletePrediction item = mPlaceAutoCompleteAdapter.getItem(i);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(googleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>()
    {
        @Override
        public void onResult(@NonNull PlaceBuffer places)
        {
            if(!places.getStatus().isSuccess()){
                Log.d(TAG, "onResult: Failed to fetch result" + places.getStatus().toString());
                places.release();
                return;
            }

            try {
                final Place place = places.get(0);
                Log.d(TAG, "onResult: Place Details: " + place.getAttributions());
                Log.d(TAG, "onResult: Place viewPort: " + place.getViewport());
                Log.d(TAG, "onResult: Place phoneNo: " + place.getPhoneNumber());
                Log.d(TAG, "onResult: Place address: " + place.getAddress());
                Log.d(TAG, "onResult: Place country: " + place.getLocale().getCountry());
                Log.d(TAG, "onResult: Place name: " + place.getName());
                Log.d(TAG, "onResult: Place Web site: " + place.getWebsiteUri());
                Log.d(TAG, "onResult: Place id: " + place.getId());

               mPlaceInfo = new PlaceInfo();
                mPlaceInfo.setPlaceId(place.getId());
                mPlaceInfo.setPlaceName(place.getName().toString());
                mPlaceInfo.setPlaceAddress(place.getAddress().toString());
                mPlaceInfo.setPlaceCountry(place.getLocale().getCountry());
                mPlaceInfo.setPlacePhoneNo(place.getPhoneNumber().toString());
                if(place.getWebsiteUri() != null)
                    mPlaceInfo.setPlaceWebSite(place.getWebsiteUri().toString());
                if(place.getViewport() != null)
                    mPlaceInfo.setPlaceViewPort(place.getViewport().toString());
                if(place.getAttributions() != null)
                    mPlaceInfo.setPlaceAttributions(place.getAttributions().toString());

                latLng = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
//                latLng = new LatLng(place.getViewport().getCenter().latitude, place.getViewport().getCenter().longitude);

                String snippet = System.getProperty("line.separator") + "Address: " + mPlaceInfo.getPlaceAddress()+
                        System.getProperty("line.separator") + "PhoneNo: " + mPlaceInfo.getPlacePhoneNo()+
                        System.getProperty("line.separator") +  "Web Site: " + mPlaceInfo.getPlaceWebSite();

                markerOptions = new MarkerOptions()
                        .position(latLng)
                        .title(mPlaceInfo.getPlaceName())
                        .snippet(snippet);

                MoveCameraToLocation(latLng, ZOOM_LEVEL_CITY, markerOptions);

                places.release();
            } catch (Exception ex) {
                Log.d(TAG, "onResult: " + ex.getMessage());
            }
        }
    };
}
