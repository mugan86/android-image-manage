package anartzmuxika.manageimages.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by anartzmugika on 4/4/16.
 *
 * To Use Preferences File, always convert all data to String format (numbers and boolean values include)
 *
 * For Example:
 *
 * To Asign property name/s
 *
 * String [] propertyName = {"START_SESSION_IMAGES", "CURRENT_DATA"};
 * String [] propertyValue = {"no", "2016-04-04"};
 *
 * DataPreferences.setPreference(getApplicationContext(), propertyName, propertyValue);
 */
public class DataPreferences {
    public static SharedPreferences getPreferencesFile(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.

        return PreferenceManager.getDefaultSharedPreferences(context);
    }
    public static String getPreference(Context context, String propertyName){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(propertyName, "");
    }
    public static void setPreference(Context context, String [] propertyNames
            , String [] propertyValues){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for (int i = 0; i< propertyNames.length; i ++)
        {
            editor.putString(propertyNames [i], propertyValues [i]);
        }

        editor.commit();
    }
}
