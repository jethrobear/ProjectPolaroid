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
import java.util.UUID;

/**
 * Created by PAULM915 on 11/08/2017.
 */

public class BluetoothSendingHelper extends AsyncTask<Void, Void, Integer> {
    private ProgressDialog dialog;
    private Activity main;
    private Intent data;

    public BluetoothSendingHelper(Activity main, Intent data){
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
    protected Integer doInBackground(Void... params) {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothServerSocket btServerSocket =
                    bluetoothAdapter.listenUsingRfcommWithServiceRecord("BluetoothImageTransfer",
                            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            Thread.sleep(100);
            BluetoothSocket btSocket = btServerSocket.accept();
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            btSocket.getOutputStream().write(outputStream.toByteArray());
            btSocket.close();
            btServerSocket.close();
        }catch(InterruptedException | IOException e){
            return -1;
        }
        return 0;
    }

    @Override
    protected void onPostExecute(Integer result)
    {
        dialog.dismiss();
        if(result == -1)
            Toast.makeText(main, "Bluetooth send failed", Toast.LENGTH_SHORT).show();
    }
}
