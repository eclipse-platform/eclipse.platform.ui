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

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.e4.core.di.AbstractObjectSupplier;
import org.eclipse.e4.core.di.IInjector;
import org.eclipse.e4.core.di.IObjectDescriptor;
import org.eclipse.e4.core.di.IRequestor;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
abstract public class Requestor implements IRequestor {

	final private WeakReference<Object> objectRef;
	final private boolean track;
	final private boolean groupUpdates;
	final private boolean isOptional;
	
	final private IInjector injector;
	final private AbstractObjectSupplier primarySupplier;

	protected Object[] actualArgs;

	// plug-in class that gets replaced in Java 1.5+
	// TBD this will be merged-in; replace with an utility class
	final protected AnnotationsSupport annotationSupport = new AnnotationsSupport();

	public abstract IObjectDescriptor[] getDependentObjects();

	public abstract Object execute() throws InvocationTargetException, InstantiationException;

	public Requestor(IInjector injector, AbstractObjectSupplier primarySupplier, Object requestingObject, boolean track, boolean groupUpdates,
			boolean isOptional) {
		this.injector = injector;
		this.primarySupplier = primarySupplier;
		if (requestingObject != null)
			objectRef = new WeakReference<Object>(requestingObject);
		else
			objectRef = null;
		this.track = track;
		this.groupUpdates = groupUpdates;
		this.isOptional = isOptional;
	}
	
	public IInjector getInjector() {
		return injector;
	}
	
	public AbstractObjectSupplier getPrimarySupplier() {
		return primarySupplier;
	}

	public Object getRequestingObject() {
		if (objectRef == null)
			return null;
		return objectRef.get();
	}

	/**
	 * Determines if the requestor wants to be called whenever one of the dependent object changes.
	 * 
	 * @return
	 */
	public boolean shouldTrack() {
		return track;
	}

	public boolean shouldGroupUpdates() {
		return groupUpdates;
	}

	public boolean isOptional() {
		return isOptional;
	}

	/**
	 * If actual arguments are resolved for this requestor
	 */
	public boolean isResolved() {
		return (actualArgs != null);
	}

	public void setResolvedArgs(Object[] actualArgs) {
		this.actualArgs = actualArgs;
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
