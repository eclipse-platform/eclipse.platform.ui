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
package org.eclipse.e4.core.services.internal.context;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.eclipse.e4.core.services.injector.IObjectDescriptor;

/**
 * NOTE: This is a preliminary form; this API will change.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ObjectDescriptor implements IObjectDescriptor {

	final private Type desiredType;
	final private String[] qualifiers;
	final private String[] values;

	// TBD make "Optional" a qualifier?
	final private boolean optional;

	public ObjectDescriptor(Type desiredType, String[] qualifiers, String[] values, boolean optional) {
		this.desiredType = desiredType;
		this.qualifiers = qualifiers;
		this.values = values;
		this.optional = optional;
	}

	// TBD rename getDesiredClass()
	public Class<?> getElementClass() {
		if (desiredType instanceof Class<?>)
			return (Class<?>) desiredType;
		if (desiredType instanceof ParameterizedType)
			return (Class<?>) ((ParameterizedType) desiredType).getRawType(); // XXX this is wrong;
																				// might be
																				// Param<T<T2>>
		return null;
	}

	public Type getElementType() {
		return desiredType;
	}

	public boolean isOptional() {
		return optional;
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

	public String[] getQualifiers() {
		return qualifiers;
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
		if (desiredType != null)
			buffer.append(((Class<?>) desiredType).getName());
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
