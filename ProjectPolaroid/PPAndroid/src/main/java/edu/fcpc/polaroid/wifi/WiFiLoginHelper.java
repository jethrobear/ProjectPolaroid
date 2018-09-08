package edu.fcpc.polaroid.wifi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.HashMap;

import edu.fcpc.polaroid.Fragment21;
import edu.fcpc.polaroid.Fragment30;
import edu.fcpc.polaroid.R;
import edu.fcpc.polaroid.packets.PackageStatus;
import edu.fcpc.polaroid.packets.SentPackage;

public class WiFiLoginHelper extends WiFiHelper {
    public WiFiLoginHelper(Activity main) {
        super(main);
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Sending login to Digital Frame");
        dialog.show();
    }

    @Override
    public Integer doInBackgroundInner(ObjectOutputStream objOutStream, String... params) throws IOException {
        SentPackage sentPackage = new SentPackage();
        sentPackage.packageStatus = PackageStatus.LOGIN;
        sentPackage.username = params[0];
        sentPackage.password = params[1];

        objOutStream.writeObject(sentPackage);
        objOutStream.flush();

        return 0;
    }

    private int retries = 0;
    private final int MAX_RETRIES = 3;

    @Override
    public void onPostExecuteAfter(HashMap<ImmutablePair<InetAddress, Integer>, SentPackage> results) {
        for (SentPackage sentPackage : results.values()) {
            if (sentPackage.packageStatus == PackageStatus.LOGIN_RESPONSE_OK || sentPackage.packageStatus == PackageStatus.NETWORK_BYPASS) {
                // Login successful
                Fragment30 fragment30 = new Fragment30();
                FragmentTransaction fragmentTransaction = main.getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_frame, fragment30, fragment30.toString());
                fragmentTransaction.commit();
                return;
            }
        }

        if (retries <= MAX_RETRIES) {
            new AlertDialog.Builder(main)
                    .setTitle("Alert Box")
                    .setCancelable(false)
                    .setMessage("Wrong username and password")
                    .setNeutralButton("Retry", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            retries = retries++;
                            dialog.dismiss();
                        }
                    }).create().show();
        } else {
            new AlertDialog.Builder(main)
                    .setTitle("Alert Box")
                    .setCancelable(false)
                    .setMessage("Username and password not valid. Create a new user?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Fragment21 fragment21 = new Fragment21();
                            FragmentTransaction fragmentTransaction = main.getFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.main_frame, fragment21, fragment21.toString());
                            fragmentTransaction.commit();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
        }
    }
}

