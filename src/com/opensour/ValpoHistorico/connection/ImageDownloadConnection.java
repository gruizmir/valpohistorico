package com.opensour.ValpoHistorico.connection;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.opensour.ValpoHistorico.listeners.OnDataReceivedListener;

public class ImageDownloadConnection extends AsyncTask<String, Integer, Bitmap> {
	private String urlBase = "http://ranking.opensour.com/media/";
	private OnDataReceivedListener onDataReceivedListener;
	private String flag=null;

	@Override
	protected Bitmap doInBackground(String... urls) {
		String urldisplay = urlBase.concat(urls[0]);
		Log.e("img", urls[0]);
		Bitmap img = null;
		try {
			InputStream in = new java.net.URL(urldisplay).openStream();
			img = BitmapFactory.decodeStream(in);
			Bundle b = new Bundle();
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			img.compress(Bitmap.CompressFormat.PNG, 100, stream);
			byte[] byteArray = stream.toByteArray();
			b.putByteArray("byte_array", byteArray);
			b.putString("flag", flag);
			this.onDataReceivedListener.onReceive(b);
			return img;
		} catch (Exception e) {
			Log.e("Error", e.getMessage(), e);
		}
		return img;
	}

	public OnDataReceivedListener getOnDataReceivedListener() {
		return onDataReceivedListener;
	}

	public void setOnDataReceivedListener(OnDataReceivedListener onDataReceivedListener) {
		this.onDataReceivedListener = onDataReceivedListener;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

}
