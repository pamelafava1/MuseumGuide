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

import it.uniupo.museumguide.adapters.ObjectAdapter;
import it.uniupo.museumguide.models.Object;
import it.uniupo.museumguide.util.Constants;
import it.uniupo.museumguide.util.Util;

// Activity che mostra tutti gli oggetti di una sala
public class ObjectListActivity extends AppCompatActivity {

    private List<Object> mDataset;
    private boolean isUpdated;
    private String idRoom;
    private SwipeRefreshLayout mRefreshLayout;
    private GridView mGridView;
    private ProgressBar mProgressBar;
    private FloatingActionButton mBtnAddObject;
    private ObjectAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_list);

        if (savedInstanceState != null) {
            mDataset = savedInstanceState.getParcelableArrayList(Constants.DATASET);
            isUpdated = savedInstanceState.getBoolean(Constants.IS_UPDATED);
        } else {
            mDataset = new ArrayList<>();
            isUpdated = false;
        }

        idRoom = getIntent().getStringExtra(Constants.ID_ROOM);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.object_list));
        }

        mRefreshLayout = findViewById(R.id.refresh_layout);
        mGridView = findViewById(R.id.grid_view);
        mProgressBar = findViewById(R.id.progress_bar);
        mBtnAddObject = findViewById(R.id.btn_add_object);

        mAdapter = new ObjectAdapter(this, R.layout.object_item, mDataset);
        mGridView.setAdapter(mAdapter);

        mRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent));

        mRefreshLayout.setOnRefreshListener(refreshListener);
        mGridView.setOnItemClickListener(itemClickListener);
        mGridView.setOnItemLongClickListener(itemLongClickListener);
        mBtnAddObject.setOnClickListener(btnAddObjectClickListener);
    }

    private SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (Util.isNetworkAvailable(ObjectListActivity.this) && !isUpdated) {
                retrieveObjects();
            }
            mRefreshLayout.setRefreshing(false);
        }
    };

    // Se l'utente clicca un elemento della lista viene fatta partire l'Activity che mostra i dettagli dell'oggetto
    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(ObjectListActivity.this, ObjectDetailsActivity.class);
            intent.putExtra(Constants.ID_ROOM, idRoom);
            intent.putExtra(Constants.OBJECT, mDataset.get(position));
            startActivity(intent);
        }
    };

    // Se l'utente clicca a lungo un elemento della lista viene fatta partire l'Activity che permette di modificare o eliminare un oggetto
    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(ObjectListActivity.this, ObjectActivity.class);
            intent.putExtra(Constants.ID_ROOM, idRoom);
            intent.putExtra(Constants.OBJECT, mDataset.get(position));
            startActivity(intent);
            return true;
        }
    };

    // Se l'utente clicca il bottone viene fatta partire l'Activity, la stessa utilizzata per la modifica e per l'eliminazione, che permette di aggiungere un nuovo oggetto
    private View.OnClickListener btnAddObjectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ObjectListActivity.this, ObjectActivity.class);
            intent.putExtra(Constants.ID_ROOM, idRoom);
            startActivity(intent);
        }
    };

    // Metodo che permette di recuperare gli oggetti della sala
    private void retrieveObjects() {
        isUpdated = true;
        mProgressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database
                .collection(Constants.OBJECTS)
                .whereEqualTo(Constants.ID_ROOM, idRoom)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        mProgressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                for (QueryDocumentSnapshot q : task.getResult()) {
                                    Object object = q.toObject(Object.class);
                                    mDataset.add(object);
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
        // Se gli oggetti non sono ancora stati caricati e c'e' una connessione ad una rete internet viene recuperata la lista di oggetti
        if (!isUpdated && Util.isNetworkAvailable(this)) {
            retrieveObjects();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(Constants.DATASET, (ArrayList<? extends Parcelable>) mDataset);
        outState.putBoolean(Constants.IS_UPDATED, isUpdated);
    }
}
