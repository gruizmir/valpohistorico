package com.opensour.ValpoHistorico.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import com.opensour.ValpoHistorico.listeners.OnDataReceivedListener;

public class ServiceConnection extends AsyncTask<String, Integer, String> {
	public static final int FLAG_VOTE = 0;
	public static final int FLAG_BEST = 1;
	public static final int FLAG_HIPSTER = 2;
	private String urlBase = "http://ranking.opensour.com/rank/";
	private Bundle data;
	public String resultData;
	private int flag=0;
	private OnDataReceivedListener onDataReceivedListener;
	private List<NameValuePair> valuePairs = new ArrayList<NameValuePair>();
	
	public ServiceConnection(){
	}
	
	public ServiceConnection(String urlBase){
		this.setUrlBase(urlBase);
	}
	
	/**
	 * Verifica si existe algun tipo de conexion a internet activa, ya sea Wifi, 3G, o similar de datos.
	 * @param cont Contexto actual de la actividad que llama a la funcion.
	 * @return boolean true si existe al menos una conexion activa.
	 */
	public static boolean isConnected(Context cont){
		Context c = cont.getApplicationContext();
		ConnectivityManager connec =  (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connec == null)
			return false;
		NetworkInfo[] redes = connec.getAllNetworkInfo();
		if(redes!=null){
			for(int i=0; i<redes.length; i++){
				if (redes[i].getState()	== NetworkInfo.State.CONNECTED)
					return true;
			}
		}
		return false;
	}
	
	public void setInfo(Bundle info){
		Set<String> set = info.keySet();
		Iterator<String> it = set.iterator();
		while(it.hasNext()){
			String key = it.next();
			valuePairs.add(new BasicNameValuePair(key, info.getString(key,"")));
		}
	}
	
	@Override
	protected String doInBackground(String... arg0) {
		HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost(urlBase);
	    try {
	        httppost.setEntity(new UrlEncodedFormEntity(valuePairs));

	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute(httppost);
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			String sResponse;
			StringBuilder s = new StringBuilder();
			while ((sResponse = reader.readLine()) != null) 
				s = s.append(sResponse);
			resultData = s.toString();
			Bundle b = new Bundle();
			b.putString("api_response", resultData);
//			Log.e("ranking response", resultData);
			if(flag!=FLAG_VOTE){
				b.putInt("flag", flag);
				this.onDataReceivedListener.onReceive(b);
			}
			return resultData;
	    } catch (ClientProtocolException e) {
	    } catch (IOException e) {
	    }
	    return null;
	}

	public Bundle getData() {
		return data;
	}

	public void setData(Bundle data) {
		this.data = data;
	}

	public String getUrlBase() {
		return urlBase;
	}

	public void setUrlBase(String urlBase) {
		this.urlBase = urlBase;
	}
	
	public OnDataReceivedListener getOnDataReceivedListener() {
		return onDataReceivedListener;
	}

	public void setOnDataReceivedListener(OnDataReceivedListener onDataReceivedListener) {
		this.onDataReceivedListener = onDataReceivedListener;
	}
	
	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}
}
