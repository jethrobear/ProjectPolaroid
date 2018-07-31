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
            InetAddress inetAddress = InetAddress.getLocalHost();
            byte[] ip = inetAddress.getAddress();

            for (int i = 1; i <= 254; i++) {
                ip[3] = (byte) i;
                InetAddress address = InetAddress.getByAddress(ip);
                if (address.isReachable(1000)) {
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

    @Override
    protected void onPostExecute(Integer result)
    {
        dialog.dismiss();
        if(result == -1)
            Toast.makeText(main, "Bluetooth send failed", Toast.LENGTH_SHORT).show();
    }
}
