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
	//Usado para obtener data desde la wiki semantica mediante consultas
	private String urlBase = "http://tpsw.opensour.com/index.php?title=Especial:Ask&q=";
	private String format="csv";
	
	//Usado para cualquier obtencion de datos
	private String url=null;
	public String resultData;
	private OnDataReceivedListener onDataReceivedListener;
	
	//Utilizado para distinguir el tipo de informacion que fue solicitada. Ej: "raw", "render", "smw"
	private String flag;
	
	public WikiConnection(){}
	
	public WikiConnection(String urlBase, String format){
		this.urlBase = urlBase;
		this.format = format;
	}
	
	/**
	 * Funcion usada para settear datos de la consulta a la wiki semantica.
	 * @param queryArgs
	 * @param requiredFields
	 */
	public void setInfo(String[] queryArgs, String[] requiredFields){
		flag = "smw";
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
	
	
	public void setInfo(String[] queryArgs, String[] unionArgs, String[] requiredFields){
		flag = "smw";
		String query="";
		for(int i=0; i<queryArgs.length; i++){
			String temp = queryArgs[i].replace("=", "::");
			temp = new String("[[").concat(temp).concat(new String("]]"));
			query = query.concat(temp);
		}
		
		for(int i=0; i<unionArgs.length; i++){
			String temp = unionArgs[i].replace("=", "::");
			temp = new String("[[").concat(temp).concat(new String("]]"));
			if(i!=unionArgs.length-1)
				temp = temp.concat(" OR ");
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
//		Log.e("url", url);
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
	
	public void setFlag(String flag){
		this.flag = flag;
	}

	@Override
	protected String doInBackground(String... urls) {
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response;
			if(url!=null)
				response = httpclient.execute(new HttpGet(url));
			else
				response = httpclient.execute(new HttpGet(urls[0]));
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			String sResponse;
			StringBuilder s = new StringBuilder();
			while ((sResponse = reader.readLine()) != null) 
				s = s.append(sResponse);
			resultData = s.toString();
			Bundle b = new Bundle();
			b.putString("api_response", resultData);
			b.putString("flag", flag);
			this.onDataReceivedListener.onReceive(b);
			return resultData;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("error", "error conexion", e);
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
