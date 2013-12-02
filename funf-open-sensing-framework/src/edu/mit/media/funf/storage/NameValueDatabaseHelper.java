/**
 * Funf: Open Sensing Framework
 * Copyright (C) 2010-2011 Nadav Aharony, Wei Pan, Alex Pentland. 
 * Acknowledgments: Alan Gardner
 * Contact: nadav@media.mit.edu
 * 
 * This file is part of Funf.
 * 
 * Funf is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of 
 * the License, or (at your option) any later version. 
 * 
 * Funf is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with Funf. If not, see <http://www.gnu.org/licenses/>.
 */
package edu.mit.media.funf.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.telephony.TelephonyManager;
import android.util.Log;
import edu.mit.media.funf.Utils;
import edu.mit.media.funf.configured.FunfConfig;

import static edu.mit.media.funf.AsyncSharedPrefs.async;

public class NameValueDatabaseHelper extends SQLiteOpenHelper {

	public static final int CURRENT_VERSION = 3;

	public static final String APP_VER = "v0.3.3.0";
	
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_TIMESTAMP = "timestamp";
	public static final String COLUMN_VALUE = "value";
	public static final Table DATA_TABLE = new Table("data", 
			Arrays.asList(new Column(COLUMN_NAME, "TEXT"), // ACTION from data broadcast
					      new Column(COLUMN_TIMESTAMP, "INTEGER"), // TIMESTAMP in data broadcast
					      new Column(COLUMN_VALUE, "TEXT"))); // JSON representing 
	public static final String COLUMN_DATABASE_NAME= "dbname";
	public static final String COLUMN_INSTALLATION = "device";
	public static final String COLUMN_DEVICE_ID = "device_id";
    public static final String COLUMN_DEVICE_BT_MAC = "device_bt_mac";
    public static final String COLUMN_SENSIBLE_TOKEN = "sensible_token";
	public static final String COLUMN_UUID = "uuid";
	public static final String COLUMN_CREATED = "created";
	public static final Table FILE_INFO_TABLE = new Table("file_info", 
			Arrays.asList(new Column(COLUMN_DATABASE_NAME, "TEXT"), // Name of this database
						  new Column(COLUMN_INSTALLATION, "TEXT"), // Universally Unique Id for device installation
						  new Column(COLUMN_DEVICE_ID, "TEXT"), // Unique id of the device
				      	  new Column(COLUMN_UUID, "TEXT"), // Universally Unique Id for file
                          new Column(COLUMN_DEVICE_BT_MAC, "TEXT"), // Bluetooth MAC for easy lookup
                          new Column(COLUMN_SENSIBLE_TOKEN, "TEXT"), // access token from the identity provider
					      new Column(COLUMN_CREATED, "INTEGER"))); // TIMESTAMP in data broadcast
    private final String TAG = this.getClass().getSimpleName();


    private final Context context;
	private final String databaseName;
	
	public NameValueDatabaseHelper(Context context, String name, int version) {
		super(context, name, null, version);
		this.context = context;
		this.databaseName = name;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATA_TABLE.getCreateTableSQL());
		db.execSQL(FILE_INFO_TABLE.getCreateTableSQL());
		// Insert file identifier information
		String installationUuid = Utils.getInstallationId(context);
		String fileUuid = UUID.randomUUID().toString();
		fileUuid += "-" + APP_VER; // add Funf version
		fileUuid += "-" + "0.5"; //add Datacollector version
		final String deviceId = ((TelephonyManager) context.getSystemService( Context.TELEPHONY_SERVICE )).getDeviceId();
		long createdTime = Utils.getTimestamp();
        BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
        final String bt_mac = mAdapter.getAddress();
        final String token = Utils.getSensibleAccessToken(context);

        //final String token = RegistrationHandler.
        Log.d(TAG, "Inserting into file_info table: " + String.format("insert into %s (%s, %s, %s, %s, %s, %s, %s) values ('%s', '%s', %s, '%s', %d, '%s', '%s')",
                FILE_INFO_TABLE.name,
                COLUMN_DATABASE_NAME, COLUMN_INSTALLATION, COLUMN_DEVICE_ID, COLUMN_UUID, COLUMN_CREATED, COLUMN_DEVICE_BT_MAC, COLUMN_SENSIBLE_TOKEN,
                databaseName, installationUuid, deviceId, fileUuid, createdTime, bt_mac, token));
		db.execSQL(String.format("insert into %s (%s, %s, %s, %s, %s, %s, %s) values ('%s', '%s', %s, '%s', %d, '%s', '%s')",
				FILE_INFO_TABLE.name, 
				COLUMN_DATABASE_NAME, COLUMN_INSTALLATION, COLUMN_DEVICE_ID, COLUMN_UUID, COLUMN_CREATED, COLUMN_DEVICE_BT_MAC, COLUMN_SENSIBLE_TOKEN,
				databaseName, installationUuid, deviceId, fileUuid, createdTime, bt_mac, token));
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Nothing yet
	}	
	
	// TODO: Consider moving these to an external utils class
	/**
	 * Immutable Table Definition
	 */
	public static class Table {
		private static final String CREATE_TABLE_FORMAT = "CREATE TABLE %s (_id INTEGER primary key autoincrement, %s);";
		
		public final String name;
		private final List<Column> columns;
		public Table(final String name, final List<Column> columns) {
			this.name = name;
			this.columns = new ArrayList<Column>(columns);
		}
		public List<Column> getColumns() { return new ArrayList<Column>(columns); }
		public String getCreateTableSQL() {
            Log.d("NameValueDatabaseHelper" + "_Table", "creating: " + String.format(CREATE_TABLE_FORMAT, name, Utils.join(columns, ", ")));
			return String.format(CREATE_TABLE_FORMAT, name, Utils.join(columns, ", "));
		}
	}

	/**
	 * Immutable Column Definition
	 *
	 */
	public static class Column {
		public final String name, type;
		public Column(final String name, final String type) {
			this.name = name;
			this.type = type;
		}
		@Override
		public String toString() {
			return name + " " + type;
		}
	}

}
