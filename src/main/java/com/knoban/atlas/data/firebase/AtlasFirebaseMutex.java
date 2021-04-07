package com.knoban.atlas.data.firebase;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.*;
import com.google.common.util.concurrent.MoreExecutors;
import com.knoban.atlas.callbacks.GenericCallback1;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

/**
 * A mutex for any Firestore Document.
 * <br><br>
 * Have you ever needed to sync some data between multiple programs accessing a Firebase document? Are transactions not
 * good enough to keep those pesky race conditions out? Behold! A AtlasFirebaseMutex. A mutex is basically like a talking
 * stick. Whoever beholds the talking stick may talk. Everyone else must remain silent. That way, no one talks over each
 * other. If you want to talk, you may politely raise your hand. When the person with the stick is done talking, they may
 * give it to you. Then you can talk.
 * <br><br>
 * A mutex is like that, but with data. Anyone holding the mutex can write/read data to the Firebase document. Everyone
 * else may only read, not write. This class lets you do the following:
 * 1. Raise your hand to get the mutex. You're placed in a queue to obtain it.
 * 2. Obtain the mutex and start writing data.
 * 3. Release the mutex and stop writing data.
 * 4. Be a Karen. Cut the line. Steal the mutex. Tell everyone else to piss off. And they will, because they're not Karens.
 * <br><br>
 * Note: This class will provide you with the functionality to create/manage a mutex, but it will not enforce the mutex
 * on Firestore calls. You are required to implement that behaviour yourself using the help from this abstraction.
 * @author Alden Bansemer (kNoAPP)
 */
public abstract class AtlasFirebaseMutex {

    protected final DocumentReference firestoreReference;
    private final Firestore firestore;
    private final Logger logger;

    protected UUID thisMutex;
    private ListenerRegistration mutexListener;
    private Thread timeout;

    /**
     * Create your talking stick (the mutex). Provide the mutex with your Firestore, the reference to the Document, and
     * a Logger for printing errors.
     * @param firestore The firestore instance
     * @param firestoreReference The document you want to keep in sync
     * @param logger A logger for printing debug and errors
     */
    public AtlasFirebaseMutex(@NotNull Firestore firestore, @NotNull DocumentReference firestoreReference, @NotNull Logger logger) {
        this.firestore = firestore;
        this.firestoreReference = firestoreReference;
        this.logger = logger;
    }

