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

import java.lang.reflect.Type;
import javax.inject.Named;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.di.IInjector;
import org.eclipse.e4.core.di.suppliers.IObjectDescriptor;
import org.eclipse.e4.core.di.suppliers.IRequestor;
import org.eclipse.e4.core.di.suppliers.PrimaryObjectSupplier;

public class ContextObjectSupplier extends PrimaryObjectSupplier {

	final static protected String ECLIPSE_CONTEXT_NAME = IEclipseContext.class.getName();

	static private class ContextInjectionListener extends RunAndTrackExt {

		final private Object[] result;
		final private String[] keys;
		final private IRequestor requestor;
		final private IEclipseContext context;

		public ContextInjectionListener(IEclipseContext context, Object[] result, String[] keys, IRequestor requestor, boolean group) {
			super(group);
			this.result = result;
			this.keys = keys;
			this.requestor = requestor;
			this.context = context;
		}

		public boolean update(IEclipseContext eventsContext, int eventType, Object[] extraArguments, final IContextRecorder recorder) {
			if (eventType == ContextChangeEvent.INITIAL) {
				// needs to be done inside runnable to establish dependencies
				for (int i = 0; i < keys.length; i++) {
					if (keys[i] == null)
						continue;
					if (ECLIPSE_CONTEXT_NAME.equals(keys[i])) {
						result[i] = context;
						context.getParent(); // creates pseudo-link
					} else if (context.containsKey(keys[i]))
						result[i] = context.get(keys[i]);
					else
						result[i] = IInjector.NOT_A_VALUE;
				}
				return true;
			}

			if (eventType == ContextChangeEvent.DISPOSE) {
				if (eventsContext == context) {
					ContextObjectSupplier originatingSupplier = eventsContext.getLocal(ContextObjectSupplier.class);
					requestor.disposed(originatingSupplier);
					return false;
				}
			} else if (eventType == ContextChangeEvent.UNINJECTED) {
				if (eventsContext == context) {
					ContextObjectSupplier originatingSupplier = eventsContext.getLocal(ContextObjectSupplier.class);
					requestor.uninject(extraArguments[0], originatingSupplier);
					return false;
				}
			} else {
				requestor.resolveArguments();
				if (recorder != null)
					recorder.stopAccessRecording();
				try {
					requestor.execute();
				} finally {
					if (recorder != null)
						recorder.startAcessRecording();
				}
			}
			return true;
		}

		public boolean changed(IEclipseContext eventsContext) {
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int hashRresult = 1;
			hashRresult = prime * hashRresult + ((context == null) ? 0 : context.hashCode());
			hashRresult = prime * hashRresult + ((requestor == null) ? 0 : requestor.hashCode());
			return hashRresult;
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
			if (requestor == null) {
				if (other.requestor != null)
					return false;
			} else if (!requestor.equals(other.requestor))
				return false;
			return true;
		}

	}

	final private IEclipseContext context;

	public ContextObjectSupplier(IEclipseContext context, IInjector injector) {
		this.context = context;
	}

	public IEclipseContext getContext() {
		return context;
	}

	@Override
	public Object[] get(IObjectDescriptor[] descriptors, final IRequestor requestor, boolean track, boolean group) {
		final Object[] result = new Object[descriptors.length];
		final String[] keys = new String[descriptors.length];

		for (int i = 0; i < descriptors.length; i++) {
			keys[i] = (descriptors[i] == null) ? null : getKey(descriptors[i]);
		}

		if (requestor != null && track) { // only track if requested
			RunAndTrack trackable = new ContextInjectionListener(context, result, keys, requestor, group);
			context.runAndTrack(trackable);
		} else {
			for (int i = 0; i < descriptors.length; i++) {
				if (keys[i] == null)
					continue;
				if (ECLIPSE_CONTEXT_NAME.equals(keys[i]))
					result[i] = context;
				else if (context.containsKey(keys[i]))
					result[i] = context.get(keys[i]);
				else
					result[i] = IInjector.NOT_A_VALUE;
			}
		}
		return result;
	}

	private String getKey(IObjectDescriptor descriptor) {
		if (descriptor.hasQualifier(Named.class)) {
			Named namedAnnotation = descriptor.getQualifier(Named.class);
			return namedAnnotation.value();
		}
		Type elementType = descriptor.getDesiredType();
		if (elementType instanceof Class<?>)
			return ((Class<?>) elementType).getName();
		return null;
	}

	static public ContextObjectSupplier getObjectSupplier(IEclipseContext context, IInjector injector) {
		if (context == null)
			return null;
		ContextObjectSupplier supplier = context.getLocal(ContextObjectSupplier.class);
		if (supplier != null)
			return supplier;
		ContextObjectSupplier objectSupplier = new ContextObjectSupplier(context, injector);
		context.set(ContextObjectSupplier.class, objectSupplier);
		return objectSupplier;
	}

}
