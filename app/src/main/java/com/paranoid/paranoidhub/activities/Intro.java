package com.paranoid.paranoidhub.activities;

import android.content.Intent;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro;
import com.paranoid.paranoidhub.R;
import com.paranoid.paranoidhub.slides.FirstSlide;

public class Intro extends AppIntro {
    @Override
    public void init(Bundle savedInstanceState) {
        addSlide(new FirstSlide(), getApplicationContext());

        setBarColor(getResources().getColor(R.color.intro_navi));
        setSeparatorColor(getResources().getColor(R.color.card_separator));
        showSkipButton(true);
    }

    private void loadMainActivity() {
        Intent intent = new Intent(this, HubActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSkipPressed() {
        loadMainActivity();
    }

    @Override
    public void onDonePressed() {
        loadMainActivity();
    }
}
