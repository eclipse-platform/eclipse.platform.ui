package org.eclipse.core.internal.properties;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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
