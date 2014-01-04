package com.opensour.ValpoHistorico.parse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
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

import com.opensour.ValpoHistorico.ObjectLink;
import com.opensour.ValpoHistorico.WikiObject;

public class XMLParser {
	private ArrayList<ObjectLink> inverseObjects;
	private Map<String, String> atributos;
	
	public XMLParser(){
		inverseObjects = new ArrayList<ObjectLink>();
		atributos = new HashMap<String, String>();
	}
	
	public Map<String,String> parseRDF(String arg){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputStream is = new ByteArrayInputStream(arg.getBytes());
			Document dom = builder.parse(is);
			Element root = dom.getDocumentElement();
			NodeList items = root.getElementsByTagName("swivt:Subject");
			
			//Este es el objeto en si
			Node item = items.item(0);
			NodeList properties = item.getChildNodes();
			for (int j=0;j<properties.getLength();j++){
				Node property = properties.item(j);
				String entry  = property.getNodeName();
				if(entry.startsWith("property:") && !entry.startsWith("property:Fecha_de_modif")){
					String key = this.extractName(entry);
					NamedNodeMap map = property.getAttributes();
					String value="";
					try{
						value = this.extractValue(map.getNamedItem("rdf:resource").getTextContent());
					}catch(NullPointerException e){
						continue;
					}
					atributos.put(key, value);
				}
			}
			
			
			int cant = items.getLength();
			//Estos son los que apuntan al articulo.
			for(int i=1;i<cant;i++){
				WikiObject tempObject = new WikiObject();
				ObjectLink tempLink = new ObjectLink();
				
				Node temp = items.item(i);
				NodeList tempProps = temp.getChildNodes();
				for (int j=0;j<tempProps.getLength();j++){
					Node tempProperty = tempProps.item(j);
					String subject  = tempProperty.getNodeName();
					
					if(subject.startsWith("property:") && !subject.startsWith("property:Fecha_de_modif")){
						String key = this.extractName(subject);
						NamedNodeMap map = tempProperty.getAttributes();
						String value="";
						try{
							value = this.extractValue(map.getNamedItem("rdf:resource").getTextContent());
						}catch(NullPointerException e){
							continue;
						}
						tempObject.addAtributo(key, value);
						tempLink.setProperty(key);
					}
					if(subject.equals("rdfs:label")){
						tempObject.setNombre(tempProperty.getTextContent());
					}
				}
				tempLink.setLinkedObject(tempObject);
				inverseObjects.add(tempLink);
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

	public ArrayList<ObjectLink> getInverseObjects() {
		return inverseObjects;
	}
}
