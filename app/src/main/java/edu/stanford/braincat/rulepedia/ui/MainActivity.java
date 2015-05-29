package edu.stanford.braincat.rulepedia.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanRecord;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import edu.stanford.braincat.rulepedia.R;
import edu.stanford.braincat.rulepedia.channels.Util;
import edu.stanford.braincat.rulepedia.exceptions.DuplicatedRuleException;
import edu.stanford.braincat.rulepedia.model.BLScanRecord;
import edu.stanford.braincat.rulepedia.model.IBeaconDevice;
import edu.stanford.braincat.rulepedia.model.Rule;
import edu.stanford.braincat.rulepedia.service.AutoStarter;
import edu.stanford.braincat.rulepedia.service.Callback;
import edu.stanford.braincat.rulepedia.service.RuleExecutor;
import edu.stanford.braincat.rulepedia.service.RuleExecutorService;


public class MainActivity extends ActionBarActivity {
    public static final String LOG_TAG = "rulepedia.UI";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    private ServiceConnection connection;
    private RuleExecutor executor;


    private class Connection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            executor = ((RuleExecutorService.Binder) iBinder).getRuleExecutor();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            executor = null;
        }
    }

    private void startService() {
        AutoStarter.startService(this);
        Intent intent = new Intent(this, RuleExecutorService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        initBLE();
        startBLE();
        scanLeDevice(true);

        // ensure the service is running
        connection = new Connection();
        startService();
    }

    public void onRuleInstalled() {
        new AlertDialog.Builder(this)
                .setTitle("Success")
                .setMessage("Rule added to database")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startActivityForResult(new Intent(MainActivity.this, GoogleFitAuthActivity.class), 0);

                        // continue with delete
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    public void onRuleInstallationError(Exception error) {
        if(error instanceof DuplicatedRuleException) {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Rule already in database")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Error adding rule")
                    .setMessage("Internal error " + error.toString())
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    private void installRule(final JSONObject jsonRule) throws JSONException {
        new AlertDialog.Builder(this)
                .setTitle("Install rule")
                .setMessage("Do you want to install the rule " + jsonRule.getString("name") + "?\n" +
                "The description says: " + jsonRule.getString("description"))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        RuleExecutor executor = getRuleExecutor();
                        if (executor == null)
                            return;

                        executor.installRule(jsonRule, new Callback<Rule>() {
                            @Override
                            public void run(@Nullable Rule result, @Nullable Exception error) {
                                if (result != null)
                                    onRuleInstalled();
                                else
                                    onRuleInstallationError(error);
                            }
                        });

                        // continue with delete
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // continue with delete
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent startIntent = getIntent();

        if (startIntent == null)
            return;

        switch (startIntent.getAction()) {
            case Intent.ACTION_VIEW:
                Uri data = startIntent.getData();

                if (!data.getScheme().equals("https") ||
                        (!data.getHost().equals("vast-hamlet-6003.herokuapp.com") &&
                                !data.getHost().equals("rulepedia.stanford.edu")) ||
                        !data.getPath().startsWith("/rule/")) {
                    Log.w(LOG_TAG, "Received spurious intent for URL " + data);
                    return;
                }

                try {
                    JSONObject json = Util.parseEncodedRule(startIntent.getData().getLastPathSegment());
                    installRule(json);
                } catch (Exception e) {
                    Log.w(LOG_TAG, "Failed to act on received rule URL", e);
                }
                return;
            case RuleExecutorService.INSTALL_RULE_INTENT:
                try {
                    installRule((JSONObject) new JSONTokener(startIntent.getStringExtra("json")).nextValue());
                } catch (Exception e) {
                    Log.w(LOG_TAG, "Failed to act on received rule install intent", e);
                }
                return;

            case Intent.ACTION_MAIN:
                return;

            default:
                Log.w(LOG_TAG, "Received spurious intent " + startIntent.getAction());
        }
    }

    public RuleExecutor getRuleExecutor() {
        return executor;
    }

    @Override
    public void onDestroy() {
        unbindService(connection);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*
        if (id == R.id.action_settings) {
            return true;
        }
        */

        return super.onOptionsItemSelected(item);

        /*
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_browse:
                //openSearch();
                return true;
            case R.id.action_install:
                return true;
            case R.id.action_manage:
                //composeMessage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
        */
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return RuleCreationFragment.newInstance();
                case 1:
                    return BrowseFragment.newInstance();
                case 2:
                    return RuleManageFragment.newInstance();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Create rules";
                case 1:
                    return "Browse rules";
                case 2:
                    return "Manage rules";
            }
            return null;
        }
    }



    @Override
    protected void onResume() {
        super.onResume();

        startBLE();
        scanLeDevice(true);
    }

    //private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 100000;

    private void initBLE()
    {
        mHandler = new Handler();

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    private void startBLE()
    {
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (mBluetoothAdapter == null)
            return;

        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Log.d("!!!!!!!!", "FOUND BLE DEVICE: " + device.toString());
                            IBeaconDevice ibd = IBeaconDevice.newIBeaconDevice(scanRecord);
                        }
                    });
                }
            };

}
