package com.opensour.ValpoHistorico;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.util.Log;

public class WikiObject implements OnDataReceivedListener{
	private String nombre;
	private String texto;
	private String categoria;
	private Map<String, String> atributos;
	private Boolean textoReceived = false;
	private Boolean atributosReceived = false;
	private OnDataReceivedListener onDataReceivedListener;
			
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

	public Map<String, String> getAtributos() {
		return atributos;
	}

	public void setAtributos(Map<String, String> atributos) {
		this.atributos = atributos;
	}
	
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
				
	}
	
	private String cleanText(String text){
		String newText=text;
		if(text.contains("id=\"map_google")){
			newText = text.substring(0, text.indexOf("<div id=\"map_google"));
			newText = newText.concat("</body></html>");
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
	

	public OnDataReceivedListener getOnDataReceivedListener() {
		return onDataReceivedListener;
	}

	public void setOnDataReceivedListener(OnDataReceivedListener onDataReceivedListener) {
		this.onDataReceivedListener = onDataReceivedListener;
	}
	
	@Override
	public void onReceive(Bundle data) {
		String flag = data.getString("flag", "default");
		if(flag.equals("render")){
			this.texto = cleanText(data.getString("api_response"));
			this.textoReceived=true;
			if(atributosReceived==true)
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
			if(textoReceived==true)
				onDataReceivedListener.onReceive(null);
			return;
		}
		else
			return;
	}
}


