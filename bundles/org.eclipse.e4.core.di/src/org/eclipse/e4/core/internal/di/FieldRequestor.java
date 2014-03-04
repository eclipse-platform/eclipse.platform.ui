/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
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
import org.eclipse.e4.core.di.IInjector;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.di.suppliers.IObjectDescriptor;
import org.eclipse.e4.core.di.suppliers.PrimaryObjectSupplier;

public class FieldRequestor extends Requestor<Field> {

	public FieldRequestor(Field field, IInjector injector, PrimaryObjectSupplier primarySupplier, PrimaryObjectSupplier tempSupplier, Object requestingObject, boolean track) {
		super(field, injector, primarySupplier, tempSupplier, requestingObject, track);
	}

	public Object execute() throws InjectionException {
		if (actualArgs == null)
			return null; // optional field
		setField(location, actualArgs[0]);
		clearResolvedArgs();
		return null;
	}

	protected IObjectDescriptor[] calcDependentObjects() {
		IObjectDescriptor objectDescriptor = new ObjectDescriptor(location.getGenericType(), location.getAnnotations());
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
		tmp.append(location.getName());
		return tmp.toString();
	}
}
