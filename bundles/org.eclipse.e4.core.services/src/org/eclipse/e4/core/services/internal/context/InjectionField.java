/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.services.internal.context;

import java.lang.reflect.Field;
import org.eclipse.e4.core.services.context.ContextChangeEvent;
import org.eclipse.e4.core.services.injector.IObjectProvider;
import org.eclipse.e4.core.services.injector.ObjectDescriptor;

/**
 * Collection of static methods that deal with reflection-based injection at a low level.
 */
public class InjectionField extends InjectionAbstract {

	private final Field field;

	public InjectionField(Object userObject, IObjectProvider primarySupplier, Field field,
			boolean batchProcess) {
		super(userObject, primarySupplier, batchProcess);
		this.field = field;
		InjectionProperties fieldProps = annotationSupport.getInjectProperties(field,
				primarySupplier);
		optional = fieldProps.isOptional();
	}

	public boolean notify(ContextChangeEvent event) {
		Object userObject = getObject();
		if (userObject == null)
			return false;
		// set variables based on the event type
		int eventType = event.getEventType();
		IObjectProvider changed = event.getContext();

		boolean ignoreMissing = ignoreMissing(eventType, changed);
		boolean injectWithNulls = injectNulls(eventType, changed);

		InjectionProperties properties = annotationSupport.getInjectProperties(field,
				primarySupplier);
		ObjectDescriptor objectDescriptor = ObjectDescriptor.make(field.getType(), properties
				.getPropertyName());
		Object value;
		try {
			value = getValue(objectDescriptor, properties, field.getType(), ignoreMissing,
					injectWithNulls);
		} catch (IllegalArgumentException e) {
			String msg = "Could not set " + field.getName();
			logError(msg, e);
			return true; // keep trying?
		}
		setField(value);
		if (eventType == ContextChangeEvent.DISPOSE && primarySupplier.equals(changed))
			return false;
		return true;
	}

	private boolean setField(Object value) {
		Object userObject = getObject();
		boolean wasAccessible = true;
		if (!field.isAccessible()) {
			field.setAccessible(true);
			wasAccessible = false;
		}
		try {
			field.set(userObject, value);
		} catch (IllegalArgumentException e) {
			logError(field, e);
			return false;
		} catch (IllegalAccessException e) {
			logError(field, e);
			return false;
		} finally {
			if (!wasAccessible)
				field.setAccessible(false);
		}
		return true;
	}

}
