package edu.fcpc.polaroid;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

/**
 * Created by grump on 24/08/2017.
 */

public class Main extends FragmentActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_frame);
        if (findViewById(R.id.main_frame) != null) {
            if (savedInstanceState != null) {
                return;
            }

            Fragment10 fragment10 = new Fragment10();
            fragment10.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(R.id.main_frame, fragment10).commit();
        }
    }
}
