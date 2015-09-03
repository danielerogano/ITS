package it.unical.its.beacons;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.altbeacon.beacon.*;

import java.util.Collection;

public class MainActivity extends ActionBarActivity implements BeaconConsumer {

    public static final int REQUEST_BLUETOOTH_ENABLE = 1;
    private BeaconManager beaconManager;
    public static final String TAG = "Presence";
    public TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = (TextView)findViewById(R.id.textView);

        verifyBluetooth();

        beaconManager = BeaconManager.getInstanceForApplication(this);

        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        beaconManager.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    private void verifyBluetooth() {

        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth not enabled");
                builder.setMessage("Please enable bluetooth in settings and restart this application.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                        System.exit(0);
                    }
                });
                builder.show();
            }
        }
        catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                    System.exit(0);
                }

            });
            builder.show();

        }

    }

    @Override
    public void onBeaconServiceConnect() {
        final Region region = new Region("myBeacons", Identifier.parse("A7AE2EB7-1F00-4168-B99B-A749BAC1CA64"), null, null);
        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                try {
                    Log.d(TAG, "Enter Region");
                    Toast.makeText(getApplicationContext(), "Enter Region", Toast.LENGTH_SHORT).show();
                    beaconManager.startRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void didExitRegion(Region region) {
                try {
                    Log.d(TAG, "Exit Region");
                    Toast.makeText(getApplicationContext(), "Exit Region", Toast.LENGTH_SHORT).show();
                    beaconManager.stopRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {

            }
        });

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                //   Log.i(TAG, "The first beacon I see is about "+beacons.iterator().next().getDistance()+" meters away.");
                for (Beacon oneBeacon : beacons) {
                    Log.d(TAG, "Beacon: " + oneBeacon.getId1() + " - " + oneBeacon.getId2() + " - " + oneBeacon.getId3());
                    Log.d(TAG, "Distance: " + oneBeacon.getDistance() + "!");

                    if (oneBeacon.getDistance() < 1.0 && oneBeacon.getId3().equals(Identifier.parse("1"))) {
                        //Log.d(TAG, "OKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK");
                        updateText("CUCINA");
                        // Perform distance-specific action here
                    }
                    if (oneBeacon.getDistance() < 1.0 && oneBeacon.getId3().equals(Identifier.parse("2"))) {
                        //Log.d(TAG, "OKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK");
                        updateText("STUDIO");
                        // Perform distance-specific action here
                    }
                    if (oneBeacon.getDistance() < 1.0 && oneBeacon.getId3().equals(Identifier.parse("3"))) {
                        //Log.d(TAG, "OKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK");
                        updateText("STANZA DA LETTO");
                        // Perform distance-specific action here
                    }

                }

            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public void updateText(final String line) {
        runOnUiThread(new Runnable() {
            public void run() {
                text = (TextView) MainActivity.this
                        .findViewById(R.id.textView);
                text.setText(line);
            }
        });
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
