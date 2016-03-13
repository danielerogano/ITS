package it.unical.its.beacons;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.altbeacon.beacon.*;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends ActionBarActivity implements BeaconConsumer {

    public static final int REQUEST_BLUETOOTH_ENABLE = 1;
    private BeaconManager beaconManager;
    public static final String TAG = "Presence";
    public TextView text, txt1, txt2, txt3, txtuuid;
    public ImageView img1, img2, img3, img4;
    public double d1, d2, d3;
    public int area;

    private DeviceUUIDFactory uuidFactory;
    private String deviceId;
    private Long time;

    public static String SEND_URL;
    public static final String TIME_SERVER = "time-a.nist.gov";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uuidFactory = new DeviceUUIDFactory(getApplicationContext());
        deviceId = uuidFactory.getDeviceUuid().toString();
        updateTextUuid(deviceId);

        SEND_URL = getResources().getString(R.string.SEND_URL);

        text = (TextView)findViewById(R.id.textView);
        txt1 = (TextView)findViewById(R.id.textViewB1value);
        txt2 = (TextView)findViewById(R.id.textViewB2value);
        txt3 = (TextView)findViewById(R.id.textViewB3value);
        txtuuid = (TextView)findViewById(R.id.textViewUuid);

        img1 = (ImageView) findViewById(R.id.imageView);
        img1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                area = 1;
                Toast.makeText(getApplicationContext(), "Area 1", Toast.LENGTH_SHORT).show();
            }
        });
        img2 = (ImageView) findViewById(R.id.imageView2);
        img2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Area 2", Toast.LENGTH_SHORT).show();
                area = 2;
            }
        });
        img3 = (ImageView) findViewById(R.id.imageView3);
        img3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Area 3", Toast.LENGTH_SHORT).show();
                area = 3;
            }
        });
        img4 = (ImageView) findViewById(R.id.imageView4);
        img4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Area 4", Toast.LENGTH_SHORT).show();
                area = 4;
            }
        });

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
                    // Toast.makeText(getApplicationContext(), "Enter Region", Toast.LENGTH_SHORT).show();
                    displayMessage("Benvenuto in ufficio!");
                    beaconManager.startRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void didExitRegion(Region region) {
                try {
                    Log.d(TAG, "Exit Region");
                    // Toast.makeText(getApplicationContext(), "Exit Region", Toast.LENGTH_SHORT).show();
                    displayMessage("Arrivederci!");
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
                //Log.i(TAG, "The first beacon I see is about "+beacons.iterator().next().getDistance()+" meters away.");
                for (Beacon oneBeacon : beacons) {

                    if (oneBeacon.getId2().equals(Identifier.parse("1")) && oneBeacon.getId3().equals(Identifier.parse("1"))) {
                        d1 = oneBeacon.getDistance();
                        updateText1("" + d1);
                    }
                    if (oneBeacon.getId2().equals(Identifier.parse("1")) && oneBeacon.getId3().equals(Identifier.parse("2"))) {
                        d2 = oneBeacon.getDistance();
                        updateText2("" + d2);
                    }
                    if (oneBeacon.getId2().equals(Identifier.parse("1")) && oneBeacon.getId3().equals(Identifier.parse("3"))) {
                        d3 = oneBeacon.getDistance();
                        updateText3("" + d3);
                    }

/*
                    if (oneBeacon.getId2().equals(Identifier.parse("2")) && oneBeacon.getId3().equals(Identifier.parse("3"))) {
                        Log.d(TAG, "Beacon: " + oneBeacon.getId2() + " - " + oneBeacon.getId3() + " - Distance: " + oneBeacon.getDistance() + " - Rssi: " + oneBeacon.getRssi() + " - TxPower: " + oneBeacon.getTxPower());
                    }
                    // Log.d(TAG, "Distance: " + oneBeacon.getDistance() + "!");

                    if (oneBeacon.getDistance() < 1.0 && oneBeacon.getId2().equals(Identifier.parse("1")) && oneBeacon.getId3().equals(Identifier.parse("1"))) {
                        updateText("Beacon PRO 1 - Distance: " + oneBeacon.getDistance());
                    }
                    if (oneBeacon.getDistance() < 1.0 && oneBeacon.getId2().equals(Identifier.parse("1")) && oneBeacon.getId3().equals(Identifier.parse("2"))) {
                        updateText("Beacon PRO 2 - Distance: " + oneBeacon.getDistance());
                    }
                    if (oneBeacon.getDistance() < 1.0 && oneBeacon.getId2().equals(Identifier.parse("1")) && oneBeacon.getId3().equals(Identifier.parse("3"))) {
                        updateText("Beacon PRO 3 - Distance: " + oneBeacon.getDistance());
                    }
                    if (oneBeacon.getDistance() < 1.0 && oneBeacon.getId2().equals(Identifier.parse("2")) && oneBeacon.getId3().equals(Identifier.parse("1"))) {
                        updateText("Beacon mini 1 - Distance: " + oneBeacon.getDistance());
                    }
                    if (oneBeacon.getDistance() < 1.0 && oneBeacon.getId2().equals(Identifier.parse("2")) && oneBeacon.getId3().equals(Identifier.parse("2"))) {
                        updateText("Beacon mini 2 - Distance: " + oneBeacon.getDistance());
                    }
                    if (oneBeacon.getDistance() < 1.0 && oneBeacon.getId2().equals(Identifier.parse("2")) && oneBeacon.getId3().equals(Identifier.parse("3"))) {
                        updateText("Beacon mini 3 - Distance: " + oneBeacon.getDistance());
                    }
*/
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

    public void updateText1(final String line) {
        runOnUiThread(new Runnable() {
            public void run() {
                txt1 = (TextView) MainActivity.this
                        .findViewById(R.id.textViewB1value);
                txt1.setText(line);
            }
        });
    }

    public void updateText2(final String line) {
        runOnUiThread(new Runnable() {
            public void run() {
                txt2 = (TextView) MainActivity.this
                        .findViewById(R.id.textViewB2value);
                txt2.setText(line);
            }
        });
    }

    public void updateText3(final String line) {
        runOnUiThread(new Runnable() {
            public void run() {
                txt3 = (TextView) MainActivity.this
                        .findViewById(R.id.textViewB3value);
                txt3.setText(line);
            }
        });
    }

    public void updateTextUuid(final String line) {
        runOnUiThread(new Runnable() {
            public void run() {
                txtuuid = (TextView) MainActivity.this
                        .findViewById(R.id.textViewUuid);
                txtuuid.setText(line);
            }
        });
    }

    public void displayMessage(final String message) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendData(){

        final String url = SEND_URL;

        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject();
            jsonObject.put("time", time);
            jsonObject.put("uuid", deviceId);
            jsonObject.put("b1", d1);
            jsonObject.put("b2", d2);
            jsonObject.put("b3", d3);
            jsonObject.put("area", area);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST,
                url,
                jsonObject,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response){

                        Log.v(TAG, "LOGIN Response: " + response.toString());

                        try {
                            boolean valid = response.getBoolean("valid");

                            if(valid) {
                                Toast.makeText(getApplicationContext(), "send OK!", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "send FAIL!", Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {

                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        String message = "A network error has occurred on " + url + "(" + error.toString() + ")";
                    }
                });

        postRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 1, 1.0f));
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(postRequest);
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
