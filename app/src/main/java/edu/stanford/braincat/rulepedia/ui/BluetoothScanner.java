package edu.stanford.braincat.rulepedia.ui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;

import edu.stanford.braincat.rulepedia.channels.ibeacon.IBeaconDevice;
import edu.stanford.braincat.rulepedia.model.DeviceDatabase;
import edu.stanford.braincat.rulepedia.model.DevicePool;
import edu.stanford.braincat.rulepedia.model.ObjectDatabase;
import edu.stanford.braincat.rulepedia.omletUI.OmletUIService;

/**
 * Created by gcampagn on 5/30/15.
 */
public class BluetoothScanner extends Handler implements BluetoothAdapter.LeScanCallback {
    private final Activity parentActivity;

    //private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;

    public static final int REQUEST_ENABLE_BT = 1;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 100000;

    public BluetoothScanner(Activity parent) {
        parentActivity = parent;

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) parentActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    @Override
    public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {
        parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                IBeaconDevice ibd = IBeaconDevice.newIBeaconDevice(scanRecord);
                if (ibd != null) {
                    Log.d(MainActivity.LOG_TAG, "FOUND IBEACON BLE DEVICE: " + device.toString());
                    iBeaconFound(ibd);
                }
            }
        });
    }

    private void iBeaconFound(IBeaconDevice ibd) {
        ObjectDatabase od = ObjectDatabase.get();
        od.store(DevicePool.PLACEHOLDER_PREFIX + "ibeacon/" + ibd.deviceType, ibd);
        asyncSaveDB();

        DeviceDatabase dd = DeviceDatabase.get();
        if(dd.addDevice(ibd)) {
            Log.d(MainActivity.LOG_TAG, "New Device: " + ibd.uuid);
            Intent intent = new Intent(parentActivity, OmletUIService.class);
            intent.setAction(OmletUIService.NOTIFY_USER_NEW_DEVICE_DETECTED);
            intent.putExtra("URL", ibd.getUrl());
            parentActivity.startService(intent);
        }
        else {
            Log.d(MainActivity.LOG_TAG, "Device already seen: " + ibd.uuid);
        }
    }

    private void asyncSaveDB() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ObjectDatabase.get().save(parentActivity);
                } catch (IOException e) {
                    Log.e(MainActivity.LOG_TAG, "Failed to save object database", e);
                }
            }
        });
    }

    public void start() {
    }

    private void ensureBluetooth() {
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            parentActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public void resume() {
        ensureBluetooth();
        scanLeDevice(true);
    }

    public void pause() {
        scanLeDevice(false);
    }

    public void stop() {
    }

    private void scanLeDevice(final boolean enable) {
        if (mBluetoothAdapter == null)
            return;
        if (mScanning == enable)
            return;

        if (enable) {
            // Stops scanning after a pre-defined scan period.
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(BluetoothScanner.this);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(this);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(this);
        }
    }
}
