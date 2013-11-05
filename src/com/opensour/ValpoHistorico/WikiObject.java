package com.opensour.ValpoHistorico;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.os.Bundle;
import android.util.Log;
import android.util.Xml;

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
			String lat = value.split(",")[0];
			String lon = value.split(",")[1];
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

	public void parseRDF(String arg){
		XmlPullParser parser = Xml.newPullParser();
		try {
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			InputStream is = new ByteArrayInputStream(arg.getBytes());
			parser.setInput(is, null);
			parser.nextTag();
            readFeed(parser);
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
	    parser.require(XmlPullParser.START_TAG, null, "rdf");
	    while (parser.next() != XmlPullParser.END_TAG) {
	        if (parser.getEventType() != XmlPullParser.START_TAG) 
	            continue;
	        String name = parser.getName();
	        if (name.contains("property:")) {
	        	String tempName = extractName(name);
	        	String tempValue = extractValue(parser, name);
	        	if(atributos==null)
	        		atributos = new HashMap<String, String>();
	        	atributos.put(tempName, tempValue);
	        } else 
	            skip(parser);
	    }  
	}
	
	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
	    if (parser.getEventType() != XmlPullParser.START_TAG) {
	        throw new IllegalStateException();
	    }
	    int depth = 1;
	    while (depth != 0) {
	        switch (parser.next()) {
	        case XmlPullParser.END_TAG:
	            depth--;
	            break;
	        case XmlPullParser.START_TAG:
	            depth++;
	            break;
	        }
	    }
	 }
	
	private String extractName(String arg){
		String temp = "";
		if(!arg.contains("property:"))
			return null;
		temp = arg.replace("property:", "");
		temp.replace("-", "%");
		try {
			temp = URLDecoder.decode(temp, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		Log.e("name", temp);
		return temp;
	}
	
	private String extractValue(XmlPullParser parser, String name) throws IOException, XmlPullParserException {
		String temp="";
		parser.require(XmlPullParser.START_TAG, null, name);
	    String tag = parser.getName();
	    if (tag.equals(name)) {
            temp = parser.getAttributeValue(null, "rdf:resource");
            if(temp.contains("http://tpsw.opensour.com/index.php/Especial:URIResolver/"))
            	temp = temp.replace("http://tpsw.opensour.com/index.php/Especial:URIResolver/", "");
            temp.replace("-", "%");
    		try {
    			temp = URLDecoder.decode(temp, "UTF-8");
    		} catch (UnsupportedEncodingException e) {
    			e.printStackTrace();
    			return null;
    		}
            parser.nextTag();
	    }
	    parser.require(XmlPullParser.END_TAG, null, "link");
	    return temp;
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
			parseRDF(data.getString("api_response"));
			this.atributosReceived=true;
			if(textoReceived==true)
				onDataReceivedListener.onReceive(null);
			return;
		}
		else
			return;
	}
	
	private String cleanText(String text){
		String newText="";
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
}


