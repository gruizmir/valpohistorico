package com.opensour.ValpoHistorico;

import android.os.Bundle;

public class SearchObject {
	private String attribute;
	private String value;
	private boolean isPosition=false;
	
	public String getAttribute() {
		return attribute;
	}
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public boolean isPosition() {
		return isPosition;
	}
	public void setPosition(boolean isPosition) {
		this.isPosition = isPosition;
	}
	
	public void search(Bundle extras){
		
	}
}
