/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.lightcontrol;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.android.lightcontrol.DBHelper.FEILD_DALICOMMAND;
import static com.example.android.lightcontrol.DBHelper.FEILD_SHORTADDRESS_DALI;
import static com.example.android.lightcontrol.DBHelper.FEILD_DALISLAVEADDRESS;
import static com.example.android.lightcontrol.DBHelper.FEILD_SHORTADDRESS;
import static com.example.android.lightcontrol.DBHelper.TABLE_NAME_DALICOMMAND;
import static com.example.android.lightcontrol.DBHelper.TABLE_NAME_DALISLAVEADDRESS;
import static com.example.android.lightcontrol.DBHelper.TABLE_NAME_SHORT_ADDRESS;
import static com.example.android.lightcontrol.DBHelper.TABLE_NAME_GROUP;
import static com.example.android.lightcontrol.DBHelper.FEILD_NAME_DALI;
import static com.example.android.lightcontrol.DBHelper.FEILD_Group;
import static com.example.android.lightcontrol.MainActivity.ListActivityFragment.*;


@SuppressLint("DefaultLocale")
public class MainActivity extends FragmentActivity implements
        ActionBar.TabListener {

    private DBHelper helper;
    private Cursor cursor;

    //Arraylist for group and dali address
    final ArrayList<String> GroupList = new ArrayList<String>();
    final ArrayList<String> DaliList = new ArrayList<String>();

    final List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
    final List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();

    public Map<Integer, Boolean> isChecked;

    AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    private static final String TAG = "FSP Light Control";
    private static final boolean D = true;
    public static boolean B = false;
    public static boolean C = false;
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    static final String NAME = "NAME";
    ExpandableListAdapter mAdapter;
    private List<Map<Integer, Boolean>> DBisSelectedList;
    private List<Map<Integer, Boolean>> isSelectedList;


    public ProgressDialog mDialog;
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // private ListActivityFragment.MyAdapter mAdapter;

    int progress1 = 0;
    private SeekBar seekBar;
    private TextView textView_progress;
    int Max_Value = 100;
    int Min_Value = 0;

    public static SimpleCursorAdapter GroupAdapter = null;

    ArrayList<HashMap> list = new ArrayList<HashMap>();
    ArrayList<HashMap> group = new ArrayList<HashMap>();

    int pos = -1;
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;

    ViewPager mViewPager;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getApplicationContext();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (mBluetoothAdapter.isEnabled()) {
            mChatService = new BluetoothChatService(this, mHandler);
        }

        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(
                getSupportFragmentManager());
        final ActionBar actionBar = getActionBar();

        assert actionBar != null;
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager
                .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        if (B) {
                            B = false;
                            Toast.makeText(MainActivity.this, "Round theme cancelled",
                                    Toast.LENGTH_SHORT).show();
                        }

                        if (position == 0) {
                        }
                        if (position == 1) {
                            updatespinner();
                            setupChat();
                        }
                        if (position == 2) {

                        }
                        if (position == 3) {
                            setupGroupMode();
                        }
                        actionBar.setSelectedNavigationItem(position);
                    }
                });

        /*for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {

            actionBar.addTab(actionBar.newTab()
                    .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                    .setTabListener(this));
        }*/
        actionBar.addTab(actionBar.newTab()
                .setText(mAppSectionsPagerAdapter.getPageTitle(0))
                .setTabListener(this).setIcon(R.drawable.btn_group_control_icon));
        actionBar.addTab(actionBar.newTab()
                .setText(mAppSectionsPagerAdapter.getPageTitle(1))
                .setTabListener(this).setIcon(R.drawable.btn_light_control_icon));
        actionBar.addTab(actionBar.newTab()
                .setText(mAppSectionsPagerAdapter.getPageTitle(2))
                .setTabListener(this).setIcon(R.drawable.btn_group_configure_icon));
        actionBar.addTab(actionBar.newTab()
                .setText(mAppSectionsPagerAdapter.getPageTitle(3))
                .setTabListener(this).setIcon(R.drawable.btn_sceario_control_icon));
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mBluetoothAdapter.isEnabled()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
            if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
                openOptionsDialog();
            }
        } else {
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        if (mChatService != null) {
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                mChatService.start();
            }
        }

    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");


        seekBar = (SeekBar) findViewById(R.id.seekBar1);
        seekBar.setMax(Max_Value);
        textView_progress = (TextView) findViewById(R.id.textView1);
        textView_progress.setText(seekBar.getProgress() + "%");
        seekBar.setProgress(0);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {


            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue,
                                          boolean fromUser) { // progress = progresValue; }
                textView_progress.setText(String.valueOf(progress1) + "%");
                if (seekBar.getProgress() == Min_Value) {
                    progress1 = Min_Value;
                    progrss_change_1();
                } else if (progress1 <= Max_Value) {
                    progress1 = seekBar.getProgress();
                    progrss_change_1();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                textView_progress.setText(String.valueOf(progress1) + "%");

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                textView_progress.setText(String.valueOf(progress1) + "%");
            }
        });

        mConversationArrayAdapter = new ArrayAdapter<String>(this,
                R.layout.message);
        ListView mConversationViewnew = (ListView) findViewById(R.id.Query_log);
        mConversationViewnew.setAdapter(mConversationArrayAdapter);

        Button light_off = (Button) findViewById(R.id.light_off);
        Button light_on = (Button) findViewById(R.id.light_on);
        // Button light_query = (Button) findViewById(R.id.light_query);
        Button send_free = (Button) findViewById(R.id.sendfreetyping);
        final EditText free_typing = (EditText) findViewById(R.id.freetyping);
        Switch ON_OFF = (Switch) findViewById(R.id.switch1);

        ON_OFF.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) {
                    light_on();
                } else {
                    light_off();
                }
            }
        });

        send_free.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
                    Toast.makeText(MainActivity.this, R.string.not_connected,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                mConversationArrayAdapter.clear();

                String free = free_typing.getText().toString();
                sendMessage(free);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ignored) {
                }
                //freetyping.setText("");

            }
        });
        light_on.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                light_on();
            }
        });
        light_off.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                light_off();
            }
        });

/*        light_query.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
                    Toast.makeText(MainActivity.this, R.string.not_connected,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                //new Query_Short().execute();
                new query_is_zigbee_exist_or_not_Async().execute();
            }
        });*/


    }

    @Override
    public synchronized void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        //super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null)
            mChatService.stop();

        super.onDestroy();
    }

    public static String byteArrayToHexString(byte[] b) {
        String data = "";
        for (byte aB : b) {
            data += Integer.toHexString((aB >> 4) & 0xf);
            data += Integer.toHexString(aB & 0xf);
        }
        return data;
    }

    public static byte[] hexStringToByteArray(String send1) {
        int len = send1.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(send1.charAt(i), 16) << 4) + Character
                    .digit(send1.charAt(i + 1), 16));
        }
        return data;
    }

    public void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = hexStringToByteArray(message);
            mChatService.write(send);
            if (D)
                Log.e(TAG, "send success");
            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }

    // The Handler that gets information back from the BluetoothChatService
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {

                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    GlobalVariable.saved = byteArrayToHexString(writeBuf);
                    if (D)
                        Log.e(TAG, "Send message = " + byteArrayToHexString(writeBuf));
                    mConversationArrayAdapter.add("Me:  "
                            + GlobalVariable.saved);
                    break;

                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    final String full_byte = byteArrayToHexString(readBuf).substring(0, GlobalVariable.bytes * 2);

                    // construct a string from the valid bytes in the buffer
                    mConversationArrayAdapter.add(mConnectedDeviceName + ":  "
                            + full_byte);
                    if (D)
                        Log.e(TAG, "MESSAGE READ " + full_byte);
                    //query router is Zigbee exist or not
                    if (full_byte.length() > 10) {
                        if (full_byte.substring(0, 16).equals("010000000a0098a9")) {
                            if (D)
                                Log.e(TAG, "No Zigbee");
                            query_daliaddr_without_zigbee();

                        } else if (full_byte.substring(0, 16).equals("010000000af800a9")) {
                            if (D)
                                Log.e(TAG, "Have Zigbee");
                        }
                    }
                    if (full_byte.length() > 20) {
                        helper = new DBHelper(getApplicationContext());
                        cursor = helper.select(TABLE_NAME_DALISLAVEADDRESS);

                        final int k;
                        final SQLiteDatabase db = helper.getWritableDatabase();


                        k = full_byte.length() - 26;
                        int l = k/2;
                        if (D)
                            Log.e(TAG, "Number of light : " +  l );
                        if (D)
                            Log.e(TAG, "cursor.getCount() : " +  cursor.getCount());
                        if (cursor.getCount()==0){
                           // db.delete(TABLE_NAME_DALISLAVEADDRESS, null, null);
                         for (int i = 0; i < k; i = i + 2) {
                                ContentValues values = new ContentValues();
                                values.put(FEILD_DALISLAVEADDRESS, full_byte.substring(i + 24, i + 26));
                                values.put(FEILD_NAME_DALI, full_byte.substring(i + 24, i + 26));
                                values.put(FEILD_SHORTADDRESS_DALI, full_byte.substring(9, 12));
                                db.insert(TABLE_NAME_DALISLAVEADDRESS, null, values);
                                try {
                                    Thread.sleep(150);
                                } catch (InterruptedException ignored) {
                                }
                                if (D)
                                    Log.e(TAG, "add light" );
                            }
                        }else{
                            if (cursor.getCount()!=l){
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Light Control")
                                        .setMessage("Gateway replied different numbers of dali slave with DB record")
                                        .setPositiveButton("Reset db data!", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                db.delete(TABLE_NAME_DALISLAVEADDRESS, null, null);
                                                for (int i = 0; i < k; i = i + 2) {
                                                    ContentValues values = new ContentValues();
                                                    values.put(FEILD_DALISLAVEADDRESS, full_byte.substring(i + 18, i + 20));
                                                    values.put(FEILD_NAME_DALI, full_byte.substring(i + 18, i + 20));
                                                    values.put(FEILD_SHORTADDRESS_DALI, full_byte.substring(9, 12));
                                                    db.insert(TABLE_NAME_DALISLAVEADDRESS, null, values);
                                                    try {
                                                        Thread.sleep(150);
                                                    } catch (InterruptedException ignored) {
                                                    }
                                                    if (D)
                                                        Log.e(TAG, "add light");
                                                }
                                            }
                                        })
                                        .setNegativeButton("Keep it and go reopen the gateway!", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Toast.makeText(getApplicationContext(), "Probably make APP crash!!", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .show();
                        }
                    }
                    }
                    /*//if Zigbee exist, query short address
                    if (byteArrayToHexString(readBuf).substring(0, GlobalVariable.bytes * 2).length() > 10) {
                        //if reply,
                        if (byteArrayToHexString(readBuf).substring(0, GlobalVariable.bytes * 2).substring(0, 10).equals("0100000009")) {
                            GlobalVariable.query_short_packet = byteArrayToHexString(readBuf).substring(0, GlobalVariable.bytes * 2);
                            //save query short's packet, and save in data base in query short address AsyncTask
                            try {
                                Thread.sleep(150);
                            } catch (InterruptedException ignored) {
                            }

                        }

                        if (byteArrayToHexString(readBuf).substring(0, GlobalVariable.bytes * 2).length() > 10 & cursor.getString(1)!=null) {
                            if (byteArrayToHexString(readBuf).substring(0, GlobalVariable.bytes * 2).substring(8, 12).equals(cursor.getString(1).substring(0, 2) + cursor.getString(1).substring(2, 4))) {
                                GlobalVariable.query_short_packet = byteArrayToHexString(readBuf).substring(0, GlobalVariable.bytes * 2);
                                helper = new DBHelper(getApplicationContext());
                                cursor = helper.select(TABLE_NAME_SHORT_ADDRESS);
                                if (GlobalVariable.i == 0) {
                                    SQLiteDatabase db = helper.getWritableDatabase();
                                    db.delete(TABLE_NAME_DALISLAVEADDRESS, null, null);
                                }
                                if (GlobalVariable.i < cursor.getCount()) {
                                    GlobalVariable.query_short_packet = byteArrayToHexString(readBuf).substring(0, GlobalVariable.bytes * 2);
                                    if (GlobalVariable.query_short_packet.length() > 20) {

                                        int k, i;
                                        SQLiteDatabase db = helper.getWritableDatabase();

                                        k = GlobalVariable.query_short_packet.length() - 20;
                                        for (i = 0; i < k; i = i + 2) {

                                            ContentValues values = new ContentValues();
                                            values.put(FEILD_DALISLAVEADDRESS, GlobalVariable.query_short_packet.substring(i + 18, i + 20));
                                            values.put(FEILD_SHORTADDRESS_DALI, GlobalVariable.query_short_packet.substring(9, 12));
                                            db.insert(TABLE_NAME_DALISLAVEADDRESS, null, values);
                                            try {
                                                Thread.sleep(150);
                                            } catch (InterruptedException ignored) {
                                            }
                                        }
                                    }

                                    //new Query_dali_Async().execute();
                                    query_daliaddr_with_zigbee();
                                } else if ((GlobalVariable.i == cursor.getCount())) {
                                    GlobalVariable.query_short_packet = byteArrayToHexString(readBuf).substring(0, GlobalVariable.bytes * 2);
                                    if (GlobalVariable.query_short_packet.length() > 20) {
                                        int k, i;
                                        SQLiteDatabase db = helper.getWritableDatabase();
                                        k = GlobalVariable.query_short_packet.length() - 20;
                                        for (i = 0; i < k; i = i + 2) {
                                            ContentValues values = new ContentValues();
                                            values.put(FEILD_DALISLAVEADDRESS, GlobalVariable.query_short_packet.substring(i + 18, i + 20));
                                            db.insert(TABLE_NAME_DALISLAVEADDRESS, null, values);
                                        }
                                    }
                                }
                            }
                        }
                    }*/
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(),
                            "Connected to " + mConnectedDeviceName,
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(),
                            msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
                            .show();
                    break;

            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D)
            Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    setupChat();
