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
package dk.dtu.imm.goactive;

import android.os.Bundle;
import com.google.gson.*;
import edu.mit.media.funf.Utils;

import java.lang.reflect.Type;
import java.util.Map;
 
public class JsonUtils {
	
	public static Gson getGson() {
		return new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Bundle.class, new BundleJsonSerializer()).create();
	}

	public static class BundleJsonSerializer implements JsonSerializer<Bundle> {
		public JsonElement serialize(Bundle bundle, Type type, JsonSerializationContext context) {
			JsonObject object = new JsonObject();
			for (Map.Entry<String, Object> entry : Utils.getValues(bundle).entrySet()) {
				object.add(entry.getKey(), context.serialize(entry.getValue()));
			}
			return object;
		}
	}
}
  