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
package dk.dtu.imm.datacollector2013;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import dk.dtu.imm.experiencesampling.ExperienceSampling;

public class LauncherReceiver extends BroadcastReceiver {
	
	private static boolean launched = false;
	
	public static void launch(Context context) {
		startService(context, MainPipeline.class); // Ensure main funf system is running

        int questionPerDayLimit = 3000; //24;
        long scheduleInterval = 1 * 30 * 1000; // 10 minutes - this is not so important as the service is started on all the onReceive events anyways.
        long gpsTimeout = 30 * 1000; // 30 sec
        ExperienceSampling.startExperienceSampling(context, RegistrationHandler.SHARED_PREFERENCES_NAME, RegistrationHandler.PROPERTY_SENSIBLE_TOKEN, questionPerDayLimit, scheduleInterval, gpsTimeout);

        launched = true;
	}
	
	public static void startService(Context context, Class<? extends Service> serviceClass) {
		Intent i = new Intent(context.getApplicationContext(), serviceClass);
		context.getApplicationContext().startService(i);
	} 
	
	public static boolean isLaunched() {
		return launched;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		launch(context);
        startService(context, RegistrationHandler.class);
	}
}
