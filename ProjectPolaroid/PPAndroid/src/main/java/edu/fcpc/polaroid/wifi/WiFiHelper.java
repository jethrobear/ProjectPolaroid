package edu.fcpc.polaroid.wifi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import edu.fcpc.polaroid.BuildConfig;
import edu.fcpc.polaroid.data.SocketCache;
import edu.fcpc.polaroid.packets.PackageStatus;
import edu.fcpc.polaroid.packets.SentPackage;

public abstract class WiFiHelper extends AsyncTask<String, Void, SentPackage> {
    ProgressDialog dialog;
    Activity main;

    public WiFiHelper(Activity main) {
        this.main = main;
        dialog = new ProgressDialog(main);
        dialog.setCancelable(false);
    }

    @Override
    protected SentPackage doInBackground(String... params) {
        // Loop through all possible servers
        for (ImmutablePair<InetAddress, Integer> key : SocketCache.workingAddresses.values()) {
            try {
                Socket socket = new Socket(key.getLeft(), key.getRight());
                ObjectOutputStream objOutStream = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));

                // Send the packet to the server
                doInBackgroundInner(objOutStream, params);

                // Receive message from the server
                ObjectInputStream objInStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                SentPackage receivePackage = (SentPackage) objInStream.readObject();
                objInStream.close();
                socket.close();

                return receivePackage;
            } catch (UnknownHostException uhe) {
                Log.w("ProjectPolaroid", uhe.getMessage());
            } catch (IOException ioe) {
                Log.e("ProjectPolaroid", ioe.getMessage());
            } catch (ClassNotFoundException cnfe) {
                Log.e("ProjectPolaroid", cnfe.getMessage());
            }
        }
        // Return the error message
        if (!BuildConfig.NETWORK_BYPASS) {
            SentPackage errorPackage = new SentPackage();
            errorPackage.packageStatus = PackageStatus.NO_SERVER_FOUND;
            return errorPackage;
        } else {
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