package eu.hellek.viajafacil.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;

/*
 *  display TOS on first start of app
 */
class TOS {

    static interface OnAgreed {
        void onAgreed();
    }

    static boolean show(final Activity activity) {
        final SharedPreferences preferences = activity.getSharedPreferences("tos_prefs", Activity.MODE_PRIVATE);
        
        //preferences.edit().putBoolean("okay", false).commit();
        
        if (!preferences.getBoolean("okay", false)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(R.string.tos_title);
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.tos_accept, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                	preferences.edit().putBoolean("okay", true).commit();
                    if (activity instanceof OnAgreed) {
                        ((OnAgreed) activity).onAgreed();
                    }
                }
            });
            builder.setNegativeButton(R.string.tos_decline, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                	activity.finish();
                }
            });
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                	activity.finish();
                }
            });
//            builder.setMessage(R.string.tos);
            builder.setMessage(readFile(activity, R.raw.tos));
            builder.create().show();
            return false;
        }
        return true;
    }
    
    private static CharSequence readFile(Activity activity, int id) {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(
                    activity.getResources().openRawResource(id)));
            String line;
            StringBuilder buffer = new StringBuilder();
            while ((line = in.readLine()) != null) buffer.append(line).append('\n');
            return buffer;
        } catch (IOException e) {
            return "";
        } finally {
        	try {
        		in.close();
        	} catch (IOException e) {
        		
        	}
        }
    }

}