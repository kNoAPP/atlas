package com.knoban.atlas.data.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * A medium for accessing Firebase.
 * @author Alden Bansemer (kNoAPP)
 */
public class AtlasFirebase {

    private FirebaseApp app;

    // Best for large data that's written once and read many times. (Charged $ based on calls)
    // Documents and collections.
    // This can be as flat or as deep as needed. Best scaling long term.
    private Firestore firestore;

    // Best for smaller data that's written and read many times. (Charged $ based on network transferred data size)
    // Basically a giant JSON object.
    // Keep this structure as flat as possible.
    private FirebaseDatabase database;

    /**
     * Creates a AtlasFirebase for use within your plugin.
     * @param databaseURL The URL of your Google Firebase application. Example: {@code https://<DATABASE_NAME>.firebaseio.com/}
     * @param privateKey The service key's file path for your account.
     * See https://console.firebase.google.com/project/_/settings/serviceaccounts/adminsdk?authuser=1
     * @throws IOException If the url/privateKey/authentication failed
     */
    public AtlasFirebase(String databaseURL, File privateKey) throws IOException {
        FileInputStream serviceAccount = new FileInputStream(privateKey);

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl(databaseURL)
                .build();
        this.app = FirebaseApp.initializeApp(options);

        this.firestore = FirestoreClient.getFirestore(app);
        this.database = FirebaseDatabase.getInstance(app);
    }

    public Firestore getFirestore() {
        return firestore;
    }

    public FirebaseDatabase getDatabase() {
        return database;
    }
}