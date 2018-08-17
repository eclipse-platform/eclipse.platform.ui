/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
 * Licensed Material - Property of IBM.
 * All rights reserved.
 * US Government Users Restricted Rights - Use, duplication or disclosure
 * restricted by GSA ADP Schedule Contract with IBM Corp.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.navigator;

import org.eclipse.core.resources.IFile;

/**
 * Provides a simple model of a name=value pair from a *.properties file.
 *
 * @since 3.2
 */
public class PropertiesTreeData {

	private IFile container;
	private String name;
	private String value;

	/**
	 * Create a property with the given name and value contained by the given file.
	 *
	 * @param aName The name of the property.
	 * @param aValue The value of the property.
	 * @param aFile The file that defines this property.
	 */
	public PropertiesTreeData(String aName, String aValue, IFile aFile) {
		name = aName;
		value = aValue;
		container = aFile;
	}

	/**
	 * The name of this property.
	 * @return The name of this property.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the value of the property in the file.
	 * @return The value of the property in the file.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * The IFile that defines this property.
	 * @return The IFile that defines this property.
	 */
	public IFile getFile() {
		return container;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof PropertiesTreeData
				&& ((PropertiesTreeData) obj).getName().equals(name);
	}

	@Override
	public String toString() {
		StringBuilder toString =
				new StringBuilder(getName()).append(":").append(getValue()); //$NON-NLS-1$
		return toString.toString();
	}


}
