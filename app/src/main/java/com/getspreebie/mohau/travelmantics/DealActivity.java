package com.getspreebie.mohau.travelmantics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class DealActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_GET = 1003;
    public static final String IS_NEW_DEAL = "com.getspreebie.mohau.travelmantics.isNewDeal";
    public static final String DEAL_OBJECT = "com.getspreebie.mohau.travelmantics.TravelDeal";

    private EditText editTextTitle;
    private EditText editTextPrice;
    private EditText editTextDescription;
    private DatabaseReference dbReference;
    private FirebaseManager fbManager;

    private String dealId;
    private boolean isNewDeal;
    private TravelDeal travelDeal;
    private FirebaseStorage firebaseStorage;
    private Boolean uploadDealImage = false;
    private ImageView imageViewDeal;
    private ProgressBar progressBarUploadImage;
    private Uri photoUri;
    private String oldImagePath = null;
    private Boolean hasAdminAccess;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);

        scrollView = (ScrollView) findViewById(R.id.scrollViewDealActivity);
        editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        editTextPrice = (EditText) findViewById(R.id.editTextPrice);
        editTextDescription = (EditText) findViewById(R.id.editTextDescription);
        imageViewDeal = (ImageView) findViewById(R.id.imageViewDeal);
        Button buttonPickImage = (Button) findViewById(R.id.buttonPickImage);
        ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.constraintLayoutDealActivity);
        progressBarUploadImage = (ProgressBar) findViewById(R.id.progressBarUploadImage);

        progressBarUploadImage.setVisibility(View.INVISIBLE);

        // Create a click listener for buttonPickImage
        buttonPickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        Settings settings = Settings.getInstance();

        // Retrieve the current access level settings
        hasAdminAccess = settings.getAdminLevelAccess();


        // Disable Views based on the user's access level settings
        if (!hasAdminAccess) {
            editTextTitle.setFocusable(hasAdminAccess);
            editTextTitle.setClickable(hasAdminAccess);
            editTextTitle.setLongClickable(hasAdminAccess);

            editTextPrice.setFocusable(hasAdminAccess);
            editTextPrice.setClickable(hasAdminAccess);
            editTextPrice.setLongClickable(hasAdminAccess);

            editTextDescription.setFocusable(hasAdminAccess);
            editTextDescription.setClickable(hasAdminAccess);
            editTextDescription.setLongClickable(hasAdminAccess);

            buttonPickImage.setVisibility(View.INVISIBLE);
        }

        // Initialize objects
        fbManager = FirebaseManager.getInstance();
        dbReference = fbManager.getDatabaseReference("traveldeals");
        firebaseStorage = FirebaseStorage.getInstance();

        Intent intent = getIntent();
        isNewDeal = intent.getBooleanExtra(IS_NEW_DEAL, true);

        if (!isNewDeal) {
            // Populate data
            travelDeal = intent.getParcelableExtra(DEAL_OBJECT);

            dealId = travelDeal.getId();

            editTextTitle.setText(travelDeal.getTitle());
            editTextTitle.setSelection(editTextTitle.getText().length()); // this line enables the cursor to be at the end of the text

            editTextPrice.setText(travelDeal.getPrice());
            editTextPrice.setSelection(editTextPrice.getText().length()); // this line enables the cursor to be at the end of the text

            editTextDescription.setText(travelDeal.getDescription());
            editTextDescription.setSelection(editTextDescription.getText().length()); // this line enables the cursor to be at the end of the text

            // Retrieve the the Deal's image from a Settings object
            Drawable drawable = settings.getDrawable();
            imageViewDeal.setImageDrawable(drawable);

            oldImagePath = travelDeal.getImagePath();

            // Prevents the focus from being in one of the EditTexts (which inadvertently triggers the keyboard)
            constraintLayout.setFocusableInTouchMode(true);


//            String imagePath = travelDeal.getImagePath();
//            downloadImage(imagePath);
        } else {
            // Start with an empty layout and create a new deal
            setTitle("Add New Deal");
        }

    }

    private void downloadImage(String path) {
        if (path != null) {
            StorageReference pathReference =
                    firebaseStorage.getReference().child(path);

            pathReference.getDownloadUrl().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("download_test", "Error: "+e.getLocalizedMessage());
                }
            }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get()
                            .load(uri)
                            .placeholder(R.drawable.ic_photo_black_24dp)
                            .into(imageViewDeal);
                }
            });
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem saveMenuItem = menu.findItem(R.id.save_menu_item);
        MenuItem deleteMenuItem = menu.findItem(R.id.delete_menu_item);

        // Hide/Show menu items based on a user's access level privileges
        if (!hasAdminAccess) {
            saveMenuItem.setVisible(false);
            deleteMenuItem.setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_menu_item:
                saveDeal();
                return true;
            case R.id.delete_menu_item:
                deleteDeal();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
//            Bitmap thumbnail = data.getParcelable("data");
            photoUri = data.getData();

            if (photoUri != null) {

                uploadDealImage = true;
                imageViewDeal.setImageURI(photoUri);
            }
        }
    }



    private void deleteDeal() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Do you want to delete this Deal?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Delete the record
                        dbReference.child(dealId).removeValue();

                        /*
                        // Delete the image associated with the just deleted record
                        if (oldImagePath != null) {
                            StorageReference deleteReference = firebaseStorage.getReference().child(oldImagePath);
                            deleteReference.delete();
                        }
                        */

                        Toast.makeText(DealActivity.this, "Deal deleted successfully!", Toast.LENGTH_LONG).show();
                        onBackPressed();
                    }
                }).setNegativeButton("No", null);

        alertDialogBuilder.show();
    }

    private void saveDeal() {
        String title = editTextTitle.getText().toString().trim();
        String price = editTextPrice.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        if (hasPassedValidation(title, price)) {
            // Re-position the ScrollView towards the top in order for the ProgressBar to
            // appear on the screen when Saving...
            scrollView.scrollTo(0, scrollView.getTop());
            progressBarUploadImage.setVisibility(View.VISIBLE);

            if (isNewDeal) {
                // Create a new record

                travelDeal = new TravelDeal(title, price, description);

                dbReference.push().setValue(travelDeal, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        dealId = databaseReference.getKey();
                        travelDeal.setId(dealId);

                        if (uploadDealImage) {
                            uploadImage();
                        } else {
                            progressBarUploadImage.setVisibility(View.INVISIBLE);
                            Toast.makeText(DealActivity.this, "Deal added successfully!", Toast.LENGTH_LONG).show();
                            onBackPressed();
                        }
                    }
                });

            } else {
                // Edit an existing record

                travelDeal.setTitle(title);
                travelDeal.setDescription(description);
                travelDeal.setPrice(price);

                dbReference.child(dealId).setValue(travelDeal).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (uploadDealImage) {
                            uploadImage();
                        } else {
                            progressBarUploadImage.setVisibility(View.INVISIBLE);
                            Toast.makeText(DealActivity.this, "Deal updated successfully!", Toast.LENGTH_LONG).show();
                            onBackPressed();
                        }
                    }
                });
            }
        }
    }

    private boolean hasPassedValidation(String title, String price) {
        if (title.isEmpty()) {
            Toast.makeText(DealActivity.this, "Please fill in the Title!", Toast.LENGTH_LONG).show();
            editTextTitle.requestFocus();
            return false;
        }

        if (price.isEmpty()) {
            Toast.makeText(DealActivity.this, "Please fill in the Price!", Toast.LENGTH_LONG).show();
            editTextPrice.requestFocus();
            return false;
        }

        return true;
    }

    private void uploadImage() {
        if (photoUri != null) {
            // Create a storage reference to the folder path
            StorageReference storageReference =
                    firebaseStorage.getReference().child("deals_pictures/"+photoUri.getLastPathSegment());

            UploadTask uploadTask = storageReference.putFile(photoUri);

            // Register observers to to listen for whe the download is done or if it fails
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBarUploadImage.setVisibility(View.INVISIBLE);

                    String messageText = "Upload Error: \n" + e.getLocalizedMessage();
                    Toast.makeText(DealActivity.this, messageText, Toast.LENGTH_LONG).show();
                    onBackPressed();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressBarUploadImage.setVisibility(View.INVISIBLE);

                    // Save the path of the stored image to enable its retrieval later on

                    String path = taskSnapshot.getMetadata().getPath();
                    travelDeal.setImagePath(path);
                    dbReference.child(dealId).setValue(travelDeal);


                    /*
                    // Delete the old image if there ever was one
                    if (oldImagePath != null) {
                        StorageReference deleteReference = firebaseStorage.getReference().child(oldImagePath);
                        deleteReference.delete();
                    }
                    */


                    String messageText;
                    if (isNewDeal) {
                        messageText = "Deal Added Successfully!";
                    } else {
                        messageText = "Deal Updated Successfully!";
                    }
                    Toast.makeText(DealActivity.this, messageText, Toast.LENGTH_LONG).show();
                    onBackPressed();
                }
            });
        } else {
            progressBarUploadImage.setVisibility(View.INVISIBLE);
            onBackPressed();
        }
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET);
        }
    }
}
