package dk.dtu.imm.datacollector;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity
{

    private static final String TAG = "AUTH_MainActivity";
    private static boolean serviceRunning = false;
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
		if (!bt.isEnabled()) {
			bt.enable();
		}
			
		if (bt.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
		{
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
			startActivity(discoverableIntent);
		}

        //Intent i = new Intent(this, AuthActivity.class);
        //startActivity(i);
        if (!serviceRunning) {
            serviceRunning = true;
            LauncherReceiver.startService(this, RegistrationHandler.class);
        } else {
            Log.d(TAG, "Not starting the service again");
        }

		
	}

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		TextView tv = (TextView)findViewById(R.id.versionLabel);
		tv.setText("Version 0.1");		
	}
}
