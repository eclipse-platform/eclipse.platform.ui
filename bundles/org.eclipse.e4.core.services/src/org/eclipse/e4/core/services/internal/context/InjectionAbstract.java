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

import java.lang.ref.WeakReference;
import org.eclipse.e4.core.services.context.ContextChangeEvent;
import org.eclipse.e4.core.services.injector.IObjectProvider;
import org.eclipse.e4.core.services.internal.annotations.AnnotationsSupport;

abstract public class InjectionAbstract implements IRunAndTrackObject {

	final static protected Object NOT_A_VALUE = new Object();

	final protected WeakReference userObjectRef;
	final protected AnnotationsSupport annotationSupport;
	final protected IObjectProvider primarySupplier;

	protected boolean optional = false;

	abstract public boolean notify(ContextChangeEvent event);

	public InjectionAbstract(Object userObject, IObjectProvider primarySupplier) {
		userObjectRef = new WeakReference(userObject);
		// plug-in class that gets replaced in Java 1.5+
		annotationSupport = new AnnotationsSupport(primarySupplier);
		this.primarySupplier = primarySupplier;
	}

	public Object getObject() {
		if (userObjectRef == null)
			return null;
		return userObjectRef.get();
	}

	protected boolean injectNulls(int eventType, IObjectProvider changed) {
		if ((eventType != ContextChangeEvent.UNINJECTED)
				&& (eventType != ContextChangeEvent.DISPOSE))
			return false;
		return (changed.equals(primarySupplier));
	}

	protected boolean ignoreMissing(int eventType, IObjectProvider changed) {
		if (eventType == ContextChangeEvent.REMOVED)
			return true;
		if ((eventType != ContextChangeEvent.UNINJECTED)
				&& (eventType != ContextChangeEvent.DISPOSE))
			return false;
		return (changed.equals(primarySupplier));
	}

	protected Object getValue(InjectionProperties properties, Class parameterType,
			boolean ignoreMissing, boolean injectWithNulls) {
		// 1) if we have a provider, use it
		Object provider = properties.getProvider();
		if (provider != null)
			return provider;

		// 2) if we have the key in the context
		if (primarySupplier.containsKey(properties)) {
			if (injectWithNulls)
				return null;
			Object value = primarySupplier.get(properties);
			if (value == null)
				return value;
			if (parameterType.isAssignableFrom(value.getClass()))
				return value;
		}

		// 3) can we ignore this argument?
		if (ignoreMissing || properties.isOptional())
			return null;

		if (!optional) {
			String msg = "Unable to find value for \"" + primarySupplier.getKey(properties) + "\"";
			throw new IllegalArgumentException(msg);
		}
		return NOT_A_VALUE;
	}

	protected void logError(Object destination, Exception e) {
		String msg = "Injection failed " + destination.toString();
		logError(msg, e);
	}

	protected void logError(String msg, Exception e) {
		System.out.println(msg); //$NON-NLS-1$
		if (e != null)
			e.printStackTrace();
		// TBD convert this into real logging
		// String msg = NLS.bind("Injection failed", destination.toString());
		// RuntimeLog.log(new Status(IStatus.WARNING,
		// IRuntimeConstants.PI_COMMON, 0, msg, e));
	}

}
