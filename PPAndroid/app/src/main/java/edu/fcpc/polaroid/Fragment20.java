package edu.fcpc.polaroid;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment20 extends Fragment {
    private ProgressDialog dialog;
    private EditText txtUsername;
    private EditText txtPassword;


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

                WiFiLoginHelper btLoginSendHlpr = new WiFiLoginHelper(Fragment20.this.getActivity());
                btLoginSendHlpr.execute(txtUsername.getText().toString(), txtPassword.getText().toString());
            }
        });
		// TODO: Move after logging in

        return rootView;
    }
}
