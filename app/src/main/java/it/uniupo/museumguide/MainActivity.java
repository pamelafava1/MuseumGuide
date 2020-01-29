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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import it.uniupo.museumguide.adapters.MuseumAdapter;
import it.uniupo.museumguide.models.Museum;
import it.uniupo.museumguide.util.Constants;
import it.uniupo.museumguide.util.Util;

// Activity che mostra tutti i musei
public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private List<Museum> mDataset;
    private boolean isUpdated;
    private SwipeRefreshLayout mRefreshLayout;
    private ListView mListView;
    private ProgressBar mProgressBar;
    private FloatingActionButton mBtnAddMuseum;
    private MuseumAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mDataset = savedInstanceState.getParcelableArrayList(Constants.DATASET);
            isUpdated = savedInstanceState.getBoolean(Constants.IS_UPDATED);
        } else {
            mDataset = new ArrayList<>();
            isUpdated = false;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (mAuth.getCurrentUser() == null) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        mRefreshLayout = findViewById(R.id.refresh_layout);
        mListView = findViewById(R.id.list_view);
        mProgressBar = findViewById(R.id.progress_bar);
        mBtnAddMuseum = findViewById(R.id.btn_add_museum);

        mAdapter = new MuseumAdapter(this, R.layout.museum_item, mDataset);
        mListView.setAdapter(mAdapter);

        mRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent));

        mRefreshLayout.setOnRefreshListener(refreshListener);
        mListView.setOnItemClickListener(itemClickListener);
        mListView.setOnItemLongClickListener(itemLongClickListener);
        mBtnAddMuseum.setOnClickListener(btnAddMuseumClickListener);
    }

    SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (Util.isNetworkAvailable(MainActivity.this) && !isUpdated) {
                retrieveMuseums();
            }
            mRefreshLayout.setRefreshing(false);
        }
    };

    // Se l'utente clicca un elemento della lista viene fatta partire l'Activity contenente la lista di sale inerenti al museo cliccato
    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(MainActivity.this, RoomListActivity.class);
            intent.putExtra(Constants.ID_MUSEUM, mDataset.get(position).getId());
            startActivity(intent);
        }
    };

    // Se l'utente clicca a lungo un elemento della lista viene fatta partire L'Activity che permette di modificare o eliminare un museo
    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Museum museum = mDataset.get(position);
            Intent intent = new Intent(MainActivity.this, MuseumActivity.class);
            intent.putExtra(Constants.MUSEUM, museum);
            startActivity(intent);
            return true;
        }
    };

    // Se l'utente clicca il bottone viene fatta partire l'Activity, la stessa utilizzata per la modifica e per l'eliminazione, che permette di aggiungere un nuovo museo
    View.OnClickListener btnAddMuseumClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(MainActivity.this, MuseumActivity.class));
        }
    };

    // Permette di recuperare i musei
    private void retrieveMuseums() {
        isUpdated = true;
        mProgressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database
                .collection(Constants.MUSEUMS)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        mProgressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                for (QueryDocumentSnapshot q : task.getResult()) {
                                    Museum museum = q.toObject(Museum.class);
                                    mDataset.add(museum);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_map:
                startActivity(new Intent(this, MapsActivity.class));
                return true;
            case R.id.action_logout:
                mAuth.signOut();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        // Se i musei non sono ancora stati caricati e c'e' una connessione ad una rete internet viene recuperata la lista di musei
        if (!isUpdated && Util.isNetworkAvailable(this)) {
            retrieveMuseums();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(Constants.DATASET, (ArrayList<? extends Parcelable>) mDataset);
        outState.putBoolean(Constants.IS_UPDATED, isUpdated);
    }
}
