package org.eclipse.core.runtime.model;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

/**
 * An object which represents the executable code for plug-in 
 * in a plug-in manifest.
  * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 */
public class LibraryModel extends PluginModelObject {

	// DTD properties (included in plug-in manifest)
	private String[] exports = null;

	// transient properties (not included in plug-in manifest)
	private boolean isExported = false;
	private boolean isFullyExported = false;
/**
 * Creates a new library model in which all fields
 * are <code>null</code>.
 */
public LibraryModel() {}
/**
 * Returns this library's export mask.
 *
 * @return this library's export mask or <code>null</code>
 */
public String[] getExports() {
	return exports;
}
/**
 * Returns whether or not any of the code in this library is exported.
 *
 * @return whether or not any of the code in this library represents is exported
 */
public boolean isExported() {
	return isExported;
}
/**
 * Returns whether or not all of the code in this library is exported.
 *
 * @return whether or not all of the code in this library is exported
 */
public boolean isFullyExported() {
	return isFullyExported;
}
/**
 * Sets this library's export mask.
 * This object must not be read-only.
 *
 * @param value this library's export mask.  May be <code>null</code>.
 */
public void setExports(String[] value) {

	assertIsWriteable();

	exports = value;
	if (value == null) {
		isExported = false;
		isFullyExported = false;
	} else {
		for (int i = 0; i < value.length; i++) {
			if (!value[i].equals(""))
				isExported = true;
			if (value[i].equals("*"))
				isFullyExported = true;
		}
	}
}
}
