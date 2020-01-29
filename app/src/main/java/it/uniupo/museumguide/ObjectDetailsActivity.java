package it.uniupo.museumguide;

import android.content.Intent;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Locale;

import it.uniupo.museumguide.models.Object;
import it.uniupo.museumguide.util.Constants;
import it.uniupo.museumguide.util.FirebaseUtil;

public class ObjectDetailsActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    private String idRoom;
    private Object mObject;
    private TextView mTextDescription;
    private ImageView mImageView;
    private Button mBtnSpeech;
    private ProgressBar mProgressBar;
    private Uri mImageUri;
    private TextToSpeech mTextToSpeech;
    private static final String TAG = "ObjectDetailsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTextDescription = findViewById(R.id.object_description);
        mImageView = findViewById(R.id.object_image);
        mBtnSpeech = findViewById(R.id.btn_speech);
        mProgressBar = findViewById(R.id.progress_bar);

        idRoom = getIntent().getStringExtra(Constants.ID_ROOM);
        if (getIntent().hasExtra(Constants.OBJECT)) {
            mObject = getIntent().getParcelableExtra(Constants.OBJECT);
            if (!TextUtils.isEmpty(mObject.getDescription())) {
                String description = getString(R.string.description) + "\n" + mObject.getDescription();
                mTextDescription.setText(description);
                mBtnSpeech.setVisibility(View.VISIBLE);
            } else {
                mTextDescription.setText(getString(R.string.no_description_available));
            }
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(mObject.getName());
        }

        if (savedInstanceState == null) {
            if (mObject.getImage() != null) {
                downloadImage();
            }
        }

        mTextToSpeech = new TextToSpeech(this, this);

        mBtnSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textToSpeak();
            }
        });
    }

    private void downloadImage() {
        Task<Uri> uriTask = FirebaseUtil.downloadImage(mObject.getImage());
        uriTask
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        mProgressBar.setVisibility(View.GONE);
                        Log.d(TAG, "Image downloaded");
                        mImageUri = uri;
                        FirebaseUtil.updateImageView(ObjectDetailsActivity.this, mImageUri, mImageView);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgressBar.setVisibility(View.GONE);
                        Log.w(TAG, "Error downloading image", e);
                        Toast.makeText(ObjectDetailsActivity.this, getString(R.string.error_message_downloading_image), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = mTextToSpeech.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "This language is not supported");
            } else {
                mBtnSpeech.setEnabled(true);
            }
        } else {
            Log.e(TAG, "Failed to initialize");
        }
    }

    private void textToSpeak() {
        String text = mTextDescription.getText().toString();
        mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ObjectListActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.ID_ROOM, idRoom);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.IMAGE_URI, mImageUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mImageUri = savedInstanceState.getParcelable(Constants.IMAGE_URI);
            if (mImageUri != null) {
                FirebaseUtil.updateImageView(this, mImageUri, mImageView);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
        }
    }
}
