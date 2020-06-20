package io.praveen.safescore;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    EditText mEmail, mPassword;
    Button mSignin, mRegister;
    ProgressBar pb;
    Intent i1, i2;
    boolean b = false;
    double lat = 0, lon = 0;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        i1 = new Intent(LoginActivity.this, MainFragment.class);
        i2 = new Intent(LoginActivity.this, DetailsActivity.class);
        mEmail = findViewById(R.id.login_email);
        mPassword = findViewById(R.id.login_password);
        pb = findViewById(R.id.pb);
        pb.setVisibility(View.INVISIBLE);
        mSignin = findViewById(R.id.login_signin);
        mRegister = findViewById(R.id.login_register);
        mSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissions()){
                    String email = mEmail.getText().toString();
                    String password = mPassword.getText().toString();
                    if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        if (password.length() > 5){
                            pb.setVisibility(View.VISIBLE);
                            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){
                                        mUser = mAuth.getCurrentUser();
                                        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(mUser.getEmail().replaceAll("\\.", ",")).child("Common Details");
                                        final DatabaseReference rLat = ref.child("Latitude");
                                        final DatabaseReference rOut = ref.child("Time Out");
                                        final DatabaseReference rBack = ref.child("Time Back");
                                        final DatabaseReference rNumber = ref.child("Emergency Mobile");
                                        final DatabaseReference rLon = ref.child("Longitude");
                                        final SharedPreferences.Editor editor = preferences.edit();
                                        rLat.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                lat = dataSnapshot.getValue(Double.class);
                                                editor.putFloat("lat", (float) lat);
                                                rLon.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        lon = dataSnapshot.getValue(Double.class);
                                                        editor.putFloat("lon", (float) lon);
                                                        rBack.addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                editor.putInt("back", dataSnapshot.getValue(Integer.class));
                                                                rOut.addValueEventListener(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                        editor.putInt("out", dataSnapshot.getValue(Integer.class));
                                                                                rNumber.addValueEventListener(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                        editor.putString("number", dataSnapshot.getValue(String.class));
                                                                                        editor.apply();
                                                                                        pb.setVisibility(View.INVISIBLE);
                                                                                        startActivity(i1);
                                                                                        finish();
                                                                                    }
                                                                                    @Override
                                                                                    public void onCancelled(@NonNull DatabaseError error) {}
                                                                                });
                                                                            }
                                                                            @Override
                                                                            public void onCancelled(@NonNull DatabaseError error) {}
                                                                        });
                                                                    }
                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {}
                                                                });
                                                            }
                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {}
                                                        });
                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {}
                                                });
                                    } else{
                                        pb.setVisibility(View.INVISIBLE);
                                        Toast.makeText(LoginActivity.this, "Check the inputs and try again!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else Toast.makeText(LoginActivity.this, "Passowrd length must be greater than 6!", Toast.LENGTH_SHORT).show();
                    } else Toast.makeText(LoginActivity.this, "Invalid email !", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissions()){
                    String email = mEmail.getText().toString();
                    String password = mPassword.getText().toString();
                    if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        if (password.length() > 5) {
                            pb.setVisibility(View.VISIBLE);
                            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    pb.setVisibility(View.INVISIBLE);
                                    if (task.isSuccessful()){
                                        mUser = mAuth.getCurrentUser();
                                        startActivity(i2);
                                        finish();
                                    } else Toast.makeText(LoginActivity.this, "Email not registered", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else Toast.makeText(LoginActivity.this, "Passowrd length must be greater than 6!", Toast.LENGTH_SHORT).show();
                    } else Toast.makeText(LoginActivity.this, "Invalid email !", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        ActivityCompat.requestPermissions(LoginActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CALL_LOG, Manifest.permission.BATTERY_STATS}, 11);
        ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.BATTERY_STATS}, 12);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainFragment.class));
            finish();
        }
    }

    public boolean permissions(){
        ActivityCompat.requestPermissions(LoginActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CALL_LOG}, 11);
        return b;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 11: {
                b = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                b = grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED;
                b = grantResults.length > 0 && grantResults[2] == PackageManager.PERMISSION_GRANTED;
                b = grantResults.length > 0 && grantResults[3] == PackageManager.PERMISSION_GRANTED;
                b = grantResults.length > 0 && grantResults[4] == PackageManager.PERMISSION_GRANTED;
                if (!b) Toast.makeText(LoginActivity.this, "You must accept all permission requests to continue!", Toast.LENGTH_SHORT).show();
            }
                
            case 12: {
               b = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
               // if (!b) Toast.makeText(LoginActivity.this, "You must accept  all permission requests to continue!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