// Initialize the BluetoothChatService to perform bluetooth connections
                    mChatService = new BluetoothChatService(this, mHandler);
                    // Initialize the buffer for outgoing messages
                    mOutStringBuffer = new StringBuffer("");
                    String address = data.getExtras().getString(
                            DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter
                            .getRemoteDevice(address);
                    // Attempt to connect to the device
                    mChatService.connect(device);
                }
                break;

            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session

                    setupChat();
                    if (mBluetoothAdapter.isEnabled()) {
                        mChatService = new BluetoothChatService(this, mHandler);
                        openOptionsDialog();
                    }
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    finish();
                }


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    public void openOptionsDialog() {

        final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setCancelable(false);
        dialog.setTitle(R.string.about_title);


        if (String.valueOf(mChatService.getState()).equals("0")) {
            String state_bt = "Bluetooth State : " + "Not Connected";
            dialog.setMessage(state_bt);
            dialog.setNegativeButton(R.string.ok_label,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface, int i) {
                            if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
                                Intent serverIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                            }
                        }
                    });
            dialog.setPositiveButton(R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface, int i) {
                            finish();
                        }

                    });
            dialog.show();
        }

        if (String.valueOf(mChatService.getState()).equals("2")) {
            String state_bt = "Bluetooth State : " + "Connecting...";
            dialog.setMessage(state_bt);
            dialog.setPositiveButton(R.string.ok_label_1,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface, int i) {
                            new wait_and_connect().execute();

                        }
                    });
            dialog.show();
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scan:
                // Launch the DeviceListActivity to see devices and do scan
                if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
                    Intent serverIntent = new Intent(this, DeviceListActivity.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                    return true;
                } else {
                    Toast.makeText(MainActivity.this, "You have connected to a device",
                            Toast.LENGTH_SHORT).show();
                    return true;

                }
            case R.id.about:
                LayoutInflater inflater = LayoutInflater.from(this);
                final View v = inflater.inflate(R.layout.hello, null);
                final AlertDialog.Builder dialog = new AlertDialog.Builder(this);


                dialog.setCancelable(false);
                dialog.setTitle(R.string.about_title);
                dialog.setView(v);
                dialog.setPositiveButton(R.string.ok_label_1,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int i) {

                            }
                        });
                dialog.show();
                return true;
            case R.id.action_settings:
                if (!B) {
                    // Launch the DeviceListActivity to see devices and do scan
                    LayoutInflater inflater1 = LayoutInflater.from(this);
                    final View v1 = inflater1.inflate(R.layout.login, null);
                    final AlertDialog.Builder dialog1 = new AlertDialog.Builder(this);
                    final EditText password = (EditText) v1.findViewById(R.id.editText2);

                    dialog1.setCancelable(false);
                    dialog1.setTitle("Enter the password");
                    dialog1.setView(v1);
                    dialog1.setPositiveButton(R.string.ok_label_1,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {

                                    if (password.getText().toString().equals(GlobalVariable.VAT_number)) {
                                        fadetime();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Error,wrong password!!",
                                                Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                    dialog1.show();
                    return true;
                } else {
                    Toast.makeText(MainActivity.this, "Cannel round theme before set fadetime",
                            Toast.LENGTH_SHORT).show();
                }
            case R.id.action_theme:
                LayoutInflater inflater1 = LayoutInflater.from(this);
                final View v2 = inflater1.inflate(R.layout.login, null);
                final AlertDialog.Builder dialog1 = new AlertDialog.Builder(this);
                final EditText password1 = (EditText) v2.findViewById(R.id.editText2);

                dialog1.setCancelable(false);
                dialog1.setTitle("Enter the password");
                dialog1.setView(v2);
                dialog1.setPositiveButton(R.string.ok_label_1,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int i) {

                                if (password1.getText().toString().equals(GlobalVariable.VAT_number)) {
                                    theme_to_who();
                                } else {
                                    Toast.makeText(MainActivity.this, "Error,wrong password!!",
                                            Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                dialog1.show();
                return true;
            case R.id.action_light:
                LayoutInflater inflater2 = LayoutInflater.from(this);
                final View v3 = inflater2.inflate(R.layout.login, null);
                final AlertDialog.Builder dialog2 = new AlertDialog.Builder(this);
                final EditText password2 = (EditText) v3.findViewById(R.id.editText2);

                dialog2.setCancelable(false);
                dialog2.setTitle("Enter the password");
                dialog2.setView(v3);
                dialog2.setPositiveButton(R.string.ok_label_1,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int i) {

                                if (password2.getText().toString().equals(GlobalVariable.VAT_number)) {
                                    light_configure();
                                } else {
                                    Toast.makeText(MainActivity.this, "Error,wrong password!!",
                                            Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                dialog2.show();
                return true;
        }
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        pos = info.position + 1;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }


    @Override
    public void onTabUnselected(ActionBar.Tab tab,
                                FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab,
                              FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab,
                                FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the primary sections of the app.
     */
    public class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    // The first section of the app is the most interesting -- it
                    // offers
                    // a launchpad into the other demonstrations in this example
                    // application.
                    Fragment fragment3 = new ListActivityFragment();
                    Bundle args3 = new Bundle();
                    args3.putInt(ARG_SECTION_NUMBER, i + 1);
                    fragment3.setArguments(args3);
                    return new ListActivityFragment();
                case 1:
                    Fragment fragment2 = new LaunchpadSectionFragment();
                    Bundle args2 = new Bundle();
                    args2.putInt(LaunchpadSectionFragment.ARG_SECTION_NUMBER2, i + 1);
                    fragment2.setArguments(args2);

                    return new LaunchpadSectionFragment();
                case 2:
                    Fragment fragment_expand = new ExpandableListFragment();
                    Bundle args_expand = new Bundle();
                    args_expand.putInt(ExpandableListFragment.ARG_SECTION_NUMBER4, i + 1);
                    fragment_expand.setArguments(args_expand);

                    return new ExpandableListFragment();

                default:
                    // The other sections of the app are dummy placeholders.
                    Fragment GroupMode_1 = new GroupMode();
                    Bundle args_GroupMode = new Bundle();
                    args_GroupMode.putInt(GroupMode.ARG_SECTION_NUMBER5, i + 1);
                    GroupMode_1.setArguments(args_GroupMode);

                    return GroupMode_1;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
        //設置三個Tab

        @Override
        public CharSequence getPageTitle(int position) {
            //設置三個Tab的名稱
            if (position == 0) {
                return "  Group Control";
            }
            if (position == 1) {
                return "  Light Control ";
            }

            if (position == 2) {
                return "  Group Configure";
            }
            if (position == 3) {
                return "  Scenario Control";
            }
            return "Section " + (position + 1);
        }
    }
//-----------------------------------------------------------------------------------------------------//
//-----------------------------------------------------------------------------------------------------//
//-----------------------------------------------------------------------------------------------------//
//-----------------------------------------------------------------------------------------------------//


    public class ExpandableListFragment extends ListFragment {
        public static final String ARG_SECTION_NUMBER4 = "section_number4";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            return inflater.inflate(
                    R.layout.expandable_list_fragment, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            final ExpandableListView mListView = (ExpandableListView) findViewById(R.id.mListView);

            getExpandablelist();
            mListView.setAdapter(mAdapter);
            mListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
                @Override
                public void onGroupExpand(int group_position) {
                    if (D)
                        Log.e(TAG, " group_position : " + group_position);
                }
            });
            if (GroupList.size() > 0) {
                for (int i = 0; i < GroupList.size(); i++) {
                    mListView.expandGroup(i);
                }
            }

            mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    LayoutInflater inflater2 = LayoutInflater.from(getActivity());
                    final View v3 = inflater2.inflate(R.layout.login, null);
                    final AlertDialog.Builder dialog2 = new AlertDialog.Builder(getActivity());
                    final EditText password2 = (EditText) v3.findViewById(R.id.editText2);

                    dialog2.setCancelable(false);
                    dialog2.setTitle("Enter the password");
                    dialog2.setView(v3);
                    dialog2.setPositiveButton(R.string.ok_label_1,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {

                                    if (password2.getText().toString().equals(GlobalVariable.VAT_number)) {
                                        helper = new DBHelper(getApplicationContext());
                                        cursor = helper.select(TABLE_NAME_GROUP);

                                        final int pos = i;
                                        cursor.moveToPosition(pos);
                                        //final String group = cursor.getString(1);
                                        new AlertDialog.Builder(getActivity())
                                                .setItems(new String[]{"Edit", "Delete"}, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface arg0, int which) {
                                                        // TODO Auto-generated method stub
                                                        switch (which) {
                                                            case 0:
                                                                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                                                                final View v = inflater.inflate(R.layout.edittext, null);
                                                                final EditText editText = (EditText) (v.findViewById(R.id.editText));
                                                                //editText.setText(group);
                                                                new AlertDialog.Builder(getActivity())
                                                                        .setTitle("Editing...")
                                                                        .setView(v)
                                                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(DialogInterface dialog, int which) {
                                                                                if (!editText.getText().equals("")) {
                                                                                    helper = new DBHelper(getApplicationContext());
                                                                                    cursor = helper.select(TABLE_NAME_GROUP);
                                                                                    if (pos > GroupList.size()) {
                                                                                        cursor.moveToLast();
                                                                                    } else {
                                                                                        cursor.moveToPosition(pos);
                                                                                    }
                                                                                    helper.update(cursor.getInt(0), TABLE_NAME_GROUP, FEILD_Group, editText.getText().toString());
                                                                                    cursor.requery();
                                                                                    getExpandablelist();
                                                                                    mListView.setAdapter(mAdapter);

                                                                                }
                                                                            }
                                                                        })
                                                                        .show();
                                                                break;
                                                            case 1:
                                                                new AlertDialog.Builder(getActivity())
                                                                        .setTitle("Delete the group.")
                                                                        .setMessage("Are you sure to delete this group？")
                                                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                                                                            @Override
                                                                            public void onClick(DialogInterface dialog, int which) {
                                                                                // TODO Auto-generated method stub
                                                                                helper = new DBHelper(getApplicationContext());
                                                                                cursor = helper.select(TABLE_NAME_GROUP);
                                                                                Log.e("which", Integer.toString(pos));
                                                                                if (pos > GroupList.size()) {
                                                                                    cursor.moveToLast();
                                                                                } else {
                                                                                    cursor.moveToPosition(pos);
                                                                                }
                                                                                helper.delete(cursor.getInt(0), TABLE_NAME_GROUP, FEILD_Group, "");
                                                                                cursor.requery();
                                                                                getExpandablelist();
                                                                                mListView.setAdapter(mAdapter);
                                                                                helper.close();
                                                                                cursor.close();
                                                                            }
                                                                        })
                                                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {

                                                                            @Override
                                                                            public void onClick(DialogInterface dialog, int which) {
                                                                                // TODO Auto-generated method stub

                                                                            }
                                                                        })
                                                                        .show();
                                                                break;
                                                        }
                                                    }
                                                }).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Error,wrong password!!",
                                                Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                    dialog2.show();


                    return false;
                }
            });



            mListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                        if(D)
                            Log.e("Expand", String.valueOf(getText(i1)));

                    return false;
                }
            });


            Button new_group = (Button) findViewById(R.id.new_group);
            new_group.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    LayoutInflater inflater2 = LayoutInflater.from(getActivity());
                    final View v3 = inflater2.inflate(R.layout.login, null);
                    final AlertDialog.Builder dialog2 = new AlertDialog.Builder(getActivity());
                    final EditText password2 = (EditText) v3.findViewById(R.id.editText2);

                    dialog2.setCancelable(false);
                    dialog2.setTitle("Enter the password");
                    dialog2.setView(v3);
                    dialog2.setPositiveButton(R.string.ok_label_1,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {

                                    if (password2.getText().toString().equals(GlobalVariable.VAT_number)) {
                                        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                                        final View v = inflater.inflate(R.layout.edittext, null);

                                        new AlertDialog.Builder(getActivity())
                                                .setTitle("請輸入群組名稱")
                                                .setView(v)
                                                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        EditText editText = (EditText) (v.findViewById(R.id.editText));
                                                        if (!editText.getText().toString().equals("")) {
                                                            helper = new DBHelper(getApplicationContext());
                                                            cursor = helper.select(TABLE_NAME_GROUP);
                                                            helper.insert(TABLE_NAME_GROUP, FEILD_Group, editText.getText().toString());

                                                            cursor.requery();
                                                            getExpandablelist();
                                                            mListView.setAdapter(mAdapter);

                                                        }
                                                    }
                                                })
                                                .show();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Error,wrong password!!",
                                                Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                    dialog2.show();



                }
            });

        }

        public void getExpandablelist() {

            GroupList.clear();
            DaliList.clear();

            helper = new DBHelper(getApplicationContext());
            cursor = helper.select(TABLE_NAME_GROUP);
            if (cursor.getCount() > 0) {
                int i;
                for (i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    GroupList.add(cursor.getString(1));
                }
            }
            helper.close();
            cursor.close();

            helper = new DBHelper(getApplicationContext());
            cursor = helper.select(TABLE_NAME_DALISLAVEADDRESS);
            if (cursor.getCount() > 0) {
                int i;
                for (i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    DaliList.add(cursor.getString(3));
                }
            }
            helper.close();
            cursor.close();

            for (int i = 0; i < GroupList.size(); i++) {
                Map<String, String> curGroupMap = new HashMap<String, String>();
                groupData.add(curGroupMap);
                curGroupMap.put(NAME, GroupList.get(i));

                final List<Map<String, String>> children = new ArrayList<Map<String, String>>();
                for (int j = 0; j < DaliList.size(); j++) {
                    Map<String, String> curChildMap = new HashMap<String, String>();
                    children.add(curChildMap);
                    curChildMap.put(NAME, DaliList.get(j));
                }
                childData.add(children);
                if (D)
                    Log.e(TAG, "ExpandableListFragment children " + children);
            }

            isSelectedList = new ArrayList<Map<Integer, Boolean>>();

            for (int i = 0, icount = GroupList.size(); i < icount; i++) {
                Map<Integer, Boolean> map = new HashMap<Integer, Boolean>();
                for (int j = 0, jcount = childData.get(i).size(); j < jcount; j++) {
                    map.put(j, false);
                }
                isSelectedList.add(map);
            }

            mAdapter = new BaseExpandableListAdapter() {
                @Override
                public int getGroupCount() {
                    return GroupList.size();
                }

                @Override
                public int getChildrenCount(int i) {
                    return DaliList.size();
                }

                @Override
                public Object getGroup(int i) {
                    return groupData.get(i);
                }

                @Override
                public Object getChild(int i, int i2) {
                    return childData.get(i).get(i2);
                }

                @Override
                public long getGroupId(int i) {
                    return i;
                }

                @Override
                public long getChildId(int i, int i2) {
                    return i2;
                }

                @Override
                public boolean hasStableIds() {
                    return true;
                }

                @Override
                public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
                    if (view == null) {
                        LayoutInflater infalInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        view = infalInflater.inflate(R.layout.group, null);
                    }
                    TextView group = (TextView) view.findViewById(R.id.group);
                    group.setText(GroupList.get(i));
                    return view;
                }

                @Override
                public View getChildView(final int groupPosition, final int childPosition, boolean b, View view, ViewGroup viewGroup) {

                    if (view == null) {
                        LayoutInflater infalInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        view = infalInflater.inflate(R.layout.baseadapter_dali, null);

                    }
                    getinit();
                    final CheckBox cb = (CheckBox) view.findViewById(R.id.checkBox);

                    cb.setChecked(isSelectedList.get(groupPosition).get(childPosition));
                    cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                            if (b) {
                                isSelectedList.get(groupPosition).put(childPosition, true);
                                if (D)
                                    Log.e(TAG, "isSelectedList ON TOUCH " + isSelectedList);

                            } else {
                                isSelectedList.get(groupPosition).put(childPosition, false);
                                if (D)
                                    Log.e(TAG, "isSelectedList ON TOUCH " + isSelectedList);
                            }
                               /* helper.close();
                                cursor.close();
                                if (D)
                                    Log.e(TAG, "isSelectedList save to db : " + isSelectedList);
                                helper = new DBHelper(getApplicationContext());
                                cursor = helper.select(TABLE_NAME_DALICOMMAND);
                                if (cursor.getCount() == 0) {
                                    SQLiteDatabase db = helper.getWritableDatabase();
                                    ContentValues values = new ContentValues();
                                    values.put(FEILD_DALICOMMAND, String.valueOf(isSelectedList));
                                    db.insert(TABLE_NAME_DALICOMMAND, null, values);
                                } else {
                                    cursor.moveToPosition(0);
                                    helper.update(cursor.getInt(0), TABLE_NAME_DALICOMMAND, FEILD_DALICOMMAND, isSelectedList.toString());
                                }
                                helper.close();
                                cursor.close();
                                if (D)
                                    Log.e(TAG, "isSelectedList =======> " + isSelectedList);*/
                        }
                    });


                    TextView dali = (TextView) view.findViewById(R.id.dali_name);
                    dali.setText(DaliList.get(childPosition));


                    Button get_check = (Button) findViewById(R.id.get_check);
                    get_check.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {

                           /* LayoutInflater inflater1 = LayoutInflater.from(getActivity());
                            final View v2 = inflater1.inflate(R.layout.login, null);
                            final AlertDialog.Builder dialog1 = new AlertDialog.Builder(getActivity());
                            final EditText password1 = (EditText) v2.findViewById(R.id.editText2);

                            dialog1.setCancelable(false);
                            dialog1.setTitle("Enter the password");
                            dialog1.setView(v2);
                            dialog1.setPositiveButton(R.string.ok_label_1,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialoginterface, int i) {

                                            if (password1.getText().toString().equals(GlobalVariable.VAT_number)) {*/
                                                helper.close();
                                                cursor.close();
                                                if (D)
                                                    Log.e(TAG, "isSelectedList save to db : " + isSelectedList);
                                                helper = new DBHelper(getApplicationContext());
                                                cursor = helper.select(TABLE_NAME_DALICOMMAND);
                                                if (cursor.getCount() == 0) {
                                                    SQLiteDatabase db = helper.getWritableDatabase();
                                                    ContentValues values = new ContentValues();
                                                    values.put(FEILD_DALICOMMAND, String.valueOf(isSelectedList));
                                                    db.insert(TABLE_NAME_DALICOMMAND, null, values);
                                                } else {
                                                    cursor.moveToPosition(0);
                                                    helper.update(cursor.getInt(0), TABLE_NAME_DALICOMMAND, FEILD_DALICOMMAND, isSelectedList.toString());
                                                }
                                                new reset_one_by_one().execute();
                                           /* } else {
                                                Toast.makeText(MainActivity.this, "Error,wrong password!!",
                                                        Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });
                            dialog1.show();*/


                        }
                    });


                    Button reset_group = (Button) findViewById(R.id.reset_group);
                    reset_group.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            /*
                            //aa 55 08 01 00 00 00 dd 00 01 FF
                            String reset_group = GlobalVariable.by1_AA + GlobalVariable.by2_55 + GlobalVariable.by3_07 + GlobalVariable.by4_01 + "00" + "00" + "00" +
                                    "df" + "00" + "ff";
                            int checksum = (Integer.parseInt(GlobalVariable.by1_AA, 16)
                                    + Integer.parseInt(GlobalVariable.by2_55, 16)
                                    + Integer.parseInt(GlobalVariable.by3_07, 16)
                                    + Integer.parseInt(GlobalVariable.by4_01, 16)
                                    + Integer.parseInt("df", 16) +
                                    Integer.parseInt("ff", 16)) % 256;
                            String send_reset = reset_group + Integer.toHexString(checksum);
                            sendMessage(send_reset);
                            try {
                                Thread.sleep(150);
                            } catch (InterruptedException ignored) {
                            }
                            isSelectedList = new ArrayList<Map<Integer, Boolean>>();
                            for (int i = 0, icount = GroupList.size(); i < icount; i++) {
                                Map<Integer, Boolean> map = new HashMap<Integer, Boolean>();
                                for (int j = 0, jcount = childData.get(i).size(); j < jcount; j++) {
                                    map.put(j, false);
                                }
                                isSelectedList.add(map);
                            }
                            if(D)
                                Log.e(TAG,"isSelectedList"+isSelectedList.toString());
                            cb.setChecked(isSelectedList.get(groupPosition).get(childPosition));
                            helper = new DBHelper(getApplicationContext());
                            cursor = helper.select(TABLE_NAME_DALICOMMAND);
                            cursor.moveToPosition(0);
                            helper.update(cursor.getInt(0), TABLE_NAME_DALICOMMAND, FEILD_DALICOMMAND, isSelectedList.toString());
                            notifyDataSetChanged();*/

                            ///////////////////////////////////////////////////////////////////////////////

                            //aa 55 08 01 00 00 00 dd 00 01~03 00 xx(checksum)
                            /*new reset_one_by_one().execute();
                            isSelectedList = new ArrayList<Map<Integer, Boolean>>();
                            for (int i = 0, icount = GroupList.size(); i < icount; i++) {
                                Map<Integer, Boolean> map = new HashMap<Integer, Boolean>();
                                for (int j = 0, jcount = childData.get(i).size(); j < jcount; j++) {
                                    map.put(j, false);
                                }
                                isSelectedList.add(map);
                            }
                            if (D)
                                Log.e(TAG, "isSelectedList" + isSelectedList.toString());
                            cb.setChecked(isSelectedList.get(groupPosition).get(childPosition));
                            helper = new  DBHelper(getApplicationContext());
                            cursor = helper.select(TABLE_NAME_DALICOMMAND);
                            cursor.moveToPosition(0);
                            helper.update(cursor.getInt(0), TABLE_NAME_DALICOMMAND, FEILD_DALICOMMAND, isSelectedList.toString());
                            notifyDataSetChanged();
                            cursor.close();
                            helper.close();*/

                        }
                    });

                    return view;
                }

                @Override
                public boolean isChildSelectable(int i, int i2) {
                    return true;
                }
            };


        }

        public class reset_one_by_one extends AsyncTask<Void, Integer, String> {
            @Override
            protected String doInBackground(Void... arg0) {
                helper = new DBHelper(getApplicationContext());
                cursor = helper.select(TABLE_NAME_GROUP);
                for (int j = 0; j < cursor.getCount(); j++) {
                    cursor = helper.select(TABLE_NAME_DALISLAVEADDRESS);

                    if (j > 10) {

                        for (int k = 0; k < cursor.getCount(); k++) {
                            cursor.moveToPosition(k);
                            String reset_group = GlobalVariable.by1_AA + GlobalVariable.by2_55 + "08" + GlobalVariable.by4_01 + "00" + "00" + "00" +
                                    "dd" + "00" + cursor.getString(1) + j;
                            int checksum = (Integer.parseInt(GlobalVariable.by1_AA, 16)
                                    + Integer.parseInt(GlobalVariable.by2_55, 16)
                                    + Integer.parseInt("08", 16)
                                    + Integer.parseInt(GlobalVariable.by4_01, 16)
                                    + Integer.parseInt("dd", 16) +
                                    Integer.parseInt(cursor.getString(k), 16) +
                                    Integer.parseInt(String.valueOf(j), 16)) % 256;

                            if (checksum > 15) {

                                String send_reset = reset_group + "0" + Integer.toHexString(checksum);
                               /* if (D)
                                    Log.e(TAG, "checksum =======> " + checksum);
                                if (D)
                                    Log.e(TAG, "send_reset =======> " + send_reset);*/
                                //sendMessage(send_reset);
                                try {
                                    Thread.sleep(300);
                                } catch (InterruptedException ignored) {
                                }

                            } else {

                                String send_reset = reset_group + Integer.toHexString(checksum);
                                /*if (D)
                                    Log.e(TAG, "checksum =======> " + checksum);
                                if (D)
                                    Log.e(TAG, "send_reset =======> " + send_reset);*/
                                //sendMessage(send_reset);
                                try {
                                    Thread.sleep(300);
                                } catch (InterruptedException ignored) {
                                }

                            }
                        }

                    } else {

                        for (int k = 0; k < cursor.getCount(); k++) {
                            cursor.moveToPosition(k);
                            String reset_group = GlobalVariable.by1_AA + GlobalVariable.by2_55 + "07" + "06" + "00" + "00"
                                    + cursor.getString(1) + "7" + j +"00";
                            if (D)
                                Log.e(TAG, "Dali address =======> " + cursor.getString(1));

                            int checksum = (Integer.parseInt(GlobalVariable.by1_AA, 16)
                                    + Integer.parseInt(GlobalVariable.by2_55, 16)
                                    + Integer.parseInt("07", 16)
                                    + Integer.parseInt("06", 16)
                                    + Integer.parseInt("70", 16) +
                                    Integer.parseInt(cursor.getString(1), 16) +
                                    Integer.parseInt(String.valueOf(j), 16)) % 256;
                            if (checksum > 15) {
                                String send_reset = reset_group + Integer.toHexString(checksum);
                               /* if (D)
                                    Log.e(TAG, "checksum =======> " + checksum);
                               */
                                if (D)
                                    Log.e(TAG, "send_reset =======> " + send_reset);
                                sendMessage(send_reset);
                                try {
                                    Thread.sleep(300);
                                } catch (InterruptedException ignored) {
                                }
                            } else {
                                String send_reset = reset_group + "0" + Integer.toHexString(checksum);
                                /*if (D)
                                    Log.e(TAG, "checksum =======> " + checksum);
                                */
                                if (D)
                                    Log.e(TAG, "send_reset =======> " + send_reset);
                                sendMessage(send_reset);
                                try {
                                    Thread.sleep(300);
                                } catch (InterruptedException ignored) {
                                }
                            }

                        }

                    }
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mDialog = ProgressDialog.show(MainActivity.this,
                        "processing...", "", true);
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                mDialog.dismiss();
                new setup_group().execute();
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
            }
        }

        public void getinit() {
            helper = new DBHelper(getApplicationContext());
            cursor = helper.select(TABLE_NAME_DALICOMMAND);
            cursor.moveToPosition(0);

            if (cursor.getCount() > 0) {
                isSelectedList.clear();
                String s = cursor.getString(1).substring(1, cursor.getString(1).length() - 1);
                String x = s.replace("}, ", "} , ");

                String[] pairs = x.split(" , ");


                if (pairs.length != GroupList.size()) {
                    isSelectedList = new ArrayList<Map<Integer, Boolean>>();
                    for (int i = 0, icount = GroupList.size(); i < icount; i++) {
                        Map<Integer, Boolean> map = new HashMap<Integer, Boolean>();
                        for (int j = 0, jcount = childData.get(i).size(); j < jcount; j++) {
                            map.put(j, false);
                        }
                        isSelectedList.add(map);
                    }
                } else {
                    isSelectedList = new ArrayList<Map<Integer, Boolean>>();


                    for (int k = 0; k < GroupList.size(); k++) {
                        String pair = pairs[k];
                        String[] pairs2 = pair.substring(1, pair.length() - 1).split(", ");
                        isChecked = new HashMap<Integer, Boolean>();
                        for (int q = 0; q < pairs2.length; q++) {
                            String pair2 = pairs2[q];
                            String[] keyValue = pair2.split("=");
                            isChecked.put(Integer.valueOf(keyValue[0]), Boolean.valueOf(keyValue[1]));
                            // if(D)
                            //     Log.e(TAG,"isChecked" + Arrays.toString(pairs));
                        }

                        isSelectedList.add(isChecked);

                    }
                }
            }
            helper.close();
            cursor.close();
        }
    }
