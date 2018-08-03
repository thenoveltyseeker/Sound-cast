package com.talview.soundcast.view.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * Class consists of Utility methods
 */
public class Utility {

	/**
	 * Check whether network connection is available.
	 *
	 * @param context Context
	 * @return status
	 */
	public static boolean isOnline(Context context) {
		ConnectivityManager cm =
				(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm != null) {
			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			return activeNetwork != null &&
					activeNetwork.isConnectedOrConnecting();
		}
		return false;
	}

	/**
	 * Show error Toast
	 * @param context context
	 * @param error error message
	 */
	public static void showError(Context context, String error) {
		Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
	}
}
