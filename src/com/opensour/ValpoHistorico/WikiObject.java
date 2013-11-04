package com.opensour.ValpoHistorico;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

public class WikiObject {
	private String nombre;
	private String texto;
	private String categoria;
	private Map<String, String> atributos;
 	
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
		this.categoria = categoria;
	}

	public Map<String, String> getAtributos() {
		return atributos;
	}

	public void setAtributos(Map<String, String> atributos) {
		this.atributos = atributos;
	}
	
	public void addAtributo(String key, String value){
		if(atributos!=null){
			atributos.put(key, value);
		}
		else{
			atributos = new HashMap<String,String>();
			atributos.put(key, value);
		}
	}
	
	public String searchAtributo(String key){
		try{
			if(key.equals("latitud")){
				String lat = atributos.get("Tiene coordenadas");
				lat = lat.split(",")[0];
				return lat;
			}
			if(key.equals("longitud")){
				String lon = atributos.get("Tiene coordenadas");
				lon = lon.split(",")[1];
				return lon;
			}
			else{
				return atributos.get("key");
			}
		}catch(Exception e){
			Log.e("retrieve error", "maybe parse o split", e);
			return null;
		}
	}
}
