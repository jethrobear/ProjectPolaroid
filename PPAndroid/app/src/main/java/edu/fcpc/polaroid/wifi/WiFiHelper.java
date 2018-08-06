package edu.fcpc.polaroid.wifi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import edu.fcpc.polaroid.BuildConfig;
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
            Socket socket = new Socket(SocketCache.getActiveHost(mWifiManager).getHostAddress(), 1234);
            ObjectOutputStream objOutStream = new ObjectOutputStream(socket.getOutputStream());

            // Send the packet to the server
            doInBackgroundInner(objOutStream, params);

            // Receive message from the server
            ObjectInputStream objInStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            SentPackage receivePackage = (SentPackage) objInStream.readObject();
            objInStream.close();
            socket.close();

            return receivePackage;
        } catch (SocketCache.NoServerFoundException nsfe) {
            errorPackage.packageStatus = PackageStatus.NO_SERVER_FOUND;
        } catch (UnknownHostException uhe) {
            errorPackage.packageStatus = PackageStatus.HOSTNAME_NOT_FOUND;
            errorPackage.retMessage = uhe.getMessage();
        } catch (IOException ioe) {
            // TODO: Add exception message here
            // Possible broken pipe
        } catch (ClassNotFoundException cnfe) {
            // Do nothing
        }

        // Return the error message
        if (!BuildConfig.NETWORK_BYPASS)
            return errorPackage;
        else {
            SentPackage networkBypass = new SentPackage();
            networkBypass.packageStatus = PackageStatus.NETWORK_BYPASS;
            return networkBypass;
        }
    }

    @Override
    protected void onPostExecute(SentPackage result) {
        dialog.dismiss();

        // General issues
        if (result.packageStatus == PackageStatus.HOSTNAME_NOT_FOUND) {
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
        } else if (result.packageStatus == PackageStatus.NO_SERVER_FOUND) {
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
        } else {
            if (result.packageStatus == PackageStatus.NETWORK_BYPASS)
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
