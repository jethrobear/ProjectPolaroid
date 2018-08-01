package edu.fcpc.polaroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentTransaction;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class WiFiLoginHelper extends AsyncTask<String, Void, Integer> {
    private ProgressDialog dialog;
    private Activity main;

    public WiFiLoginHelper(Activity main){
        this.main = main;
        dialog = new ProgressDialog(main);
        dialog.setCancelable(false);
    }

    @Override
    protected void onPreExecute()
    {
        dialog.setMessage("Sending login to Digital Frame");
        dialog.show();
    }

    @Override
    protected Integer doInBackground(String... params) {
        try {
            WifiManager mWifiManager = (WifiManager) main.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
            int subnet = mWifiManager.getDhcpInfo().gateway;
            byte[] ipBytes = new byte[]{(byte)(subnet & 0xff),
                                        (byte)(subnet >> 8 & 0xff),
                                        (byte)(subnet >> 16 & 0xff),
                                        0x00};

            for (int i = 1; i <= 254; i++) {
                ipBytes[3] = (byte) i;
                InetAddress address = InetAddress.getByAddress(ipBytes);
                if (address.isReachable(1000)) {
                    Socket socket = new Socket(address.getHostAddress(), 1234);
                    ObjectOutputStream objOutStream = new ObjectOutputStream(socket.getOutputStream());

                    SentPackage sentPackage = new SentPackage();
                    sentPackage.packageStatus = PackageStatus.LOGIN;
                    sentPackage.username = params[0];
                    sentPackage.password = params[1];

                    objOutStream.writeObject(sentPackage);
                    objOutStream.flush();
                    objOutStream.close();

                    return 0;
                }
            }
        }catch(UnknownHostException uhe){
            // TODO: Something to check more addresses
        }catch(IOException ioe){
            // TODO: IF the address doesnt cater to the port
        }finally {
            return -1;
        }
    }

    private int retries = 0;
    @Override
    protected void onPostExecute(Integer result)
    {
        dialog.dismiss();
        if(result == -1) {
            new AlertDialog.Builder(main)
                    .setTitle("Alert Box")
                    .setCancelable(false)
                    .setMessage("Something went wrong")  // TODO: Check error on the onProgress
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    }).create().show();
        }

        boolean hasHit = (result == 1);
        if(hasHit) {
            // Login successful
//            Fragment30 fragment30 = new Fragment30();
//            FragmentTransaction fragmentTransaction = main.getFragmentManager().beginTransaction();
//            fragmentTransaction.replace(R.id.main_frame, fragment30, fragment30.toString());
//            fragmentTransaction.commit();
        }else if(!hasHit && retries <= 1){
            new AlertDialog.Builder(main)
                    .setTitle("Alert Box")
                    .setCancelable(false)
                    .setMessage("Wrong username and password")
                    .setNeutralButton("Retry", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
            retries++;
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
