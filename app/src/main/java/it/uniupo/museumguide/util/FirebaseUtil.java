package it.uniupo.museumguide.util;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import it.uniupo.museumguide.models.Object;
import it.uniupo.museumguide.models.Room;

public class FirebaseUtil {

    private static final String TAG = "FirebaseUtil";

    // Metodo che permette di eliminare un museo
    public static void deleteMuseum(String idMuseum) {
        final FirebaseFirestore database = FirebaseFirestore.getInstance();
        database
                .collection(Constants.ROOMS)
                .whereEqualTo(Constants.ID_MUSEUM, idMuseum)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                for (QueryDocumentSnapshot q : task.getResult()) {
                                    Room room = q.toObject(Room.class);
                                    deleteRoom(room.getId());
                                }
                            }
                        }
                    }
                });
        database
                .collection(Constants.MUSEUMS)
                .document(idMuseum)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Museum deleted");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting museum", e);
                    }
                });
    }

    // Metodo che permette di eliminare una sala
    public static void deleteRoom(String idRoom) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database
                .collection(Constants.OBJECTS)
                .whereEqualTo(Constants.ID_ROOM, idRoom)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                for (QueryDocumentSnapshot q : task.getResult()) {
                                    Object object = q.toObject(Object.class);
                                    deleteObject(object.getId());
                                }
                            }
                        }
                    }
                });
        database
                .collection(Constants.ROOMS)
                .document(idRoom)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Room deleted");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting room", e);
                    }
                });
    }

    // Metodo che permette di eliminare un oggetto
    public static void deleteObject(String idObject) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database
                .collection(Constants.OBJECTS)
                .document(idObject)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Object deleted");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting object", e);
                    }
                });
    }

    // Metodo che permette di eliminare un'immagine
    public static void deleteImage(String image) {
        if (image != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(Constants.UPLOADS);
            storageReference
                    .child(image)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Image deleted");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error deleting image", e);
                        }
                    });
        }
    }

    public static Task<Uri> downloadImage(String image) {
        if (image != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(Constants.UPLOADS);
            return storageReference
                    .child(image)
                    .getDownloadUrl();
        }
        return null;
    }
}
