package org.ryancutter.barcajolt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * Lightweight Java CouchDB library specifically built for interacting with
 * Cloudant databases on Android devices.<p>
 * 
 * http://github.com/ryancutter/BarcaJolt<p>
 * 
 * Copyright (C) 2011 by Ryan Cutter
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

public class BarcaJolt {
	private static final String TAG = "BarcaJolt";

	private String mHost; 				// username.cloudant.com
	private int mPort = 5984; 			// 5984 or 443 most likely
	private String mProtocol = "http"; 	// http or https
	private String mUsername;
	private String mPassword;

	/**
	 * Constructor which assumes http connection. You should most likely use this.
	 * 
	 * @param host Database server host, e.g. 'username.cloudant.com'
	 * @param username Username - strongly recommend generating API key rather than using master acct.
	 * @param password Password - strongly recommend generating API key rather than using master acct
	 */
	public BarcaJolt(String host, String username, String password) {
		mHost = host;
		mUsername = username;
		mPassword = password;
	}

	/**
	 * Constructor which accepts all possible parameters
	 * 
	 * @param host Database server host, e.g. 'username.cloudant.com'
	 * @param port Port, most likely 5984 or 443
	 * @param protocol Protocol, "http" or "https"
	 * @param username Username - strongly recommend generating API key rather than using master acct.
	 * @param password Password - strongly recommend generating API key rather than using master acct
	 */
	public BarcaJolt(String host, int port, String protocol, String username, String password) {
		mHost = host;
		mPort = port;
		mProtocol = protocol;
		mUsername = username;
		mPassword = password;
	}
	
	/**
	 * GET details about account configuration
	 * 
	 * @return JSONObject Results of GET operation showing software version numbers
	 */
	public JSONObject getAcctInfo() {
		return this.get("/");
	}

	/**
	 * Find all databases user has access to. Username must have admin access to see this info.
	 * 
	 * @return JSONArray Results of GET operation showing array of database names. Returns null
	 * for any HTTP status code other than 200.
	 */
	public JSONArray getDBs() {
		JSONObject jObject = this.get("/_all_dbs", true);
		JSONArray jArray = null;
		
		// since Cloudant returns a JSONArray rather than JSONObject for /_all_dbs requests, this 
		// class temporarily wraps the array like {"dbs":["database1", "database2"]}
		if(jObject != null) {
			try {
				jArray = jObject.getJSONArray("dbs");
			} catch (JSONException e) {
				Log.e(TAG, "JSONException converting Object to Array", e);
			}
		} else {
			return null;
		}

		return jArray;
	}

	/**
	 * Execute GET and collect response. This is the function users should call to run views.
	 * 
	 * @param getURL Requested GET operation. Will be appended to host. Will likely look like
	 * "/databasename/_design/myapp/_view/all"
	 * 
	 * @return JSONObject Results of GET operation. Will retrun null for HTTP status codes other
	 * than 200.
	 */
	public JSONObject get(String getURL) {
		return get(getURL, false);
	}

	/**
	 * Execute GET and collect response
	 * 
	 * @param getURL Requested GET operation. Will be appended to HOST.
	 * 
	 * @return JSONObject Results of GET operation. Will return null for HTTP status codes other
	 * than 200.
	 */
	private JSONObject get(String getURL, boolean arrayResult) {
		// set up DefaultHttpClient and other objects
		StringBuilder builder = new StringBuilder();
		DefaultHttpClient client = new DefaultHttpClient();

		HttpHost host = new HttpHost(mHost, mPort, mProtocol);
		HttpGet get = new HttpGet(getURL);

		client.getCredentialsProvider().setCredentials(
				new AuthScope(host.getHostName(), host.getPort()),
				new UsernamePasswordCredentials(mUsername, mPassword));

		JSONObject jObject = null;
		try {
			// execute GET and collect results if valid response
			HttpResponse response = client.execute(host, get);

			StatusLine status = response.getStatusLine();
			int statusCode = status.getStatusCode();
			if (statusCode == 200) {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
				for (String line = null; (line = reader.readLine()) != null;) {
					builder.append(line);
				}

				try {
					// check if this is the result of BarcaJolt.getDBs(). since Cloudant returns a JSONArray 
					// rather than JSONObject for /_all_dbs requests, this class temporarily wraps the array
					// like {"dbs":["database1", "database2"]}. unwrapped in BarcaJolt.getDBs()
					if (arrayResult) {
						builder.insert(0, "{'dbs' : ");
						builder.append("}");
					}

					jObject = new JSONObject(builder.toString());
				} catch (JSONException e) {
					Log.e(TAG, "JSONException converting StringBuilder to JSONObject", e);
				}
			} else {
				// we only want to process 200's
				// TODO process non-200's in a more clever way
				Log.e(TAG, "Bad HttoResponse status: " + new Integer(statusCode).toString());
				return null;
			}
		} catch (ClientProtocolException e) {
			Log.e(TAG, "ClientProtocolException", e);
		} catch (IOException e) {
			Log.e(TAG, "DefaultHttpClient IOException", e);
		}

		// let go of resources
		client.getConnectionManager().shutdown();

		return jObject;
	}

