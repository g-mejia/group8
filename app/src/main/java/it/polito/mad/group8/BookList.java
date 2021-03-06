package it.polito.mad.group8;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class BookList extends AppCompatActivity {

    public final int SIGN_IN = 1000;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private NavigationView mNavigationView;
    private User user;
    private String userID;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.user = new User();
        setContentView(R.layout.activity_book_list);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        mDrawerLayout.addDrawerListener(mToggle);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){
                    case R.id.nav_profile:
                        startProfileActivity();
                        return true;

                    case R.id.logout:
                        signOut();
                        return true;

                    case R.id.sign:
                        signIn();
                        return true;

                    default:
                        mDrawerLayout.closeDrawers();
                }
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUi(FirebaseAuth.getInstance().getCurrentUser());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout,R.string.open,R.string.close);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        updateUi(FirebaseAuth.getInstance().getCurrentUser());

    }

    public void signIn(){
        mDrawerLayout.closeDrawers();
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .build(),
                SIGN_IN);

    }
    public void signOut(){
        mDrawerLayout.closeDrawers();
        AuthUI.getInstance()
                .signOut(BookList.this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUi(FirebaseAuth.getInstance().getCurrentUser());
                    }
                });
    }
    public void startProfileActivity(){
        Intent intent = new Intent(BookList.this,ShowProfile.class);
        finish();
        startActivity(intent);
    }
    public void updateUi(FirebaseUser currentUser){
        if (currentUser != null){
            this.userID = currentUser.getUid();
            mNavigationView.getMenu().clear();
            mNavigationView.inflateMenu(R.menu.menu_drawer_loggedin);
            ref = database.getReference("users/"+this.userID);
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    getData(dataSnapshot);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }else{
            mNavigationView.getMenu().clear();
            mNavigationView.inflateMenu(R.menu.menu_drawer_not_loggedin);
        }
    }

    private void getData(DataSnapshot dataSnapshot){
        if(!dataSnapshot.exists()){
            ref.setValue(this.user);
        }else{
            this.user.setName(dataSnapshot.getValue(User.class).getName());
            this.user.setEmail(dataSnapshot.getValue(User.class).getEmail());
            this.user.setBiography(dataSnapshot.getValue(User.class).getBiography());
            setHeaderDrawer();
        }
    }
    public void setHeaderDrawer(){
        View headerView = mNavigationView.getHeaderView(0);
        TextView name = headerView.findViewById(R.id.header_name);
        TextView email = headerView.findViewById(R.id.header_email);
        CircleImageView image = headerView.findViewById(R.id.header_image);

        name.setText(this.user.getName());
        email.setText(this.user.getEmail());

    }
}

