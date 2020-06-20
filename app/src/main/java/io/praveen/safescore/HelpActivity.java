package io.praveen.safescore;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class HelpActivity extends Fragment {

    SharedPreferences sp;
    Button police, parent, us, scream;

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_help, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        police = view.findViewById(R.id.call_police);
        parent = view.findViewById(R.id.call_parents);
        us = view.findViewById(R.id.call_dev);
        scream = view.findViewById(R.id.scream);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final String mNumber = sp.getString("number", "");
        police.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", "100", null)));
            }
        });
        parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", mNumber, null)));
            }
        });
        us.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(),"We're not available at the moment, Call Police instead!", Toast.LENGTH_LONG).show();
            }
        });
        scream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MediaPlayer mp2 = MediaPlayer.create(getActivity(), R.raw.raw);
                mp2.setLooping(true);
                AudioManager am = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
                am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
                scream.setText("CLICK AGAIN TO SCREAM OUT LOUD");
                scream.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View v) {
                        if(mp2.isPlaying()) {
                            mp2.pause();
                            scream.setText("CLICK TO SCREAM OUT LOUD");
                        } else {
                            mp2.start();
                            scream.setText("CLICK AGAIN TO TURN OFF");
                        }
                    }
                });
            }
        });

    }

}
