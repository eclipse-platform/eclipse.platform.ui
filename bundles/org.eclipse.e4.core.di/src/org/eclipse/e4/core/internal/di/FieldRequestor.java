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

import java.lang.reflect.Field;
import org.eclipse.e4.core.di.*;

public class FieldRequestor extends Requestor {

	final private Field field;

	public FieldRequestor(Field field, IInjector injector, AbstractObjectSupplier primarySupplier, Object requestingObject, boolean track, boolean groupUpdates, boolean optional) {
		super(injector, primarySupplier, requestingObject, track, groupUpdates, optional);
		this.field = field;
	}

	public Object execute() throws InjectionException {
		setField(field, actualArgs[0]);
		return null;
	}

	@Override
	public IObjectDescriptor[] getDependentObjects() {
		InjectionProperties properties = annotationSupport.getInjectProperties(field);
		IObjectDescriptor objectDescriptor = ObjectDescriptorFactory.make(field.getGenericType(), properties.getQualifiers());
		return new IObjectDescriptor[] {objectDescriptor};
	}

	private boolean setField(Field field, Object value) throws InjectionException {
		Object userObject = getRequestingObject();
		if (userObject == null)
			return false;
		boolean wasAccessible = true;
		if (!field.isAccessible()) {
			field.setAccessible(true);
			wasAccessible = false;
		}
		try {
			field.set(userObject, value);
		} catch (IllegalArgumentException e) {
			throw new InjectionException(e);
		} catch (IllegalAccessException e) {
			throw new InjectionException(e);
		} finally {
			if (!wasAccessible)
				field.setAccessible(false);
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuffer tmp = new StringBuffer();
		Object object = getRequestingObject();
		if (object != null)
			tmp.append(object.getClass().getSimpleName());
		tmp.append('.');
		tmp.append(field.getName());
		return tmp.toString();
	}

}
