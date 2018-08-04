package edu.fcpc.polaroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

public class Main extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_frame);
        if (findViewById(R.id.main_frame) != null) {
            if (savedInstanceState != null) {
                return;
            }

            Fragment10 fragment10 = new Fragment10();
            fragment10.setArguments(getIntent().getExtras());
            getFragmentManager().beginTransaction().add(R.id.main_frame, fragment10).commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0)
            getFragmentManager().popBackStack();
        else {
            new AlertDialog.Builder(this)
                    .setTitle("Quit")
                    .setCancelable(false)
                    .setMessage("Continue closing the app?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Main.super.onBackPressed();
                        }
                    })
                    .create().show();
        }
    }
}
