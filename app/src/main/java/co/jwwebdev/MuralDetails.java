package co.jwwebdev;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.List;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import co.jwwebdev.adapter.MuralImagesAdapter;
import co.jwwebdev.mapHelper.FetchURL;
import co.jwwebdev.mapHelper.TaskLoadedCallback;
import co.jwwebdev.muraletorun.R;

public class MuralDetails extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback/*, DirectionFinderListener*/ {

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_REQUEST = 1;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final float LOCATION_ZOOM = 14f;
    private static final float DEFAULT_ZOOM = 10f;
    private Polyline currentPolyline;
    private ArrayList<Integer> muralImagesList = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private MapView mapView;
    private GoogleMap gMap;
    private LatLng muralPosition, myPosition;
    private ImageButton locationIB;
    private CircularProgressButton directionCPB;
    private Snackbar snack;
    private String name;
    private Typeface typeface;
    private boolean isAfterOnCreate = true, isAfterClickLocalize = false;
    private LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muraldetails);

        Toolbar toolbar = findViewById(R.id.mural_detailsTbr);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NestedScrollView nestedScrollView = (NestedScrollView) findViewById(R.id.mural_detailsNSV);
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.mural_detailsABL);
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.mural_detailsCTL);
        TextView addressTV = (TextView) findViewById(R.id.mural_details_addressTV);
        TextView descriptionTV = (TextView) findViewById(R.id.mural_details_descriptionTV);
        locationIB = (ImageButton) findViewById(R.id.mural_details_localizeIB);
        directionCPB = (CircularProgressButton) findViewById(R.id.mural_details_directionCPB);

        Bundle extras = getIntent().getBundleExtra("Mural");

        if (extras != null) {

            name = extras.getString("Name");
            String address = extras.getString("Address");
            double lat = extras.getDouble("Lat");
            double lon = extras.getDouble("Lon");
            String description = extras.getString("Description");
            muralImagesList = extras.getIntegerArrayList("Images");

            toolbar.setTitle(name);
            addressTV.setText(address);
            descriptionTV.setText(description);

            muralPosition = new LatLng(lat, lon);
        }

        Bundle mapViewBundle = null;
        if (savedInstanceState != null)
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);

        mapView = findViewById(R.id.mural_detailsMV);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        typeface = ResourcesCompat.getFont(this, R.font.karma);
        directionCPB.setTypeface(typeface);

        blockScrollABLOnMap(appBarLayout);
        initAndFitHeightCTL(collapsingToolbar);
        initRecyclerView(nestedScrollView, toolbar);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {

            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

        if (checkLocationPermission())
            getDeviceLocation();
    }


    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }


    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        locationManager.removeUpdates(locationListenerNetwork);
    }


    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.right_to_left_enter, R.anim.right_to_left_exit);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {

            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        gMap = googleMap;
        gMap.addMarker(new MarkerOptions().position(muralPosition).title(name));
        gMap.getUiSettings().setMyLocationButtonEnabled(false);
        gMap.getUiSettings().setZoomControlsEnabled(true);
        gMap.getUiSettings().setCompassEnabled(false);
        gMap.setTrafficEnabled(true);

        geoLocate();
    }


    @Override
    public void onTaskDone(Object... values) {

        if (currentPolyline != null)
            currentPolyline.remove();

        currentPolyline = gMap.addPolyline((PolylineOptions) values[0]);
        directionCPB.revertAnimation();
    }


    @Override
    public void onTaskCancel() {

        directionCPB.revertAnimation();
        Toast.makeText(this, getString(R.string.no_limit), Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == LOCATION_REQUEST) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                locationIB.setVisibility(View.VISIBLE);
                getDeviceLocation();
                getDirection();
                //sendRequest();
            }

            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    private void blockScrollABLOnMap(final AppBarLayout appBarLayout) {

        if (appBarLayout.getLayoutParams() != null) {

            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
            AppBarLayout.Behavior appBarLayoutBehaviour = new AppBarLayout.Behavior();
            appBarLayoutBehaviour.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
                @Override
                public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                    return false;
                }
            });
            layoutParams.setBehavior(appBarLayoutBehaviour);
        }
    }


    private void initAndFitHeightCTL(CollapsingToolbarLayout collapsingToolbar) {

        collapsingToolbar.setCollapsedTitleTypeface(typeface);
        collapsingToolbar.setExpandedTitleTypeface(typeface);

        if (name.equals("II pokój toruński 1466") || name.equals("Jesień 1939 r. w Toruniu")) { // zamiana wysokosci Collapsing Toolbar gdy tytuł jest jedną linią na 345dp, gdy tytuł ma wiecej lini to domyslnie w .xml 380dp

            ViewGroup.LayoutParams params = collapsingToolbar.getLayoutParams();
            final float scale = getResources().getDisplayMetrics().density;
            params.height = (int) (345 * scale + 0.5f); // zamiana 345dp na pixele zamiast
            collapsingToolbar.setLayoutParams(params);
        }
    }


    private void initRecyclerView(NestedScrollView nestedScrollView, final Toolbar toolbar) {

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.mural_detailsRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        MuralImagesAdapter muralImagesAdapter = new MuralImagesAdapter(MuralDetails.this, muralImagesList);
        recyclerView.setAdapter(muralImagesAdapter);

        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() { // dodanie elevation do toolbar kiedy scrolluje
            @Override
            public void onScrollChange(NestedScrollView nestedScrollView, int i, int i1, int i2, int i3) {

                if (toolbar != null) {

                    if (!nestedScrollView.canScrollVertically(-1))
                        toolbar.setElevation(0f);
                    else
                        toolbar.setElevation(50f);
                }
            }
        });
    }


    private boolean checkLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                locationIB.setVisibility(View.VISIBLE);

                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                if (location != null)
                    myPosition = (new LatLng(location.getLatitude(), location.getLongitude()));

                return true;
            }
        }
        return false;
    }


    private void getLocationPermission() {

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (!checkLocationPermission()) {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_REQUEST);
        } else
            getDirection();
        //sendRequest();
    }


    private void getDirection() {

        directionCPB.startAnimation();

        if (checkLocationPermission()) {

            System.out.println("Mural internet: " + internetIsConnected());
            if (internetIsConnected()) {

                if (myPosition != null)
                    new FetchURL(MuralDetails.this).execute(getUrl(myPosition, muralPosition, "driving"), "driving");
                else {

                    Toast.makeText(this, "Nie ustalono pozycji", Toast.LENGTH_SHORT).show();
                    isAfterClickLocalize = true;
                    getDeviceLocation();
                    directionCPB.revertAnimation();
                }
            } else {

                Toast.makeText(this, "Sprawdź połączenie z Internetem", Toast.LENGTH_SHORT).show();
                directionCPB.revertAnimation();
            }
        } else {

            directionCPB.revertAnimation();
            Toast.makeText(this, "Brak dostępu do lokalizacji", Toast.LENGTH_SHORT).show();
        }
    }


    private final LocationListener locationListenerNetwork = new LocationListener() {

        public void onLocationChanged(Location location) {

            runOnUiThread(new Runnable() {
                @SuppressLint("MissingPermission")
                @Override
                public void run() {

                    myPosition = new LatLng(location.getLatitude(), location.getLongitude());
                    gMap.setMyLocationEnabled(true);

                    if (snack != null)
                        snack.dismiss();
                }
            });

            locationManager.removeUpdates(this);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    };


    private void getDeviceLocation() {

        if (checkLocationPermission()) {

            FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

            try {

                final Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {

                    @Override
                    public void onComplete(@NonNull Task task) {

                        if (task.isSuccessful()) {

                            Location currentLocation = (Location) task.getResult();
                            if (currentLocation != null) {

                                myPosition = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                                gMap.setMyLocationEnabled(true);

                                if (snack != null)
                                    snack.dismiss();
                            }
                        }
                    }
                });
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        if (myPosition == null && locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            snack = Snackbar.make(findViewById(android.R.id.content), "Trwa ustalanie lokalizacji...", Snackbar.LENGTH_INDEFINITE);
            ViewGroup snackbarView = (ViewGroup) snack.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
            snackbarView.addView(new ProgressBar(this));
            snack.show();

            @SuppressLint("CutPasteId") TextView snackBarText = (TextView) snack.getView().findViewById(android.support.design.R.id.snackbar_text);
            snackBarText.setTypeface(typeface);
        }

        if (myPosition == null) {

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && isAfterClickLocalize)
                buildAlertMessageNoGps();

            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(true);
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            String provider = locationManager.getBestProvider(criteria, true);

            if (provider != null) {
                locationManager.requestLocationUpdates(provider, 2 * 60 * 1000, 200, locationListenerNetwork);
            }
        }
    }


    private void buildAlertMessageNoGps() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Moduł GPS jest wyłączony. Chcesz go włączyć?")
                .setCancelable(false)
                .setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("Nie", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();

        isAfterClickLocalize = false;
    }


    private void geoLocate() {

        if (isAfterOnCreate) {

            try {

                moveCamera(muralPosition, LOCATION_ZOOM, name);
                isAfterOnCreate = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void moveCamera(LatLng latLng, float zoom, String title) {

        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        MarkerOptions options = new MarkerOptions().position(latLng).title(title);
        gMap.addMarker(options);
    }


    private String getUrl(LatLng origin, LatLng dest, String directionMode) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }


    public boolean internetIsConnected() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

            try {

                String command = "ping -c aaa google.com";
                System.out.println("Mural internet: " + Runtime.getRuntime().exec(command).waitFor());

                if (Runtime.getRuntime().exec(command).waitFor() == 2 || Runtime.getRuntime().exec(command).waitFor() == 0)
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }


    public void onClickButton(View view) {
        getLocationPermission();
    }


    public void onClickLocalize(View view) {

        if (myPosition != null)
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition, gMap.getCameraPosition().zoom));
        else {

            isAfterClickLocalize = true;
            getDeviceLocation();
        }
    }
}