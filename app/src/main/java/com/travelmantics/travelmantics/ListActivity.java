package com.travelmantics.travelmantics;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.travelmantics.travelmantics.Adapter.ListDealAdapter;
import com.travelmantics.travelmantics.Interface.AdapterRefresh;
import com.travelmantics.travelmantics.Interface.DataAvailability;
import com.travelmantics.travelmantics.Interface.GotoListActivity;
import com.travelmantics.travelmantics.Models.TravelDeal;
import com.travelmantics.travelmantics.Util.FirebaseUtil;
import com.travelmantics.travelmantics.Util.NetworkUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity implements GotoListActivity, DataAvailability {

    ArrayList<TravelDeal> deals;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChhildEventListener;
    private ListDealAdapter listDealAdapter;
    private RecyclerView deal_list_rv;

    private Toolbar toolbar;
    private ProgressBar progressBarList;
    private View emptyView;
    private MenuItem newDeal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FirebaseUtil.getInstance(getApplicationContext());
        if(FirebaseUtil.isAdmin){
            showMenu();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseUtil.isAdmin){
            showMenu();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.deal_list,menu);
        newDeal = menu.findItem(R.id.new_travel_deal);

        if(FirebaseUtil.isAdmin){
            newDeal.setVisible(true);
        }else{
            newDeal.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.new_travel_deal:
                Intent newIntent = new Intent(ListActivity.this,InsertActivity.class);
                startActivity(newIntent);
                return true;
            case R.id.logout:
                AuthUI.getInstance()
                        .signOut(ListActivity.this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(ListActivity.this,"Logout Successfully",Toast.LENGTH_SHORT).show();
                                FirebaseUtil.attachListener();
                            }
                        });
                FirebaseUtil.detachListener();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void logout(){
        AuthUI.getInstance()
                .signOut(ListActivity.this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(ListActivity.this,"Logout Successfully",Toast.LENGTH_SHORT).show();
                        FirebaseUtil.attachListener();
                    }
                });
        FirebaseUtil.detachListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUtil.detachListener();

    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUtil.openFbReference("traveldeals",ListActivity.this);
        FirebaseUtil.attachListener();

        listDealAdapter = new ListDealAdapter(ListActivity.this,this,this);
        deal_list_rv = (RecyclerView) findViewById(R.id.deals_list_rv);
        progressBarList = (ProgressBar) findViewById(R.id.progressBarList);
        emptyView = (View) findViewById(R.id.network_view);
        deal_list_rv.setLayoutManager(new LinearLayoutManager(this));
        deal_list_rv.setHasFixedSize(true);
        deal_list_rv.setAdapter(listDealAdapter);
        itemCheck();
    }

    public void refresh(View view){
        itemCheck();
    }

    public void itemCheck(){
        if(FirebaseUtil.mDeals.size() > 0 ){
            progressBarList.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
        }else if(FirebaseUtil.mDeals.size() == 0 && !NetworkUtil.NetworkAvailable(ListActivity.this)){
                progressBarList.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
        }else{
                progressBarList.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            }

    }


    public void showMenu(){
        invalidateOptionsMenu();
    }


    @Override
    public void editTravelDeal(TravelDeal selectedDeal) {
        Intent intent = new Intent(this, InsertActivity.class);
        intent.putExtra("Deal",selectedDeal);
        startActivity(intent);
    }


}
