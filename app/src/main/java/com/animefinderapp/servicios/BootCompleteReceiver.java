package com.animefinderapp.servicios;

import com.animefinderapp.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class BootCompleteReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		PreferenceManager.setDefaultValues(context, R.xml.settings, false);
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Boolean check = sharedPref.getBoolean("notificarcheckbox", false);
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			if (check) {
				context.startService(new Intent(context, AnimeService.class));
			} else {
				context.stopService(new Intent(context, AnimeService.class));
			}
		}
	}

}
