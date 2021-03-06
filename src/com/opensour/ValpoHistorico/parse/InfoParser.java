package com.opensour.ValpoHistorico.parse;

import java.util.ArrayList;

import android.util.Log;

import com.opensour.ValpoHistorico.WikiObject;

public class InfoParser {
	
	public ArrayList<WikiObject> parseData(String data){
		ArrayList<WikiObject> lista = new ArrayList<WikiObject>();
		String[] entradas = data.split("\"\"");
		String[] headers = entradas[0].replace("\"", "").split(";");
		for(int i=1; i<entradas.length; i++){
			String[] temp = entradas[i].replace("\"", "").split(";");
			WikiObject wikiTemp = new WikiObject();
			wikiTemp.setNombre(temp[0]);
			for(int j=1;j<headers.length; j++){
				try{
					if(headers[j].equals("Categoría"))
						wikiTemp.setCategoria(temp[j]);
					else
						if(!temp[j].equals(""))
							wikiTemp.addAtributo(headers[j], temp[j]);
				}catch(IndexOutOfBoundsException ioe){
					Log.e("ErrorClass", "IndexOutOfBounds", ioe);
					continue;
				}catch(NullPointerException npe){
					Log.e("ErrorClass", "NullPointerException", npe);
					continue;
				}
			}
			lista.add(wikiTemp);
		}
		return lista;
	}
}
