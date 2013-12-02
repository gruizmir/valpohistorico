package com.opensour.ValpoHistorico.parse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.util.JsonReader;

import com.opensour.ValpoHistorico.WikiObject;

public class JSONParser {
	private ArrayList<WikiObject> lista;

	public ArrayList<WikiObject> parse(String data){
		lista = new ArrayList<WikiObject>();
		JsonReader reader;
		try {
			reader = new JsonReader(
					new InputStreamReader(
							new ByteArrayInputStream(data.getBytes()), "UTF-8"));
			reader.beginArray();
			while (reader.hasNext()) {
				WikiObject temp = readObject(reader);
				if(temp!=null)
					lista.add(temp);
			}
			reader.endArray();
			reader.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return lista;
	}

	private WikiObject readObject(JsonReader reader){
		WikiObject obj = null;
		try {
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				if(name.equals("fields")) {
					obj = readArticle(reader);
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return obj;
	}

	private WikiObject readArticle(JsonReader reader){
		WikiObject temp = new WikiObject(); 
		try {
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				if (name.equals("name")) {
					temp.setNombre(reader.nextString());
				} else if (name.equals("total_count")) {
					temp.setTotalCount(reader.nextInt());
				} else if (name.equals("facebook_count")) {
					temp.setFacebookCount(reader.nextInt());
				} else if (name.equals("twitter_count")) {
					temp.setTwitterCount(reader.nextInt());
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return temp;
	}
	
	
	
	public ArrayList<String> parseImageList(String data){
		ArrayList<String> lista = new ArrayList<String>();
		JsonReader reader;
		try {
			reader = new JsonReader(
					new InputStreamReader(
							new ByteArrayInputStream(data.getBytes()), "UTF-8"));
			reader.beginArray();
			while (reader.hasNext()) {
				String temp = readURL(reader);
				if(temp!=null)
					lista.add(temp);
			}
			reader.endArray();
			reader.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return lista;
	}
	
	
	private String readURL(JsonReader reader){
		String img=null;
		try {
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				if(name.equals("fields")) {
					img = readImg(reader);
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return img;
	}
	
	private String readImg(JsonReader reader){
		String imgURI=null;
		try {
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				if (name.equals("img"))
					imgURI = reader.nextString();
				else 
					reader.skipValue();
			}
			reader.endObject();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return imgURI;
	}
}
