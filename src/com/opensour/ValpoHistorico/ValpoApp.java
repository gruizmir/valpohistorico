package com.opensour.ValpoHistorico;

import java.util.ArrayList;

import android.app.Application;

public class ValpoApp extends Application {
	private ArrayList<WikiObject> lista;

	public ArrayList<WikiObject> getLista() {
		return lista;
	}

	public void setLista(ArrayList<WikiObject> lista) {
		this.lista = lista;
	}
}
