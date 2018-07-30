package edu.fcpc.polaroid;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment21 extends Fragment {
    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothServerSocket btServerSocket;
    private BluetoothSocket btSocket;
    private DataOutputStream dataOutputStream;
    private ProgressDialog dialog;
    private String errMessage;

    public Fragment21() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Activity main = Fragment21.this.getActivity();
        View rootView = inflater.inflate(R.layout.fragment_21, container, false);

        dialog = new ProgressDialog(main);
        dialog.setCancelable(false);

        final EditText txtLastname = (EditText) rootView.findViewById(R.id.txtLastname);
		final EditText txtFirstname = (EditText) rootView.findViewById(R.id.txtFirstname);
		final EditText txtRegisterMonth = (EditText) rootView.findViewById(R.id.txtRegisterMonth);
		final EditText txtRegisterDay = (EditText) rootView.findViewById(R.id.txtRegisterDay);
		final EditText txtRegisterYear = (EditText) rootView.findViewById(R.id.txtRegisterYear);
		final EditText txtUsername2 = (EditText) rootView.findViewById(R.id.txtUsername2);
		final EditText txtPassword2 = (EditText) rootView.findViewById(R.id.txtPassword2);
		Button btnRegister = (Button) rootView.findViewById(R.id.btnRegister);
		
		btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(txtLastname.getText().toString().equals("") ||
                   txtFirstname.getText().toString().equals("") ||
                   txtRegisterMonth.getText().toString().equals("") ||
                   txtRegisterDay.getText().toString().equals("") ||
                   txtRegisterYear.getText().toString().equals("") ||
                   txtUsername2.getText().toString().equals("") ||
                   txtPassword2.getText().toString().equals(""))
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Alert Box")
                            .setMessage("Please input every required information on the register form")
                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                else {
                    AsyncTask<String, Void, Integer> btRegisterSendHlpr = new AsyncTask<String, Void, Integer>() {
                        @Override
                        protected void onPreExecute()
                        {
                            dialog.setMessage("Sending registration to Digital Frame");
                            dialog.show();
                        }

                        @Override
                        protected Integer doInBackground(String... params) {
                            try {
                                btServerSocket =
                                        bluetoothAdapter.listenUsingRfcommWithServiceRecord("BluetoothImageTransfer",
                                                UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                                Thread.sleep(100);
                                btSocket = btServerSocket.accept();

                                dataOutputStream = new DataOutputStream(btSocket.getOutputStream());

                                String loginInfo = "register|" + params[0] + "|" + params[1] + "|" + params[2]
                                        + "|" + params[3] + "|" + params[4] + "|" + params[5] + "|" + params[6]
                                        + (char)255;
                                dataOutputStream.writeUTF(loginInfo);
                                dataOutputStream.flush();
                                dataOutputStream.close();
                                btSocket.close();
                                btServerSocket.close();
                                return 0;
                            }catch(InterruptedException | IOException e){
                                errMessage = e.getMessage();
                                return -1;
                            }
                        }

                        @Override
                        protected void onPostExecute(Integer result) {
                            dialog.dismiss();
                            if (result == -1) {
                                new AlertDialog.Builder(getActivity())
                                        .setTitle("Alert Box")
                                        .setCancelable(false)
                                        .setMessage(errMessage)
                                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // pass
                                            }
                                        }).create().show();
                            }else{
                                Toast.makeText(main, "User registered", Toast.LENGTH_SHORT).show();
                                Fragment20 fragment20 = new Fragment20();
                                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                                fragmentTransaction.replace(R.id.main_frame, fragment20, fragment20.toString());
                                fragmentTransaction.commit();
                            }
                        }
                    };
                    btRegisterSendHlpr.execute(txtLastname.getText().toString(),
                                               txtFirstname.getText().toString(),
                                               txtRegisterMonth.getText().toString(),
                                               txtRegisterDay.getText().toString(),
                                               txtRegisterYear.getText().toString(),
                                               txtUsername2.getText().toString(),
                                               txtPassword2.getText().toString());
                }
            }
        });

        return rootView;
    }
}