package dk.dtu.imm.datacollector2013;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String KEY_MESSAGES = "MESSAGES";
	private static final String TAG = "AUTH_MainActivity";
	private static boolean serviceRunning = false;

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			listMsg.add(0, new MessageItem(
					intent.getExtras().getString("title"), 
					Long.parseLong(intent.getExtras().getString("timestamp")), 
					intent.getExtras().getString("body"),
					intent.getExtras().getString("url")));
			listAdapter.notifyDataSetChanged();
		}
	};
	private List<MessageItem> listMsg;
	private MessagesAdapter listAdapter;
	private ListView listview;
	private Gson gson;
	
	@Override
	protected void onStart() {
		super.onStart();

		BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
		if (!bt.isEnabled()) {
			bt.enable();
		}

		if (bt.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
			startActivity(discoverableIntent);
		}

		// Intent i = new Intent(this, AuthActivity.class);
		// startActivity(i);
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

		gson = new Gson();
		
		ImageView imgStatus = (ImageView) findViewById(R.id.imgStatus);
		TextView txtFilesCount = (TextView) findViewById(R.id.textFilesCount);

		int filesCount = getFilesCount();
		if (filesCount > 0) {
			imgStatus.setImageResource(R.drawable.status_problem);
		} else {
			imgStatus.setImageResource(R.drawable.status_ok);
		}
		txtFilesCount.setText("" + filesCount);
		listview = (ListView) findViewById(R.id.listMessages);
		listMsg = new LinkedList<MessageItem>();
		listAdapter = new MessagesAdapter(this, listMsg);
		listview.setAdapter(listAdapter);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	
	@Override
	protected void onPause() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
	    SharedPreferences.Editor editor = prefs.edit();
	    String msgJson = gson.toJson(listMsg);
	    editor.putString(KEY_MESSAGES, msgJson);
	    editor.commit();
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		String msgJson = prefs.getString(KEY_MESSAGES, "");
		if(msgJson.isEmpty() == false) {
			listMsg.clear();
			listMsg.addAll((List<MessageItem>)gson.fromJson(msgJson, new TypeToken<LinkedList<MessageItem>>() {}.getType()));
			listAdapter.notifyDataSetChanged();
		}
		LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
				new IntentFilter(GcmBroadcastReceiver.EVENT_MSG_RECEIVED));
		super.onResume();
	}

	private int getFilesCount() {
		String[] files = null;
		try {
			files = new File(Environment.getExternalStorageDirectory(), "dk.dtu.imm.datacollector2013/mainPipeline/archive").list();
		} catch(Exception ignore) {
			
		}
		return files != null ? files.length : 0;
	}

	class MessageItem {

		String title;
		long timestamp;
		String body;
		String url;
		boolean collapsed = true;
		
		public MessageItem(String title, long timestamp, String body, String url) {
			this.title = title;
			this.timestamp = timestamp;
			this.body = body;
			this.url = url;
		}
		
	}

	class MessagesAdapter extends ArrayAdapter<MessageItem> {
		private final Context context;
		private final List<MessageItem> values;

		public MessagesAdapter(Context context, List<MessageItem> values) {
			super(context, R.layout.messageitem_layout, values);
			this.context = context;
			this.values = values;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View viewMsg = inflater.inflate(R.layout.messageitem_layout, parent, false);
			final TextView tvDate = (TextView) viewMsg.findViewById(R.id.messageDate);
			Calendar cal = GregorianCalendar.getInstance();
			cal.setTimeInMillis(values.get(position).timestamp * 1000);
			java.text.DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm");
			String date = dateFormat.format(cal.getTime());
			tvDate.setText(date);
			final TextView tvBody = (TextView) viewMsg.findViewById(R.id.messageBody);
			tvBody.setText(values.get(position).body);
			tvBody.setVisibility(values.get(position).collapsed ? View.GONE : View.VISIBLE);
			final ImageView imgCollapse = (ImageView) viewMsg.findViewById(R.id.imgCollapse);
			imgCollapse.setImageResource(values.get(position).collapsed ? R.drawable.arrow_down : R.drawable.arrow_up);
			imgCollapse.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					boolean newCollapsedStatus = !values.get(position).collapsed;
					for(MessageItem msi : values) {
						msi.collapsed = true;
					}
					values.get(position).collapsed = newCollapsedStatus;
					listview.invalidateViews();
				}
			});
			final TextView tvTitle = (TextView) viewMsg.findViewById(R.id.messageTitle);
			tvTitle.setText(values.get(position).title);
			viewMsg.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					String url = values.get(position).url;
					if(url != null) {
						Intent i = new Intent(Intent.ACTION_VIEW);
						i.setData(Uri.parse(url));
						startActivity(i);
					}
				}
			});
			return viewMsg;
		}
	}

}
