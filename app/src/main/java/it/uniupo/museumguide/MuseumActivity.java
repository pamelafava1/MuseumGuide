package it.uniupo.museumguide;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import it.uniupo.museumguide.models.Museum;
import it.uniupo.museumguide.models.Schedule;
import it.uniupo.museumguide.util.Constants;
import it.uniupo.museumguide.util.FirebaseUtil;
import it.uniupo.museumguide.util.Util;

// Activity che permette di salvare, modificare o eliminare un museo
public class MuseumActivity extends AppCompatActivity {

    private TextInputLayout mNameWrapper, mLocationWrapper;
    private EditText mEditName, mEditLocation;
    private Museum mMuseum;
    private CheckBox mCheckBoxMon, mCheckBoxTues, mCheckBoxWed, mCheckBoxThurs;
    private CheckBox mCheckBoxFri, mCheckBoxSat, mCheckBoxSun;
    private TextView mTextViewMon, mTextViewTues, mTextViewWed, mTextViewThurs;
    private TextView mTextViewFri, mTextViewSat, mTextViewSun;
    private static final String TAG = "MuseumActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_museum);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        findView();

        if (getIntent().hasExtra(Constants.MUSEUM)) {
            mMuseum = getIntent().getParcelableExtra(Constants.MUSEUM);
            updateUI();
        }

