package edu.fcpc.polaroid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import edu.fcpc.polaroid.SentPackage;

public class WiFiSendingHelper extends AsyncTask<Void, Void, Integer> {
    private ProgressDialog dialog;
    private Activity main;
    private Intent data;

    public WiFiSendingHelper(Activity main, Intent data){
        this.main = main;
        this.data = data;
        dialog = new ProgressDialog(main);
        dialog.setCancelable(false);
    }

    @Override
    protected void onPreExecute()
    {
        dialog.setMessage("Sending image to Digital Frame");
        dialog.show();
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        // Scan all the machine IPs in the network
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
                    try{
                        Socket socket = new Socket(address.getHostAddress(), 1234);
                        ObjectOutputStream objOutStream = new ObjectOutputStream(socket.getOutputStream());

                        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        outputStream.flush();
                        outputStream.close();
                        SentPackage sentPackage = new SentPackage();
                        sentPackage.packageStatus = PackageStatus.PICTURE;
                        sentPackage.imagebinary = outputStream.toByteArray();
                        sentPackage.filename = "A"; // TODO: Determine if this is needed

                        objOutStream.writeObject(sentPackage);
                        objOutStream.flush();
                        objOutStream.close();
                    catch(IOException ioe){
                        // TODO: The address had not opened port
                    }
                    return 0;
                }
            }
        }catch(UnknownHostException uhe){
            // TODO: Something to check more addresses
        }finally {
            return -1;
        }
    }

    @Override
    protected void onPostExecute(Integer result)
    {
        dialog.dismiss();
        if(result == -1)
            Toast.makeText(main, "Bluetooth send failed", Toast.LENGTH_SHORT).show();
    }
}
