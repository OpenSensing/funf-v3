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

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import edu.mit.media.funf.probe.SensorProbe;
import edu.mit.media.funf.probe.builtin.ProbeKeys.OrientationSensorKeys;

public class OrientationSensorProbe extends SensorProbe implements OrientationSensorKeys {

	public int getSensorType() {
		return Sensor.TYPE_ORIENTATION;
	}

	public String[] getRequiredFeatures() {
		return new String[]{
			"android.hardware.sensor.gyroscope"
		};
	}
	
	public String[] getValueNames() {
		return new String[] {
			AZIMUTH, PITCH, ROLL
		};
	}

	@Override
	protected long getDefaultDuration() {
		return 15L;
	}

	@Override
	protected long getDefaultPeriod() {
		return 180L;
	}

	

}
