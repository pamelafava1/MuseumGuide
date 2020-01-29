package it.uniupo.museumguide;

import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import it.uniupo.museumguide.adapters.RoomAdapter;
import it.uniupo.museumguide.models.Room;
import it.uniupo.museumguide.util.Constants;
import it.uniupo.museumguide.util.Util;

// Activity che mostra tutte le sale di un museo
public class RoomListActivity extends AppCompatActivity {

    private List<Room> mDataset;
    private boolean isUpdated;
    private String idMuseum;
    private SwipeRefreshLayout mRefreshLayout;
    private GridView mGridView;
    private ProgressBar mProgressBar;
    private FloatingActionButton mBtnAddRoom;
    private RoomAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_list);

        if (savedInstanceState != null) {
            mDataset = savedInstanceState.getParcelableArrayList(Constants.DATASET);
            isUpdated = savedInstanceState.getBoolean(Constants.IS_UPDATED);
        } else {
            mDataset = new ArrayList<>();
            isUpdated = false;
        }

        idMuseum = getIntent().getStringExtra(Constants.ID_MUSEUM);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.room_list));
        }

        mRefreshLayout = findViewById(R.id.refresh_layout);
        mGridView = findViewById(R.id.grid_view);
        mProgressBar = findViewById(R.id.progress_bar);
        mBtnAddRoom = findViewById(R.id.btn_add_room);

        mAdapter = new RoomAdapter(this, R.layout.room_item, mDataset);
        mGridView.setAdapter(mAdapter);

        mRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent));

        mRefreshLayout.setOnRefreshListener(refreshListener);
        mGridView.setOnItemClickListener(itemClickListener);
        mGridView.setOnItemLongClickListener(itemLongClickListener);
        mBtnAddRoom.setOnClickListener(btnAddRoomClickListener);
    }

    private SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (Util.isNetworkAvailable(RoomListActivity.this) && !isUpdated) {
                retrieveRooms();
            }
            mRefreshLayout.setRefreshing(false);
        }
    };

    // Se l'utente clicca un elemento della lista viene fatta partire l'Activity contenente la lista di oggetti inerenti alla sala cliccata
    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(RoomListActivity.this, ObjectListActivity.class);
            intent.putExtra(Constants.ID_ROOM, mDataset.get(position).getId());
            startActivity(intent);
        }
    };

    // Se l'utente clicca a lungo un elemento della lista viene fatta partire l'Activity che permette di modificare o eliminare una sala
    private AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(RoomListActivity.this, RoomActivity.class);
            intent.putExtra(Constants.ID_MUSEUM, idMuseum);
            intent.putExtra(Constants.ROOM, mDataset.get(position));
            startActivity(intent);
            return true;
        }
    };

    // Se l'utente clicca il bottone viene fatta partire l'Activity, la stessa utilizzata per la modifica e per l'eliminazione, che permette di aggiungere un nuova sala
    private View.OnClickListener btnAddRoomClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(RoomListActivity.this, RoomActivity.class);
            intent.putExtra(Constants.ID_MUSEUM, idMuseum);
            startActivity(intent);
        }
    };

    // Metodo che permette di recuperare le sale del museo
    private void retrieveRooms() {
        isUpdated = true;
        mProgressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database
                .collection(Constants.ROOMS)
                .whereEqualTo(Constants.ID_MUSEUM, idMuseum)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        mProgressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                for (QueryDocumentSnapshot q : task.getResult()) {
                                    Room room = q.toObject(Room.class);
                                    mDataset.add(room);
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        } else {
                            isUpdated = false;
                        }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Se le sale non sono ancora state caricate e c'e' una connessione ad una rete internet viene recuperata la lista delle sale
        if (!isUpdated && Util.isNetworkAvailable(this)) {
            retrieveRooms();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(Constants.DATASET, (ArrayList<? extends Parcelable>) mDataset);
        outState.putBoolean(Constants.IS_UPDATED, isUpdated);
    }
}
