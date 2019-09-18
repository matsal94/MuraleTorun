package co.jwwebdev;

import android.animation.Animator;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.List;

import co.jwwebdev.adapter.MuralAdapter;
import co.jwwebdev.model.Mural;
import co.jwwebdev.muraletorun.R;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private ArrayList<Mural> muralsList = new ArrayList<>();
    private ImageButton addMapIB, removeMapIB;
    private AppBarLayout appBarLayout;
    private RecyclerView recyclerView;
    private MuralAdapter muralAdapter;
    //private NestedScrollView nestedScrollView;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private MapView mapView;
    private GoogleMap gMap;
    boolean isEmptyFilteredList = true;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.mainCTL);
        appBarLayout = (AppBarLayout) findViewById(R.id.mainABL);
        addMapIB = (ImageButton) findViewById(R.id.main_map_addIB);
        removeMapIB = (ImageButton) findViewById(R.id.main_map_removeIB);
        //nestedScrollView = (NestedScrollView) findViewById(R.id.mainNSV);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null)
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);

        mapView = findViewById(R.id.mainMV);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        blockScrollABLOnMap();
        initRecyclerView();
        getMuralsList();
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

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Czy na pewno chcesz wyjść z aplikacji?")
                .setCancelable(true)
                .setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        finish();
                    }
                })
                .setNegativeButton("Nie", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        gMap = googleMap;
        gMap.clear();
        gMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        gMap.getUiSettings().setMyLocationButtonEnabled(false);
        gMap.getUiSettings().setZoomControlsEnabled(true);
        gMap.getUiSettings().setCompassEnabled(true);
        gMap.getUiSettings().setMapToolbarEnabled(false);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (int i = 0; i < muralsList.size(); i++) {

            MarkerOptions marker = new MarkerOptions().position(new LatLng(muralsList.get(i).getLat(), muralsList.get(i).getLon())).title(muralsList.get(i).getName());
            builder.include(marker.getPosition());
            gMap.addMarker(marker);
        }


        gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                String title = marker.getTitle();
                ArrayList<Mural> filteredList = new ArrayList<>();

                for (Mural row : muralsList)
                    if (row.getName().equals(title))
                        filteredList.add(row);

                muralAdapter.reload(filteredList);
                isEmptyFilteredList = false;
                return false;
            }
        });

        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                reloadFullMuralListAfterFilter();
            }
        });


        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.20); // offset from edges of the map 10% of screen

        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

        gMap.animateCamera(cu);
    }


    private void blockScrollABLOnMap() {

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


    private void initRecyclerView() {

        recyclerView = (RecyclerView) findViewById(R.id.mainRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        muralAdapter = new MuralAdapter(MainActivity.this, muralsList);
        recyclerView.setAdapter(muralAdapter);
/*
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView nestedScrollView, int i, int i1, int i2, int i3) {

                if (appBarLayout != null) {

                    if (!nestedScrollView.canScrollVertically(-1))
                        appBarLayout.setElevation(0f);
                    else
                        appBarLayout.setElevation(10f);
                }
            }
        });*/

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (appBarLayout != null) {

                    if (!recyclerView.canScrollVertically(-1))
                        appBarLayout.setElevation(0f);
                    else
                        appBarLayout.setElevation(10f);
                }
            }
        });
    }


    private void reloadFullMuralListAfterFilter() {

        if (!isEmptyFilteredList) {

            muralAdapter.reload(muralsList);
            isEmptyFilteredList = true;
        }
    }


    private void getMuralsList() {

        List<Integer> mural1List = new ArrayList<>();
        List<Integer> mural2List = new ArrayList<>();
        List<Integer> mural3List = new ArrayList<>();
        List<Integer> mural4List = new ArrayList<>();
        List<Integer> mural5List = new ArrayList<>();
        List<Integer> mural6List = new ArrayList<>();
        List<Integer> mural7List = new ArrayList<>();
        List<Integer> mural8List = new ArrayList<>();

        mural1List.add(R.drawable.mural1_1);
        mural1List.add(R.drawable.mural1_2);
        mural1List.add(R.drawable.mural1_3);
        mural1List.add(R.drawable.mural1_4);
        mural1List.add(R.drawable.mural1_5);

        mural2List.add(R.drawable.mural2_1);
        mural2List.add(R.drawable.mural2_2);
        mural2List.add(R.drawable.mural2_3);
        mural2List.add(R.drawable.mural2_4);
        mural2List.add(R.drawable.mural2_5);
        mural2List.add(R.drawable.mural2_6);

        mural3List.add(R.drawable.mural3_1);
        mural3List.add(R.drawable.mural3_2);
        mural3List.add(R.drawable.mural3_3);
        mural3List.add(R.drawable.mural3_4);
        mural3List.add(R.drawable.mural3_5);
        mural3List.add(R.drawable.mural3_6);

        mural4List.add(R.drawable.mural4_1);
        mural4List.add(R.drawable.mural4_2);
        mural4List.add(R.drawable.mural4_3);
        mural4List.add(R.drawable.mural4_4);
        mural4List.add(R.drawable.mural4_5);
        mural4List.add(R.drawable.mural4_6);
        mural4List.add(R.drawable.mural4_7);
        mural4List.add(R.drawable.mural4_8);
        mural4List.add(R.drawable.mural4_9);
        mural4List.add(R.drawable.mural4_10);
        mural4List.add(R.drawable.mural4_11);

        mural5List.add(R.drawable.mural5_1);
        mural5List.add(R.drawable.mural5_2);
        mural5List.add(R.drawable.mural5_3);
        mural5List.add(R.drawable.mural5_4);
        mural5List.add(R.drawable.mural5_5);
        mural5List.add(R.drawable.mural5_6);
        mural5List.add(R.drawable.mural5_7);
        mural5List.add(R.drawable.mural5_8);

        mural6List.add(R.drawable.mural6_1);
        mural6List.add(R.drawable.mural6_2);
        mural6List.add(R.drawable.mural6_3);
        mural6List.add(R.drawable.mural6_4);
        mural6List.add(R.drawable.mural6_5);
        mural6List.add(R.drawable.mural6_6);
        mural6List.add(R.drawable.mural6_7);

        mural7List.add(R.drawable.mural7_1);
        mural7List.add(R.drawable.mural7_2);
        mural7List.add(R.drawable.mural7_3);
        mural7List.add(R.drawable.mural7_4);
        mural7List.add(R.drawable.mural7_5);
        mural7List.add(R.drawable.mural7_6);

        mural8List.add(R.drawable.mural8_1);
        mural8List.add(R.drawable.mural8_2);
        mural8List.add(R.drawable.mural8_3);

        muralsList.add(new Mural("II pokój toruński 1466", "Juliana Fałata 58, 87-100 Toruń", 53.016475, 18.569375, getString(R.string.description1), mural1List));
        muralsList.add(new Mural("Obrona Torunia przed Szwedami 1629 r.", "Jurija Gagarina 44, 87-100 Toruń", 53.019178, 18.565394, getString(R.string.description2), mural2List));
        muralsList.add(new Mural("Walka o polskość Torunia pod zaborami", "Juliana Fałata 72, 87-100 Toruń", 53.016449, 18.568308, getString(R.string.description3), mural3List));
        muralsList.add(new Mural("Włączenie Torunia do odrodzonej Polski w 1920 r.", "Jana Matejki 73, 87-100 Toruń", 53.016102, 18.591566, getString(R.string.description4), mural4List));
        muralsList.add(new Mural("Toruń stolicą województwa pomorskiego (1920-1939)", "Prejsa 2G, 87-100 Toruń", 53.024063, 18.678435, getString(R.string.description5), mural5List));
        muralsList.add(new Mural("Jesień 1939 r. w Toruniu", "Jakuba Suleckiego 4, 87-100 Toruń", 53.021190, 18.671823, getString(R.string.description6), mural6List));
        muralsList.add(new Mural("Torunianie na frontach II wojny światowej", "Wincentego Witosa 2, 87-100 Toruń", 53.025158, 18.689611, getString(R.string.description7), mural7List));
        muralsList.add(new Mural("Walka torunian z reżimem komunistycznym", "Adama Asnyka 10, 87-100 Toruń", 53.012224, 18.567611, getString(R.string.description8), mural8List));
    }


    private void setExpandEnabled(boolean enabled) {

        appBarLayout.setExpanded(enabled, true);
        appBarLayout.setActivated(enabled);
        final AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();

        if (enabled) {

            recyclerView.getLayoutManager().scrollToPosition(0);
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
        } else
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);

        collapsingToolbarLayout.setLayoutParams(params);
    }


    public void onClickMapAdd(View view) {

        setExpandEnabled(true);
        onMapReady(gMap);

        YoYo.with(Techniques.FadeInDown)
                .duration(400)
                .onStart(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        mapView.setVisibility(View.VISIBLE);
                    }
                })
                .playOn(mapView);

        YoYo.with(Techniques.FadeOutLeft)
                .duration(400)
                .onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        addMapIB.setVisibility(View.INVISIBLE);
                    }
                })
                .playOn(addMapIB);

        YoYo.with(Techniques.FadeInRight)
                .duration(400)
                .onStart(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        removeMapIB.setVisibility(View.VISIBLE);
                    }
                })
                .playOn(removeMapIB);
    }


    public void onClickMapRemove(View view) {

        setExpandEnabled(false);
        reloadFullMuralListAfterFilter();

        YoYo.with(Techniques.FadeOutUp)
                .duration(400)
                .onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        appBarLayout.setElevation(0);
                        mapView.setVisibility(View.GONE);
                    }
                })
                .playOn(mapView);

        YoYo.with(Techniques.FadeOutLeft)
                .duration(400)
                .onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        removeMapIB.setVisibility(View.INVISIBLE);
                    }
                })
                .playOn(removeMapIB);

        YoYo.with(Techniques.FadeInRight)
                .duration(400)
                .onStart(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        addMapIB.setVisibility(View.VISIBLE);
                    }
                })
                .playOn(addMapIB);
    }
}