        mCheckBoxMon.setOnClickListener(checkListener);
        mCheckBoxTues.setOnClickListener(checkListener);
        mCheckBoxWed.setOnClickListener(checkListener);
        mCheckBoxThurs.setOnClickListener(checkListener);
        mCheckBoxFri.setOnClickListener(checkListener);
        mCheckBoxSat.setOnClickListener(checkListener);
        mCheckBoxSun.setOnClickListener(checkListener);
    }

    private void findView() {
        mNameWrapper = findViewById(R.id.name_wrapper);
        mLocationWrapper = findViewById(R.id.location_wrapper);
        mEditName = findViewById(R.id.edit_name);
        mEditLocation = findViewById(R.id.edit_location);
        mCheckBoxMon = findViewById(R.id.check_box_mon);
        mCheckBoxTues = findViewById(R.id.check_box_tues);
        mCheckBoxWed = findViewById(R.id.check_box_wed);
        mCheckBoxThurs = findViewById(R.id.check_box_thurs);
        mCheckBoxFri = findViewById(R.id.check_box_fri);
        mCheckBoxSat = findViewById(R.id.check_box_sat);
        mCheckBoxSun = findViewById(R.id.check_box_sun);
        mTextViewMon = findViewById(R.id.text_view_mon);
        mTextViewTues = findViewById(R.id.text_view_tues);
        mTextViewWed = findViewById(R.id.text_view_wed);
        mTextViewThurs = findViewById(R.id.text_view_thurs);
        mTextViewFri = findViewById(R.id.text_view_fri);
        mTextViewSat = findViewById(R.id.text_view_sat);
        mTextViewSun = findViewById(R.id.text_view_sun);
    }

    private void updateUI() {
        mEditName.setText(mMuseum.getName());
        mEditLocation.setText(mMuseum.getLocation());
        List<Schedule> schedules = mMuseum.getSchedules();
        String tmp;
        mCheckBoxMon.setChecked(schedules.get(0).isOpen());
        if (mCheckBoxMon.isChecked()) {
            tmp = schedules.get(0).getOpeningTime() + "-" + schedules.get(0).getClosingTime();
            mTextViewMon.setText(tmp);
        }
        mCheckBoxTues.setChecked(schedules.get(1).isOpen());
        if (mCheckBoxTues.isChecked()) {
            tmp = schedules.get(1).getOpeningTime() + "-" + schedules.get(1).getClosingTime();
            mTextViewTues.setText(tmp);
        }
        mCheckBoxWed.setChecked(schedules.get(2).isOpen());
        if (mCheckBoxWed.isChecked()) {
            tmp = schedules.get(2).getOpeningTime() + "-" + schedules.get(2).getClosingTime();
            mTextViewWed.setText(tmp);
        }
        mCheckBoxThurs.setChecked(schedules.get(3).isOpen());
        if (mCheckBoxThurs.isChecked()) {
            tmp = schedules.get(3).getOpeningTime() + "-" + schedules.get(3).getClosingTime();
            mTextViewThurs.setText(tmp);
        }
        mCheckBoxFri.setChecked(schedules.get(4).isOpen());
        if (mCheckBoxFri.isChecked()) {
            tmp = schedules.get(4).getOpeningTime() + "-" + schedules.get(4).getClosingTime();
            mTextViewFri.setText(tmp);
        }
        mCheckBoxSat.setChecked(schedules.get(5).isOpen());
        if (mCheckBoxSat.isChecked()) {
            tmp = schedules.get(5).getOpeningTime() + "-" + schedules.get(5).getClosingTime();
            mTextViewSat.setText(tmp);
        }
        mCheckBoxSun.setChecked(schedules.get(6).isOpen());
        if (mCheckBoxSun.isChecked()) {
            tmp = schedules.get(6).getOpeningTime() + "-" + schedules.get(6).getClosingTime();
            mTextViewSun.setText(tmp);
        }
    }

    private View.OnClickListener checkListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CheckBox checkBox = (CheckBox) v;
            if (checkBox.isChecked()) {
                switch (checkBox.getId()) {
                    case R.id.check_box_mon:
                        showTimePicker(mCheckBoxMon, mTextViewMon);
                        break;
                    case R.id.check_box_tues:
                        showTimePicker(mCheckBoxTues, mTextViewTues);
                        break;
                    case R.id.check_box_wed:
                        showTimePicker(mCheckBoxWed, mTextViewWed);
                        break;
                    case R.id.check_box_thurs:
                        showTimePicker(mCheckBoxThurs, mTextViewThurs);
                        break;
                    case R.id.check_box_fri:
                        showTimePicker(mCheckBoxFri, mTextViewFri);
                        break;
                    case R.id.check_box_sat:
                        showTimePicker(mCheckBoxSat, mTextViewSat);
                        break;
                    case R.id.check_box_sun:
                        showTimePicker(mCheckBoxSun, mTextViewSun);
                        break;
                }
            } else {
                switch (checkBox.getId()) {
                    case R.id.check_box_mon:
                        mTextViewMon.setText("");
                        break;
                    case R.id.check_box_tues:
                        mTextViewTues.setText("");
                        break;
                    case R.id.check_box_wed:
                        mTextViewWed.setText("");
                        break;
                    case R.id.check_box_thurs:
                        mTextViewThurs.setText("");
                        break;
                    case R.id.check_box_fri:
                        mTextViewFri.setText("");
                        break;
                    case R.id.check_box_sat:
                        mTextViewSat.setText("");
                        break;
                    case R.id.check_box_sun:
                        mTextViewSun.setText("");
                        break;
                }
            }
        }
    };

    // Metodo che permette all'utente di scegliere l'orario di apertura e chiusura
    // Se una delle due finestre di dialogo viene chiusa si toglie il check dal CheckBox e si rimuove l'orario dalla TextView
    public void showTimePicker(final CheckBox checkBox, final TextView textView) {
        final Calendar calendar = Calendar.getInstance();
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        TimePickerDialog openingTimePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String openingTime = String.format(Locale.getDefault(),"%02d:%02d", hourOfDay, minute);
                textView.setText(openingTime);
                final TimePickerDialog closingTimePickerDialog = new TimePickerDialog(MuseumActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String closingTime = String.format(Locale.getDefault(),"%02d:%02d", hourOfDay, minute);
                        textView.append("-" + closingTime);
                    }
                }, hourOfDay, minute, true);
                closingTimePickerDialog.setMessage(getString(R.string.closing_time));
                closingTimePickerDialog.show();

                closingTimePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        clearDay(checkBox, textView);
                        dialog.cancel();
                    }
                });
            }
        }, hour, minute, true);
        openingTimePickerDialog.setMessage(getString(R.string.opening_time));
        openingTimePickerDialog.show();

        openingTimePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                clearDay(checkBox, textView);
                dialog.cancel();
            }
        });
    }

    private void clearDay(CheckBox checkBox, TextView textView) {
        checkBox.setChecked(false);
        textView.setText("");
    }

    // Metodo che permette di impostare gli orari di apertura e chiusura del museo
    private List<Schedule> createSchedule() {
        List<Schedule> schedules = new ArrayList<>();
        String[] tmp;
        if (mCheckBoxMon.isChecked()) {
            tmp = mTextViewMon.getText().toString().split("-");
            schedules.add(0, new Schedule(true, tmp[0], tmp[1]));
        } else {
            schedules.add(0, new Schedule(false, null, null));
        }
        if (mCheckBoxTues.isChecked()) {
            tmp = mTextViewTues.getText().toString().split("-");
            schedules.add(1, new Schedule(true, tmp[0], tmp[1]));
        } else {
            schedules.add(1, new Schedule(false, null, null));
        }
        if (mCheckBoxWed.isChecked()) {
            tmp = mTextViewWed.getText().toString().split("-");
            schedules.add(2, new Schedule(true, tmp[0], tmp[1]));
        } else {
            schedules.add(2, new Schedule(false, null, null));
        }
        if (mCheckBoxThurs.isChecked()) {
            tmp = mTextViewThurs.getText().toString().split("-");
            schedules.add(3, new Schedule(true, tmp[0], tmp[1]));
        } else {
            schedules.add(3, new Schedule(false, null, null));
        }
        if (mCheckBoxFri.isChecked()) {
            tmp = mTextViewFri.getText().toString().split("-");
            schedules.add(4, new Schedule(true, tmp[0], tmp[1]));
        } else {
            schedules.add(4, new Schedule(false, null, null));
        }
        if (mCheckBoxSat.isChecked()) {
            tmp = mTextViewSat.getText().toString().split("-");
            schedules.add(5, new Schedule(true, tmp[0], tmp[1]));
        } else {
            schedules.add(5, new Schedule(false, null, null));
        }
        if (mCheckBoxSun.isChecked()) {
            tmp = mTextViewSun.getText().toString().split("-");
            schedules.add(6, new Schedule(true, tmp[0], tmp[1]));
        } else {
            schedules.add(6, new Schedule(false, null, null));
        }
        return schedules;
    }

    // Metodo che permette di ottenere la posizione dal nome del luogo inserito dall'utente
    private List<Address> getLocationFromAddress(String strAddress) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocationName(strAddress, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addresses;
    }

    private void actionDone() {
        mNameWrapper.setError("");
        mLocationWrapper.setError("");

        String name = mEditName.getText().toString().trim();
        String location = mEditLocation.getText().toString().trim();
        // Controlla che sia stato inserito il nome del museo
        if (!TextUtils.isEmpty(name)) {
            // Controlla che sia stato inserito il luogo del museo
            if (!TextUtils.isEmpty(location)) {
                List<Address> addresses = getLocationFromAddress(location);
                if (!addresses.isEmpty()) {
                    Address a = addresses.get(0);
                    double latitude = a.getLatitude();
                    double longitude = a.getLongitude();
                    if (getIntent().hasExtra(Constants.MUSEUM)) {
                        editMuseum(name, location, latitude, longitude);
                    } else {
                        saveMuseum(name, location, latitude, longitude);
                    }
                } else {
                    mLocationWrapper.setError(getString(R.string.place_not_found));
                    mEditLocation.requestFocus();
                }
            } else {
                mLocationWrapper.setError(getString(R.string.location_empty_error_message));
                mEditLocation.requestFocus();
            }
        } else {
            mNameWrapper.setError(getString(R.string.name_empty_error_message));
            mEditName.requestFocus();
        }
    }

    // Metodo che permette di salvare un nuovo museo
    private void saveMuseum(String name, String location, double latitude, double longidute) {
        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection(Constants.MUSEUMS)
                .document();
        String id = docRef.getId();

        Museum museum = new Museum(id, name, location, latitude, longidute, createSchedule());
        docRef
                .set(museum)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Museum saved");
                        Toast.makeText(MuseumActivity.this, getString(R.string.museum_successfully_saved), Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error saving museum", e);
                        Toast.makeText(MuseumActivity.this, getString(R.string.error_message_saving_museum), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Metodo che permette di modificare un museo
    private void editMuseum(String name, String location, double latitude, double longitude) {
        Museum museum = new Museum(mMuseum.getId(), name, location, latitude, longitude, createSchedule());
        FirebaseFirestore
                .getInstance()
                .collection(Constants.MUSEUMS)
                .document(mMuseum.getId())
                .set(museum)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Museum updated");
                        Toast.makeText(MuseumActivity.this, getString(R.string.museum_successfully_updated), Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating museum", e);
                        Toast.makeText(MuseumActivity.this, getString(R.string.error_message_updating_museum), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Se il museo non e' da modificare viene nascosta l'icona che permette di eliminare il museo
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        if (!getIntent().hasExtra(Constants.MUSEUM)) {
            MenuItem actionDelete = menu.findItem(R.id.action_delete);
            actionDelete.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_delete:
                // Controlla che ci sia una connessione ad una rete internet prima di eliminare un museo
                if (Util.isNetworkAvailable(this)) {
                    FirebaseUtil.deleteMuseum(mMuseum.getId());
                    onBackPressed();
                }
                return true;
            case R.id.action_done:
                // Controlla che ci sia una connessione ad una rete internet prima di modificare o salvare un museo
                if (Util.isNetworkAvailable(this)) {
                    actionDone();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mTextViewMon.setText(savedInstanceState.getString(Constants.TEXT_MON));
            mTextViewTues.setText(savedInstanceState.getString(Constants.TEXT_TUES));
            mTextViewWed.setText(savedInstanceState.getString(Constants.TEXT_WED));
            mTextViewThurs.setText(savedInstanceState.getString(Constants.TEXT_THURS));
            mTextViewFri.setText(savedInstanceState.getString(Constants.TEXT_FRI));
            mTextViewSat.setText(savedInstanceState.getString(Constants.TEXT_SAT));
            mTextViewSun.setText(savedInstanceState.getString(Constants.TEXT_SUN));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.TEXT_MON, mTextViewMon.getText().toString());
        outState.putString(Constants.TEXT_TUES, mTextViewTues.getText().toString());
        outState.putString(Constants.TEXT_WED, mTextViewWed.getText().toString());
        outState.putString(Constants.TEXT_THURS, mTextViewThurs.getText().toString());
        outState.putString(Constants.TEXT_FRI, mTextViewFri.getText().toString());
        outState.putString(Constants.TEXT_SAT, mTextViewSat.getText().toString());
        outState.putString(Constants.TEXT_SUN, mTextViewSun.getText().toString());
    }
}
