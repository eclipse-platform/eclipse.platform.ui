package org.eclipse.ant.internal.core;

import java.net.URL;

public class Type {

	protected String typeName;
	protected String className;
	protected URL library;

/**
 * Gets the className.
 * @return Returns a String
 */
public String getClassName() {
	return className;
}

/**
 * Sets the className.
 * @param className The className to set
 */
public void setClassName(String className) {
	this.className = className;
}

/**
 * Gets the library.
 * @return Returns a URL
 */
public URL getLibrary() {
	return library;
}

/**
 * Sets the library.
 * @param library The library to set
 */
public void setLibrary(URL library) {
	this.library = library;
}

/**
 * Gets the taskName.
 * @return Returns a String
 */
public String getTypeName() {
	return typeName;
}

/**
 * Sets the taskName.
 * @param taskName The taskName to set
 */
public void setTypeName(String taskName) {
	this.typeName = taskName;
}
}