package it.uniupo.museumguide;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import it.uniupo.museumguide.models.Object;
import it.uniupo.museumguide.util.Constants;
import it.uniupo.museumguide.util.FirebaseUtil;
import it.uniupo.museumguide.util.PhotoUtil;
import it.uniupo.museumguide.util.Util;

// Activity che permette di salvare, modificare o eliminare un oggetto
public class ObjectActivity extends AppCompatActivity {

    private String idRoom;
    private Object mObject;
    private TextInputLayout mNameWrapper;
    private EditText mEditName, mEditDescription;
    private ImageButton mBtnAddImage;
    private Button mBtnSpeak;
    private ProgressBar mProgressBar;
    private Uri mImageUri;
    private byte[] mImage;
    private static final int REQUEST_CAPTURE_IMAGE = 1000;
    private static final int REQUEST_GALLERY_IMAGE = 1001;
    private static final int REQUEST_SPEECH_INPUT = 1002;
    private static final String TAG = "ObjectActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        mNameWrapper = findViewById(R.id.name_wrapper);
        mEditName = findViewById(R.id.edit_name);
        mEditDescription = findViewById(R.id.edit_description);
        mBtnAddImage = findViewById(R.id.btn_add_image);
        mBtnSpeak = findViewById(R.id.btn_speak);
        mProgressBar = findViewById(R.id.progress_bar);

        if (savedInstanceState == null) {
            idRoom = getIntent().getStringExtra(Constants.ID_ROOM);
            if (getIntent().hasExtra(Constants.OBJECT)) {
                mObject = getIntent().getParcelableExtra(Constants.OBJECT);
                mEditName.setText(mObject.getName());
                mEditDescription.setText(mObject.getDescription());
                downloadImage();
            }
        }

        mBtnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceInput();
            }
        });
        mBtnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePicker();
            }
        });
    }

    private void updateImageButton() {
        if (!this.isFinishing()) {
            Glide
                    .with(ObjectActivity.this)
                    .load(mImageUri)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mBtnAddImage);
        }
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
                        updateImageButton();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgressBar.setVisibility(View.GONE);
                        Log.w(TAG, "Error downloading image", e);
                        Toast.makeText(ObjectActivity.this, getString(R.string.error_message_downloading_image), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // AlertDialog che permette all'utente di scegliere se scattare una foto oppure scegliere un'immagine dalla galleria del dispositivo
    private void imagePicker() {
        final String[] items = {getString(R.string.camera), getString(R.string.gallery)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle(getString(R.string.upload_from))
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (items[which].equals(getString(R.string.camera))) {
                            if (Util.checkPermission(ObjectActivity.this, Manifest.permission.CAMERA) && Util.checkPermission(ObjectActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                captureImage();
                            } else {
                                ActivityCompat.requestPermissions(ObjectActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAPTURE_IMAGE);
                            }
                        } else if (items[which].equals(getString(R.string.gallery))) {
                            if (Util.checkPermission(ObjectActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                pickImage();
                            } else {
                                ActivityCompat.requestPermissions(ObjectActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_GALLERY_IMAGE);
                            }
                        }
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Metodo che permette di scegliere un'immagine dalla galleria del dispositivo
    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY_IMAGE);
    }

    // Metodo che permette di acquisire una foto utilizzando l'app della fotocamera del dispositivo
    private void captureImage() {
        ContentValues values = new ContentValues();
        mImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        startActivityForResult(intent, REQUEST_CAPTURE_IMAGE);
    }

    // Metodo che permette di convertire l'audio in testo (Speech to Text)
    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speak_message));
        try {
            startActivityForResult(intent, REQUEST_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            a.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CAPTURE_IMAGE:
                    handleResult();
                    break;
                case REQUEST_GALLERY_IMAGE:
                    if(data != null) {
                        mImageUri = data.getData();
                        handleResult();
                    }
                    break;
                case REQUEST_SPEECH_INPUT:
                    if (data != null) {
                        ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        mEditDescription.setText(result.get(0));
                    }
                    break;
            }
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, getString(R.string.cancelled), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleResult() {
        try {
            Bitmap bitmap = PhotoUtil.setPic(this, mImageUri);
            mImage = PhotoUtil.getBytesFromBitmap(bitmap);
            mBtnAddImage.setImageBitmap(PhotoUtil.getBitmapFromBytes(mImage));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAPTURE_IMAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    captureImage();
                }
                break;
            case REQUEST_GALLERY_IMAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImage();
                }
        }
    }

    private void closeKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void actionDone() {
        closeKeyboard();
        mNameWrapper.setError("");

        String name = mEditName.getText().toString().trim();
        String description = mEditDescription.getText().toString().trim();
        // Controlla che sia stato inserito il nome dell'oggetto
        if (!TextUtils.isEmpty(name)) {
            if (getIntent().hasExtra(Constants.OBJECT)) {
                editObject(name, description);
            } else {
                saveObject(name, description);
            }
        } else {
            mNameWrapper.setError(getString(R.string.name_empty_error_message));
            mEditName.requestFocus();
        }
    }

    // Metodo che permette di salvare un nuovo oggetto
    private void saveObject(final String name, String description) {
        mProgressBar.setVisibility(View.VISIBLE);
        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection(Constants.OBJECTS)
                .document();
        final String id = docRef.getId();

        String image = null;
        if (mImage != null) {
            image = (id + "." + getFileExtension());
        }
        Object object = new Object(id, idRoom, name, image, description);
        docRef
                .set(object)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mProgressBar.setVisibility(View.GONE);
                        Log.d(TAG, "Object saved");
                        updloadImage(id);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgressBar.setVisibility(View.GONE);
                        Log.w(TAG, "Error saving object", e);
                        Toast.makeText(ObjectActivity.this, getString(R.string.error_message_saving_object), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Metodo che permette di modificare un oggetto gia' esistente
    private void editObject(String name, String description) {
        mProgressBar.setVisibility(View.VISIBLE);

        HashMap<String, java.lang.Object> hashMap = new HashMap<>();
        hashMap.put(Constants.NAME, name);
        if (mImage != null) {
            hashMap.put(Constants.IMAGE, (mObject.getId() + "." + getFileExtension()));
        }
        hashMap.put(Constants.DESCRIPTION, description);

        FirebaseFirestore.getInstance()
                .collection(Constants.OBJECTS)
                .document(mObject.getId())
                .update(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mProgressBar.setVisibility(View.GONE);
                        Log.d(TAG, "Object updated");
                        updloadImage(mObject.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Error updating Object", e);
                        Toast.makeText(ObjectActivity.this, getString(R.string.error_message_updating_object), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getFileExtension() {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(mImageUri));
    }

    // Metodo che permette di salvare un'immagine
    private void updloadImage(String id) {
        if (mImage != null) {
            mProgressBar.setVisibility(View.VISIBLE);
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(Constants.UPLOADS);
            StorageReference fileReference = storageReference.child(id + "." + getFileExtension());
            fileReference
                    .putBytes(mImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mProgressBar.setVisibility(View.GONE);
                            Log.d(TAG, "Image uploaded");
                            Toast.makeText(ObjectActivity.this, getString(R.string.object_successfully_saved), Toast.LENGTH_SHORT).show();
                            onBackPressed();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mProgressBar.setVisibility(View.GONE);
                            Log.w(TAG, "Error uploading image", e);
                            Toast.makeText(ObjectActivity.this, getString(R.string.error_message_uploading_image), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            onBackPressed();
        }
    }

    // Se l'oggetto non e' da modificare viene nascosta l'icona che permette di eliminarlo
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        if (!getIntent().hasExtra(Constants.OBJECT)) {
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
                if (Util.isNetworkAvailable(this)) {
                    // Controlla che ci sia una connessione ad una rete internet prima di eliminare un oggetto
                    FirebaseUtil.deleteImage(mObject.getImage());
                    FirebaseUtil.deleteObject(mObject.getId());
                    onBackPressed();
                }
                return true;
            case R.id.action_done:
                // Controlla che ci sia una connessione ad una rete internet prima di modificare o salvare un oggetto
                if (Util.isNetworkAvailable(this)) {
                    actionDone();
                }
                return true;
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
        outState.putString(Constants.ID_ROOM, idRoom);
        outState.putParcelable(Constants.OBJECT, mObject);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mImageUri = savedInstanceState.getParcelable(Constants.IMAGE_URI);
            idRoom = savedInstanceState.getString(Constants.ID_ROOM);
            mObject = savedInstanceState.getParcelable(Constants.OBJECT);
            if (mImageUri != null) {
                updateImageButton();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProgressBar.setVisibility(View.GONE);
    }
}
