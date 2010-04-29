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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Qualifier;
import org.eclipse.e4.core.di.suppliers.IObjectDescriptor;

public class ObjectDescriptor implements IObjectDescriptor {

	final private Type desiredType;
	final private Annotation[] annotations;

	public ObjectDescriptor(Type desiredType, Annotation[] allAnnotations) {
		this.desiredType = desiredType;
		this.annotations = (allAnnotations == null) ? null : qualifiers(allAnnotations);
	}

	public Type getDesiredType() {
		return desiredType;
	}

	public boolean hasQualifier(Class<? extends Annotation> clazz) {
		if (clazz == null)
			return false;
		if (annotations == null)
			return false;
		for (Annotation annotation : annotations) {
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
	 */
	public <T extends Annotation> T getQualifier(Class<T> clazz) {
		if (clazz == null)
			return null;
		if (annotations == null)
			return null;
		for (Annotation annotation : annotations) {
			if (annotation.annotationType().equals(clazz))
				return clazz.cast(annotation);
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		if (desiredType instanceof Class<?>)
			buffer.append(((Class<?>) desiredType).getSimpleName());
		else if (desiredType != null)
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

	private Annotation[] qualifiers(Annotation[] allAnnotations) {
		if (allAnnotations.length == 0)
			return null;
		Annotation[] result;
		List<Annotation> qualifiers = new ArrayList<Annotation>();
		for (Annotation annotation : allAnnotations) {
			if (annotation.annotationType().isAnnotationPresent(Qualifier.class))
				qualifiers.add(annotation);
		}
		if (qualifiers.isEmpty())
			return null;
		result = new Annotation[qualifiers.size()];
		qualifiers.toArray(result);
		return result;
	}

}
