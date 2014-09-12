package dk.dtu.imm.datacollector2013;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import edu.mit.media.funf.configured.ConfiguredPipeline;
import edu.mit.media.funf.probe.edu.mit.media.funf.activity.EpiStateActivity;

public class MainActivity extends Activity {

	public static final String KEY_MESSAGES = "MESSAGES";
    public static final String RESTART_DEVICE_MESSAGE = "På grund af problemer med Android drivers er der et problem med din Bluetooth. Genstart venligst telefonen, så skulle den virke igen. Mange tak!";
    private static final String TAG = "AUTH_MainActivity";
    public static final String RESTART_DEVICE_MESSAGE_TITLE = "Bluetooth problem";
    private static boolean serviceRunning = false;
    private ConnectivityManager connectivityManager;
    private FileObserver fileObserver;

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
            listMsg.add(0, new MessageItem(
					intent.getExtras().getString("title"),
					Long.parseLong(intent.getExtras().getString("timestamp",Long.toString(System.currentTimeMillis()/1000))),
					intent.getExtras().getString("message"),
					intent.getExtras().getString("url")));
			listAdapter.notifyDataSetChanged();
		}
	};
	private List<MessageItem> listMsg;
	private MessagesAdapter listAdapter;
	private ListView listview;
	private Gson gson;
	private ImageView imgStatus;
	private TextView txtFilesCount;
    private boolean restartPopupOn = false;
    private Button uploadButton;
    private Button dataQualityButton;
    private Button epigameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        gson = new Gson();
        imgStatus = (ImageView) findViewById(R.id.imgStatus);
        txtFilesCount = (TextView) findViewById(R.id.textFilesCount);
        listview = (ListView) findViewById(R.id.listMessages);
        listMsg = new LinkedList<MessageItem>();
        listAdapter = new MessagesAdapter(this, listMsg);
        listview.setAdapter(listAdapter);

        String action = getIntent().getAction();
        if (action != null && action.equals(BluetoothTimeoutBroadcastReceiver.RESTART_POPUP_ACTION)) {
            showRestartDialog();
            restartPopupOn = true;
        }

        fileObserver = new FileObserver(new File(Environment.getExternalStorageDirectory(), "dk.dtu.imm.datacollector2013/mainPipeline/archive").getAbsolutePath()) {
            @Override
            public void onEvent(int i, String s) {
                if(i == FileObserver.CREATE || i == FileObserver.DELETE){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setFileCountStatus();
                        }
                    });
                }
            }
        };

        uploadButton = (Button) findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(new UploadButtonClickListener());
        dataQualityButton = (Button)findViewById(R.id.dataQualityButton);
        dataQualityButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DataQualityActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });
        epigameButton = (Button)findViewById(R.id.epigameButton);
        epigameButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent dialogIntent = new Intent(getBaseContext(), EpiStateActivity.class);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |  Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                getApplication().startActivity(dialogIntent);
            }
        });
    }

    @Override
	protected void onStart() {
		super.onStart();
        setFileCountStatus();
        fileObserver.startWatching();
        if(restartPopupOn) return;

		BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
		if (!bt.isEnabled()) {
			bt.enable();
		}

		if (bt.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
			startActivity(discoverableIntent);
		}

		if (!serviceRunning) {
			serviceRunning = true;
			LauncherReceiver.startService(this, RegistrationHandler.class);
		} else {
			Log.d(TAG, "Not starting the service again");
		}

	}

    private void showRestartDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder
                .setTitle(RESTART_DEVICE_MESSAGE_TITLE)
                .setMessage(RESTART_DEVICE_MESSAGE)
                .setCancelable(false)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        restartPopupOn = false;
                    }
                }).create().show();
    }

    private void showNoInternetDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder
                .setTitle("No Internet")
                .setMessage("Please connect to the Internet before attempting to upload your files")
                .setCancelable(false)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
    }

    private void showAcceptUploadDialog() {
        DecimalFormat df = new DecimalFormat("#.0");
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder
                .setTitle("Upload files")
                .setMessage("You are about to upload " + df.format(getUploadFileSize()) + " MB worth of files. Are you sure?")
                .setCancelable(true)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startUploadService();
                    }
                }).create().show();
    }

    private void startUploadService() {
        Intent uploadIntent = new Intent(getApplicationContext(), MainPipeline.class);
        uploadIntent.setAction(MainPipeline.ACTION_UPLOAD_DATA);
        uploadIntent.putExtra(ConfiguredPipeline.EXTRA_FORCE_UPLOAD, true);
        startService(uploadIntent);
        Toast.makeText(this, "Upload started", Toast.LENGTH_SHORT).show();
    }

    private double getUploadFileSize() {
        double fileSize = 0;
        for(File file: new File(Environment.getExternalStorageDirectory(), "dk.dtu.imm.datacollector2013/mainPipeline/archive").listFiles()) {
            fileSize += file.length();
        }
        fileSize = fileSize / (1024 * 1024);

        return fileSize;
    }

    private boolean isUploadServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo service : runningServices) {
            if ("edu.mit.media.funf.storage.HttpUploadService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	@Override
	protected void onPause() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        saveMessages();
        fileObserver.stopWatching();
		super.onPause();
	}

    private void saveMessages() {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String msgJson = gson.toJson(listMsg);
        editor.putString(KEY_MESSAGES, msgJson);
        editor.commit();
    }

    @Override
	protected void onResume() {
        setFileCountStatus();
        fileObserver.startWatching();
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		String msgJson = prefs.getString(KEY_MESSAGES, "");
        if(!msgJson.isEmpty()) {
			listMsg.clear();
			listMsg.addAll((List<MessageItem>)gson.fromJson(msgJson, new TypeToken<LinkedList<MessageItem>>() {}.getType()));
			listAdapter.notifyDataSetChanged();
		}
		LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(GcmBroadcastReceiver.EVENT_MSG_RECEIVED));
		super.onResume();
	}

    private void setFileCountStatus() {
        int filesCount = getFilesCount();
        if (filesCount > 4) {
            imgStatus.setImageResource(R.drawable.status_problem);
        } else {
            imgStatus.setImageResource(R.drawable.status_ok);
        }
        txtFilesCount.setText("" + filesCount);
    }

    private int getFilesCount() {
		String[] files = null;
		try {
			files = new File(Environment.getExternalStorageDirectory(), "dk.dtu.imm.datacollector2013/mainPipeline/archive").list();
		} catch(Exception e) {
            Log.e(TAG, e.toString());
		}
		return files != null ? files.length : 0;
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
			tvBody.setText(values.get(position).message);
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

    private class UploadButtonClickListener implements OnClickListener {
        @Override
        public void onClick(View view) {
            if(isUploadServiceRunning()) {
                Toast.makeText(MainActivity.this, "Already uploading", Toast.LENGTH_SHORT).show();
                return;
            }
            if(getFilesCount() == 0) {
                Toast.makeText(MainActivity.this, "No files to upload", Toast.LENGTH_SHORT).show();
                return;
            }
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting()) {
                showAcceptUploadDialog();
            } else {
                showNoInternetDialog();
            }
        }
    }
}