    /**
     * Raise your hand for the talking stick (the mutex). Get in line. You're trying to get permission to speak.
     * @param callback Callback with true if you got in line. False if you couldn't get in line. Callback occurs on
     *                 another thread.
     */
    protected void addMutex(@NotNull GenericCallback1<Boolean> callback) {
        if(thisMutex != null)
            return;

        thisMutex = UUID.randomUUID();

        ApiFuture<Void> future = firestore.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(firestoreReference).get();
            ArrayList<String> mutex = (ArrayList<String>) snapshot.get("mutex");
            if(mutex == null)
                mutex = new ArrayList<>();
            mutex.add(thisMutex.toString());
            Map<String, Object> map = new TreeMap<>();
            map.put("mutex", mutex);
            transaction.set(firestoreReference, map, SetOptions.merge());
            return null;
        });

        ApiFutures.addCallback(future, new ApiFutureCallback<Void>() {
            @Override
            public void onFailure(Throwable t) {
                logger.warning("Failed to create mutex: " + t.getMessage());
                callback.call(false);
            }

            @Override
            public void onSuccess(Void result) {
                callback.call(true);
            }
        }, MoreExecutors.directExecutor());
    }

    /**
     * Unleash your inner Karen. Cut the line. Instantly obtain the mutex. Everyone else waiting on it will be promptly
     * told to piss off. Anyone waiting for the mutex will be told they can't have it. They will need to rejoin the
     * line again.
     */
    protected void clearMutex() {
        if(thisMutex == null)
            return;

        firestore.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(firestoreReference).get();
            ArrayList<String> mutex = (ArrayList<String>) snapshot.get("mutex");
            if(mutex == null)
                mutex = new ArrayList<>();
            mutex.clear();
            mutex.add(thisMutex.toString());
            Map<String, Object> map = new TreeMap<>();
            map.put("mutex", mutex);
            transaction.set(firestoreReference, map, SetOptions.merge());
            return null;
        });
    }

    /**
     * Return the mutex or/and leave the line. You no longer need the talking stick.
     * @param block Set to true if you want to wait for confirmation that you returned the mutex and/or left the line.
     */
    protected void removeMutex(boolean block) {
        if(thisMutex == null)
            return;

        ApiFuture<Void> future = firestore.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(firestoreReference).get();
            ArrayList<String> mutex = (ArrayList<String>) snapshot.get("mutex");
            if(mutex == null)
                mutex = new ArrayList<>();
            mutex.remove(thisMutex.toString());
            Map<String, Object> map = new TreeMap<>();
            map.put("mutex", mutex);
            transaction.set(firestoreReference, map, SetOptions.merge());
            return null;
        });

        if(block) {
            try {
                future.get();
                thisMutex = null;
            } catch(InterruptedException | ExecutionException e) {
                logger.warning("Failed to remove mutex: " + e.getMessage());
                thisMutex = null;
            }
        } else {
            ApiFutures.addCallback(future, new ApiFutureCallback<Void>() {
                @Override
                public void onFailure(Throwable t) {
                    logger.warning("Failed to remove mutex: " + t.getMessage());
                    thisMutex = null;
                }

                @Override
                public void onSuccess(Void result) {
                    thisMutex = null;
                }
            }, MoreExecutors.directExecutor());
        }
    }

    /**
     * Wait in line for the talking stick (the mutex). You can also be rudely impatient, waiting only a certain amount
     * of time before becoming a Karen and cutting the line. Or you can be politely impatient, waiting only a certain
     * amount of time before leaving the line without the mutex. You'll be told when you've either obtained the mutex,
     * or been unsuccessful trying to do so.
     * @param timeoutMillis The maximum amount of time to wait before cutting the line (set to -1 for infinite time,
     *                      if you're a nice person)
     * @param karen If the timeout is reached: If passed true, cut the line as a Karen stealing the mutex. If passed
     *              false, politely leave the line without the mutex.
     * @param callback Callback is fired when the mutex has been obtained (true), or the mutex will never be obtained
     *                 (false). Callback occurs on another thread.
     */
    protected void listenToMutex(long timeoutMillis, boolean karen, @NotNull GenericCallback1<Boolean> callback) {
        if(thisMutex == null)
            return;

        this.timeout = new Thread(() -> {
            try {
                Thread.sleep(timeoutMillis);
            } catch(InterruptedException e) {
                return;
            }

            mutexListener.remove();
            if(karen) {
                logger.warning("A mutex has been force-loaded. Possible data inconsistencies may now occur.");
                clearMutex();
                callback.call(true);
            } else {
                removeMutex(false);
                callback.call(false);
            }
        });

        this.mutexListener = firestoreReference.addSnapshotListener((value, error) -> {
            if(error != null) {
                logger.warning("Failed to listen for mutex: " + error.getMessage());
                mutexListener.remove();
                timeout.interrupt();
                removeMutex(false); // Try to clean up if possible.
                callback.call(false);
                return;
            }

            if(value != null) {
                Map<String, Object> data = value.getData();
                ArrayList<String> mutex = (ArrayList<String>) data.get("mutex");
                if(mutex == null || thisMutex == null || !mutex.contains(thisMutex.toString())) {
                    mutexListener.remove();
                    timeout.interrupt();
                    callback.call(false);
                    return;
                }

                if(mutex.get(0).equals(thisMutex.toString())) {
                    timeout.interrupt();
                    mutexListener.remove();
                    callback.call(true);
                    return;
                }
            }

            // This lets us check once for our mutex before starting the timer.
            // Preferable because we don't risk unnecessarily clearing the mutex.
            // Don't use a hammer for a screw. Use a screwdriver.
            if(timeoutMillis >= 0 && timeout.getState() == Thread.State.NEW)
                timeout.start();
        });
    }
}
