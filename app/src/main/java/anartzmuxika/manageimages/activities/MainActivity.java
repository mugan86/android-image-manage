package anartzmuxika.manageimages.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import anartzmuxika.manageimages.R;
import anartzmuxika.manageimages.utils.DataPreferences;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DataPreferences.loadLocale(MainActivity.this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                openImageManageOptionsActivity();

            }
        });

        Button open_image_manage_optionsButton = (Button) findViewById(R.id.open_image_manage_optionsButton);
        open_image_manage_optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageManageOptionsActivity();
            }
        });
    }

    private void openImageManageOptionsActivity()
    {
        Intent open_upload_activity_intent = new Intent(MainActivity.this, UploadManagerActivity.class);
        startActivity(open_upload_activity_intent);
    }

}
