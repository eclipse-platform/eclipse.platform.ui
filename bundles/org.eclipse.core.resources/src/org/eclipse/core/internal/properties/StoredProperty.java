package org.eclipse.core.internal.properties;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.QualifiedName;
//
public class StoredProperty {
	protected QualifiedName name = null;
	protected String value = null;
public StoredProperty(QualifiedName name, String value) {
	super();
	this.name = name;
	this.value = value;
}
public QualifiedName getName() {
	return name;
}
public String getStringValue() {
	return value;
}
}
