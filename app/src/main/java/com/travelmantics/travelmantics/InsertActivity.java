package com.travelmantics.travelmantics;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.travelmantics.travelmantics.Models.TravelDeal;
import com.travelmantics.travelmantics.Util.FirebaseUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;


public class InsertActivity extends AppCompatActivity{

    private final int REQUEST_CODE = 42;
    private final int  MY_PERMISSIONS_REQUEST_READ_STORAGE = 2000;
    private EditText deal_title;
    private EditText deal_price;
    private EditText deal_description;
    private Button deal_image_btn;
    private ImageView deal_image;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private TextInputLayout deal_title_layout;
    private TextInputLayout deal_price_layout;
    private TextInputLayout deal_description_layout;
    private TravelDeal deal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FirebaseUtil.openFbReference("traveldeals", null);
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;

        deal_title_layout = (TextInputLayout) findViewById(R.id.title_layout);
        deal_price_layout = (TextInputLayout) findViewById(R.id.price_layout);
        deal_description_layout = (TextInputLayout) findViewById(R.id.description_layout);
        deal_title = (EditText) findViewById(R.id.deal_title);
        deal_description = (EditText) findViewById(R.id.deal_description);
        deal_price = (EditText) findViewById(R.id.deal_price);
        deal_image = (ImageView) findViewById(R.id.deal_image);
        deal_image_btn = (Button) findViewById(R.id.deal_image_btn);

        deal_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(InsertActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(InsertActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                   } else {
                        ActivityCompat.requestPermissions(InsertActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_STORAGE);
                    }
                } else {

                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/jpeg");
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                    startActivityForResult(Intent.createChooser(intent, "InsertPicture"), REQUEST_CODE);

                }
            }
        });



        Intent receivedIntent = getIntent();
        if(receivedIntent != null){
            TravelDeal clickedDeal = (TravelDeal) receivedIntent.getParcelableExtra("Deal");
            if (clickedDeal == null) {
                clickedDeal = new TravelDeal();
            }
            this.deal = clickedDeal;
            deal_title.setText(deal.getTitle());
            deal_price.setText(deal.getPrice());
            deal_description.setText(deal.getDescription());
            showImage(deal.getImage());
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/jpeg");
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                    startActivityForResult(Intent.createChooser(intent, "InsertPicture"), REQUEST_CODE);
                }

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if(FirebaseUtil.isAdmin){
            menu.findItem(R.id.action_save).setVisible(true);
            menu.findItem(R.id.action_delete).setVisible(true);
            deal_image_btn.setVisibility(View.VISIBLE);
            enableEditTexts(true);
        }else{
            menu.findItem(R.id.action_save).setVisible(false);
            menu.findItem(R.id.action_delete).setVisible(false);
            deal_image_btn.setVisibility(View.GONE);
            enableEditTexts(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_save:
                saveDeal();
                return true;
            case R.id.action_delete:
                deleteDeal();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void saveDeal() {
        String title = deal_title.getText().toString();
        String price = deal_price.getText().toString();
        String description = deal_description.getText().toString();

        deal.setTitle(title);
        deal.setPrice(price);
        deal.setDescription(description);

        if (title.isEmpty()) {
            //        Toast.makeText(this,"Please Enter a title",Toast.LENGTH_SHORT).show();
            deal_title_layout.setErrorEnabled(true);
            deal_title_layout.setError("Please enter a Title");
        } else {
            deal_title_layout.setError("");
            deal_title_layout.setErrorEnabled(false);
            deal_price_layout.requestFocus();
        }
        if (price.isEmpty()) {
            //       Toast.makeText(this,"Please Enter a price",Toast.LENGTH_SHORT).show();
            deal_price_layout.setErrorEnabled(true);
            deal_price_layout.setError("Please enter a Price");
        } else {
            deal_price_layout.setError("");
            deal_price_layout.setErrorEnabled(false);
            deal_description_layout.requestFocus();
        }
        if (description.isEmpty()) {
            //        Toast.makeText(this,"Please Enter a Description",Toast.LENGTH_SHORT).show();
            deal_description_layout.setErrorEnabled(true);
            deal_description_layout.setError("please Enter a Description");
        } else {
            deal_description_layout.setError("");
            deal_description_layout.setErrorEnabled(false);
        }
        if (!title.isEmpty() && !price.isEmpty() && !description.isEmpty()) {
            if (deal.getId() == null) {
                mDatabaseReference.push().setValue(deal)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Snackbar.make(deal_title_layout, "Deal Saved Sucessfully", Snackbar.LENGTH_SHORT).show();
                                backToList();
                                clean();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(deal_title_layout, "Deal was not Saved Sucessfully", Snackbar.LENGTH_SHORT).show();
                    }
                });
            } else {
                mDatabaseReference.child(deal.getId()).setValue(deal)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Snackbar.make(deal_title_layout, "Deal Updated Sucessfully", Snackbar.LENGTH_SHORT).show();
                                backToList();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(deal_title_layout, "Deal was not Updated Sucessfully", Snackbar.LENGTH_SHORT).show();
                    }
                });
            }

        }
    }

    private void deleteDeal() {
        if (deal.getId() == null) {
            Snackbar.make(deal_title_layout, "Please save deal before deleting", Snackbar.LENGTH_SHORT).show();
        }else {
            if (deal.getImageName() != null && !deal.getImageName().isEmpty()) {
                StorageReference picRef = FirebaseUtil.mFirebaseStorage.getReference().child(deal.getImageName());
                picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Delete Image", "onSuccess: Image Deleted Successfully");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Delete Image", "onFailure: Image Delete Failed");
                    }
                });
            }
            mDatabaseReference.child(deal.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Snackbar.make(deal_title_layout, "Deal Deleted Sucessfully", Snackbar.LENGTH_SHORT).show();
                    backToList();
                }
            });
        }
    }

    private void backToList() {
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void showImage(String url){
        if(url != null && !url.isEmpty()){

            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.with(this)
                    .load(url)
                    .resize(width,width*2/3)
                    .centerCrop()
                    .into(deal_image);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
     //   super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            if(imageUri != null) {
                final StorageReference ref = FirebaseUtil.mStorageReference.child(imageUri.getLastPathSegment());
                UploadTask uploadTask = ref.putFile(imageUri);
                Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return ref.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            String downloadUri = task.getResult().toString();
                            deal.setImage(downloadUri);
                            deal.setImageName(ref.getName());
                            showImage(downloadUri);
                        }
                    }
                });
            }
        }
    }
    private void enableEditTexts(boolean isEnabled){
        deal_title.setEnabled(isEnabled);
        deal_price.setEnabled(isEnabled);
        deal_description.setEnabled(isEnabled);
    }

    private void clean() {
        deal_title.setText("");
        deal_price.setText("");
        deal_description.setText("");
    }
}
