package it.uniupo.museumguide;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.List;

import it.uniupo.museumguide.models.Museum;
import it.uniupo.museumguide.models.Schedule;
import it.uniupo.museumguide.util.Constants;
import it.uniupo.museumguide.util.LocationTracker;
import it.uniupo.museumguide.util.Util;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private EditText mEditRadius;
    private GoogleMap mMap;
    private View mMapView;
    private LocationTracker mLocationTracker;
    private LatLng mLastLocation;
    private double mRadius;
    private static final float DEFAULT_ZOOM = 15.0f;
    private static final int REQUEST_LOCATION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mMapView = mapFragment.getView();
        }
        mapFragment.getMapAsync(this);

        mEditRadius = findViewById(R.id.edit_radius);
        mEditRadius.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE && !TextUtils.isEmpty(mEditRadius.getText().toString().trim())) {
                    Util.hideKeyboard(MapsActivity.this, v);
                    mMap.clear();
                    mRadius = Double.parseDouble(mEditRadius.getText().toString());
                    initMap();
                    return true;
                }
                return false;
            }
        });
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap != null) {
            if (Util.checkPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) && Util.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                mMap.setMyLocationEnabled(true);

                mLocationTracker = new LocationTracker(this);
                if (mLocationTracker.canGetLocaion()) {
                    if (!String.valueOf(mLocationTracker.getLatitude()).equals("0.0") && !String.valueOf(mLocationTracker.getLongitude()).equals("0.0")) {
                        mLastLocation = new LatLng(mLocationTracker.getLatitude(), mLocationTracker.getLongitude());

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastLocation, DEFAULT_ZOOM));
                        mMap.addCircle(new CircleOptions()
                                .radius(mRadius * 1000)
                                .center(mLastLocation)
                                .fillColor(0x1700CC3F)
                                .strokeWidth(2f))
                                .setStrokeColor(0x00CC3F);

                        retrieveMuseums();
                    }
                } else {
                    mLocationTracker.showSettingsAlertDialog();
                }

                if (mMapView != null) {
                    View locationButton = ((View) mMapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                    layoutParams.setMargins(0, 180, 180, 0);
                }

                mMap.setOnMarkerClickListener(this);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            }
        }
    }

    // Metodo che permette di recuperare i musei aperti nel raggio specificato dall'utente
    private void retrieveMuseums() {
        FirebaseFirestore
                .getInstance()
                .collection(Constants.MUSEUMS)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                for (QueryDocumentSnapshot q : task.getResult()) {
                                    Museum museum = q.toObject(Museum.class);
                                    LatLng latLng = new LatLng(museum.getLatitude(), museum.getLongitude());
                                    if (haversine(mLastLocation, latLng) < mRadius) {
                                        List<Schedule> schedules = museum.getSchedules();
                                        if (isOpen(schedules, Calendar.getInstance())) {
                                            mMap.addMarker(new MarkerOptions()
                                                    .position(latLng)
                                                    .title(museum.getName())
                                                    .snippet(getString(R.string.museum_open))
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                        }

                                    }
                                }
                            }
                        }
                    }
                });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return true;
    }

    private boolean isOpen(List<Schedule> schedules, Calendar calendar) {
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        switch (day) {
            case Calendar.MONDAY:
                if (schedules.get(0).isOpen()) {
                    return checkTime(calendar, schedules.get(0).getOpeningTime(), schedules.get(0).getClosingTime());
                } else {
                    return false;
                }
            case Calendar.TUESDAY:
                if (schedules.get(1).isOpen()) {
                    return checkTime(calendar, schedules.get(1).getOpeningTime(), schedules.get(1).getClosingTime());
                } else {
                    return false;
                }
            case Calendar.WEDNESDAY:
                if (schedules.get(2).isOpen()) {
                    return checkTime(calendar, schedules.get(2).getOpeningTime(), schedules.get(2).getClosingTime());
                } else {
                    return false;
                }
            case Calendar.THURSDAY:
                if (schedules.get(3).isOpen()) {
                    return checkTime(calendar, schedules.get(3).getOpeningTime(), schedules.get(3).getClosingTime());
                } else {
                    return false;
                }
            case Calendar.FRIDAY:
                if (schedules.get(4).isOpen()) {
                    return checkTime(calendar, schedules.get(4).getOpeningTime(), schedules.get(4).getClosingTime());
                } else {
                    return false;
                }
            case Calendar.SATURDAY:
                if (schedules.get(5).isOpen()) {
                    return checkTime(calendar, schedules.get(5).getOpeningTime(), schedules.get(5).getClosingTime());
                } else {
                    return false;
                }
                case Calendar.SUNDAY:
                    if (schedules.get(6).isOpen()) {
                        return checkTime(calendar, schedules.get(6).getOpeningTime(), schedules.get(6).getClosingTime());
                    } else {
                        return false;
                    }
        }
        return false;
    }

    private boolean checkTime(Calendar calendar, String openingTime, String closingTime){
        boolean flag;
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        String[] open = openingTime.split(":");
        int openHour = Integer.parseInt(open[0]);
        int openMinute = Integer.parseInt(open[1]);

        String[] close = closingTime.split(":");
        int closeHour = Integer.parseInt(close[0]);
        int closeMinute = Integer.parseInt(close[1]);

        if (hour > openHour && hour < closeHour) {
            flag = true;
        } else if (hour == openHour) {
            flag = (minute >= openMinute);
        } else if (hour == closeHour) {
            flag = minute <= closeMinute;
        } else {
            flag = false;
        }
        return flag;
    }


    // Restituisce la distanza in km da p a t approssimata con la formula di Haversine
    private double haversine(LatLng p, LatLng t) {
        double lon1 = Math.toRadians(p.longitude);
        double lat1 = Math.toRadians(p.latitude);
        double lon2 = Math.toRadians(t.longitude);
        double lat2 = Math.toRadians(t.latitude);
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return (6367 * c);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                initMap();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mLocationTracker != null) {
            mLocationTracker.stopListener();
        }
    }
}
