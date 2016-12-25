package anartzmuxika.manageimages.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import anartzmuxika.manageimages.R;
import anartzmuxika.manageimages.utils.DataPreferences;

public class MainActivity extends AppCompatActivity {

    private TextView select_language_title, text_about_libraryTextView;
    private Button open_image_manage_optionsButton;
    private Activity activity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        activity = MainActivity.this;

        DataPreferences.loadLocale(activity);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                openImageManageOptionsActivity();

            }
        });

        open_image_manage_optionsButton = (Button) findViewById(R.id.open_image_manage_optionsButton);
        open_image_manage_optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageManageOptionsActivity();
            }
        });

        Button select_basqueButton = (Button) findViewById(R.id.select_basqueButton);
        Button select_spanishButton = (Button) findViewById(R.id.select_spanishButton);
        Button select_catalaButton = (Button) findViewById(R.id.select_catalaButton);
        Button select_englishButton = (Button) findViewById(R.id.select_englishButton);

        select_basqueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataPreferences.changeLang("eu", activity);
                updateTexts();
            }
        });

        select_spanishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataPreferences.changeLang("es", activity);
                updateTexts();
            }
        });

        select_catalaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataPreferences.changeLang("ca", activity);
                updateTexts();
            }
        });

        select_englishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataPreferences.changeLang("en", activity);
                updateTexts();
            }
        });

        select_language_title = (TextView) findViewById(R.id.select_language_title);

        text_about_libraryTextView = (TextView) findViewById(R.id.text_about_libraryTextView);
    }

    private void openImageManageOptionsActivity()
    {
        Intent open_upload_activity_intent = new Intent(MainActivity.this, UploadManagerActivity.class);
        startActivity(open_upload_activity_intent);
    }

    //Use to update text in select language
    private void updateTexts()
    {
        invalidateOptionsMenu();

        DataPreferences.loadLocale(getApplicationContext());

        Log.d("IDIOMA--> ", "Select language in app is: " + DataPreferences.getLocaleLanguage(getApplicationContext()));

        //Update app title
        //getSupportActionBar().setTitle(getResources().getString(R.string.app_name));

        //Update texts
        select_language_title.setText(getResources().getString(R.string.select_language_title));
        text_about_libraryTextView.setText(getResources().getString(R.string.explain_text));
        open_image_manage_optionsButton.setText(getResources().getString(R.string.manage_upload_images));

    }

}
