package com.hsd.contest.spain.clover.huawei;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.maps.CameraUpdate;
import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.MapView;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.Marker;
import com.huawei.hms.maps.model.MarkerOptions;
import com.huawei.hms.site.api.SearchResultListener;
import com.huawei.hms.site.api.SearchService;
import com.huawei.hms.site.api.SearchServiceFactory;
import com.huawei.hms.site.api.model.Coordinate;
import com.huawei.hms.site.api.model.HwLocationType;
import com.huawei.hms.site.api.model.SearchStatus;
import com.huawei.hms.site.api.model.Site;
import com.huawei.hms.site.api.model.TextSearchRequest;
import com.huawei.hms.site.api.model.TextSearchResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private HuaweiMap hMap;
    private MapView mMapView;
    private Marker mMarker;
    private SearchService searchService;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private Task<Location> task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Checks permissions.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 45);

        //create a fusedLocationProviderClient
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        // Obtain the last known location.
        task = fusedLocationProviderClient.getLastLocation()
                // Define callback for success in obtaining the last known location.
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location == null) {
                        }
                        // Define logic for processing the Location object upon success.
                        // ...
                    }
                })
                // Define callback for failure in obtaining the last known location.
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // ...
                    }
                });

        mMapView = findViewById(R.id.mapView);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(HuaweiMap huaweiMap) {
        // Checks permissions.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 45);

        //get map instance in a callback method
        Log.d("HIMAP", "onMapReady: ");
        hMap = huaweiMap;
        hMap.setMyLocationEnabled(true);// Enable the my-location overlay.
        hMap.getUiSettings().setMyLocationButtonEnabled(true);// Enable the my-location icon.

        CameraUpdate cameraUpdate;
        if (task.getResult() != null) {
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude()), 18);
            hMap.animateCamera(cameraUpdate);
        }

        try {
            searchService = SearchServiceFactory.create(this, URLEncoder.encode("CgB6e3x987J8d2tsyCD7V65ny+L0DW4dShv458nbCdSamqlZUOy6Sem0BsY7cDjyqcTFb+biOjNiy7bOd+/X3Qqs", "utf-8")); //Create searchService Object
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        final Coordinate location;
        if (task.getResult() != null) {
            location = new Coordinate(task.getResult().getLatitude(), task.getResult().getLongitude());
            TextSearchRequest textSearchRequest = new TextSearchRequest();
            textSearchRequest.setLocation(location);
            textSearchRequest.setLanguage("es");
            textSearchRequest.setCountryCode("ES");
            textSearchRequest.setQuery("farmacia");
            textSearchRequest.setRadius(1500);
            textSearchRequest.setHwPoiType(HwLocationType.PHARMACY);

            SearchResultListener<TextSearchResponse> listener = new SearchResultListener<TextSearchResponse>() {
                @Override
                public void onSearchResult(TextSearchResponse textSearchResponse) {
                    if (textSearchResponse == null || textSearchResponse.getTotalCount() <= 0)  {
                        return;
                    }
                    List<Site> sites = textSearchResponse.getSites();
                    for (Site site : sites) {
                        MarkerOptions options = new MarkerOptions()
                                .position(new LatLng(site.getLocation().getLat(), site.getLocation().getLng()))
                                .title(site.getName());
                        mMarker = hMap.addMarker(options);
                    }
                }

                @Override
                public void onSearchError(SearchStatus searchStatus) {
                    Toast.makeText(getApplicationContext(), searchStatus.errorMessage, Toast.LENGTH_SHORT).show();
                }
            };
            searchService.textSearch(textSearchRequest, listener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }
}