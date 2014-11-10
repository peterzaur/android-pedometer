/*
 *  Pedometer - Android App
 *  Copyright (C) 2009 Levente Bagi
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package name.bagi.levente.pedometer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

/**
 * Detects angles and notifies all listeners (that implement StepListener).
 */
public class AngleDetector implements SensorEventListener
{   
	private final static String TAG = "AngleDetector";
	DefaultHttpClient httpClient = new DefaultHttpClient();
	HttpPost httpPostRequest = new HttpPost();
	
	public AngleDetector() {
    }

    //public void onSensorChanged(int sensor, float[] values) {
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        synchronized (this) {
            if (sensor.getType() == Sensor.TYPE_ORIENTATION) {
            	Log.i(TAG, "True");
            	try{
            		URI test = new URI("http://upkk80345995.pjliu.koding.io:8080/api/steps");
            		JSONObject obj = new JSONObject();
                	obj.put("step", "True");
            		StringEntity se;
            		se = new StringEntity(obj.toString());
                	httpPostRequest.setEntity(se);
                	httpPostRequest.setURI(test);
                	httpPostRequest.setHeader("Content-type", "application/json");
                	
                	HttpResponse response = (HttpResponse) httpClient.execute(httpPostRequest);
                	Log.i(TAG, "HTTPResponse received!!!");
                	
                	HttpEntity entity = response.getEntity();
                	if (entity != null) {
        				// Read the content stream
        				InputStream instream = entity.getContent();
        				Header contentEncoding = response.getFirstHeader("Content-Encoding");
        				if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
        					instream = new GZIPInputStream(instream);
        				}

        				// convert content stream to a String
        				String resultString = convertStreamToString(instream);
        				instream.close();
        				resultString = resultString.substring(1,resultString.length()-1); // remove wrapping "[" and "]"

        				// Transform the String into a JSONObject
        				JSONObject jsonObjRecv = new JSONObject(resultString);
        				// Raw DEBUG output of our received JSON object:
        				Log.i(TAG,"<JSONObject>\n"+jsonObjRecv.toString()+"\n</JSONObject>");
        			} 
            	} catch (Exception e) {
        			e.printStackTrace();
            	}
            	
            	for (int i=0 ; i<3 ; i++) {	
            		float test = event.values[i];
                	Log.i(TAG, Float.toString(test));
            	}
            }
        }
    }
    
    private static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the BufferedReader.readLine()
		 * method. We iterate until the BufferedReader return null which means
		 * there's no more data to read. Each line will appended to a StringBuilder
		 * and returned as String.
		 * 
		 * (c) public domain: http://senior.ceng.metu.edu.tr/2009/praeda/2009/01/11/a-simple-restful-client-at-android/
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}


    	@Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    	// TODO Auto-generated method stub	
    }
        
}