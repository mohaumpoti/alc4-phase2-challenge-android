package com.getspreebie.mohau.travelmantics;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class DealListActivity extends AppCompatActivity {

    private Boolean isFirstChild = true;
    private FirebaseManager fbManager;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private ProgressBar progressBarLoadDealList;
    private Boolean isDataDisplayed = false;
    public static final int RC_SIGN_IN = 1024;
    private static final String ADMIN_ACCESS = "Admin Access";
    private static final String READ_ONLY_ACCESS = "Read-only Access";
    private String userDisplayName = "";
    private String userEmail = "";
    private Uri userPhotoUrl = null;
    private TextView textViewUserDisplayName;
    private TextView textViewEmail;
    private CircleImageView imageViewUserProfilePic;
    private TextView textViewAccessLevel;
    private FloatingActionButton fabAddNewDeal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textViewUserDisplayName = (TextView) findViewById(R.id.textViewDisplayName);
        textViewEmail = (TextView) findViewById(R.id.textViewEmail);
        imageViewUserProfilePic = (CircleImageView) findViewById(R.id.imageViewProfilePic);
        fabAddNewDeal = (FloatingActionButton) findViewById(R.id.fab);
        progressBarLoadDealList = (ProgressBar) findViewById(R.id.progressBarLoadDealList);
        progressBarLoadDealList.setVisibility(View.INVISIBLE);
        Switch switchSetAdminAccess = (Switch) findViewById(R.id.switchSetAdminAccess);
        textViewAccessLevel = (TextView) findViewById(R.id.textViewAccessLevel);


        final Settings settings = Settings.getInstance();

        // Configure the Switch to display the initial value of the adminLevelAccess
        Boolean hasAdminAccess = settings.getAdminLevelAccess();
        switchSetAdminAccess.setChecked(hasAdminAccess);
        toggleScreenSettings(hasAdminAccess);

        // Attach a setOnCheckedChangeListener to toggle the access level privileges between 'Read-only' and 'Admin'
        switchSetAdminAccess.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                settings.setAdminLevelAccess(checked);
                toggleScreenSettings(checked);
            }
        });


        fbManager = FirebaseManager.getInstance();

        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {

                    // Choose authentication providers
                    List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.EmailBuilder().build(),
                            new AuthUI.IdpConfig.GoogleBuilder().build());

                    // Create and launch sign-in intent
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(providers)
                                    .build(), RC_SIGN_IN
                    );
                } else {
                    userDisplayName = (user.getDisplayName() == null) ? "" : user.getDisplayName();
                    userEmail = user.getEmail();
                    userPhotoUrl = user.getPhotoUrl();
                    if (!isDataDisplayed) {
                        isDataDisplayed = true;
                        displayTravelDealData();
                    }
                }
            }

        };

//        FloatingActionButton fab = findViewById(R.id.fab);
        fabAddNewDeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

                // Click to add a NEW deal
                Intent intent = new Intent(DealListActivity.this, DealActivity.class);
                intent.putExtra(DealActivity.IS_NEW_DEAL, true);

                startActivity(intent);
            }
        });
    }

    private void toggleScreenSettings(Boolean hasAdminAccess) {
        if (hasAdminAccess) {
            textViewAccessLevel.setText(ADMIN_ACCESS);
            fabAddNewDeal.setEnabled(true);
        } else {
            textViewAccessLevel.setText(READ_ONLY_ACCESS);
            fabAddNewDeal.setEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem saveMenuItem = menu.findItem(R.id.save_menu_item);
        saveMenuItem.setVisible(false);

        MenuItem deleteMenuItem = menu.findItem(R.id.delete_menu_item);
        deleteMenuItem.setVisible(false);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.logout_menu_item) {
            // Log the user out of the application
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {

                            String messageText = "Bye for now!";
                            Toast.makeText(DealListActivity.this, messageText, Toast.LENGTH_SHORT).show();
                        }
                    });
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                userDisplayName = (user == null) ? "" : user.getDisplayName();
                userEmail = (user == null) ? "" : user.getEmail();
                userPhotoUrl = (user == null) ? null : user.getPhotoUrl();

                if (!isDataDisplayed) {
                    isDataDisplayed = true;
                    displayTravelDealData();
                }
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }



    private void displayTravelDealData() {

        // Display personalized user data on the screen

        // display the user's name
        if (!userDisplayName.isEmpty()) {
            textViewUserDisplayName.setText(userDisplayName);
        }

        // display the user's email
        if (!userEmail.isEmpty()) {
            textViewEmail.setText(userEmail);
        }

        // display the user's profile pic
        Picasso.get()
                .load(userPhotoUrl)
                .placeholder(R.drawable.ic_account_circle_black_24dp)
                .into(imageViewUserProfilePic);

        DatabaseReference dbReference = fbManager.getDatabaseReference("traveldeals");

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerViewDeals);

        final DealAdapter adapter = new DealAdapter(this, null);
        recyclerView.setAdapter(adapter);

        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        progressBarLoadDealList.setVisibility(View.VISIBLE);

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                progressBarLoadDealList.setVisibility(View.INVISIBLE);

                TravelDeal deal = dataSnapshot.getValue(TravelDeal.class);

                if (isFirstChild) { fbManager.resetDealList(); }

                if (deal != null) {
                    deal.setId(dataSnapshot.getKey());
                    fbManager.addDeal(deal);
                    adapter.updateList(fbManager.getDeals());
                }

                isFirstChild = false;
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                progressBarLoadDealList.setVisibility(View.INVISIBLE);

                TravelDeal deal = dataSnapshot.getValue(TravelDeal.class);

                if (deal != null) {
                    fbManager.updateDeal(deal);
                    adapter.updateList(fbManager.getDeals());
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                progressBarLoadDealList.setVisibility(View.INVISIBLE);

                TravelDeal deal = dataSnapshot.getValue(TravelDeal.class);

                if (deal != null) {
                    String id = dataSnapshot.getKey();
                    deal.setId(id);
                    fbManager.removeDeal(deal.getId());
                    adapter.updateList(fbManager.getDeals());
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                progressBarLoadDealList.setVisibility(View.INVISIBLE);

                /*
                TravelDeal deal = dataSnapshot.getValue(TravelDeal.class);

                if (deal != null) {
                    adapter.updateList(fbManager.getDeals());
                }
                */

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBarLoadDealList.setVisibility(View.INVISIBLE);

            }
        };
        dbReference.addChildEventListener(childEventListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
