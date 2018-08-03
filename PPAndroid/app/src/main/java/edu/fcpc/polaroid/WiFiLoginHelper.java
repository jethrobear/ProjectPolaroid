package edu.fcpc.polaroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;

import java.io.IOException;
import java.io.ObjectOutputStream;

import edu.fcpc.polaroid.packets.PackageStatus;
import edu.fcpc.polaroid.packets.SentPackage;

public class WiFiLoginHelper extends WiFiHelper {
    public WiFiLoginHelper(Activity main){
        super(main);
    }

    @Override
    protected void onPreExecute()
    {
        dialog.setMessage("Sending login to Digital Frame");
        dialog.show();
    }

    @Override
    public Integer doInBackgroundInner(ObjectOutputStream objOutStream, String... params) throws IOException{
        SentPackage sentPackage = new SentPackage();
        sentPackage.packageStatus = PackageStatus.LOGIN;
        sentPackage.username = params[0];
        sentPackage.password = params[1];

        objOutStream.writeObject(sentPackage);
        objOutStream.flush();

        return 0;
    }

    private int retries = 0;
    @Override
    public void onPostExecuteAfter(SentPackage sentPackage) {
        if(sentPackage.packageStatus == PackageStatus.LOGIN_RESPONSE_OK || sentPackage.packageStatus == PackageStatus.NETWORK_BYPASS) {
            if(sentPackage.packageStatus == PackageStatus.NETWORK_BYPASS)
                new AlertDialog.Builder(main)
                        .setTitle("Warning")
                        .setCancelable(false)
                        .setMessage("Assuming Network Bypass, proceed with warning")
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Do nothing
                            }
                        }).create().show();

            // Login successful
            Fragment30 fragment30 = new Fragment30();
            FragmentTransaction fragmentTransaction = main.getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.main_frame, fragment30, fragment30.toString());
            fragmentTransaction.commit();
        }else if(sentPackage.packageStatus == PackageStatus.LOGIN_RESPONSE_FAIL && retries <= 1){
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
        }else{
            new AlertDialog.Builder(main)
                    .setTitle("Alert Box")
                    .setCancelable(false)
                    .setMessage("Username and password not valid. Create a new user?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
//                            Fragment21 fragment21 = new Fragment21();
//                            FragmentTransaction fragmentTransaction = main.getFragmentManager().beginTransaction();
//                            fragmentTransaction.replace(R.id.main_frame, fragment21, fragment21.toString());
//                            fragmentTransaction.commit();
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

