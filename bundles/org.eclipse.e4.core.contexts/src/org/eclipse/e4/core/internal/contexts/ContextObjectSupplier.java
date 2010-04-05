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
package org.eclipse.e4.core.internal.contexts;

import javax.inject.Named;

import org.eclipse.e4.core.contexts.ContextChangeEvent;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.IRunAndTrack;
import org.eclipse.e4.core.di.AbstractObjectSupplier;
import org.eclipse.e4.core.di.IInjector;
import org.eclipse.e4.core.di.IObjectDescriptor;
import org.eclipse.e4.core.di.IRequestor;

public class ContextObjectSupplier extends AbstractObjectSupplier {

	static private class ContextInjectionListener implements IRunAndTrackObject {

		final private Object[] result;
		final private String[] keys;
		final private IInjector injector;
		final private IRequestor requestor;
		final private AbstractObjectSupplier supplier;
		final private IEclipseContext context;

		public ContextInjectionListener(IEclipseContext context, Object[] result, String[] keys,
				IInjector injector, IRequestor requestor, AbstractObjectSupplier supplier) {
			this.result = result;
			this.keys = keys;
			this.injector = injector;
			this.requestor = requestor;
			this.supplier = supplier;
			this.context = context;
		}

		public boolean notify(ContextChangeEvent event) {
			if (event.getEventType() == ContextChangeEvent.INITIAL) {
				// needs to be done inside runnable to establish dependencies
				for (int i = 0; i < keys.length; i++) {
					if (keys[i] == null)
						continue;
					if (context.containsKey(keys[i]))
						result[i] = context.get(keys[i]);
					else
						result[i] = IInjector.NOT_A_VALUE; // TBD make sure this still creates
															// dependency on the key
				}
			} else if (event.getEventType() == ContextChangeEvent.DISPOSE) {
				injector.disposed(supplier);
				return false;
			} else if (event.getEventType() == ContextChangeEvent.UNINJECTED) {
				injector.uninject(event.getArguments()[0], supplier);
				return false;
			} else
				injector.update(new IRequestor[] { requestor }, supplier);
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((context == null) ? 0 : context.hashCode());
			result = prime * result + ((injector == null) ? 0 : injector.hashCode());
			result = prime * result + ((requestor == null) ? 0 : requestor.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ContextInjectionListener other = (ContextInjectionListener) obj;
			if (context == null) {
				if (other.context != null)
					return false;
			} else if (!context.equals(other.context))
				return false;
			if (injector == null) {
				if (other.injector != null)
					return false;
			} else if (!injector.equals(other.injector))
				return false;
			if (requestor == null) {
				if (other.requestor != null)
					return false;
			} else if (!requestor.equals(other.requestor))
				return false;
			return true;
		}

		public Object getObject() {
			// XXX remove?
			// TODO Auto-generated method stub
			return null;
		}

		public boolean batchProcess() {
			return requestor.shouldGroupUpdates();
		}
	}

	final static private String ECLIPSE_CONTEXT_NAME = IEclipseContext.class.getName();
	final private IEclipseContext context;

	public ContextObjectSupplier(IEclipseContext context, IInjector injector) {
		super(injector);
		this.context = context;
	}

	public IEclipseContext getContext() {
		return context;
	}

	@Override
	public Object get(IObjectDescriptor descriptor, IRequestor requestor) {
		// This method is rarely or never used on the primary supplier
		if (descriptor == null)
			return IInjector.NOT_A_VALUE;
		Object[] result = get(new IObjectDescriptor[] { descriptor }, requestor);
		if (result == null)
			return null;
		return result[0];
	}

	@Override
	public Object[] get(IObjectDescriptor[] descriptors, final IRequestor requestor) {
		final Object[] result = new Object[descriptors.length];
		final String[] keys = new String[descriptors.length];

		for (int i = 0; i < descriptors.length; i++) {
			keys[i] = (descriptors[i] == null) ? null : getKey(descriptors[i]);
			if (ECLIPSE_CONTEXT_NAME.equals(keys[i])) {
				result[i] = context;
				keys[i] = null;
			}
		}

		if (requestor != null && requestor.shouldTrack()) { // only track if requested
			IRunAndTrack trackable = new ContextInjectionListener(context, result, keys, injector,
					requestor, this);
			context.runAndTrack(trackable, null);
		} else {
			for (int i = 0; i < descriptors.length; i++) {
				if (keys[i] == null)
					continue;
				if (context.containsKey(keys[i]))
					result[i] = context.get(keys[i]);
				else
					result[i] = IInjector.NOT_A_VALUE;
			}
		}
		return result;
	}

	private String getKey(IObjectDescriptor descriptor) {
		if (descriptor.hasQualifier(Named.class.getName()))
			return descriptor.getQualifierValue(Named.class.getName());
		Class<?> elementClass = descriptor.getElementClass();
		if (elementClass != null)
			return elementClass.getName();
		return null;
	}

	static public ContextObjectSupplier getObjectSupplier(IEclipseContext context,
			IInjector injector) {
		String key = ContextObjectSupplier.class.getName();
		if (context.containsKey(key, true))
			return (ContextObjectSupplier) context.get(key);
		ContextObjectSupplier objectSupplier = new ContextObjectSupplier(context, injector);
		context.set(key, objectSupplier);
		return objectSupplier;
	}

}
