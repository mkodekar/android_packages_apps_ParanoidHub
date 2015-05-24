/*
 * Copyright 2014 ParanoidAndroid Project
 *
 * This file is part of Paranoid OTA.
 *
 * Paranoid OTA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Paranoid OTA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Paranoid OTA.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.paranoid.paranoidhub.cards;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import com.paranoid.paranoidhub.R;
import com.paranoid.paranoidhub.helpers.SettingsHelper;
import com.paranoid.paranoidhub.widget.Card;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

public class SettingsCard extends Card {

    DiscreteSeekBar seekBar;

    public SettingsCard(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super(context, attrs, savedInstanceState);

        setTitle(R.string.settings_checktime_title);
        setLayoutId(R.layout.card_settings);

        seekBar = (DiscreteSeekBar) findViewById(R.id.seekbar);
        if (!showExpanded()) {
            collapse();
        } else {
            super.expand();
        }
        seekBar.setProgress(SettingsHelper.getCheckTime() / 3600 / 1000);
        seekBar.setMin(1);
        seekBar.setMax(48);
        seekBar.setTrackColor(getResources().getColor(R.color.red_900));
        seekBar.setThumbColor(getResources().getColor(R.color.red_400),
                getResources().getColor(R.color.red_900));
        seekBar.setScrubberColor(getResources().getColor(R.color.red_900));
        seekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                SettingsHelper.savePreference(SettingsHelper.PROPERTY_CHECK_TIME,
                        value * 3600 * 1000); //in ms
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
                // ignored
            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                // ignored
            }
        });
    }

    @Override
    public boolean canExpand() {
        return true;
    }

    protected boolean showExpanded() {
        return SettingsHelper.getCheckTime() > 0;
    }

    @Override
    public void expand() {
        super.expand();
        if (seekBar != null) {
            seekBar.setVisibility(View.VISIBLE);
            SettingsHelper.savePreference(SettingsHelper.PROPERTY_CHECK_TIME,
                    SettingsHelper.DEFAULT_CHECK_TIME);
            seekBar.setProgress(SettingsHelper.getCheckTime() / 3600 / 1000);
        }
    }

    @Override
    public void collapse() {
        super.collapse();
        seekBar.setVisibility(View.GONE);
        SettingsHelper.savePreference(SettingsHelper.PROPERTY_CHECK_TIME, -1);
    }

}