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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.net.Uri;
import android.util.Log;


import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import javax.net.ssl.HttpsURLConnection;

import static edu.mit.media.funf.Utils.TAG;
/**
 * Archives a file to the url specified using POST HTTP method.
 * 
 * NOTE: not complete or tested
 *
 */
public class HttpArchive implements RemoteArchive {
	
	private String uploadUrl;
	private String mimeType;
	
	public HttpArchive(final String uploadUrl) {
		this(uploadUrl, "application/x-binary");
	}

    public HttpArchive(final String uploadUrl, final String[] get_params) {
        String address = uploadUrl;
        if (get_params.length > 0) {
            address += "?" + get_params[0];
            if (get_params.length > 1) {
                for (int i = 1; i < get_params.length; i++) {
                    address += "&" + get_params[i];
                }
            }
        }
        this.uploadUrl = address;
        this.mimeType = "application/x-binary";
    }
	
	public HttpArchive(final String uploadUrl, final String mimeType) {
		this.uploadUrl = uploadUrl;
		this.mimeType = mimeType;
	}
	
	public String getId() {
		return uploadUrl;
	}
	
	public boolean add(File file) {
		/*
		HttpClient httpclient = new DefaultHttpClient();
		try {
		    httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		    HttpPost httppost = new HttpPost(uploadUrl);
		    
		    httppost.setEntity(new FileEntity(file, mimeType));
	
		    Log.i(TAG, "executing request " + httppost.getRequestLine());
		    HttpResponse response = httpclient.execute(httppost);
	
		    HttpEntity resEntity = response.getEntity();
		    if (resEntity == null) {
		    	Log.i(TAG, "Null response entity.");
		    	return false;
		    }
		    Log.i(TAG, "Response " + response.getStatusLine().getStatusCode() + ": " 
		    		+ IOUtils.inputStreamToString(resEntity.getContent(), "UTF-8"));
		} catch (ClientProtocolException e) {
			Log.e(TAG, e.getLocalizedMessage());
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			Log.e(TAG, e.getLocalizedMessage());
			e.printStackTrace();
			return false;
		} finally {
		    httpclient.getConnectionManager().shutdown();
		}
	    return true;
		*/
		return isValidUrl(uploadUrl) && uploadFile(file, uploadUrl);
	}
	
	public static boolean isValidUrl(String url) {
		Log.d(TAG, "Validating url");
		boolean isValidUrl = false;
		if (url != null &&  !url.trim().equals("")) {
			try {
				Uri test = Uri.parse(url);
				isValidUrl = test.getScheme() != null 
				&& test.getScheme().startsWith("http") 
				&& test.getHost() != null 
				&& !test.getHost().trim().equals("");
			} catch (Exception e) {
				Log.d(TAG, "Not valid", e);
			}
		}
		Log.d(TAG, "Valid url? " + isValidUrl);
		return isValidUrl;
	}

    public static boolean uploadFile(File file, String uploadurl) {
        HttpClient httpClient = new DefaultHttpClient() ;

        HttpPost httpPost = new HttpPost(uploadurl);
        MultipartEntity entity = new MultipartEntity();
        entity.addPart("uploadedfile", new FileBody(file));
        httpPost.setEntity(entity );
        HttpResponse response = null;

        try {
            response = httpClient.execute(httpPost);
        } catch (ClientProtocolException e) {
            Log.e("ClientProtocolException : "+e, e.getMessage());
            return false;
        } catch (IOException e) {
            Log.e("IOException : "+e, e.getMessage());
            return false;

        }

        if(response == null) {
            return false;
        }

        if(response.getStatusLine().getStatusCode() == 200) {
            return true;
        }
        return false;
    }



}

