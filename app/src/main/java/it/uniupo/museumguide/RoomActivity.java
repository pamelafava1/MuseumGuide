package it.uniupo.museumguide;

import android.content.Intent;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import it.uniupo.museumguide.models.Room;
import it.uniupo.museumguide.util.Constants;
import it.uniupo.museumguide.util.FirebaseUtil;
import it.uniupo.museumguide.util.Util;

// Activity che permette di salvare, modificare o eliminare una sala
public class RoomActivity extends AppCompatActivity {

    private String idMuseum;
    private Room mRoom;
    private TextInputLayout mNameWrapper;
    private EditText mEditName;
    private Button mBtnDone;
    private ProgressBar mProgressBar;
    private static final String TAG = "RoomActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        mNameWrapper = findViewById(R.id.name_wrapper);
        mEditName = findViewById(R.id.edit_name);
        mBtnDone = findViewById(R.id.btn_done);
        mProgressBar = findViewById(R.id.progress_bar);

        idMuseum = getIntent().getStringExtra(Constants.ID_MUSEUM);
        if (getIntent().hasExtra(Constants.ROOM)) {
            mRoom = getIntent().getParcelableExtra(Constants.ROOM);
            mEditName.setText(mRoom.getName());
        }

        mBtnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionDone();
            }
        });
    }

    // Metodo che permette di salvare una sala
    private void saveRoom(String name) {
        mProgressBar.setVisibility(View.VISIBLE);
        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection(Constants.ROOMS)
                .document();
        String id = docRef.getId();
        Room room = new Room(id, idMuseum, name);
        docRef
                .set(room)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mProgressBar.setVisibility(View.GONE);
                        Log.d(TAG, "Room saved");
                        Toast.makeText(RoomActivity.this, getString(R.string.room_successfully_saved), Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgressBar.setVisibility(View.GONE);
                        Log.w(TAG, "Error saving room", e);
                        Toast.makeText(RoomActivity.this, getString(R.string.error_message_saving_room), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Metodo che permette di modificare una sala
    private void editRoom(String name) {
        mProgressBar.setVisibility(View.VISIBLE);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(Constants.NAME, name);
        FirebaseFirestore.getInstance()
                .collection(Constants.ROOMS)
                .document(mRoom.getId())
                .update(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mProgressBar.setVisibility(View.GONE);
                        Log.d(TAG, "Room updated");
                        Toast.makeText(RoomActivity.this, getString(R.string.room_successfully_updated), Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgressBar.setVisibility(View.GONE);
                        Log.w(TAG, "Error updating room", e);
                        Toast.makeText(RoomActivity.this, getString(R.string.error_message_updating_room), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void actionDone() {
        mNameWrapper.setError("");

        String name = mEditName.getText().toString().trim();
        // Prima di modificare o salvare una sala si verifica che l'utente abbia inserito il nome della sala
        if (!TextUtils.isEmpty(name)) {
            if (getIntent().hasExtra(Constants.ROOM)) {
                editRoom(name);
            } else {
                saveRoom(name);
            }
        } else {
            mNameWrapper.setError(getString(R.string.name_empty_error_message));
            mEditName.requestFocus();
        }
    }

    // Se la sala non e' da modificare viene nascosta l'icona che permette di eliminare la sala
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        if (getIntent().hasExtra(Constants.ROOM)) {
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
            // Controlla che ci sia una connessione ad una rete internet prima di eliminare una sala
            case R.id.action_delete:
                if (Util.isNetworkAvailable(this)) {
                    FirebaseUtil.deleteRoom(mRoom.getId());
                    onBackPressed();
                }
                return true;
            case R.id.action_done:
                // Controlla che ci sia una connessione ad una rete internet prima di modificare o salvare una sala
                if (Util.isNetworkAvailable(this)) {
                    actionDone();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, RoomListActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.ID_MUSEUM, idMuseum);
        startActivity(intent);
    }
}
