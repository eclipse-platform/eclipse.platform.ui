/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.services.injector;

import javax.inject.Named;

/**
 * NOTE: This is a preliminary form; this API will change.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ObjectDescriptor {

	final private Class desiredClass;
	final private String[] qualifiers;
	final private String[] values;

	static final private String named = Named.class.getName();

	static public ObjectDescriptor make(Class clazz) {
		return new ObjectDescriptor(clazz, null, null);
	}

	static public ObjectDescriptor make(String name) {
		return new ObjectDescriptor(null, new String[] { named }, new String[] { name });
	}

	static public ObjectDescriptor make(Class clazz, String name) {
		if (name == null)
			return make(clazz);
		return new ObjectDescriptor(clazz, new String[] { named }, new String[] { name });
	}

	public ObjectDescriptor(Class desiredClass, String[] qualifiers, String[] values) {
		this.desiredClass = desiredClass;
		this.qualifiers = qualifiers;
		this.values = values;
	}

	public Class getElementClass() {
		return desiredClass;
	}

	public boolean hasQualifier(String qualifier) {
		if (qualifier == null)
			return false;
		if (qualifiers == null)
			return false;
		for (int i = 0; i < qualifiers.length; i++) {
			if (qualifier.equals(qualifiers[i]))
				return true;
		}
		return false;
	}

	/**
	 * Returns null if qualifier is not present
	 * 
	 * @param qualifier
	 * @return
	 */
	public String getQualifierValue(String qualifier) {
		if (qualifier == null)
			return null;
		if (qualifiers == null)
			return null;
		for (int i = 0; i < qualifiers.length; i++) {
			if (qualifier.equals(qualifiers[i]))
				return values[i];
		}
		return null;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		if (desiredClass != null)
			buffer.append(desiredClass.getName());
		else
			buffer.append("_descriptor_");
		if (qualifiers != null) {
			buffer.append("{");
			for (int i = 0; i < qualifiers.length; i++) {
				if (i != 0)
					buffer.append(", ");
				buffer.append(qualifiers[i]);
				buffer.append("=\"");
				buffer.append(values[i]);
				buffer.append("\"");
			}
			buffer.append("}");
		}
		return buffer.toString();
	}

}
