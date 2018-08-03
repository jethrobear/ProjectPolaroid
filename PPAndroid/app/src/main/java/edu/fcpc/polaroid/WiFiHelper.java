package edu.fcpc.polaroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import edu.fcpc.polaroid.packets.PackageStatus;
import edu.fcpc.polaroid.packets.SentPackage;

public abstract class WiFiHelper extends AsyncTask<String, Void, SentPackage> {
    protected ProgressDialog dialog;
    protected Activity main;

    public WiFiHelper(Activity main) {
        this.main = main;
        dialog = new ProgressDialog(main);
        dialog.setCancelable(false);
    }

    @Override
    protected SentPackage doInBackground(String... params) {
        SentPackage errorPackage = new SentPackage();

        try {
            WifiManager mWifiManager = (WifiManager) main.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            //WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
            int subnet = mWifiManager.getDhcpInfo().gateway;
            byte[] ipBytes = new byte[]{(byte) (subnet & 0xff),
                    (byte) (subnet >> 8 & 0xff),
                    (byte) (subnet >> 16 & 0xff),
                    0x00};

            for (int i = 1; i <= 254; i++) {
                ipBytes[3] = (byte) i;
                InetAddress address = InetAddress.getByAddress(ipBytes);
                try {
                    if (address.isReachable(100)) {
                        Socket socket = new Socket(address.getHostAddress(), 1234);
                        ObjectOutputStream objOutStream = new ObjectOutputStream(socket.getOutputStream());

                        // Send the packet to the server
                        doInBackgroundInner(objOutStream, params);

                        // Receive message from the server
                        ObjectInputStream objInStream = new ObjectInputStream(socket.getInputStream());
                        SentPackage receivePackage = (SentPackage) objInStream.readObject();

                        return receivePackage;
                    }
                } catch (IOException ioe) {
                    // TODO: The address had not opened port
                } catch (ClassNotFoundException cnfe){
                    // INFO: This should not enter this block
                }
            }

            errorPackage.packageStatus = PackageStatus.NO_SERVER_FOUND;
        } catch (UnknownHostException uhe) {
            errorPackage.packageStatus = PackageStatus.HOSTNAME_NOT_FOUND;
            errorPackage.retMessage = uhe.getMessage();
        }

        if(!main.getString(R.string.allownetworkbypass).toUpperCase().equals("TRUE"))
            return errorPackage;
        else{
            SentPackage networkBypass = new SentPackage();
            networkBypass.packageStatus = PackageStatus.NETWORK_BYPASS;
            return networkBypass;
        }
    }

    @Override
    protected void onPostExecute(SentPackage result) {
        dialog.dismiss();

        // General issues
        if (result.packageStatus == PackageStatus.HOSTNAME_NOT_FOUND){
            new AlertDialog.Builder(main)
                    .setTitle("Alert Box")
                    .setCancelable(false)
                    .setMessage("Hostname provided was wrong")
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    }).create().show();
        }else if(result.packageStatus == PackageStatus.NO_SERVER_FOUND){
            new AlertDialog.Builder(main)
                    .setTitle("Alert Box")
                    .setCancelable(false)
                    .setMessage("No server have been found")
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    }).create().show();
        }else{
            if(result.packageStatus == PackageStatus.NETWORK_BYPASS)
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
            onPostExecuteAfter(result);
        }
    }

    public abstract Integer doInBackgroundInner(ObjectOutputStream objOutStream, String... params) throws IOException;
    public abstract void onPostExecuteAfter(SentPackage sentPackage);
}
