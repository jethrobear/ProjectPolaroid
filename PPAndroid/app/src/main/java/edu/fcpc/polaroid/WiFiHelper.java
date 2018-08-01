package edu.fcpc.polaroid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public abstract class WiFiHelper extends AsyncTask<String, Void, Integer> {
    protected ProgressDialog dialog;
    protected Activity main;

    public WiFiHelper(Activity main){
        this.main = main;
        dialog = new ProgressDialog(main);
        dialog.setCancelable(false);
    }

    @Override
    protected Integer doInBackground(String... params) {
        try {
            WifiManager mWifiManager = (WifiManager) main.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
            int subnet = mWifiManager.getDhcpInfo().gateway;
            byte[] ipBytes = new byte[]{(byte) (subnet & 0xff),
                    (byte) (subnet >> 8 & 0xff),
                    (byte) (subnet >> 16 & 0xff),
                    0x00};

            for (int i = 1; i <= 254; i++) {
                ipBytes[3] = (byte) i;
                InetAddress address = InetAddress.getByAddress(ipBytes);
                if (address.isReachable(1000)) {
                    try {
                        Socket socket = new Socket(address.getHostAddress(), 1234);
                        ObjectOutputStream objOutStream = new ObjectOutputStream(socket.getOutputStream());

                        return doInBackgroundInner(objOutStream);
                    } catch (IOException ioe) {
                        // TODO: The address had not opened port
                    }
                }
            }
        } catch (UnknownHostException uhe) {
            // TODO: Something to check more addresses
        } finally {
            return -1;
        }
    }

    public abstract Integer doInBackgroundInner(ObjectOutputStream objOutStream, String... params) throws IOException;
}
