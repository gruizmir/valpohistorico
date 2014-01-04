package com.opensour.ValpoHistorico;

public class ObjectLink {
	private WikiObject linkedObject;
	private String property;
	
	public WikiObject getLinkedObject() {
		return linkedObject;
	}
	public void setLinkedObject(WikiObject linkedObject) {
		this.linkedObject = linkedObject;
	}
	public String getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = property;
	}
}
