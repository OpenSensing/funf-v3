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
package edu.mit.media.funf.probe.builtin;

import android.content.Context;
import android.os.Bundle;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import edu.mit.media.funf.probe.SynchronousProbe;
import edu.mit.media.funf.probe.builtin.ProbeKeys.CellKeys;

public class CellProbe extends SynchronousProbe implements CellKeys {
	
	@Override
	public String[] getRequiredPermissions() {
		return new String[] {
				android.Manifest.permission.ACCESS_COARSE_LOCATION
		};
	}
	
	@Override
	public String[] getRequiredFeatures() {
		return new String[] {
				"android.hardware.telephony"
		};
	}
	
	@Override
	protected String getDisplayName() {
		return "Nearby Cellular Towers Probe";
	}

	@Override
	protected Bundle getData() {
		TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		CellLocation location = manager.getCellLocation();
		Bundle data = new Bundle();
		if (location instanceof GsmCellLocation) {
			GsmCellLocation gsmLocation = (GsmCellLocation) location;
			gsmLocation.fillInNotifierBundle(data);
			data.putInt(TYPE, TelephonyManager.PHONE_TYPE_GSM);
		} else if (location instanceof CdmaCellLocation) {
			CdmaCellLocation cdmaLocation = (CdmaCellLocation) location;
			cdmaLocation.fillInNotifierBundle(data);
			data.putInt(TYPE, TelephonyManager.PHONE_TYPE_CDMA);
		} else {
			data.putInt(TYPE, TelephonyManager.PHONE_TYPE_NONE);
		}
		return data;
	}
	
	@Override
	protected long getDefaultPeriod() {
		return 300L;
	}

}
