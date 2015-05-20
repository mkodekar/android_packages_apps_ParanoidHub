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
import android.content.res.Resources;
import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.TextView;

import com.paranoid.paranoidhub.R;
import com.paranoid.paranoidhub.updater.RomUpdater;
import com.paranoid.paranoidhub.utils.Utils;
import com.paranoid.paranoidhub.widget.Card;

public class SystemCard extends Card {

    public SystemCard(Context context, AttributeSet attrs, RomUpdater romUpdater,
                      Bundle savedInstanceState) {
        super(context, attrs, savedInstanceState);

        setTitle(R.string.system_title);
        setLayoutId(R.layout.card_system);

        Resources res = context.getResources();

        TextView deviceView = (TextView) findLayoutViewById(R.id.device);
        deviceView.setText(Utils.getDeviceInfo());

        TextView dateView = (TextView) findLayoutViewById(R.id.date);
        dateView.setText(res.getString(R.string.released) + " " + Utils.getReadableDate(romUpdater.getVersion().getDate()));
    }

    @Override
    public boolean canExpand() {
        return false;
    }

}