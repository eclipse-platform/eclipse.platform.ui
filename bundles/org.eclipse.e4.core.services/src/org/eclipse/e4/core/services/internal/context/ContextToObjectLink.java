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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.e4.core.services.context.ContextChangeEvent;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.IRunAndTrack;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.core.services.context.spi.IContextConstants;

/**
 * Implements injection of context values into an object. Tracks context changes and makes the
 * corresponding updates to injected objects. See class comment of {@link ContextInjectionFactory}
 * for details on the injection algorithm.
 */
public class ContextToObjectLink implements IRunAndTrack, IContextConstants {

	// annotation names
	protected IEclipseContext context;

	protected List userObjects = new ArrayList(3); // start small

	public ContextToObjectLink(IEclipseContext context) {
		this.context = context;
	}

	private void handleAdd(final ContextChangeEvent event) {
		final String name = event.getName();
		if (IContextConstants.PARENT.equals(name)) {
			handleParentChange(event);
			return;
		}
		ContextInjector injector = new ContextInjector(new ObjectProviderContext(context));
		Object[] objectsCopy = safeObjectsCopy();
		for (int i = 0; i < objectsCopy.length; i++) {
			injector.inject(name, objectsCopy[i]);
		}
	}

	private void handleParentChange(final ContextChangeEvent event) {
		final EclipseContext eventContext = (EclipseContext) event.getContext();
		final EclipseContext oldParent = (EclipseContext) event.getOldValue();
		final EclipseContext newParent = (EclipseContext) eventContext
				.get(IContextConstants.PARENT);
		if (oldParent == newParent)
			return;

		ContextInjector injector = new ContextInjector(new ObjectProviderContext(context));
		Object[] objectsCopy = safeObjectsCopy();
		for (int i = 0; i < objectsCopy.length; i++) {
			injector.reparent(objectsCopy[i], new ObjectProviderContext(oldParent));
		}
	}

	private void handleRelease(ContextChangeEvent event) {
		Object releasedObject = event.getArguments()[0];
		synchronized (userObjects) {
			boolean found = false;
			for (Iterator i = userObjects.iterator(); i.hasNext();) {
				WeakReference ref = (WeakReference) i.next();
				Object userObject = ref.get();
				if (userObject == null)
					continue;
				if (userObject.equals(releasedObject)) {
					i.remove();
					found = true;
					break;
				}
			}
			if (!found)
				return;
		}

		ContextInjector injector = new ContextInjector(new ObjectProviderContext(context));
		injector.uninject(releasedObject);
	}

	private void handleDispose(ContextChangeEvent event) {
		ContextInjector injector = new ContextInjector(new ObjectProviderContext(context));
		Object[] objectsCopy = safeObjectsCopy();
		for (int i = 0; i < objectsCopy.length; i++) {
			injector.dispose(objectsCopy[i]);
		}
	}

	private void handleInitial(final ContextChangeEvent event) {
		if (event.getArguments() == null || event.getArguments().length == 0
				|| event.getArguments()[0] == null)
			throw new IllegalArgumentException();

		ContextInjector injector = new ContextInjector(new ObjectProviderContext(context));
		injector.inject(event.getArguments()[0]);

		WeakReference ref = new WeakReference(event.getArguments()[0]);
		synchronized (userObjects) {
			userObjects.add(ref);
		}
	}

	private void handleRemove(final ContextChangeEvent event) {
		final String name = event.getName();
		if (IContextConstants.PARENT.equals(name)) {
			handleParentChange(event);
			return;
		}
		ContextInjector injector = new ContextInjector(new ObjectProviderContext(context));
		Object[] objectsCopy = safeObjectsCopy();
		for (int i = 0; i < objectsCopy.length; i++) {
			injector.uninject(name, objectsCopy[i]);
		}
	}

	static void logWarning(Object destination, Exception e) {
		System.out.println("Injection failed " + destination.toString()); //$NON-NLS-1$
		if (e != null)
			e.printStackTrace();
		// TBD convert this into real logging
		// String msg = NLS.bind("Injection failed", destination.toString());
		// RuntimeLog.log(new Status(IStatus.WARNING,
		// IRuntimeConstants.PI_COMMON, 0, msg, e));
	}

	public boolean notify(final ContextChangeEvent event) {
		switch (event.getEventType()) {
		case ContextChangeEvent.INITIAL:
			handleInitial(event);
			break;
		case ContextChangeEvent.ADDED:
			handleAdd(event);
			break;
		case ContextChangeEvent.REMOVED:
			handleRemove(event);
			break;
		case ContextChangeEvent.UNINJECTED:
			handleRelease(event);
			break;
		case ContextChangeEvent.DISPOSE:
			handleDispose(event);
			break;
		}
		return (!userObjects.isEmpty());
	}

	private Object[] safeObjectsCopy() {
		Object[] result;
		int pos = 0;
		synchronized (userObjects) {
			result = new Object[userObjects.size()];
			for (Iterator i = userObjects.iterator(); i.hasNext();) {
				WeakReference ref = (WeakReference) i.next();
				Object userObject = ref.get();
				if (userObject == null) { // user object got GCed, clean up refs
					// for future
					i.remove();
					continue;
				}
				result[pos] = userObject;
				pos++;
			}
		}
		if (pos == result.length)
			return result;
		// reallocate the array
		Object[] tmp = new Object[pos];
		System.arraycopy(result, 0, tmp, 0, pos);
		return tmp;
	}

	public String toString() {
		return "InjectionTracker(" + context + ')'; //$NON-NLS-1$
	}
}
