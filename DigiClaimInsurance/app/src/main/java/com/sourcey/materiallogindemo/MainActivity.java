package com.sourcey.materiallogindemo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.itextpdf.text.pdf.PdfTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;


public class MainActivity extends AppCompatActivity {

    public static Camera cam = null;
    ListView listView;
    SensorManager sensorManager;
    List<Sensor> listsensor;
    List<String> liststring;
    ArrayAdapter<String> adapter;
    TextView imei_number;
    Button get_imei;
    String IMEI_Number_Holder;
    TelephonyManager telephonyManager;
    Map<String,Boolean> parameters;
    public   String[] header1={"ID","Sensors"};
    public   String[] header={"ID","Paramenter Name","Working State(Yes/No)"};
    private String shortText="Report";
    private String longText="Report will include status of various parameters of mobile phone";
    private TemplatePDF templatePDF;

    FirebaseDatabase mbase;
    DatabaseReference dbref;


    Button test;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mbase=FirebaseDatabase.getInstance();
        dbref=mbase.getReference("users/"+ FirebaseAuth.getInstance().getUid()+"/PhoneAttributes");
        dbref.child("Model").setValue(Build.MODEL);
        dbref.child("Id").setValue(Build.ID);
        dbref.child("Manufacturer").setValue(Build.MANUFACTURER);
        dbref.child("Brand").setValue(Build.BRAND);
        dbref.child("User").setValue(Build.USER);
        dbref.child("SDK").setValue(Build.VERSION.SDK);
        dbref.child("Version Code").setValue(Build.VERSION.RELEASE);

        test=(Button)findViewById(R.id.test1);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test();
            }
        });


    }
    public void test()
    {
        parameters=new HashMap<String, Boolean>();

        try
        {
            WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifi.setWifiEnabled(true);
            wifi.setWifiEnabled(false);
            parameters.put("Wifi",true);
            parameters.put("Hotspot",true);
        }
        catch (Exception e)
        {
            Toast.makeText(getBaseContext(), "Exception WifiOff",
                    Toast.LENGTH_SHORT).show();
            parameters.put("Wifi",false);
            parameters.put("Hotspot",false);
        }



        try
        {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
            {
                cam = Camera.open();
                Camera.Parameters p = cam.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                cam.setParameters(p);
                cam.startPreview();
                parameters.put("FlashLight",true);
            }
        }

        catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), "Exception flashLightOn()",
                    Toast.LENGTH_SHORT).show();
            parameters.put("FlashLight",false);
        }

        try
        {
            if (getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH)) {
                cam.stopPreview();
                cam.release();
                cam = null;
                parameters.put("FlashLight",true);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), "Exception flashLightOff",
                    Toast.LENGTH_SHORT).show();
            parameters.put("FlashLight",false);
        }



        try
        {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            boolean isEnabled = bluetoothAdapter.isEnabled();
            if (!isEnabled) {
                bluetoothAdapter.enable();
            } else if (isEnabled) {

            }

            boolean isEnabled1 = bluetoothAdapter.isEnabled();
            if (isEnabled1) {
                bluetoothAdapter.disable();
            } else if (!isEnabled1) {
                bluetoothAdapter.enable();
            }
            bluetoothAdapter.disable();
            parameters.put("Bluetooth",true);
        }
        catch (Exception e)
        {
            Toast.makeText(getBaseContext(), "Exception BluetoothOff",
                    Toast.LENGTH_SHORT).show();
            parameters.put("Bluetooth",false);
        }
/*
        FingerprintManager fingerprintManager = (FingerprintManager) getApplicationContext().getSystemService(Context.FINGERPRINT_SERVICE);
        if (!fingerprintManager.isHardwareDetected()) {
            // Device doesn't support fingerprint authentication
            Toast.makeText(getApplicationContext(),"No support",Toast.LENGTH_LONG).show();
        } else if (!fingerprintManager.hasEnrolledFingerprints()) {
            // User hasn't enrolled any fingerprints to authenticate with
              Toast.makeText(getApplicationContext(),"supports",Toast.LENGTH_LONG).show();
        } else {
            // Everything is ready for fingerprint authentication
              Toast.makeText(getApplicationContext()," supports",Toast.LENGTH_LONG).show();
        }
        */
        liststring = new ArrayList<String>();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        listsensor = sensorManager.getSensorList(Sensor.TYPE_ALL);

        for (int i = 0; i < listsensor.size(); i++) {

            liststring.add(listsensor.get(i).getName());
        }
        telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        IMEI_Number_Holder = telephonyManager.getDeviceId();
        dbref.child("IMEI").setValue(IMEI_Number_Holder);
       // imei_number.setText(IMEI_Number_Holder);

        try {
            LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            parameters.put("GPS", true);
        }
        catch (Exception e)
        {
            Toast.makeText(getBaseContext(), "Exception GPSOff",
                    Toast.LENGTH_SHORT).show();
            parameters.put("GPS", false);
        }

        Log.d("Para",parameters.toString());
//        for (Map.Entry<String,Boolean> entry : parameters.entrySet()) {
//            Toast.makeText(this,"Key = " + entry.getKey() + ", Value = " + entry.getValue(),Toast.LENGTH_SHORT).show();
//        }
//        FingerprintManager fingerprintManager = (FingerprintManager) getApplicationContext().getSystemService(Context.FINGERPRINT_SERVICE);
//        if (!fingerprintManager.isHardwareDetected()) {
//            // Device doesn't support fingerprint authentication
//            parameters.put("FingerPrintService",true);
//            Toast.makeText(this,"FingerPrint available",Toast.LENGTH_SHORT).show();
//        } else if (!fingerprintManager.hasEnrolledFingerprints()) {
//            // User hasn't enrolled any fingerprints to authenticate with
//            Toast.makeText(this,"FingerPrint Not available",Toast.LENGTH_SHORT).show();
//        } else {
//            parameters.put("FingerPrintService",false);// Everything is ready for fingerprint authentication
//            Toast.makeText(this,"FingerPrint Not available",Toast.LENGTH_SHORT).show();
//        }


        templatePDF=new TemplatePDF(getApplicationContext());
        templatePDF.openDocument();
        templatePDF.addMetaDeta("Test for Insurance","Insurance","Abhishek Kotkar");
        templatePDF.addTitles("Mobile Performance Report","Complete Analysis","29/3/2019");
        templatePDF.addParagraph(shortText);
        templatePDF.addParagraph(longText);
        templatePDF.createTable(header1,getClients1(liststring));
        templatePDF.createTable(header,getClients(parameters));
        templatePDF.closeDocument();
        templatePDF.viewPDF();

    }




    public ArrayList<String[]>getClients(Map<String,Boolean> parameters)
    {
        ArrayList<String[]> rows=new ArrayList<>();
        int count=0;
        for (Map.Entry<String,Boolean> entry : parameters.entrySet()) {
            Toast.makeText(this,"Key = " + entry.getKey() + ", Value = " + entry.getValue(),Toast.LENGTH_SHORT).show();
            String[] temp=new String[3];
            temp[0]=count+"";
            count++;
            temp[1]=entry.getKey();
            if(entry.getValue()==true)
            {
                temp[2]="Yes";
            }
            else {
                temp[2]="No";
            }

            rows.add(temp);
        }
        return  rows;
    }

    public ArrayList<String[]>getClients1(List<String> SensorList)
    {
        ArrayList<String[]> rows=new ArrayList<>();
        for(int i=0;i<SensorList.size();i++)
        {
            String[] temp=new String[2];
            temp[0]=i+"";
            temp[1]=SensorList.get(i);
            rows.add(temp);
        }
        return  rows;
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
