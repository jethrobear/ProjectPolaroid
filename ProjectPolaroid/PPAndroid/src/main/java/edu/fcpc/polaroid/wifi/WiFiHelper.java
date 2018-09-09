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
import java.util.Collection;
import java.util.LinkedHashMap;

import edu.fcpc.polaroid.BuildConfig;
import edu.fcpc.polaroid.data.SocketCache;
import edu.fcpc.polaroid.packets.PackageStatus;
import edu.fcpc.polaroid.packets.SentPackage;

public abstract class WiFiHelper extends AsyncTask<String, Void, LinkedHashMap<ImmutablePair<InetAddress, Integer>, SentPackage>> {
    ProgressDialog dialog;
    Activity main;
    private final ImmutablePair<InetAddress, Integer> NO_SERVER_SET = new ImmutablePair<>(null, null);
    Collection<ImmutablePair<InetAddress, Integer>> workingServerSet = null;

    public WiFiHelper(Activity main) {
        this.main = main;
        dialog = new ProgressDialog(main);
        dialog.setCancelable(false);
    }

    @Override
    protected LinkedHashMap<ImmutablePair<InetAddress, Integer>, SentPackage> doInBackground(String... params) {
        // Loop through all possible servers
        LinkedHashMap<ImmutablePair<InetAddress, Integer>, SentPackage> resultSet = new LinkedHashMap<>();
        for (ImmutablePair<InetAddress, Integer> key : workingServerSet) {
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

                resultSet.put(key, receivePackage);
            } catch (UnknownHostException uhe) {
                Log.w("ProjectPolaroid", uhe.getMessage());
            } catch (IOException ioe) {
                Log.e("ProjectPolaroid", ioe.getMessage());
            } catch (ClassNotFoundException cnfe) {
                Log.e("ProjectPolaroid", cnfe.getMessage());
            }
        }

        // Return the error message
        if (resultSet.size() == 0) {
            if (!BuildConfig.NETWORK_BYPASS) {
                SentPackage errorPackage = new SentPackage();
                errorPackage.packageStatus = PackageStatus.NO_SERVER_FOUND;
                resultSet.put(NO_SERVER_SET, errorPackage);
            } else {
                SentPackage networkBypass = new SentPackage();
                networkBypass.packageStatus = PackageStatus.NETWORK_BYPASS;
                resultSet.put(NO_SERVER_SET, networkBypass);
            }
        }

        // Send to postExecute
        return resultSet;
    }

    @Override
    protected void onPostExecute(LinkedHashMap<ImmutablePair<InetAddress, Integer>, SentPackage> result) {
        dialog.dismiss();

        // General issues
        if (result.get(NO_SERVER_SET) != null) {
            if (result.get(NO_SERVER_SET).packageStatus == PackageStatus.HOSTNAME_NOT_FOUND) {
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
            } else if (result.get(NO_SERVER_SET).packageStatus == PackageStatus.NO_SERVER_FOUND) {
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
            } else if (result.get(NO_SERVER_SET).packageStatus == PackageStatus.NETWORK_BYPASS) {
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
        } else {
            onPostExecuteAfter(result);
        }
    }

    public abstract Integer doInBackgroundInner(ObjectOutputStream objOutStream, String... params) throws IOException;

    public abstract void onPostExecuteAfter(LinkedHashMap<ImmutablePair<InetAddress, Integer>, SentPackage> sentPackage);
}
