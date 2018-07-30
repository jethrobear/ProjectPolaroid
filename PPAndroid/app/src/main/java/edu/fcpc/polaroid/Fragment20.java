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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment20 extends Fragment {
    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothServerSocket btServerSocket;
    private BluetoothSocket btSocket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    private ProgressDialog dialog;
    private String errMessage;
    private EditText txtUsername;
    private EditText txtPassword;
    private int retries = 0;

    public Fragment20() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Activity main = Fragment20.this.getActivity();
        View rootView = inflater.inflate(R.layout.fragment_20, container, false);

        dialog = new ProgressDialog(main);
        dialog.setCancelable(false);
        txtUsername = (EditText) rootView.findViewById(R.id.txtUsername);
		txtPassword = (EditText) rootView.findViewById(R.id.txtPassword);
		Button btnLogin2 = (Button) rootView.findViewById(R.id.btnLogin2);
		
		btnLogin2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(txtUsername.getText().toString().equals("") || txtPassword.getText().toString().equals("")){
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Alert Box")
                            .setCancelable(false)
                            .setMessage("Please enter your complete username and password")
                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                }

                // TODO: Validate stuff
//                DatabaseHelper databaseHelper = new DatabaseHelper(Fragment20.this.getContext());
//                List<User> users = databaseHelper.getAllUsers();
//                boolean hasHit = false;
//                for (User user : users) {
//                    if(user.getUsername().equals(txtUsername.getText().toString()) &&
//                       user.getPassword().equals(txtPassword.getText().toString()))
//                        hasHit = true;
//                }
                AsyncTask<String, Void, Integer> btLoginSendHlpr = new AsyncTask<String, Void, Integer>() {
                    @Override
                    protected void onPreExecute()
                    {
                        dialog.setMessage("Sending login to Digital Frame");
                        dialog.show();
                    }

                    @Override
                    protected Integer doInBackground(String... params) {
                        int returnInt = 0;
                        try {
                            btServerSocket =
                                    bluetoothAdapter.listenUsingRfcommWithServiceRecord("BluetoothImageTransfer",
                                            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                            Thread.sleep(100);
                            btSocket = btServerSocket.accept();

                            // Send Login
                            dataOutputStream = new DataOutputStream(btSocket.getOutputStream());
                            String loginInfo = "login|" + params[0] + "|" + params[1] + (char)255;
                            dataOutputStream.writeUTF(loginInfo);
                            dataOutputStream.flush();
                            //dialog.setMessage("Data sent, waiting for response");

                            // Get status
                            dataInputStream = new DataInputStream(btSocket.getInputStream());
                            try {
                                String returnValue = dataInputStream.readUTF();
                                if (returnValue.toUpperCase().contains("PASS"))
                                    returnInt = 1;
                            }catch(Exception e){
                                //dialog.setMessage(e.getMessage());
                                Thread.sleep(Integer.MAX_VALUE);
                            }

                            // Trickle
                            btSocket.close();
                            btServerSocket.close();
                        }catch(InterruptedException | IOException e){
                            errMessage = e.getMessage();
                            return -1;
                        }
                        return returnInt;
                    }

                    @Override
                    protected void onPostExecute(Integer result)
                    {
                        dialog.dismiss();
                        if(result == -1) {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle("Alert Box")
                                    .setCancelable(false)
                                    .setMessage(errMessage)
                                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            System.exit(0);
                                        }
                                    }).create().show();
                        }

                        boolean hasHit = (result == 1);
                        if(hasHit) {
                            // Login successful
                            Fragment30 fragment30 = new Fragment30();
                            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.main_frame, fragment30, fragment30.toString());
                            fragmentTransaction.commit();
                        }else if(!hasHit && retries <= 1){
                            new AlertDialog.Builder(getActivity())
                                    .setTitle("Alert Box")
                                    .setCancelable(false)
                                    .setMessage("Wrong username and password")
                                    .setNeutralButton("Retry", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).create().show();
                            retries++;
                        }else{
                            new AlertDialog.Builder(getActivity())
                                    .setTitle("Alert Box")
                                    .setCancelable(false)
                                    .setMessage("Username and password not valid. Create a new user?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            Fragment21 fragment21 = new Fragment21();
                                            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                                            fragmentTransaction.replace(R.id.main_frame, fragment21, fragment21.toString());
                                            fragmentTransaction.commit();
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).create().show();
                        }
                    }
                };
                btLoginSendHlpr.execute(txtUsername.getText().toString(), txtPassword.getText().toString());
            }
        });

        return rootView;
    }
}