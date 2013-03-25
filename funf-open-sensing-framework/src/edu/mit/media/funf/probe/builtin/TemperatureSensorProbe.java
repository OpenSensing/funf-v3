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
import edu.mit.media.funf.probe.builtin.ProbeKeys.TemperatureSensorKeys;

/**
 * Used to record temperature.  Implementation depends on the device and does not exist on all devices.  
 *	Some will record temperature of battery, others temperature of CPU or environment.
 *
 */
public class TemperatureSensorProbe extends SensorProbe implements TemperatureSensorKeys {

	public int getSensorType() {
		return Sensor.TYPE_TEMPERATURE;
	}

	public String[] getRequiredFeatures() {
		return new String[]{
			//"android.hardware.sensor.temperature"  doesn't exist yet
		};
	}
	
	public String[] getValueNames() {
		return new String[] {
			TEMPERATURE	
		};
	}

	@Override
	protected long getDefaultDuration() {
		return 10L;
	}
	
	@Override
	protected long getDefaultPeriod() {
		return 1200;
	}

}
