package org.eclipse.core.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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