	/**
	 * read "total_rows" from JSONObject returned by view request
	 * 
	 * @param jObject JSONObject returned by view request
	 * 
	 * @return int Value of "total_rows". Returns -1 if not found.
	 */
	public static int getTotalRows(JSONObject jObject) {
		int totalRows = -1;

		try {
			totalRows = jObject.getInt("total_rows");
		} catch (JSONException e) {
			Log.e(TAG, "JSONException while looking for 'total_rows'", e);
		}

		return totalRows;
	}

	/**
	 * read "offset" from JSONObject returned by view request
	 * 
	 * @param jObject JSONObject returned by view request
	 * 
	 * @return int Value of "offset". Returns -1 if not found.
	 */
	public static int getOffset(JSONObject jObject) {
		int offset = -1;

		try {
			offset = jObject.getInt("offset");
		} catch (JSONException e) {
			Log.e(TAG, "JSONException while looking for 'offset'", e);
		}

		return offset;
	}

	/**
	 * read "rows" array from JSONObject returned by view request
	 * 
	 * @param jObject JSONObject returned by view request
	 * 
	 * @return JSONArray Array stored in "rows". Returns null if not found.
	 */
	public static JSONArray getRows(JSONObject jObject) {
		JSONArray rows = null;

		try {
			rows = jObject.getJSONArray("rows");
		} catch (JSONException e) {
			Log.e(TAG, "JSONException while looking for 'rows'", e);
		}

		return rows;
	}

	/**
	 * Extract "value" object from JSONArray
	 * 
	 * @param jArray Array of JSONObjects
	 * @param index Index of array
	 * 
	 * @return JSONObject JSONObject in "value" at JArray[index]. Returns null if not found.
	 */
	public static JSONObject getValueFromArray(JSONArray jArray, int index) {
		JSONObject value = null;

		try {
			value = jArray.getJSONObject(index).getJSONObject("value");
		} catch (JSONException e) {
			Log.e(TAG, "JSONException while looking for value in array", e);
		}

		return value;
	}

	/**
	 * Extract "key" from JSONArray
	 * 
	 * @param jArray Array of JSONObjects
	 * @param index Index of array
	 * 
	 * @return String String in "key" at JArray[index]. Returns null if not found.
	 */
	public static String getKeyFromArray(JSONArray jArray, int index) {
		String key = null;

		try {
			key = jArray.getJSONObject(index).getString("key");
		} catch (JSONException e) {
			Log.e(TAG, "JSONException while looking for key in array", e);
		}

		return key;
	}

	/**
	 * Extract "id" from JSONArray
	 * 
	 * @param jArray Array of JSONObjects
	 * @param index Index of array
	 * 
	 * @return String String in "id" at JArray[index]. Returns null if not found.
	 */
	public static String getIdFromArray(JSONArray jArray, int index) {
		String id = null;

		try {
			id = jArray.getJSONObject(index).getString("id");
		} catch (JSONException e) {
			Log.e(TAG, "JSONException while looking for id in array", e);
		}

		return id;
	}
}
