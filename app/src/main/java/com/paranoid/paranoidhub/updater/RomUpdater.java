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

package com.paranoid.paranoidhub.updater;

import android.content.Context;

import com.paranoid.paranoidhub.R;
import com.paranoid.paranoidhub.updater.server.PaServer;
import com.paranoid.paranoidhub.utils.OTAUtils;
import com.paranoid.paranoidhub.utils.Version;

public class RomUpdater extends Updater {

    public RomUpdater(Context context, boolean fromAlarm) {
        super(context, new Server[]{
                new PaServer()}, fromAlarm);
    }

    public static String getVersionString(Context context) {
        return getDevice(context) + "-" + OTAUtils.getProp(OTAUtils.MOD_VERSION);
    }

    private static String getDevice(Context context) {
        String device = OTAUtils.getProp(PROPERTY_DEVICE);
        if (device == null || device.isEmpty()) {
            device = OTAUtils.getProp(PROPERTY_DEVICE_EXT);
        }
        return device == null ? "" : device.toLowerCase();
    }

    @Override
    public Version getVersion() {
        return new Version(getVersionString(getContext()));
    }

    @Override
    public String getDevice() {
        return getDevice(getContext());
    }

    @Override
    public int getErrorStringId() {
        return R.string.check_rom_updates_error;
    }

}
