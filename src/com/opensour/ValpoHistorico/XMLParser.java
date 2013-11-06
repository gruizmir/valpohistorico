package com.opensour.ValpoHistorico;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

public class XMLParser {
	private Map<String, String> atributos;

	public Map<String,String> parseRDF(String arg){
		atributos = new HashMap<String,String>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputStream is = new ByteArrayInputStream(arg.getBytes());
			Document dom = builder.parse(is);
			Element root = dom.getDocumentElement();
			NodeList items = root.getElementsByTagName("swivt:Subject");
			Node item = items.item(0);
			NodeList properties = item.getChildNodes();
			for (int j=0;j<properties.getLength();j++){
				Node property = properties.item(j);
				String entry  = property.getNodeName();
				if(entry.startsWith("property:") && !entry.startsWith("property:Fecha_de_modif")){
					String key = this.extractName(entry);
					NamedNodeMap map = property.getAttributes();
					String value = this.extractValue(map.getNamedItem("rdf:resource").getTextContent());
					atributos.put(key, value);
				}
			}
		}catch (Exception e) {
			Log.e("Parser", "unknown exception", e);
			return new HashMap<String,String>();
		} 

		return atributos;
	}

	private String extractName(String arg){
		String temp = "";
		if(!arg.contains("property:"))
			return null;
		temp = arg.replace("property:", "");
		temp = temp.replace("-", "%");
		try {
			temp = URLDecoder.decode(temp, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Log.e("parser", "EncodingException",e);
			return null;
		}
		temp = temp.replace("_", " ");
		return temp;
	}

	private String extractValue(String data){
		if(data.contains("http://tpsw.opensour.com/index.php/Especial:URIResolver/"))
			data = data.replace("http://tpsw.opensour.com/index.php/Especial:URIResolver/", "");
		else if(data.contains("&wiki;"))
			data = data.replace("&wiki;", "");
		data =data.replace("-", "%");
		try {
			data = URLDecoder.decode(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Log.e("parser", "EncodingException",e);
			return null;
		}
		data = data.replace("_", " ");
		return data;
	}
}
