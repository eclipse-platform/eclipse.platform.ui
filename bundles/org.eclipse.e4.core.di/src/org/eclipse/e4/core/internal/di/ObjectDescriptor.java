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
package org.eclipse.e4.core.internal.di;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.eclipse.e4.core.di.IObjectDescriptor;
import org.eclipse.e4.core.di.annotations.Optional;

/**
 * NOTE: This is a preliminary form; this API will change.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ObjectDescriptor implements IObjectDescriptor {

	final private Type desiredType;
	final private Annotation[] annotations;

	public ObjectDescriptor(Type desiredType, Annotation[] annotations) {
		this.desiredType = desiredType;
		this.annotations = annotations;
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
		return hasQualifier(Optional.class);
	}

	public boolean hasQualifier(Class<? extends Annotation> clazz) {
		if (clazz == null)
			return false;
		if (annotations == null)
			return false;
		for(Annotation annotation : annotations) {
			if (annotation.annotationType().equals(clazz))
				return true;
		}
		return false;
	}

	public Annotation[] getQualifiers() {
		return annotations;
	}

	/**
	 * Returns null if qualifier is not present
	 * 
	 * @param qualifier
	 * @return
	 */
	public Object getQualifier(Class<? extends Annotation> clazz) {
		if (clazz == null)
			return null;
		if (annotations == null)
			return null;
		for(Annotation annotation : annotations) {
			if (annotation.annotationType().equals(clazz))
				return annotation;
		}
		return null;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		if (desiredType != null)
			buffer.append(desiredType);
		if (annotations != null) {
			buffer.append('[');
			boolean first = true;
			for (Annotation annotation : annotations) {
				if (first)
					first = false;
				else
					buffer.append(',');
				buffer.append(annotation.toString());
			}
			buffer.append(']');
		}
		return buffer.toString();
	}

}
