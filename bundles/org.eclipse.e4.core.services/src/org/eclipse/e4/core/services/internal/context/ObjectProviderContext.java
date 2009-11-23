/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.services.internal.context;

import org.eclipse.e4.core.services.context.ContextChangeEvent;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.IRunAndTrack;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.core.services.injector.IObjectDescriptor;
import org.eclipse.e4.core.services.injector.IObjectProvider;
import org.eclipse.e4.core.services.injector.Injector;

public class ObjectProviderContext implements IObjectProvider, IRunAndTrack {

	final static private String ECLIPSE_CONTEXT_NAME = IEclipseContext.class.getName();

	final private IEclipseContext context;
	private Injector injector;

	public ObjectProviderContext(IEclipseContext context) {
		this.context = context;
	}

	public boolean containsKey(IObjectDescriptor properties) {
		String key = getKey(properties);
		if (key == null)
			return false;
		if (ECLIPSE_CONTEXT_NAME.equals(key))
			return (context != null);
		return context.containsKey(key);
	}

	public Object get(IObjectDescriptor properties) {
		String key = getKey(properties);
		if (key == null)
			return null;
		if (ECLIPSE_CONTEXT_NAME.equals(key))
			return context;
		return context.get(key);
	}

	public void setInjector(Injector injector) {
		this.injector = injector;
	}

	public Injector getInjector() {
		return injector;
	}

	public String getKey(IObjectDescriptor key) {
		String result = key.getPropertyName();
		if (result != null)
			return result;
		Class elementClass = key.getElementClass();
		if (elementClass != null)
			return elementClass.getName();
		return null;
	}

	public String toString() {
		return "ContextToInjectorLink(" + context + ')'; //$NON-NLS-1$
	}

	// /////////////////////////////////////////////////////////////////////////////////
	// Context events

	public boolean notify(ContextChangeEvent event) {
		switch (event.getEventType()) {
		case ContextChangeEvent.INITIAL:
			injector.inject(event.getArguments()[0]);
			break;
		case ContextChangeEvent.UNINJECTED:
			injector.uninject(event.getArguments()[0]);
			break;
		case ContextChangeEvent.DISPOSE:
			injector.dispose();
			break;
		case ContextChangeEvent.ADDED: {
			String name = event.getName();
			if (IContextConstants.PARENT.equals(name))
				handleParentChange(event);
			else
				injector.added(new InjectionProperties(true, name, false, null));
			break;
		}
		case ContextChangeEvent.REMOVED: {
			String name = event.getName();
			if (IContextConstants.PARENT.equals(name))
				handleParentChange(event);
			else
				injector.removed(new InjectionProperties(true, name, false, null));
			break;
		}
		}
		return true; // only dispose of the injector when the context has been disposed
	}

	private void handleParentChange(final ContextChangeEvent event) {
		IEclipseContext eventContext = (IEclipseContext) event.getContext();
		IEclipseContext oldParent = (IEclipseContext) event.getOldValue();
		IEclipseContext newParent = (IEclipseContext) eventContext.get(IContextConstants.PARENT);
		if (oldParent == newParent)
			return;
		injector.reparent(getObjectProvider(oldParent));
	}

	static public ObjectProviderContext getObjectProvider(IEclipseContext context) {
		String key = ObjectProviderContext.class.getName();
		if (context.containsKey(key, true))
			return (ObjectProviderContext) context.get(key);
		ObjectProviderContext objectProvider = new ObjectProviderContext(context);
		Injector injector = new Injector(objectProvider);
		objectProvider.setInjector(injector);
		context.set(key, objectProvider);
		return objectProvider;
	}

}
