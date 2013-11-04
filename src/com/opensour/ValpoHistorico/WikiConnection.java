package com.opensour.ValpoHistorico;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class WikiConnection extends AsyncTask<String, Integer, String> {
	private String urlBase = "http://tpsw.opensour.com/index.php?title=Especial:Ask&q=";
	private String format="csv";
	private String url;
	public WikiConnection(){}
	public String resultData;
	private OnDataReceivedListener onDataReceivedListener;
	
	public WikiConnection(String urlBase, String format){
		this.urlBase = urlBase;
		this.format = format;
	}

	public void setInfo(String[] queryArgs, String[] requiredFields){
		String query="";
		for(int i=0; i<queryArgs.length; i++){
			String temp = queryArgs[i].replace("=", "::");
			temp = new String("[[").concat(temp).concat(new String("]]"));
			query = query.concat(temp);
		}
		try {
			query = URLEncoder.encode(query, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		
		String fields = new String("");
		for(int j=0; j<requiredFields.length; j++){
			fields = fields.concat("?").concat(requiredFields[j]).concat("\n");
		}
		try {
			fields = URLEncoder.encode(fields, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		url = urlBase
					.concat(query)
					.concat("&po=")
					.concat(fields)
					.concat("&eq=yes")
					.concat("&p%5Bformat%5D=")
					.concat(format)
					.concat("&p%5Bsep%5D=%3B")
					.concat("&eq=yes");
	}
	
	public String getUrlBase() {
		return urlBase;
	}

	public void setUrlBase(String urlBase) {
		this.urlBase = urlBase;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	@Override
	protected String doInBackground(String... urls) {
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = httpclient.execute(new HttpGet(url));
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			String sResponse;
			StringBuilder s = new StringBuilder();
			while ((sResponse = reader.readLine()) != null) 
				s = s.append(sResponse);
			resultData = s.toString();
			Bundle b = new Bundle();
			b.putString("api_response", resultData);
			this.onDataReceivedListener.onReceive(b);
			return resultData;
		} catch (Exception e) {
			Log.e("error", "mensaje", e);
		}
		return null;
	}
	
	protected void onPostExecute(Long result) {
    }

	public OnDataReceivedListener getOnDataReceivedListener() {
		return onDataReceivedListener;
	}

	public void setOnDataReceivedListener(OnDataReceivedListener onDataReceivedListener) {
		this.onDataReceivedListener = onDataReceivedListener;
	}
}
