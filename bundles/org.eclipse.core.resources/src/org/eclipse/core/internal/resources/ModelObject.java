package org.eclipse.core.internal.resources;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
public abstract class ModelObject implements Cloneable {
	protected String name;
public ModelObject() {
}
public ModelObject(String name) {
	setName(name);
}
public Object clone() {
	try {
		return super.clone();
	} catch (CloneNotSupportedException e) {
		return null; // won't happen
	}
}
public String getName() {
	return name;
}
public void setName(String value) {
	name = value;
}
}
