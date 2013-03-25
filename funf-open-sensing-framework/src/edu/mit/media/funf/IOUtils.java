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
package edu.mit.media.funf;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;

import android.util.Log;
import static edu.mit.media.funf.Utils.TAG;

public class IOUtils {
	
	public static String inputStreamToString(InputStream is, String encoding) throws IOException {
		final char[] buffer = new char[0x10000];
		StringBuilder out = new StringBuilder();
		Reader in = new InputStreamReader(is, encoding);
		int read;
		do {
		  read = in.read(buffer, 0, buffer.length);
		  if (read>0) {
		    out.append(buffer, 0, read);
		  }
		} while (read>=0);
		return out.toString();
	}
	
	public static String httpGet(String uri,HttpParams params){
		String responseBody=null;
		HttpClient httpclient = new DefaultHttpClient();
		StringBuilder uriBuilder = new StringBuilder(uri);
		HttpGet httpget = new HttpGet(uriBuilder.toString());
		if (params != null) {
			httpget.setParams(params);
		}
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        try {
		    responseBody = httpclient.execute(httpget, responseHandler);
		} catch (ClientProtocolException e) {
			Log.e(TAG, "HttpGet Error: ", e);
			responseBody=null;
		} catch (IOException e) {
			Log.e(TAG, "HttpGet Error: ", e);
			responseBody=null;
		} finally{
	        httpclient.getConnectionManager().shutdown();  
		}
        return responseBody;
	}
}
