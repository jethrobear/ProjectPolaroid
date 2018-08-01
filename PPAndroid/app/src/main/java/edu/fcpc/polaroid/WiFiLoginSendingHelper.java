package edu.fcpc.polaroid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
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
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Created by PAULM915 on 11/08/2017.
 */

public class WiFiLoginSendingHelper extends WiFiHelper {

    public WiFiLoginSendingHelper(Activity main){
        super(main);
    }

    @Override
    public Integer doInBackgroundInner(ObjectOutputStream objOutStream, String... params) throws IOException{

        SentPackage sentPackage = new SentPackage();
        sentPackage.packageStatus = PackageStatus.LOGIN;
        sentPackage.username = params[0];
        sentPackage.password = params[1];

        objOutStream.writeObject(sentPackage);
        objOutStream.flush();
        objOutStream.close();

        return 0;
    }

    @Override
    protected void onPreExecute()
    {
        dialog.setMessage("Sending image to Digital Frame");
        dialog.show();
    }

    @Override
    protected void onPostExecute(Integer result)
    {
        dialog.dismiss();
        if(result == -1)
            Toast.makeText(main, "Bluetooth send failed", Toast.LENGTH_SHORT).show();
    }
}
