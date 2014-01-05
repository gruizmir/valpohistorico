package com.opensour.ValpoHistorico;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import com.opensour.ValpoHistorico.connection.ImageDownloadConnection;
import com.opensour.ValpoHistorico.connection.WikiConnection;
import com.opensour.ValpoHistorico.listeners.OnDataReceivedListener;
import com.opensour.ValpoHistorico.parse.JSONParser;
import com.opensour.ValpoHistorico.parse.XMLParser;

public class WikiObject implements OnDataReceivedListener{
	private String nombre;
	private String texto;
	private String categoria;
	private Bitmap img;
	private ArrayList<String> imgList; //No se debe acceder desde fuera del objeto
	private int facebookCount = 0;
	private int twitterCount = 0;
	private int totalCount = 0;
	private Map<String, String> atributos;
	private Boolean textoReceived = false;
	private Boolean atributosReceived = false;
	private Boolean imgReceived = false;
	private OnDataReceivedListener onDataReceivedListener;
	private ArrayList<ObjectLink> linkedObjects;

	public void addAtributo(String key, String value){
		if(atributos==null)
			atributos = new HashMap<String,String>();
		if(key.equals("Tiene coordenadas")){
			String lat = value.replace("\"", "").split(",")[0];
			String lon = value.replace("\"", "").replace(" ","").split(",")[1];
			atributos.put("latitud", lat);
			atributos.put("longitud", lon);
		}
		atributos.put(key, value);
	}

	public String searchAtributo(String key){
		try{
			return atributos.get(key);
		}catch(Exception e){
			Log.e("retrieve error", "maybe parse o split", e);
			return null;
		}
	}

	public void retrieveData(){
		String urlRender = "http://tpsw.opensour.com/index.php/".concat(nombre.replace(" ", "_")).concat("?action=render");
		String urlRDF = "http://tpsw.opensour.com/index.php/Especial:ExportRDF/".concat(nombre.replace(" ", "_"));
		WikiConnection connRender = new WikiConnection();
		connRender.setFlag("render");
		connRender.setOnDataReceivedListener(this);
		connRender.execute(urlRender);

		WikiConnection connRdf = new WikiConnection();
		connRdf.setFlag("rdf");
		connRdf.setOnDataReceivedListener(this);
		connRdf.execute(urlRDF);

		WikiConnection connImgList = new WikiConnection();
		connImgList.setFlag("img_list");
		String elemUrl = "http://ranking.opensour.com/getimages?elem=";
		try {
			elemUrl = elemUrl.concat(URLEncoder.encode(nombre, "UTF-8"));
			connImgList.setUrlBase(elemUrl);
			connImgList.setOnDataReceivedListener(this);
			connImgList.execute(elemUrl);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	private String cleanText(String text){
		String newText=text;
		//Eliminar las imagenes del texto para mostrarlas aparte.
		if(text.contains("class=\"thumb")){
			newText = text.substring(0, text.indexOf("<div class=\"thumb "));
			newText = newText.concat(text.substring(text.indexOf("<p>", 10),text.length()));
			text = newText;
		}
		if(text.contains("id=\"map_google")){
			newText = text.substring(0, text.indexOf("<div id=\"map_google"));
		}

		while(newText.contains("<span class=\"editsection\">")){
			String temp1 ="";
			String temp2 ="";
			int index = newText.indexOf("<span class=\"editsection\">");
			temp1 = newText.substring(0, index);
			temp2 = newText.substring(newText.indexOf("<span class=\"mw-headline\"", index));
			newText = temp1.concat(temp2);
		}
		return newText;
	}

	@Override
	public void onReceive(Bundle data) {
		String flag = data.getString("flag", "default");
		if(flag.equals("render")){
			this.texto = cleanText(data.getString("api_response"));
			this.textoReceived=true;
			if(atributosReceived==true && imgReceived==true)
				onDataReceivedListener.onReceive(null);
			return;
		}
		if(flag.equals("rdf")){
			XMLParser parser = new XMLParser();
			if(atributos==null || atributos.isEmpty())
				atributos = parser.parseRDF(data.getString("api_response"));
			else
				atributos.putAll(parser.parseRDF(data.getString("api_response")));
			this.atributosReceived=true;
			if(textoReceived==true && imgReceived==true)
				onDataReceivedListener.onReceive(null);
			setLinkedObjects(parser.getInverseObjects());
			return;
		}
		if(flag.equals("img_list")){
			JSONParser jParser = new JSONParser();
			Log.e("img_list", data.getString("api_response"));
			this.imgList = jParser.parseImageList(data.getString("api_response"));
			if(!this.imgList.isEmpty()){
				ImageDownloadConnection imgDownloader = new ImageDownloadConnection();
				imgDownloader.setOnDataReceivedListener(this);
				imgDownloader.setFlag("img_download");
				String newName = imgList.get(0).replace("img/", "img/thumb_"); 
				imgDownloader.execute(newName);
			}
			else{
				img = null;
				if(textoReceived==true && atributosReceived==true)
					onDataReceivedListener.onReceive(null);
			}
		}
		if(flag.equals("img_download")){
			byte[] byteArray = data.getByteArray("byte_array");
			img = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
			imgReceived = true;
			if(textoReceived==true && atributosReceived==true)
				onDataReceivedListener.onReceive(null);
		}
		else
			return;
	}


	/*
	 * Setters y getters
	 */

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria.replace("Categoría:", "");
	}

	public int getFacebookCount() {
		return facebookCount;
	}

	public void setFacebookCount(int facebookCount) {
		this.facebookCount = facebookCount;
	}

	public int getTwitterCount() {
		return twitterCount;
	}

	public void setTwitterCount(int twitterCount) {
		this.twitterCount = twitterCount;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public Bitmap getImg() {
		return img;
	}

	public void setImg(Bitmap img) {
		this.img = img;
	}

	public Map<String, String> getAtributos() {
		return atributos;
	}

	public void setAtributos(Map<String, String> atributos) {
		this.atributos = atributos;
	}

	public OnDataReceivedListener getOnDataReceivedListener() {
		return onDataReceivedListener;
	}

	public void setOnDataReceivedListener(OnDataReceivedListener onDataReceivedListener) {
		this.onDataReceivedListener = onDataReceivedListener;
	}

	public ArrayList<ObjectLink> getLinkedObjects() {
		return linkedObjects;
	}

	public void setLinkedObjects(ArrayList<ObjectLink> linkedObjects) {
		this.linkedObjects = linkedObjects;
	}
}