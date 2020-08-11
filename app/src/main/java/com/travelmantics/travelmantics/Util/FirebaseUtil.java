package com.travelmantics.travelmantics.Util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.travelmantics.travelmantics.ListActivity;
import com.travelmantics.travelmantics.Models.TravelDeal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FirebaseUtil {
    public static final int  RC_SIGN_IN = 123;
    private static FirebaseUtil firebaseUtil;
    public static FirebaseDatabase mFirebaseDatabase;
    public static DatabaseReference mDatabaseReference;
    public static FirebaseAuth mFirebaseAuth;
    public static FirebaseAuth.AuthStateListener mAuthListener;
    public static FirebaseStorage mFirebaseStorage;
    public static StorageReference mStorageReference;
    public static ArrayList<TravelDeal> mDeals;
    public static FirebaseApp firebaseApp;
    private static ListActivity callerActivity;
    public static boolean isAdmin;

    private FirebaseUtil() { }

    public static void getInstance(Context context){
        firebaseApp = FirebaseApp.initializeApp(context);
    }

    public static void openFbReference(String ref , final ListActivity caller){
        if (firebaseUtil == null){
            firebaseUtil = new FirebaseUtil();
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            mFirebaseAuth = FirebaseAuth.getInstance();
            callerActivity = caller;
            mAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if(mFirebaseAuth.getCurrentUser() == null){
                        FirebaseUtil.signIn();
                    }else {
                        String uid = firebaseAuth.getCurrentUser().getUid();
                        checkAdmin(uid);
                    }

                }
            };
            connectStorage();
        }
        mDeals = new ArrayList<>();
        mDatabaseReference = mFirebaseDatabase.getReference().child(ref);
    }

    public static void connectStorage(){
        mFirebaseStorage =FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStorage.getReference().child("deals_pictures");
    }

    private static void checkAdmin(String uid){
        FirebaseUtil.isAdmin = false;
        DatabaseReference ref = mFirebaseDatabase.getReference().child("administrators").child(uid);
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                FirebaseUtil.isAdmin = true;
                callerActivity.showMenu();
                Log.d("Admin", "onChildAdded: You are an Administrator");
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public static void attachListener(){

        if(mFirebaseAuth != null) {
            mFirebaseAuth.addAuthStateListener(mAuthListener);
        }
    }
    public static void detachListener(){
        if(mFirebaseAuth != null){
            mFirebaseAuth.removeAuthStateListener(mAuthListener);
        }

    }

   private static void signIn(){
       List<AuthUI.IdpConfig> providers = Arrays.asList(
               new AuthUI.IdpConfig.EmailBuilder().build(),
               new AuthUI.IdpConfig.GoogleBuilder().build()
       );
       callerActivity.startActivityForResult(
               AuthUI.getInstance()
               .createSignInIntentBuilder()
               .setAvailableProviders(providers)
               .build(), RC_SIGN_IN );
    }

}
