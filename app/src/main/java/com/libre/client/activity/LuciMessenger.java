package com.libre.client.activity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.libre.client.LUCIControl;
import com.libre.client.LUCIPacket;
import com.libre.constants.LSSDPCONST;
import com.libre.constants.MIDCONST;

/**
 * Created by libre on 08-02-2015.
 */
public class LuciMessenger extends ActionBarActivity {
    String SERVER_IP;
    String DEVICE_NAME;
    LUCIControl luciControl;
    int m_type=2;
    Handler updatingHandler;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.luci_messenger);
        final TextView textView = (TextView) findViewById(R.id.logs);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        SERVER_IP = getIntent().getStringExtra(Constant.IPADRESS);
        DEVICE_NAME = getIntent().getStringExtra(Constant.DEVICENAME);
        getSupportActionBar().setTitle(DEVICE_NAME);
        getSupportActionBar().show();
        updatingHandler = new Handler() {
            @SuppressLint("InflateParams")
            @Override
            public void handleMessage(Message msg) {

                if (msg.what == LSSDPCONST.LUCI_RESP_RECIEVED)

                {

                   final LUCIPacket pkt = (LUCIPacket) msg.obj;
                    byte[] data = new byte[pkt.getDataLen()];
                    Log.e("LUCIMESSANGER","Length of packet"+pkt.getDataLen());

                    pkt.getpayload(data);
                   final  String message= new String(data, 0, pkt.getDataLen());
                    Log.e("LUCIMESSANGER",message);

                    textView.setText(textView.getText()+
                            " id "+pkt.getCommand()+" Data = "+message+ "\n");

                }
            }
        };


        luciControl= new LUCIControl(SERVER_IP);

        final SwitchCompat type= (SwitchCompat)findViewById(R.id.switchfortype);
        type.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    m_type = 2;
                    type.setText("Method: SET");
                }
                else {
                    m_type = 1;
                    type.setText("Method: GET");
                }


            }
        });





        ImageButton sendbutton=(ImageButton) findViewById(R.id.myButton);
        sendbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Send();


                }

        });
        ImageButton button=(ImageButton) findViewById(R.id.clearlogs);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            textView.setText("");


            }

        });

        ((EditText)findViewById(R.id.EditText3)).setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEND ||
                                actionId == EditorInfo.IME_ACTION_DONE ||
                                event.getAction() == KeyEvent.ACTION_DOWN &&
                                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                            Send();

                            return true;
                        }
                        return false;
                    }
                });



    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {

            case android.R.id.home:
             finish();
                return true;
        }
        return true;
    }

    private void Send()
    {
        try {
            EditText et = (EditText) findViewById(R.id.EditText1);
            // EditText et2= (EditText)findViewById(R.id.EditText2);
            EditText et3= (EditText)findViewById(R.id.EditText3);

            String mid = et.getText().toString();
            //String mtype = et2.getText().toString();
            String data= et3.getText().toString();

            if (mid==null||mid.equals("")){
                Toast.makeText(getApplicationContext(),"Messagebox id should not be null",Toast.LENGTH_SHORT).show();
                return;
            }

            int cmd = Integer.parseInt(mid);

            // int cmdtype = Integer.parseInt(mtype);



            if (data==null||data.equals("")) {
                luciControl.SendCommand(cmd, null, m_type);
            }
            else {
                luciControl.SendCommand(cmd, data, m_type);
            }


            et3.getText().clear();



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (luciControl!=null)
        luciControl.addhandler(updatingHandler);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (luciControl!=null)
        luciControl.addhandler(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        luciControl.close();
    }
}