//-----------------------------------------------------------------------------------------------------//
//-----------------------------------------------------------------------------------------------------//
//-----------------------------------------------------------------------------------------------------//
//-----------------------------------------------------------------------------------------------------//

    /**
     * A fragment that launches other parts of the demo application.
     */
    public class LaunchpadSectionFragment extends Fragment {
        public static final String ARG_SECTION_NUMBER2 = "section_number2";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            return inflater.inflate(
                    R.layout.light_control_panel, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            updatespinner();
            //當created的時候 執行updatespinner
            Button theme_configure = (Button) view.findViewById(R.id.button2);

            theme_configure.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    LayoutInflater inflater1 = LayoutInflater.from(getActivity());
                    final View v2 = inflater1.inflate(R.layout.login, null);
                    final AlertDialog.Builder dialog1 = new AlertDialog.Builder(getActivity());
                    final EditText password1 = (EditText) v2.findViewById(R.id.editText2);

                    dialog1.setCancelable(false);
                    dialog1.setTitle("Enter the password");
                    dialog1.setView(v2);
                    dialog1.setPositiveButton(R.string.ok_label_1,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {

                                    if (password1.getText().toString().equals(GlobalVariable.VAT_number)) {
                                        GlobalVariable.Light_percentage_choice = "00";

                                        LayoutInflater inflater = LayoutInflater.from(getActivity());
                                        final View v = inflater.inflate(R.layout.group_mode_settings, null);

                                        Spinner Theme1 = (Spinner) v.findViewById(R.id.spinner);
                                        //Spinner Light_Percentage = (Spinner) v.findViewById(R.id.spinner2);

                                        ArrayAdapter<String> Themelist;
                                        final String[] Theme = {"Reading", "Sleep", "Movie", "Meal", "Break", "Round", "Test1", "Test2", "Test3"};
                                        Themelist = new ArrayAdapter<String>(getActivity(), R.layout.my_spinner, Theme);
                                        Theme1.setAdapter(Themelist);

                                        Theme1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                            @Override
                                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                                if (Theme[i].equals("Reading")) {
                                                    GlobalVariable.Theme_choice = "00";
                                                }
                                                if (Theme[i].equals("Sleep")) {
                                                    GlobalVariable.Theme_choice = "01";
                                                }
                                                if (Theme[i].equals("Movie")) {
                                                    GlobalVariable.Theme_choice = "02";
                                                }
                                                if (Theme[i].equals("Meal")) {
                                                    GlobalVariable.Theme_choice = "03";
                                                }
                                                if (Theme[i].equals("Break")) {
                                                    GlobalVariable.Theme_choice = "04";
                                                }
                                                if (Theme[i].equals("Round")) {
                                                    GlobalVariable.Theme_choice = "05";
                                                }
                                                if (Theme[i].equals("Test1")) {
                                                    GlobalVariable.Theme_choice = "06";
                                                }
                                                if (Theme[i].equals("Test2")) {
                                                    GlobalVariable.Theme_choice = "07";
                                                }
                                                if (Theme[i].equals("Test3")) {
                                                    GlobalVariable.Theme_choice = "08";
                                                }
                                            }

                                            @Override
                                            public void onNothingSelected(AdapterView<?> adapterView) {

                                            }
                                        });

                                        final TextView percentage_text = (TextView) v.findViewById(R.id.textView11);
                                        final SeekBar percentage = (SeekBar) v.findViewById(R.id.seekBar2);
                                        percentage.setMax(Max_Value);
                                        percentage.setProgress(0);

                                        percentage.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                                            int Progress = 0;

                                            @Override
                                            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                                                int stepSize = 10;
                                                Progress = percentage.getProgress();
                                                Progress = (Progress / stepSize) * stepSize;
                                                seekBar.setProgress(Progress);

                                                percentage_text.setText("" + Progress + "%");
                                            }

                                            @Override
                                            public void onStartTrackingTouch(SeekBar seekBar) {

                                            }

                                            @Override
                                            public void onStopTrackingTouch(SeekBar seekBar) {
                                                if (D)
                                                    Log.e(TAG, "Fuck progrss = " + Progress);
                                                if (Progress == 0) {
                                                    GlobalVariable.Light_percentage_choice = "00";
                                                }
                                                if (Progress == 10) {
                                                    GlobalVariable.Light_percentage_choice = "AA";
                                                }
                                                if (Progress == 20) {
                                                    GlobalVariable.Light_percentage_choice = "C4";
                                                }
                                                if (Progress == 30) {
                                                    GlobalVariable.Light_percentage_choice = "D2";
                                                }
                                                if (Progress == 40) {
                                                    GlobalVariable.Light_percentage_choice = "DD";
                                                }
                                                if (Progress == 50) {
                                                    GlobalVariable.Light_percentage_choice = "E5";
                                                }
                                                if (Progress == 60) {
                                                    GlobalVariable.Light_percentage_choice = "EB";
                                                }
                                                if (Progress == 70) {
                                                    GlobalVariable.Light_percentage_choice = "F1";
                                                }
                                                if (Progress == 80) {
                                                    GlobalVariable.Light_percentage_choice = "F6";
                                                }
                                                if (Progress == 90) {
                                                    GlobalVariable.Light_percentage_choice = "FA";
                                                }
                                                if (Progress == 100) {
                                                    GlobalVariable.Light_percentage_choice = "FE";
                                                }
                                                if (D)
                                                    Log.e(TAG, "Fuck GlobalVariable.Light_percentage_choice = " + GlobalVariable.Light_percentage_choice);
                                            }
                                        });

                                        helper = new DBHelper(getApplicationContext());
                                        cursor = helper.select(TABLE_NAME_DALISLAVEADDRESS);
                                        if (cursor.getCount() > 0) {
                                            new AlertDialog.Builder(getActivity())
                                                    .setTitle("Light " + GlobalVariable.OnSpinnerItemSelected + " Theme Configure")
                                                    .setView(v)
                                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                            //int position_final = position + position + 1;
                                                            String send_mode_1 = GlobalVariable.by1_AA + GlobalVariable.by2_55 + "09" + GlobalVariable.by4_01 + "00" + "00" + "00" + "e1" + "00"
                                                                    + GlobalVariable.OnSpinnerItemSelected + GlobalVariable.Theme_choice + GlobalVariable.Light_percentage_choice;
                                                            int checksum = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
                                                                    + (Integer.parseInt(GlobalVariable.by2_55, 16))
                                                                    + (Integer.parseInt("09", 16))
                                                                    + (Integer.parseInt(GlobalVariable.by4_01, 16))
                                                                    + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelected, 16))
                                                                    //     + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(0, 2), 16))
                                                                    //      + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(2, 4), 16))
                                                                    + (Integer.parseInt("e1" +
                                                                    "", 16))
                                                                    + (Integer.parseInt(GlobalVariable.Theme_choice, 16))
                                                                    + (Integer.parseInt(GlobalVariable.Light_percentage_choice, 16))) % 256;
                                                            if (checksum > 15) {
                                                                if (D)
                                                                    Log.e(TAG, "send_set_theme_checksum : " + checksum);
                                                                String send_group_on1 = send_mode_1 + Integer.toHexString(checksum);
                                                                if (D)
                                                                    Log.e(TAG, "send_set_theme : " + send_group_on1);
                                                                sendMessage(send_group_on1);
                                                                try {
                                                                    Thread.sleep(150);
                                                                } catch (InterruptedException ignored) {
                                                                }
                                                            } else {
                                                                if (D)
                                                                    Log.e(TAG, "send_set_theme_checksum : " + checksum);
                                                                String send_group_on1 = send_mode_1 + "0" + Integer.toHexString(checksum);
                                                                if (D)
                                                                    Log.e(TAG, "send_set_theme : " + send_group_on1);
                                                                sendMessage(send_group_on1);
                                                                try {
                                                                    Thread.sleep(150);
                                                                } catch (InterruptedException ignored) {
                                                                }
                                                            }


                                                        }
                                                    })
                                                    .show();
                                        } else {

                                        }
                                    } else {
                                        Toast.makeText(MainActivity.this, "Error,wrong password!!",
                                                Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                    dialog1.show();


                }
            });

        }


    }

    public void updatespinner() {
        //從DB撈出目前User建立的Group有哪些並秀在Spinner選單內
        helper = new DBHelper(getApplicationContext());
        cursor = helper.select(TABLE_NAME_DALISLAVEADDRESS);
        if (cursor.getCount() > 0) {

            Spinner spinner = (Spinner) findViewById(R.id.mySpinner);
            cursor.moveToFirst();

            SimpleCursorAdapter adapter = new SimpleCursorAdapter(MainActivity.this, R.layout.message, cursor, new String[]{"_dali_name"}, new int[]{R.id.message});
            spinner.setAdapter(adapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    helper = new DBHelper(getApplicationContext());
                    cursor = helper.select(TABLE_NAME_DALISLAVEADDRESS);
                    cursor.moveToPosition(i);
                    if (cursor.getString(1) != null) {
                        GlobalVariable.OnSpinnerItemSelected = cursor.getString(1);
                        //if (cursor.getString(2)!=null){
                        //GlobalVariable.OnSpinnerItemSelectedOfShort = cursor.getString(2);}
                        //  if (D)
                        //     Log.e(TAG, "GlobalVariable.OnSpinnerItemSelected = : " + GlobalVariable.OnSpinnerItemSelected);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }
    }


    public class GroupMode extends Fragment {
        public static final String ARG_SECTION_NUMBER5 = "GroupMode";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            return inflater.inflate(
                    R.layout.group_mode, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner3);
            helper = new DBHelper(getApplicationContext());
            cursor = helper.select(TABLE_NAME_GROUP);
            if (cursor.getCount() > 0) {

                view.findViewById(R.id.textView13).setVisibility(View.VISIBLE);
                spinner.setVisibility(View.VISIBLE);
                cursor.moveToFirst();

                SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), R.layout.my_spinner, cursor, new String[]{"_group"}, new int[]{R.id.my_Spinner});
                spinner.setAdapter(adapter);

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        helper = new DBHelper(getApplicationContext());
                        cursor = helper.select(TABLE_NAME_GROUP);
                        cursor.moveToPosition(i);
                        if (cursor.getString(1) != null) {
                            GlobalVariable.OnSpinnerItemSelectedOfGroup = cursor.getString(1);
                            //if (cursor.getString(2)!=null){
                            //GlobalVariable.OnSpinnerItemSelectedOfShort = cursor.getString(2);}
                            //  if (D)
                            //     Log.e(TAG, "GlobalVariable.OnSpinnerItemSelected = : " + GlobalVariable.OnSpinnerItemSelected);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
            } else {
                view.findViewById(R.id.textView13).setVisibility(View.INVISIBLE);
                spinner.setVisibility(View.INVISIBLE);
            }

            setupGroupMode();

        }
    }

    public void setupGroupMode() {


        final Button mode_reading = (Button) findViewById(R.id.reading);
        mode_reading.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                String send_mode_1 = GlobalVariable.by1_AA + GlobalVariable.by2_55 + "06" + GlobalVariable.by4_06 + "00" + "00" + "81" + "10" + "fe";

                int checksum = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
                        + (Integer.parseInt(GlobalVariable.by2_55, 16))
                        + (Integer.parseInt("06", 16))
                        + (Integer.parseInt(GlobalVariable.by4_06, 16))
                        + (Integer.parseInt("81", 16))
                        + (Integer.parseInt("10", 16))
                        + (Integer.parseInt("fe", 16))) % 256;
                if (D)
                    Log.e(TAG, "checksum" + checksum);
                String send_group_on1 = send_mode_1 + Integer.toHexString(checksum);
                if (D)
                    Log.e(TAG, "send_group_on1" + send_group_on1);
                sendMessage(send_group_on1);
                try {
                    Thread.sleep(150);
                } catch (InterruptedException ignored) {
                }


            }
        });
        final Button mode_sleep = (Button) findViewById(R.id.sleep);
        mode_sleep.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                String send_mode_1 = GlobalVariable.by1_AA + GlobalVariable.by2_55 + "06" + GlobalVariable.by4_06 + "00" + "00" + "81"   + "11" + "00";

                int checksum = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
                        + (Integer.parseInt(GlobalVariable.by2_55, 16))
                        + (Integer.parseInt("06", 16))
                        + (Integer.parseInt(GlobalVariable.by4_06, 16))
                        + (Integer.parseInt("81", 16))
                        + (Integer.parseInt("11", 16))) % 256;
                if (D)
                    Log.e(TAG, "checksum" + checksum);
                String send_group_on1 = send_mode_1 + Integer.toHexString(checksum);
                if (D)
                    Log.e(TAG, "send_group_on1" + send_group_on1);
                sendMessage(send_group_on1);
                try {
                    Thread.sleep(150);
                } catch (InterruptedException ignored) {
                }


            }
        });
        final Button mode_movie = (Button) findViewById(R.id.movie);
        mode_movie.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                String send_mode_1 = GlobalVariable.by1_AA + GlobalVariable.by2_55 + "06" + GlobalVariable.by4_06 + "00" + "00" + "81"   + "12" + "00";

                int checksum = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
                        + (Integer.parseInt(GlobalVariable.by2_55, 16))
                        + (Integer.parseInt("06", 16))
                        + (Integer.parseInt(GlobalVariable.by4_06, 16))
                        + (Integer.parseInt("81", 16))





                        + (Integer.parseInt("12", 16))) % 256;
                if (D)
                    Log.e(TAG, "checksum" + checksum);
                String send_group_on1 = send_mode_1 + Integer.toHexString(checksum);
                if (D)
                    Log.e(TAG, "send_group_on1" + send_group_on1);
                sendMessage(send_group_on1);
                try {
                    Thread.sleep(150);
                } catch (InterruptedException ignored) {
                }


            }
        });
        final Button mode_meal = (Button) findViewById(R.id.meal);
        mode_meal.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                String send_mode_1 = GlobalVariable.by1_AA + GlobalVariable.by2_55 + "06" + GlobalVariable.by4_06 + "00" + "00"  + "83"   + "13" + "00";

                int checksum = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
                        + (Integer.parseInt(GlobalVariable.by2_55, 16))
                        + (Integer.parseInt("06", 16))
                        + (Integer.parseInt(GlobalVariable.by4_06, 16))
                        + (Integer.parseInt("83", 16))
                        + (Integer.parseInt("13", 16))) % 256;
                if (D)
                    Log.e(TAG, "checksum" + checksum);
                String send_group_on1 = send_mode_1 + Integer.toHexString(checksum);
                if (D)
                    Log.e(TAG, "send_group_on1" + send_group_on1);
                sendMessage(send_group_on1);
                try {
                    Thread.sleep(150);
                } catch (InterruptedException ignored) {
                }


            }
        });
        final Button mode_break = (Button) findViewById(R.id.break_);
        mode_break.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                String send_mode_1 = GlobalVariable.by1_AA + GlobalVariable.by2_55 + "06" + GlobalVariable.by4_06 + "00" + "00" + "83"   + "14" + "00";

                int checksum = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
                        + (Integer.parseInt(GlobalVariable.by2_55, 16))
                        + (Integer.parseInt("06", 16))
                        + (Integer.parseInt(GlobalVariable.by4_06, 16))
                        + (Integer.parseInt("83", 16))
                        + (Integer.parseInt("14", 16))) % 256;
                if (D)
                    Log.e(TAG, "checksum" + checksum);
                String send_group_on1 = send_mode_1 + Integer.toHexString(checksum);
                if (D)
                    Log.e(TAG, "send_group_on1" + send_group_on1);
                sendMessage(send_group_on1);
                try {
                    Thread.sleep(150);
                } catch (InterruptedException ignored) {
                }


            }
        });

        Button mode_round = (Button) findViewById(R.id.round);
        mode_round.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                B = !B;
                if (B) {
                    Toast.makeText(MainActivity.this, "Round theme running",
                            Toast.LENGTH_SHORT).show();
                    mode_break.setVisibility(View.INVISIBLE);
                    mode_meal.setVisibility(View.INVISIBLE);
                    mode_movie.setVisibility(View.INVISIBLE);
                    mode_reading.setVisibility(View.INVISIBLE);
                    mode_sleep.setVisibility(View.INVISIBLE);

                } else {
                    mode_break.setVisibility(View.VISIBLE);
                    mode_meal.setVisibility(View.VISIBLE);
                    mode_movie.setVisibility(View.VISIBLE);
                    mode_reading.setVisibility(View.VISIBLE);
                    mode_sleep.setVisibility(View.VISIBLE);
                }
                if (D)
                    Log.e(TAG, "Boolean B = " + B);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String send_mode_1 = GlobalVariable.by1_AA + GlobalVariable.by2_55 + "06" + GlobalVariable.by4_06 + "00" + "00" + "83" + "16" + "00";

                        int checksum = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
                                + (Integer.parseInt(GlobalVariable.by2_55, 16))
                                + (Integer.parseInt("06", 16))
                                + (Integer.parseInt(GlobalVariable.by4_06, 16))
                                + (Integer.parseInt("83", 16))
                                //     + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(0, 2), 16))
                                //      + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(2, 4), 16))
                                + (Integer.parseInt("16", 16))) % 256;
                        if (D)
                            Log.e(TAG, "checksum" + checksum);
                        String send_group_on1 = send_mode_1 + Integer.toHexString(checksum);
                        if (D)
                            Log.e(TAG, "send_group_on1" + send_group_on1);


                        String send_mode_2 = GlobalVariable.by1_AA + GlobalVariable.by2_55 + "06" + GlobalVariable.by4_06 + "00" + "00" + "83" + "17" + "00";

                        int checksum1 = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
                                + (Integer.parseInt(GlobalVariable.by2_55, 16))
                                + (Integer.parseInt("06", 16))
                                + (Integer.parseInt(GlobalVariable.by4_06, 16))
                                + (Integer.parseInt("83", 16))
                                //     + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(0, 2), 16))
                                //      + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(2, 4), 16))
                                + (Integer.parseInt("17", 16))) % 256;
                        if (D)
                            Log.e(TAG, "checksum" + checksum);
                        String send_group_on2 = send_mode_2 + Integer.toHexString(checksum1);
                        if (D)
                            Log.e(TAG, "send_group_on2" + send_group_on2);


                        while (B) {

                            sendMessage(send_group_on1);
                            try {
                                Thread.sleep(GlobalVariable.t+100);
                            } catch (InterruptedException ignored) {
                            }


                            sendMessage(send_group_on2);
                            try {
                                Thread.sleep(GlobalVariable.t+100);
                            } catch (InterruptedException ignored) {
                            }


                        }
                    }
                }).start();


            }


        });


        final Button test1 = (Button) findViewById(R.id.test1);
        test1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                String send_mode_1 = GlobalVariable.by1_AA + GlobalVariable.by2_55 + "06" + GlobalVariable.by4_06 + "00" + "00" + "ff" + "16" + "00";

                int checksum = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
                        + (Integer.parseInt(GlobalVariable.by2_55, 16))
                        + (Integer.parseInt("06", 16))
                        + (Integer.parseInt(GlobalVariable.by4_06, 16))
                        + (Integer.parseInt("ff", 16))
                        //     + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(0, 2), 16))
                        //      + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(2, 4), 16))
                        + (Integer.parseInt("16", 16))) % 256;
                if (D)
                    Log.e(TAG, "checksum" + checksum);
                String send_group_on1 = send_mode_1 + Integer.toHexString(checksum);
                if (D)
                    Log.e(TAG, "send_group_on1" + send_group_on1);
                sendMessage(send_group_on1);
                try {
                    Thread.sleep(150);
                } catch (InterruptedException ignored) {
                }


            }
        });
        final Button test2 = (Button) findViewById(R.id.test2);
        test2.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                String send_mode_1 = GlobalVariable.by1_AA + GlobalVariable.by2_55 + "06" + GlobalVariable.by4_06 + "00" + "00" + "ff" + "17" + "00";

                int checksum = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
                        + (Integer.parseInt(GlobalVariable.by2_55, 16))
                        + (Integer.parseInt("06", 16))
                        + (Integer.parseInt(GlobalVariable.by4_06, 16))
                        + (Integer.parseInt("ff", 16))
                        //     + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(0, 2), 16))
                        //      + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(2, 4), 16))
                        + (Integer.parseInt("17", 16))) % 256;
                if (D)
                    Log.e(TAG, "checksum" + checksum);
                String send_group_on1 = send_mode_1 + Integer.toHexString(checksum);
                if (D)
                    Log.e(TAG, "send_group_on1" + send_group_on1);
                sendMessage(send_group_on1);
                try {
                    Thread.sleep(150);
                } catch (InterruptedException ignored) {
                }


            }
        });
        final Button test3 = (Button) findViewById(R.id.test3);
        test3.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                String send_mode_1 = GlobalVariable.by1_AA + GlobalVariable.by2_55 + "06" + GlobalVariable.by4_06 + "00" + "00" + "ff" + "18" + "00";

                int checksum = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
                        + (Integer.parseInt(GlobalVariable.by2_55, 16))
                        + (Integer.parseInt("06", 16))
                        + (Integer.parseInt(GlobalVariable.by4_06, 16))
                        + (Integer.parseInt("ff", 16))
                        //     + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(0, 2), 16))
                        //      + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(2, 4), 16))
                        + (Integer.parseInt("18", 16))) % 256;
                if (D)
                    Log.e(TAG, "checksum" + checksum);
                String send_group_on1 = send_mode_1 + Integer.toHexString(checksum);
                if (D)
                    Log.e(TAG, "send_group_on1" + send_group_on1);
                sendMessage(send_group_on1);
                try {
                    Thread.sleep(150);
                } catch (InterruptedException ignored) {
                }


            }
        });
        final Button round_test = (Button) findViewById(R.id.roundtest);
        round_test.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                C = !C;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (C) {
                            String send_mode_1 = GlobalVariable.by1_AA + GlobalVariable.by2_55 + "06" + GlobalVariable.by4_06 + "00" + "00" + "ff" + "16" + "00";

                            int checksum = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
                                    + (Integer.parseInt(GlobalVariable.by2_55, 16))
                                    + (Integer.parseInt("06", 16))
                                    + (Integer.parseInt(GlobalVariable.by4_06, 16))
                                    + (Integer.parseInt("ff", 16))
                                    //     + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(0, 2), 16))
                                    //      + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(2, 4), 16))
                                    + (Integer.parseInt("16", 16))) % 256;
                            if (D)
                                Log.e(TAG, "checksum" + checksum);
                            String send_group_on1 = send_mode_1 + Integer.toHexString(checksum);
                            if (D)
                                Log.e(TAG, "send_group_on1" + send_group_on1);
                            sendMessage(send_group_on1);
                            try {
                                Thread.sleep(GlobalVariable.t);
                            } catch (InterruptedException ignored) {
                            }


                            String send_mode_2 = GlobalVariable.by1_AA + GlobalVariable.by2_55 + "06" + GlobalVariable.by4_06 + "00" + "00" + "ff" + "17" + "00";

                            int checksum1 = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
                                    + (Integer.parseInt(GlobalVariable.by2_55, 16))
                                    + (Integer.parseInt("06", 16))
                                    + (Integer.parseInt(GlobalVariable.by4_06, 16))
                                    + (Integer.parseInt("ff", 16))
                                    //     + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(0, 2), 16))
                                    //      + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(2, 4), 16))
                                    + (Integer.parseInt("17", 16))) % 256;
                            if (D)
                                Log.e(TAG, "checksum" + checksum);
                            String send_group_on2 = send_mode_2 + Integer.toHexString(checksum1);
                            if (D)
                                Log.e(TAG, "send_group_on1" + send_group_on1);
                            sendMessage(send_group_on2);
                            try {
                                Thread.sleep(GlobalVariable.t);
                            } catch (InterruptedException ignored) {
                            }

                            String send_mode_3 = GlobalVariable.by1_AA + GlobalVariable.by2_55 + "06" + GlobalVariable.by4_06 + "00" + "00" + "ff" + "18" + "00";

                            int checksum2 = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
                                    + (Integer.parseInt(GlobalVariable.by2_55, 16))
                                    + (Integer.parseInt("06", 16))
                                    + (Integer.parseInt(GlobalVariable.by4_06, 16))
                                    + (Integer.parseInt("ff", 16))
                                    //     + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(0, 2), 16))
                                    //      + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(2, 4), 16))
                                    + (Integer.parseInt("18", 16))) % 256;
                            if (D)
                                Log.e(TAG, "checksum" + checksum);
                            String send_group_on3 = send_mode_3 + Integer.toHexString(checksum2);
                            if (D)
                                Log.e(TAG, "send_group_on1" + send_group_on3);
                            sendMessage(send_group_on3);
                            try {
                                Thread.sleep(GlobalVariable.t);
                            } catch (InterruptedException ignored) {
                            }


                        }
                    }
                }).start();


            }
        });
        mode_break.setVisibility(View.VISIBLE);
        mode_meal.setVisibility(View.VISIBLE);
        mode_movie.setVisibility(View.VISIBLE);
        mode_reading.setVisibility(View.VISIBLE);
        mode_sleep.setVisibility(View.VISIBLE);
    }

    public class ListActivityFragment extends ListFragment {

        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.listactivityframe,
                    container, false);
        }

        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            int i;

            helper = new DBHelper(getApplicationContext());
            cursor = helper.select(TABLE_NAME_GROUP);
            if (cursor.getCount() > 0) {
                list.clear();//清除list避免重複

                for (i = 0; i < cursor.getCount(); i++) {
                    HashMap item = new HashMap();
                    if (getnumber(i) == null) {
                    } else {
                        item.put("text", getnumber(i));
                        list.add(item);
                    }
                }
                setListAdapter(new MyAdapter(getActivity()));
            }

            /*Button adapter_button = (Button) findViewById(R.id.adapter_button);
            adapter_button.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {

                }
            });*/
            helper.close();
            cursor.close();
        }


        public class MyAdapter extends BaseAdapter {
            private LayoutInflater adapterLayoutInflater;

            public MyAdapter(Context c) {
                adapterLayoutInflater = LayoutInflater.from(c);
            }

            @Override
            public int getCount() {
                return list.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(final int position, View view, ViewGroup parent) {
                final TagView tag;

                if (view == null) {
                    view = adapterLayoutInflater.inflate(R.layout.baseadapter, null);
                    tag = new TagView(
                            (SeekBar) view.findViewById(R.id.seekBar),
                            (Switch) view.findViewById(R.id.switch2),
                            (TextView) view.findViewById(R.id.textView),
                            (TextView) view.findViewById(R.id.textView6),
                            (Button) view.findViewById(R.id.button)
                    );

                    view.setTag(tag);
                } else {
                    tag = (TagView) view.getTag();
                }

                tag.button.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LayoutInflater inflater2 = LayoutInflater.from(getActivity());
                        final View v3 = inflater2.inflate(R.layout.login, null);
                        final AlertDialog.Builder dialog2 = new AlertDialog.Builder(getActivity());
                        final EditText password2 = (EditText) v3.findViewById(R.id.editText2);

                        dialog2.setCancelable(false);
                        dialog2.setTitle("Enter the password");
                        dialog2.setView(v3);
                        dialog2.setPositiveButton(R.string.ok_label_1,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialoginterface, int i) {

                                        if (password2.getText().toString().equals(GlobalVariable.VAT_number)) {
                                            GlobalVariable.Group_percentage_choice = "00";
                                            LayoutInflater inflater = LayoutInflater.from(getActivity());
                                            final View v = inflater.inflate(R.layout.group_mode_settings, null);

                                            Spinner Theme1 = (Spinner) v.findViewById(R.id.spinner);
                                            Spinner Light_Percentage = (Spinner) v.findViewById(R.id.spinner2);

                                            ArrayAdapter<String> Themelist;
                                            final String[] Theme = {"Reading", "Sleep", "Movie", "Meal", "Break", "Round", "Test1", "Test2", "Test3"};
                                            Themelist = new ArrayAdapter<String>(getActivity(), R.layout.my_spinner, Theme);
                                            Theme1.setAdapter(Themelist);

                                            Theme1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                @Override
                                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                                    if (Theme[i].equals("Reading")) {
                                                        GlobalVariable.Group_Theme_choice = "00";
                                                    }
                                                    if (Theme[i].equals("Sleep")) {
                                                        GlobalVariable.Group_Theme_choice = "01";
                                                    }
                                                    if (Theme[i].equals("Movie")) {
                                                        GlobalVariable.Group_Theme_choice = "02";
                                                    }
                                                    if (Theme[i].equals("Meal")) {
                                                        GlobalVariable.Group_Theme_choice = "03";
                                                    }
                                                    if (Theme[i].equals("Break")) {
                                                        GlobalVariable.Group_Theme_choice = "04";
                                                    }
                                                    if (Theme[i].equals("Round")) {
                                                        GlobalVariable.Group_Theme_choice = "05";
                                                    }
                                                    if (Theme[i].equals("Test1")) {
                                                        GlobalVariable.Group_Theme_choice = "06";
                                                    }
                                                    if (Theme[i].equals("Test2")) {
                                                        GlobalVariable.Group_Theme_choice = "07";
                                                    }
                                                    if (Theme[i].equals("Test3")) {
                                                        GlobalVariable.Group_Theme_choice = "08";
                                                    }

                                                }

                                                @Override
                                                public void onNothingSelected(AdapterView<?> adapterView) {

                                                }
                                            });

                      /*  ArrayAdapter<String> Light_percentage_list;
                        final String[] Light_percentage = {"10%", "20%", "30%", "40%", "50%", "60%", "70%", "80%", "90%", "100%"};
                        Light_percentage_list = new ArrayAdapter<String>(getActivity(), R.layout.my_spinner, Light_percentage);
                        Light_Percentage.setAdapter(Light_percentage_list);

                        Light_Percentage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                if (Light_percentage[i].equals("10%")) {
                                    GlobalVariable.Light_percentage_choice = "1A";
                                }
                                if (Light_percentage[i].equals("20%")) {
                                    GlobalVariable.Light_percentage_choice = "34";
                                }
                                if (Light_percentage[i].equals("30%")) {
                                    GlobalVariable.Light_percentage_choice = "4E";
                                }
                                if (Light_percentage[i].equals("40%")) {
                                    GlobalVariable.Light_percentage_choice = "68";
                                }
                                if (Light_percentage[i].equals("50%")) {
                                    GlobalVariable.Light_percentage_choice = "82";
                                }
                                if (Light_percentage[i].equals("60%")) {
                                    GlobalVariable.Light_percentage_choice = "9C";
                                }
                                if (Light_percentage[i].equals("70%")) {
                                    GlobalVariable.Light_percentage_choice = "B6";
                                }
                                if (Light_percentage[i].equals("80%")) {
                                    GlobalVariable.Light_percentage_choice = "D0";
                                }
                                if (Light_percentage[i].equals("90%")) {
                                    GlobalVariable.Light_percentage_choice = "EA";
                                }
                                if (Light_percentage[i].equals("100%")) {
                                    GlobalVariable.Light_percentage_choice = "FE";
                                }


                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });
*/
                                            final TextView percentage_text = (TextView) v.findViewById(R.id.textView11);
                                            final SeekBar percentage = (SeekBar) v.findViewById(R.id.seekBar2);
                                            percentage.setMax(Max_Value);
                                            percentage.setProgress(0);
                                            //percentage.setMax(100);
                                            //percentage.setProgress(0);
                                            percentage.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                                                int Progress = 0;

                                                @Override
                                                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                                                    int stepSize = 10;
                                                    Progress = percentage.getProgress();
                                                    Progress = (Progress / stepSize) * stepSize;
                                                    seekBar.setProgress(Progress);

                                                    percentage_text.setText("" + Progress + "%");
                                                }

                                                @Override
                                                public void onStartTrackingTouch(SeekBar seekBar) {

                                                }

                                                @Override
                                                public void onStopTrackingTouch(SeekBar seekBar) {
                                                    if (Progress == 0) {
                                                        GlobalVariable.Group_percentage_choice = "00";
                                                    }
                                                    if (D)
                                                        Log.e(TAG, "Fuck progrss = " + Progress);
                                                    if (Progress == 10) {
                                                        GlobalVariable.Group_percentage_choice = "AA";
                                                    }
                                                    if (Progress == 20) {
                                                        GlobalVariable.Group_percentage_choice = "C4";
                                                    }
                                                    if (Progress == 30) {
                                                        GlobalVariable.Group_percentage_choice = "D2";
                                                    }
                                                    if (Progress == 40) {
                                                        GlobalVariable.Group_percentage_choice = "DD";
                                                    }
                                                    if (Progress == 50) {
                                                        GlobalVariable.Group_percentage_choice = "E5";
                                                    }
                                                    if (Progress == 60) {
                                                        GlobalVariable.Group_percentage_choice = "EB";
                                                    }
                                                    if (Progress == 70) {
                                                        GlobalVariable.Group_percentage_choice = "F1";
                                                    }
                                                    if (Progress == 80) {
                                                        GlobalVariable.Group_percentage_choice = "F6";
                                                    }
                                                    if (Progress == 90) {
                                                        GlobalVariable.Group_percentage_choice = "FA";
                                                    }
                                                    if (Progress == 100) {
                                                        GlobalVariable.Group_percentage_choice = "FE";
                                                    }
                                                    if (D)
                                                        Log.e(TAG, "Fuck GlobalVariable.Group_percentage_choice = " + GlobalVariable.Group_percentage_choice);
                                                }
                                            });

                                            new AlertDialog.Builder(MainActivity.this)
                                                    .setTitle(list.get(position).get("text") + " Theme Configure")
                                                    .setView(v)
                                                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                            int position_final = position + position + 1;
                                                            String send_mode_1 = GlobalVariable.by1_AA + GlobalVariable.by2_55 + "09" + GlobalVariable.by4_01 + "00" + "00" + "00" + "e1" + "00"
                                                                    + (80 + position_final) + GlobalVariable.Group_Theme_choice + GlobalVariable.Group_percentage_choice;
                                                            int checksum = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
                                                                    + (Integer.parseInt(GlobalVariable.by2_55, 16))
                                                                    + (Integer.parseInt("09", 16))
                                                                    + (Integer.parseInt(GlobalVariable.by4_01, 16))
                                                                    + (Integer.parseInt(String.valueOf(80 + position_final), 16))
                                                                    //     + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(0, 2), 16))
                                                                    //      + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(2, 4), 16))
                                                                    + (Integer.parseInt("e1"
                                                                    , 16))
                                                                    + (Integer.parseInt(GlobalVariable.Group_Theme_choice, 16))
                                                                    + (Integer.parseInt(GlobalVariable.Group_percentage_choice, 16))) % 256;
                                                            if (checksum > 16) {
                                                                if (D)
                                                                    Log.e(TAG, "checksum" + checksum);
                                                                String send_group_on1 = send_mode_1 + Integer.toHexString(checksum);
                                                                if (D)
                                                                    Log.e(TAG, "send_group_on1" + send_group_on1);
                                                                sendMessage(send_group_on1);
                                                                try {
                                                                    Thread.sleep(150);
                                                                } catch (InterruptedException ignored) {
                                                                }
                                                            } else {
                                                                if (D)
                                                                    Log.e(TAG, "checksum" + checksum);
                                                                String send_group_on1 = send_mode_1 + "0" + Integer.toHexString(checksum);
                                                                if (D)
                                                                    Log.e(TAG, "send_group_on1" + send_group_on1);
                                                                sendMessage(send_group_on1);
                                                                try {
                                                                    Thread.sleep(150);
                                                                } catch (InterruptedException ignored) {
                                                                }
                                                            }

                                                        }
                                                    })
                                                    .show();
                                        } else {
                                            Toast.makeText(MainActivity.this, "Error,wrong password!!",
                                                    Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                        dialog2.show();




                    }
                });

                tag.text.setText((CharSequence) list.get(position).get("text"));
                tag.aswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {

                        if (checked) {
                            helper = new DBHelper(getApplicationContext());
                            cursor = helper.select(TABLE_NAME_DALISLAVEADDRESS);
                            cursor.moveToPosition(position);

                            int position_final = position + position + 1;
                            String send_group_on = GlobalVariable.send_group_command + Integer.toHexString(position_final) + GlobalVariable.by8_ON_MAX + GlobalVariable.by9_NoReturn;
                            int checksum = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
                                    + (Integer.parseInt(GlobalVariable.by2_55, 16))
                                    + (Integer.parseInt(GlobalVariable.by3_06, 16))
                                    + (Integer.parseInt(GlobalVariable.by4_06, 16))
                                    + (Integer.parseInt("80", 16))
                                    //     + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(0, 2), 16))
                                    //      + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(2, 4), 16))
                                    + (Integer.parseInt(Integer.toHexString(position_final), 16))
                                    + (Integer.parseInt(GlobalVariable.by8_ON_MAX, 16))
                                    + (Integer.parseInt(GlobalVariable.by9_NoReturn, 16))) % 256;
                            if (D)
                                Log.e(TAG, "checksum" + checksum);
                            String send_group_on1 = send_group_on + Integer.toHexString(checksum);
                            if (D)
                                Log.e(TAG, "send_group_on1" + send_group_on1);
                            sendMessage(send_group_on1);
                            try {
                                Thread.sleep(150);
                            } catch (InterruptedException ignored) {
                            }
                        } else {
                            helper = new DBHelper(getApplicationContext());
                            cursor = helper.select(TABLE_NAME_DALISLAVEADDRESS);
                            cursor.moveToPosition(position);
                            int position_final = position + position + 1;
                            String send_group_off = GlobalVariable.send_group_command + Integer.toHexString(position_final) + GlobalVariable.by8_OFF + GlobalVariable.by9_NoReturn;
                            int checksum = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
                                    + (Integer.parseInt(GlobalVariable.by2_55, 16))
                                    + (Integer.parseInt(GlobalVariable.by3_06, 16))
                                    + (Integer.parseInt(GlobalVariable.by4_06, 16))
                                    + (Integer.parseInt("80", 16))
                                    //     + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(0, 2), 16))
                                    //      + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(2, 4), 16))
                                    + (Integer.parseInt(Integer.toHexString(position_final), 16))
                                    + (Integer.parseInt(GlobalVariable.by8_OFF, 16))
                                    + (Integer.parseInt(GlobalVariable.by9_NoReturn, 16))) % 256;
                            String send_group_on1 = send_group_off + Integer.toHexString(checksum);
                            if (D)
                                Log.e(TAG, "send_group_on1" + send_group_on1);
                            sendMessage(send_group_on1);
                            try {
                                Thread.sleep(150);
                            } catch (InterruptedException ignored) {
                            }

                        }
                        helper.close();
                        cursor.close();
                    }
                });

                tag.seekBar.setMax(Max_Value);
                tag.seekBar.setProgress(0);
                tag.text_percentage.setText(String.valueOf(0) + "%");

                tag.seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                    int progress = 0;

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        if (seekBar.getProgress() == Min_Value) {
                            progress = Min_Value;
                            seekBar.setProgress(0);
                            helper = new DBHelper(getApplicationContext());
                            cursor = helper.select(TABLE_NAME_DALISLAVEADDRESS);
                            cursor.moveToPosition(position);
                            int position_final = position + position;
                            String send_group_off = GlobalVariable.send_group_command + Integer.toHexString(position_final) + GlobalVariable.by8_OFF + GlobalVariable.by9_NoReturn;
                            int checksum = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
                                    + (Integer.parseInt(GlobalVariable.by2_55, 16))
                                    + (Integer.parseInt(GlobalVariable.by3_06, 16))
                                    + (Integer.parseInt(GlobalVariable.by4_06, 16))
                                    + (Integer.parseInt("80", 16))
                                    //     + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(0, 2), 16))
                                    //      + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(2, 4), 16))
                                    + (Integer.parseInt(Integer.toHexString(position_final), 16))
                                    + (Integer.parseInt(GlobalVariable.by8_OFF, 16))
                                    + (Integer.parseInt(GlobalVariable.by9_NoReturn, 16))) % 256;
                            String send_group_on1 = send_group_off + Integer.toHexString(checksum);
                            if (D)
                                Log.e(TAG, "send_group_on1" + send_group_on1);
                            sendMessage(send_group_on1);
                            try {
                                Thread.sleep(150);
                            } catch (InterruptedException ignored) {
                            }
                        } else if (progress <= Max_Value) {
                            progress = seekBar.getProgress();

                            String by8_light_pecentage = //Integer.toHexString((progress * 7 / 3) + 14);
                                    Integer.toHexString(GlobalVariable.getpercentage(progress));
                            int position_final = position + position;
                            int by10 = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
                                    + (Integer.parseInt(GlobalVariable.by2_55, 16))
                                    + (Integer.parseInt(GlobalVariable.by3_06, 16))
                                    + (Integer.parseInt(GlobalVariable.by4_06, 16))
                                    //     + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(0, 2), 16))
                                    //      + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(2, 4), 16))
                                    + (Integer.parseInt("80", 16))
                                    //     + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(0, 2), 16))
                                    //      + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(2, 4), 16))
                                    + (Integer.parseInt(Integer.toHexString(position_final), 16))
                                    + (Integer.parseInt(by8_light_pecentage, 16))
                                    + (Integer.parseInt(GlobalVariable.by9_NoReturn, 16)))
                                    % 256;
                            if (by10 < 16) {

                                String send1 = "0" + Integer.toHexString(by10);//.substring(1, 2);
                                String sendprogress = GlobalVariable.by1_AA + GlobalVariable.by2_55
                                        + GlobalVariable.by3_06 + GlobalVariable.by4_06 + "00"
                                        + "00" + "8"
                                        + (Integer.parseInt(Integer.toHexString(position_final), 16)) + by8_light_pecentage + "00"
                                        + send1;

                                if (D)
                                    Log.e(TAG, sendprogress);
                                sendMessage(sendprogress);
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException ignored) {
                                }

                            } else {
                                String send1 = Integer.toHexString(by10);//.substring(1, 2);
                                String sendprogress = GlobalVariable.by1_AA + GlobalVariable.by2_55
                                        + GlobalVariable.by3_06 + GlobalVariable.by4_06 + "00"
                                        + "00" + "8"
                                        + (Integer.parseInt(Integer.toHexString(position_final), 16)) + by8_light_pecentage + "00"
                                        + send1;
                                sendMessage(sendprogress);
                                if (D)
                                    Log.e(TAG, sendprogress);
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException ignored) {
                                }
                            }

                        }
                        tag.text_percentage.setText(String.valueOf(progress) + "%");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        tag.text_percentage.setText(String.valueOf(progress) + "%");
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        tag.text_percentage.setText(String.valueOf(progress) + "%");
                    }
                });

                return view;
            }


        }


        public class TagView {
            SeekBar seekBar;
            TextView text;
            Switch aswitch;

            TextView text_percentage;
            Button button;


            public TagView(SeekBar button, Switch aswitch, TextView text, TextView text_percentage, Button button1) {
                this.seekBar = button;
                this.aswitch = aswitch;
                this.text = text;

                this.text_percentage = text_percentage;
                this.button = button1;

            }
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            openOptionsDialog1();
        }
        return false;
    }

    private void openOptionsDialog1() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle(R.string.about_title);
        dialog.setMessage("Sure for quit?");
        dialog.setPositiveButton(R.string.ok_label_1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        finish();
                    }
                });
        dialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {

                    }

                });
        dialog.show();

    }

    private void fadetime() {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View v = inflater.inflate(R.layout.fadetime, null);
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        Spinner fade_time = (Spinner) v.findViewById(R.id.fade_time);

        ArrayAdapter<String> fadetimelist;
        final String[] fadetime = {"No Fade Time", "0.7sec", "1sec", "1.4sec", "2sec", "2.8sec", "4sec"};
        fadetimelist = new ArrayAdapter<String>(this, R.layout.my_spinner, fadetime);
        fade_time.setAdapter(fadetimelist);


        fade_time.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (fadetime[i].equals("No Fade Time")) {
                    GlobalVariable.fade_time = "00";
                    GlobalVariable.t = 50;
                }
                if (fadetime[i].equals("0.7sec")) {
                    GlobalVariable.fade_time = "01";
                    GlobalVariable.t = 700;
                }
                if (fadetime[i].equals("1sec")) {
                    GlobalVariable.fade_time = "02";
                    GlobalVariable.t = 1000;
                }
                if (fadetime[i].equals("1.4sec")) {
                    GlobalVariable.fade_time = "03";
                    GlobalVariable.t = 1400;
                }
                if (fadetime[i].equals("2sec")) {
                    GlobalVariable.fade_time = "04";
                    GlobalVariable.t = 2000;
                }
                if (fadetime[i].equals("2.8sec")) {
                    GlobalVariable.fade_time = "05";
                    GlobalVariable.t = 2800;
                }
                if (fadetime[i].equals("4sec")) {
                    GlobalVariable.fade_time = "06";
                    GlobalVariable.t = 4000;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        dialog.setCancelable(false);
        dialog.setTitle(R.string.about_title);
        dialog.setView(v);
        dialog.setPositiveButton(R.string.ok_label_1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        //aa 55 07 01 00 00 00 de 00 xx chk

                        String send_mode_1 = GlobalVariable.by1_AA + GlobalVariable.by2_55 + "07" + GlobalVariable.by4_01 + "00" + "00" + "00" + "de" + "00" + GlobalVariable.fade_time;

                        int checksum = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
                                + (Integer.parseInt(GlobalVariable.by2_55, 16))
                                + (Integer.parseInt("07", 16))
                                + (Integer.parseInt(GlobalVariable.by4_01, 16))
                                + (Integer.parseInt("de", 16))
                                //     + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(0, 2), 16))
                                //      + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(2, 4), 16))
                                + (Integer.parseInt(GlobalVariable.fade_time, 16))) % 256;
                        if (D)
                            Log.e(TAG, "checksum" + checksum);
                        String send_group_on1 = send_mode_1 + Integer.toHexString(checksum);
                        if (D)
                            Log.e(TAG, "send_group_on1" + send_group_on1);
                        sendMessage(send_group_on1);
                        try {
                            Thread.sleep(150);
                        } catch (InterruptedException ignored) {
                        }
                    }
                });
        dialog.show();

    }

    private void theme_to_who() {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View v = inflater.inflate(R.layout.theme_to_who, null);
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        Spinner theme_to_who = (Spinner) v.findViewById(R.id.theme_to_who);
        final Spinner theme_to_area1 = (Spinner) v.findViewById(R.id.area_1);
        final Spinner theme_to_area2 = (Spinner) v.findViewById(R.id.area_2);
        final TextView region1 = (TextView) v.findViewById(R.id.textView16);
        final TextView region2 = (TextView) v.findViewById(R.id.textView17);
        ArrayAdapter<String> arr_theme_to_who_list;
        ArrayAdapter<String> arr_theme_to_area1_list;
        ArrayAdapter<String> arr_theme_to_area2_list;
        final String[] theme_to_who_list = {"All Lights", "Group"};
        final String[] theme_to_area1_list = {"Group 1"};
        final String[] theme_to_area2_list = {"Group 2"};
        arr_theme_to_who_list = new ArrayAdapter<String>(this, R.layout.my_spinner, theme_to_who_list);
        theme_to_who.setAdapter(arr_theme_to_who_list);
        arr_theme_to_area1_list = new ArrayAdapter<String>(this, R.layout.my_spinner, theme_to_area1_list);
        theme_to_area1.setAdapter(arr_theme_to_area1_list);
        arr_theme_to_area2_list = new ArrayAdapter<String>(this, R.layout.my_spinner, theme_to_area2_list);
        theme_to_area2.setAdapter(arr_theme_to_area2_list);


        theme_to_who.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (theme_to_who_list[i].equals("All Lights")) {
                    theme_to_area1.setVisibility(View.INVISIBLE);
                    theme_to_area2.setVisibility(View.INVISIBLE);
                    region1.setVisibility(View.INVISIBLE);
                    region2.setVisibility(View.INVISIBLE);
                    GlobalVariable.Theme_to_group_or_all = "ff";
                    GlobalVariable.Theme_to_area1_group_8X = "";
                    GlobalVariable.Theme_to_area2_group_8X = "";
                }
                if (theme_to_who_list[i].equals("Group")) {
                    GlobalVariable.Theme_to_group_or_all = "8";
                    theme_to_area1.setVisibility(View.VISIBLE);
                    theme_to_area2.setVisibility(View.VISIBLE);
                    region1.setVisibility(View.VISIBLE);
                    region2.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        theme_to_area1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if (theme_to_area1_list[i].equals("Group 1")) {
                    GlobalVariable.Theme_to_area1_group_8X = "1";
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        theme_to_area2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if (theme_to_area2_list[i].equals("Group 2")) {
                    GlobalVariable.Theme_to_area2_group_8X = "3";
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        dialog.setCancelable(false);
        dialog.setTitle(R.string.about_title);
        dialog.setView(v);
        dialog.setPositiveButton(R.string.ok_label_1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {

                    }
                });
        dialog.show();

    }


    private void light_configure() {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View v = inflater.inflate(R.layout.light_configure, null);
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        helper = new DBHelper(getApplicationContext());
        cursor = helper.select(TABLE_NAME_DALISLAVEADDRESS);
        final EditText change_light_name = (EditText) v.findViewById(R.id.editText3);

        Button change_name = (Button) v.findViewById(R.id.button3);

        final Spinner light_list = (Spinner) v.findViewById(R.id.light_list);

        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(MainActivity.this, R.layout.message, cursor, new String[]{"_dali_name"}, new int[]{R.id.message});
        light_list.setAdapter(adapter);


        light_list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                cursor.moveToPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        change_name.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                helper.update(cursor.getInt(0), TABLE_NAME_DALISLAVEADDRESS, FEILD_NAME_DALI, String.valueOf(change_light_name.getText()));
                helper.close();
                cursor.close();
                helper = new DBHelper(getApplicationContext());
                cursor = helper.select(TABLE_NAME_DALISLAVEADDRESS);
                adapter.notifyDataSetChanged();
                final Spinner light_list = (Spinner) v.findViewById(R.id.light_list);
                final SimpleCursorAdapter adapter = new SimpleCursorAdapter(MainActivity.this, R.layout.message, cursor, new String[]{"_dali_name"}, new int[]{R.id.message});
                light_list.setAdapter(adapter);

            }
        });

        dialog.setCancelable(false);
        dialog.setTitle(R.string.about_title);
        dialog.setView(v);
        dialog.setPositiveButton(R.string.ok_label_1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        updatespinner();
                        helper.close();
                        cursor.close();
                    }
                });
        dialog.show();

    }


    public void light_on() {
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(MainActivity.this, R.string.not_connected,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mConversationArrayAdapter.clear();
        /*int by10 = (GlobalVariable.SumFF
                + (Integer.parseInt(GlobalVariable.by8_ON_MAX, 16)) + (Integer
                .parseInt(GlobalVariable.by9_NoReturn, 16))) % 256;
        */
       /* String spinner_dali_send_on_checksum = GlobalVariable.by1_AA + GlobalVariable.by2_55
                + GlobalVariable.by3_06 + GlobalVariable.by4_01 + GlobalVariable.OnSpinnerItemSelectedOfShort.substring(0, 2)
                + GlobalVariable.OnSpinnerItemSelectedOfShort.substring(2, 4) + GlobalVariable.OnSpinnerItemSelected
                + GlobalVariable.by8_ON_MAX + GlobalVariable.by9_NoReturn;*/
        String spinner_dali_send_on_checksum = GlobalVariable.by1_AA + GlobalVariable.by2_55
                + GlobalVariable.by3_06 + GlobalVariable.by4_06 + "00"
                + "00" + GlobalVariable.OnSpinnerItemSelected
                + GlobalVariable.by8_ON_MAX + GlobalVariable.by9_NoReturn;
        int checksum = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
                + (Integer.parseInt(GlobalVariable.by2_55, 16))
                + (Integer.parseInt(GlobalVariable.by3_06, 16))
                + (Integer.parseInt(GlobalVariable.by4_06, 16))
                // + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(0, 2), 16))
                // + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(2, 4), 16))
                + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelected, 16))
                + (Integer.parseInt(GlobalVariable.by8_ON_MAX, 16))
                + (Integer.parseInt(GlobalVariable.by9_NoReturn, 16))) % 256;
        if (checksum >= 16) {
            String send2 = spinner_dali_send_on_checksum + Integer.toHexString(checksum);

            if (D)
                Log.e(TAG, "send2  : " + send2);
            sendMessage(send2);
            try {
                Thread.sleep(150);
            } catch (InterruptedException ignored) {
            }
        } else {
            String send2 = spinner_dali_send_on_checksum + "0" + Integer.toHexString(checksum);

            if (D)
                Log.e(TAG, "send2  : " + send2);
            sendMessage(send2);
            try {
                Thread.sleep(150);
            } catch (InterruptedException ignored) {
            }
        }

    }

    public void light_off() {
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(MainActivity.this, R.string.not_connected,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mConversationArrayAdapter.clear();
/*
        int by10 = (GlobalVariable.SumFF
                + (Integer.parseInt(GlobalVariable.by8_OFF, 16)) + (Integer
                .parseInt(GlobalVariable.by9_NoReturn, 16))) % 256;

        String send1 = "0" + Integer.toHexString(by10);
        String send2 = GlobalVariable.String_FF
                + GlobalVariable.by8_OFF + GlobalVariable.by9_NoReturn
                + send1;*/
/*
        String spinner_dali_send_on_checksum = GlobalVariable.by1_AA + GlobalVariable.by2_55
                + GlobalVariable.by3_06 + GlobalVariable.by4_01 + GlobalVariable.OnSpinnerItemSelectedOfShort.substring(0, 2)
                + GlobalVariable.OnSpinnerItemSelectedOfShort.substring(2, 4) + GlobalVariable.OnSpinnerItemSelected
                + GlobalVariable.by8_OFF + GlobalVariable.by9_NoReturn;*/
        String spinner_dali_send_on_checksum = GlobalVariable.by1_AA + GlobalVariable.by2_55
                + GlobalVariable.by3_06 + GlobalVariable.by4_06 + "00"
                + "00" + GlobalVariable.OnSpinnerItemSelected
                + GlobalVariable.by8_OFF + GlobalVariable.by9_NoReturn;
        int checksum = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
                + (Integer.parseInt(GlobalVariable.by2_55, 16))
                + (Integer.parseInt(GlobalVariable.by3_06, 16))
                + (Integer.parseInt(GlobalVariable.by4_06, 16))
                //     + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(0, 2), 16))
                //      + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(2, 4), 16))
                + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelected, 16))
                + (Integer.parseInt(GlobalVariable.by8_OFF, 16))
                + (Integer.parseInt(GlobalVariable.by9_NoReturn, 16))) % 256;
        if (checksum >= 16) {
            String send2 = spinner_dali_send_on_checksum + Integer.toHexString(checksum);
            if (D)
                Log.e(TAG, "checksum  : " + checksum);
            if (D)
                Log.e(TAG, "send2  : " + send2);
            sendMessage(send2);
            try {
                Thread.sleep(150);
            } catch (InterruptedException ignored) {
            }
        } else {
            String send2 = spinner_dali_send_on_checksum + "0" + Integer.toHexString(checksum);
            if (D)
                Log.e(TAG, "checksum  : " + checksum);
            if (D)
                Log.e(TAG, "send2  : " + send2);
            sendMessage(send2);
            try {
                Thread.sleep(150);
            } catch (InterruptedException ignored) {
            }
        }
    }

    public void progrss_change_1() {

        String OnSpinnerItemSelected = Integer.toHexString((Integer.parseInt(GlobalVariable.OnSpinnerItemSelected, 16) - 1));
        if (progress1 == 0) {
            if (OnSpinnerItemSelected.length() == 1) {
                OnSpinnerItemSelected = "0" + OnSpinnerItemSelected;
                String by8_light_pecentage = "00";
                int by10 = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
                        + (Integer.parseInt(GlobalVariable.by2_55, 16))
                        + (Integer.parseInt(GlobalVariable.by3_06, 16))
                        + (Integer.parseInt(GlobalVariable.by4_06, 16))
                        //     + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(0, 2), 16))
                        //      + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(2, 4), 16))
                        + (Integer.parseInt(OnSpinnerItemSelected, 16))
                        + (Integer.parseInt(by8_light_pecentage, 16))
                        + (Integer.parseInt(GlobalVariable.by9_NoReturn, 16)))
                        % 256;
                if (by10 < 16) {
                    String send1 = "0" + Integer.toHexString(by10);//.substring(1, 2);
                    String sendprogress = GlobalVariable.by1_AA + GlobalVariable.by2_55
                            + GlobalVariable.by3_06 + GlobalVariable.by4_06 + "00"
                            + "00" + OnSpinnerItemSelected
                            + by8_light_pecentage + GlobalVariable.by9_NoReturn
                            + send1;
                    if (D)
                        Log.e(TAG, "by10  : " + by10);
                    if (D)
                        Log.e(TAG, "sendprogress  : " + sendprogress);
                    sendMessage(sendprogress);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ignored) {
                    }
                } else {
                    String send1 = Integer.toHexString(by10);//.substring(1, 2);
                    String sendprogress = GlobalVariable.by1_AA + GlobalVariable.by2_55
                            + GlobalVariable.by3_06 + GlobalVariable.by4_06 + "00"
                            + "00" + OnSpinnerItemSelected
                            + by8_light_pecentage + GlobalVariable.by9_NoReturn
                            + send1;
                    if (D)
                        Log.e(TAG, "by10  : " + by10);
                    if (D)
                        Log.e(TAG, "sendprogress  : " + sendprogress);
                    sendMessage(sendprogress);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ignored) {
                    }
                }
            } else {
                String by8_light_pecentage = "00";
                int by10 = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
                        + (Integer.parseInt(GlobalVariable.by2_55, 16))
                        + (Integer.parseInt(GlobalVariable.by3_06, 16))
                        + (Integer.parseInt(GlobalVariable.by4_06, 16))
                        //     + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(0, 2), 16))
                        //      + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(2, 4), 16))
                        + (Integer.parseInt(OnSpinnerItemSelected, 16))
                        + (Integer.parseInt(by8_light_pecentage, 16))
                        + (Integer.parseInt(GlobalVariable.by9_NoReturn, 16)))
                        % 256;
                if (by10 < 16) {
                    String send1 = "0" + Integer.toHexString(by10);//.substring(1, 2);
                    String sendprogress = GlobalVariable.by1_AA + GlobalVariable.by2_55
                            + GlobalVariable.by3_06 + GlobalVariable.by4_06 + "00"
                            + "00" + OnSpinnerItemSelected
                            + by8_light_pecentage + GlobalVariable.by9_NoReturn
                            + send1;
                    if (D)
                        Log.e(TAG, "by10  : " + by10);
                    if (D)
                        Log.e(TAG, "sendprogress  : " + sendprogress);
                    sendMessage(sendprogress);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ignored) {
                    }
                } else {
                    String send1 = Integer.toHexString(by10);//.substring(1, 2);
                    String sendprogress = GlobalVariable.by1_AA + GlobalVariable.by2_55
                            + GlobalVariable.by3_06 + GlobalVariable.by4_06 + "00"
                            + "00" + OnSpinnerItemSelected
                            + by8_light_pecentage + GlobalVariable.by9_NoReturn
                            + send1;
                    if (D)
                        Log.e(TAG, "by10  : " + by10);
                    if (D)
                        Log.e(TAG, "sendprogress  : " + sendprogress);
                    sendMessage(sendprogress);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        } else {
            if (OnSpinnerItemSelected.length() == 1) {
                OnSpinnerItemSelected = "0" + OnSpinnerItemSelected;
                String by8_light_pecentage = Integer.toHexString(GlobalVariable.getpercentage(progress1));//Integer.toHexString((progress1 * 7 / 3) + 14);
                int by10 = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
                        + (Integer.parseInt(GlobalVariable.by2_55, 16))
                        + (Integer.parseInt(GlobalVariable.by3_06, 16))
                        + (Integer.parseInt(GlobalVariable.by4_06, 16))
                        //     + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(0, 2), 16))
                        //      + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(2, 4), 16))
                        + (Integer.parseInt(OnSpinnerItemSelected, 16))
                        + (Integer.parseInt(by8_light_pecentage, 16))
                        + (Integer.parseInt(GlobalVariable.by9_NoReturn, 16)))
                        % 256;
                if (by10 < 16) {
                    String send1 = "0" + Integer.toHexString(by10);//.substring(1, 2);
                    String sendprogress = GlobalVariable.by1_AA + GlobalVariable.by2_55
                            + GlobalVariable.by3_06 + GlobalVariable.by4_06 + "00"
                            + "00" + OnSpinnerItemSelected
                            + by8_light_pecentage + GlobalVariable.by9_NoReturn
                            + send1;
                    if (D)
                        Log.e(TAG, "by10  : " + by10);
                    if (D)
                        Log.e(TAG, "sendprogress  : " + sendprogress);
                    sendMessage(sendprogress);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ignored) {
                    }
                } else {
                    String send1 = Integer.toHexString(by10);//.substring(1, 2);
                    String sendprogress = GlobalVariable.by1_AA + GlobalVariable.by2_55
                            + GlobalVariable.by3_06 + GlobalVariable.by4_06 + "00"
                            + "00" + OnSpinnerItemSelected
                            + by8_light_pecentage + GlobalVariable.by9_NoReturn
                            + send1;
                    if (D)
                        Log.e(TAG, "by10  : " + by10);
                    if (D)
                        Log.e(TAG, "sendprogress  : " + sendprogress);
                    sendMessage(sendprogress);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ignored) {
                    }
                }
            } else {
                String by8_light_pecentage = Integer.toHexString(GlobalVariable.getpercentage(progress1));
                int by10 = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
                        + (Integer.parseInt(GlobalVariable.by2_55, 16))
                        + (Integer.parseInt(GlobalVariable.by3_06, 16))
                        + (Integer.parseInt(GlobalVariable.by4_06, 16))
                        //     + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(0, 2), 16))
                        //      + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(2, 4), 16))
                        + (Integer.parseInt(OnSpinnerItemSelected, 16))
                        + (Integer.parseInt(by8_light_pecentage, 16))
                        + (Integer.parseInt(GlobalVariable.by9_NoReturn, 16)))
                        % 256;
                if (by10 < 16) {
                    String send1 = "0" + Integer.toHexString(by10);//.substring(1, 2);
                    String sendprogress = GlobalVariable.by1_AA + GlobalVariable.by2_55
                            + GlobalVariable.by3_06 + GlobalVariable.by4_06 + "00"
                            + "00" + OnSpinnerItemSelected
                            + by8_light_pecentage + GlobalVariable.by9_NoReturn
                            + send1;
                    if (D)
                        Log.e(TAG, "by10  : " + by10);
                    if (D)
                        Log.e(TAG, "sendprogress  : " + sendprogress);
                    sendMessage(sendprogress);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ignored) {
                    }
                } else {
                    String send1 = Integer.toHexString(by10);//.substring(1, 2);
                    String sendprogress = GlobalVariable.by1_AA + GlobalVariable.by2_55
                            + GlobalVariable.by3_06 + GlobalVariable.by4_06 + "00"
                            + "00" + OnSpinnerItemSelected
                            + by8_light_pecentage + GlobalVariable.by9_NoReturn
                            + send1;
                    if (D)
                        Log.e(TAG, "by10  : " + by10);
                    if (D)
                        Log.e(TAG, "sendprogress  : " + sendprogress);
                    sendMessage(sendprogress);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }


    }

    public class Query_Short extends AsyncTask<Void, Integer, String> {
        @Override
        protected String doInBackground(Void... arg0) {
            // TODO Auto-generated method stub
            querylight();
            // 再背景中處理的耗時工作
            return null; // 會傳給 onPostExecute(String result) 的 String result
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            mDialog = ProgressDialog.show(MainActivity.this,
                    "processing...", "", true);
            // 背景工作處理"前"需作的事
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
            // 背景工作處理"中"更新的事
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            mDialog.dismiss();
            query_shortaddr();
            // 背景工作處理完"後"需作的事
        }

        @Override
        protected void onCancelled() {
            // TODO Auto-generated method stub
            super.onCancelled();
            // 背景工作被"取消"時作的事，此時不作 onPostExecute(String result)
        }
    }

    public void query_is_zigbee_exist_or_not() {
        helper = new DBHelper(getApplicationContext());
        cursor = helper.select(TABLE_NAME_SHORT_ADDRESS);
        sendMessage(GlobalVariable.String_query_short);
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }
    }

    public class query_is_zigbee_exist_or_not_Async extends AsyncTask<Void, Integer, String> {
        @Override
        protected String doInBackground(Void... arg0) {
            // TODO Auto-generated method stub
            query_is_zigbee_exist_or_not();
            // 再背景中處理的耗時工作
            return null; // 會傳給 onPostExecute(String result) 的 String result
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            mDialog = ProgressDialog.show(MainActivity.this,
                    "processing...", "", true);
            // 背景工作處理"前"需作的事
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
            // 背景工作處理"中"更新的事
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            mDialog.dismiss();
            // 背景工作處理完"後"需作的事

        }

        @Override
        protected void onCancelled() {
            // TODO Auto-generated method stub
            super.onCancelled();
            // 背景工作被"取消"時作的事，此時不作 onPostExecute(String result)
        }
    }

    public void querylight() {
        int by10 = (GlobalVariable.SumQuery_short + (Integer
                .parseInt(GlobalVariable.by9_NoReturn, 16))) % 256;
        String send_query = GlobalVariable.String_query_short + Integer.toHexString(by10);
        sendMessage(send_query);
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }
    }


    public void query_shortaddr() {
        helper = new DBHelper(getApplicationContext());
        cursor = helper.select(TABLE_NAME_SHORT_ADDRESS);
        int i;

        if (GlobalVariable.query_short_packet == null) {
            Toast.makeText(getApplicationContext(), "can't get the short address", Toast.LENGTH_SHORT).show();
        } else {
            SQLiteDatabase db = helper.getWritableDatabase();
            if (cursor.getCount() > 0) {
                db.delete(TABLE_NAME_SHORT_ADDRESS, null, null);
            }
            for (i = 14; i < GlobalVariable.query_short_packet.length(); i = i + 22) {
                ContentValues values = new ContentValues();
                values.put(FEILD_SHORTADDRESS, GlobalVariable.query_short_packet.substring(i, i + 4));
                db.insert(TABLE_NAME_SHORT_ADDRESS, null, values);
            }
            cursor.requery();
            //query_daliaddr();
            new Query_dali_Async().execute();
        }
        if (D)
            Log.e(TAG, "in querylight " + list);

    }


    public class Query_dali_Async extends AsyncTask<Void, Integer, String> {
        @Override
        protected String doInBackground(Void... arg0) {
            helper = new DBHelper(getApplicationContext());
            cursor = helper.select(TABLE_NAME_SHORT_ADDRESS);
            query_daliaddr_with_zigbee();

            return null; // 會傳給 onPostExecute(String result) 的 String result
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            mDialog = ProgressDialog.show(MainActivity.this,
                    "processing...", "", true);
            // 背景工作處理"前"需作的事
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
            // 背景工作處理"中"更新的事
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            mDialog.dismiss();
            // 背景工作處理完"後"需作的事
        }


        @Override
        protected void onCancelled() {
            // TODO Auto-generated method stub
            super.onCancelled();
            // 背景工作被"取消"時作的事，此時不作 onPostExecute(String result)
        }
    }

    public class setup_group extends AsyncTask<Void, Integer, String> {
        @Override
        protected String doInBackground(Void... arg0) {

            /*helper.close();
            cursor.close();
            if (D)
                Log.e(TAG, "isSelectedList save to db : " + isSelectedList);
            helper = new DBHelper(getApplicationContext());
            cursor = helper.select(TABLE_NAME_DALICOMMAND);
            if (cursor.getCount() == 0) {
                SQLiteDatabase db = helper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(FEILD_DALICOMMAND, String.valueOf(isSelectedList));
                db.insert(TABLE_NAME_DALICOMMAND, null, values);
            } else {
                cursor.moveToPosition(0);
                helper.update(cursor.getInt(0), TABLE_NAME_DALICOMMAND, FEILD_DALICOMMAND, isSelectedList.toString());
            }*/
            //helper.close();
            //cursor.close();
            // if(D)
            //      Log.e(TAG,"isSelectedList.get(1);"+isSelectedList.get(1).get(1));
            for (int a = 0; a < GroupList.size(); a++) {
                for (int b = 0; b < DaliList.size(); b++) {
                    if (isSelectedList.get(a).get(b)) {

                        //{a{1,2},b{3,4}}
                        if (D)
                            Log.e(TAG, "isSelectedList.get(" + a + ")" + ".get(" + b + ")");
                        helper = new DBHelper(getApplicationContext());
                        cursor = helper.select(TABLE_NAME_DALISLAVEADDRESS);
                        cursor.moveToPosition(b);
                        String Dali = cursor.getString(1);

                        String send_set_group = GlobalVariable.set_group + Dali;

                        int by10 = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
                                + (Integer.parseInt(GlobalVariable.by2_55, 16))
                                + (Integer.parseInt("07", 16))
                                + (Integer.parseInt("06", 16))
                                + (Integer.parseInt("60", 16))
                                + (Integer.parseInt(Dali, 16))
                                + a)
                                % 256;
                        if (D)
                            Log.e(TAG, "by10 " + by10);
                        //int checksum = Integer.parseInt(send_set_group, 16);
                        String send_set_group1 = send_set_group + "6" + (Integer.parseInt(String.valueOf(a), 16))+"00" + Integer.toHexString(by10);
                        sendMessage(send_set_group1);
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException ignored) {
                        }
                        helper.close();
                        cursor.close();
                    }
                }
            }

            return null; // 會傳給 onPostExecute(String result) 的 String result
        }
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            mDialog = ProgressDialog.show(MainActivity.this,
                    "processing...", "", true);
            // 背景工作處理"前"需作的事
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
            // 背景工作處理"中"更新的事
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            mDialog.dismiss();
            // 背景工作處理完"後"需作的事
        }


        @Override
        protected void onCancelled() {
            // TODO Auto-generated method stub
            super.onCancelled();
            // 背景工作被"取消"時作的事，此時不作 onPostExecute(String result)
        }
    }

    public void query_daliaddr_with_zigbee() {
        cursor.moveToPosition(GlobalVariable.i);
        if (D)
            Log.e(TAG, "----------cursor.getString(1).substring(0, 2)----------" + cursor.getString(1).substring(0, 2));
        Log.e(TAG, "----------cursor.getString(1).substring(2, 4)----------" + cursor.getString(1).substring(2, 4));

        String send_query_daliaddr = GlobalVariable.String_query_dali_1 + cursor.getString(1).substring(0, 2) + cursor.getString(1).substring(2, 4) + GlobalVariable.String_query_dali_2;
        int SumQuery_dali = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
                + (Integer.parseInt(GlobalVariable.by2_55, 16))
                + (Integer.parseInt(GlobalVariable.by3_06, 16))
                + (Integer.parseInt(GlobalVariable.by4_01, 16))
                + (Integer.parseInt(cursor.getString(1).substring(0, 2), 16))
                + (Integer.parseInt(cursor.getString(1).substring(2, 4), 16))
                + (Integer.parseInt(GlobalVariable.by7_00, 16))
                + (Integer.parseInt(GlobalVariable.by8_query_dali, 16))
                + (Integer.parseInt(GlobalVariable.by9_Return, 16))) % 256;
        String send_query_daliaddr_with_check = send_query_daliaddr + Integer.toHexString(SumQuery_dali);
        if (D)
            Log.e(TAG, "----------send_query_daliaddr_with_check----------" + send_query_daliaddr_with_check);
        sendMessage(send_query_daliaddr_with_check);
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }
        GlobalVariable.i = GlobalVariable.i + 1;
    }

    public void query_daliaddr_without_zigbee() {
        String send_query_daliaddr = GlobalVariable.String_query_dali_1 + "00" + "00" + GlobalVariable.String_query_dali_2;
        int SumQuery_dali = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
                + (Integer.parseInt(GlobalVariable.by2_55, 16))
                + (Integer.parseInt(GlobalVariable.by3_06, 16))
                + (Integer.parseInt(GlobalVariable.by4_01, 16))
                + (Integer.parseInt(GlobalVariable.by7_00, 16))
                + (Integer.parseInt(GlobalVariable.by8_query_dali, 16))
                + (Integer.parseInt(GlobalVariable.by9_Return, 16))) % 256;
        String send_query_daliaddr_with_check = send_query_daliaddr + "0" + Integer.toHexString(SumQuery_dali);
        if (D)
            Log.e(TAG, "----------send_query_daliaddr_with_check----------" + send_query_daliaddr_with_check);
        sendMessage(send_query_daliaddr_with_check);
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }

    }

    public String getnumber(int i) {
        cursor.moveToPosition(i);
        return cursor.getString(1);
    }


    public class wait_and_connect extends AsyncTask<Void, Integer, String> {
        @Override
        protected String doInBackground(Void... arg0) {

            try {
                Thread.sleep(1500);
            } catch (InterruptedException ignored) {
            }


            return null; // 會傳給 onPostExecute(String result) 的 String result
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            mDialog = ProgressDialog.show(MainActivity.this,
                    "processing...", "", true);
            // 背景工作處理"前"需作的事
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
            // 背景工作處理"中"更新的事
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            mDialog.dismiss();
            if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
                Intent serverIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            } else {
                new query_is_zigbee_exist_or_not_Async().execute();
            }
            // 背景工作處理完"後"需作的事
        }


        @Override
        protected void onCancelled() {
            // TODO Auto-generated method stub
            super.onCancelled();
            // 背景工作被"取消"時作的事，此時不作 onPostExecute(String result)
        }
    }


}

