package io.praveen.safescore;

import android.annotation.SuppressLint;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class ZoneActivity extends Fragment{

    SharedPreferences sp;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    ImageView locate1;
    EditText cur;
    RadioGroup radioGroup1;
    RadioGroup radioGroup2;
    RadioGroup radioGroup3;
    RadioGroup radioGroup4;
    RadioGroup radioGroup5;
    Button button;
    double lat, lon;
    int time;
    int pf;
    int pol;
    int bar;
    int res;
    Intent i;

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_zone, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        cur = view.findViewById(R.id.location);
        locate1 = view.findViewById(R.id.locate);
        radioGroup1=view.findViewById(R.id.radiogroup1);
        radioGroup2=view.findViewById(R.id.radiogroup2);
        radioGroup3=view.findViewById(R.id.radiogroup3);
        radioGroup4=view.findViewById(R.id.radiogroup4);
        radioGroup5=view.findViewById(R.id.radiogroup5);
        button=view.findViewById(R.id.details_submit);

        locate1.setOnClickListener(new View.OnClickListener() {
            @SuppressLint({"MissingPermission", "SetTextI18n"})
            @Override
            public void onClick(View v) {
                LocationManager locationManager = (LocationManager) Objects.requireNonNull(getActivity()).getSystemService(Context.LOCATION_SERVICE);
                Location location = null;
                if (locationManager != null)
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {
                    lat = location.getLatitude();
                    lon = location.getLongitude();
                    cur.setText(lat + ", " + lon);
                } else cur.setHint("Turn on GPS and try again!");
            }
        });

        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch(i){
                    case R.id.morning:
                        time=1;
                        break;
                    case R.id.afternoon:
                        time=2;
                        break;
                    case R.id.evening:
                        time=3;
                        break;
                    case R.id.night:
                        time=4;
                        break;
                }
            }
        });
        radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch(i){
                    case R.id.pf1:
                        pf=1;
                        break;
                    case R.id.pf2:
                        pf=2;
                        break;
                    case R.id.pf3:
                        pf=3;
                        break;
                }
            }
        });
        radioGroup3.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch(i){
                    case R.id.pol1:
                        pol=1;
                        break;
                    case R.id.pol2:
                        pol=0;
                        break;
                }
            }
        });
        radioGroup4.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch(i){
                    case R.id.bar1:
                        bar=1;
                        break;
                    case R.id.bar2:
                        bar=0;
                        break;
                }
            }
        });
        radioGroup5.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch(i){
                    case R.id.res1:
                        res=1;
                        break;
                    case R.id.res2:
                        res=2;
                        break;
                    case R.id.res3:
                        res=3;
                        break;
                }
            }
        });

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(mUser.getEmail()).replaceAll("\\.", ",")).child("Zone Details");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button.setText("PLEASE WAIT");
                Toast.makeText(getContext(),"time selected "+time+" pf selected "+pf+" pol selected "+pol+" bar selected "+bar+" res selected "+res, Toast.LENGTH_LONG).show();
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().build();
                mUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        final DatabaseReference dLat=database.child("Latitude");
                        final DatabaseReference dLon=database.child("Longitude");
                        final DatabaseReference dTime=database.child("Time");
                        final DatabaseReference dPf=database.child("Frequency");
                        final DatabaseReference dPol =database.child("Police");
                        final DatabaseReference dBar =database.child("Bar");
                        final DatabaseReference dRes =database.child("Residence");
                        dLat.setValue(lat).addOnCompleteListener( new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                dLon.setValue(lon).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        dTime.setValue(time).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                dPf.setValue(pf).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        dPol.setValue(pol).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                dBar.setValue(bar).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        dRes.setValue(res);
                                                                    }
                                                                });
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }
}
