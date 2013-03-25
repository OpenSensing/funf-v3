package dk.dtu.imm.datacollector;

import java.io.File;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;

public class MainActivity extends Activity
{
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
		
	}

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		TextView tv = (TextView)findViewById(R.id.versionLabel);
		tv.setText("Version 0.1");		
	}
}
