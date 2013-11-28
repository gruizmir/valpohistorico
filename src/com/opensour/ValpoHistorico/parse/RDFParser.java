package com.opensour.ValpoHistorico.parse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

public class RDFParser {
	private String namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private Map<String, String> atributos;
	
	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	public Map<String,String> parseRDF(String arg){
		atributos = new HashMap<String,String>();
		XmlPullParser parser = Xml.newPullParser();
		Log.e("parser", "parseRdf");
		try {
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			InputStream is = new ByteArrayInputStream(arg.getBytes());
			parser.setInput(is, null);
			parser.nextTag();
            readFeed(parser);
            return atributos;
		} catch (XmlPullParserException e) {
			Log.e("Parser", "XmlPullParserException", e);
			return new HashMap<String,String>();
		} catch (IOException e) {
			Log.e("Parser", "IOException", e);
			return new HashMap<String,String>();
		}
	}
	
	private void readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, namespace, null);
	    Log.e("namespace", parser.getAttributeNamespace(0));
	    Log.e("name", parser.getAttributeName(0));
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
}
