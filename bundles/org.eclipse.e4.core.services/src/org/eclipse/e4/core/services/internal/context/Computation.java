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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.e4.core.services.context.ContextChangeEvent;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.injector.IObjectProvider;

abstract class Computation {
	Map dependencies = new HashMap();

	void addDependency(IEclipseContext context, String name) {
		Set properties = (Set) dependencies.get(context);
		if (properties == null) {
			properties = new HashSet(4);
			dependencies.put(context, properties);
		}
		properties.add(name);
	}

	final void clear(IEclipseContext context, String name) {
		doClear();
		stopListening(context, name);
	}

	protected void doClear() {
	}

	protected void doHandleInvalid(ContextChangeEvent event, List scheduled) {
	}

	/**
	 * Computations must define equals because they are stored in a set.
	 */
	public abstract boolean equals(Object arg0);

	final void handleInvalid(ContextChangeEvent event, List scheduled) {
		IObjectProvider provider = event.getContext();
		IEclipseContext context = ((ObjectProviderContext) provider).getContext();

		String name = event.getName();
		Set names = (Set) dependencies.get(context);
		if (name == null && event.getEventType() == ContextChangeEvent.DISPOSE) {
			clear(context, null);
			doHandleInvalid(event, scheduled);
		} else if (names != null && names.contains(name)) {
			clear(context, name);
			doHandleInvalid(event, scheduled);
		}
	}

	final void handleUninjected(ContextChangeEvent event, List scheduled) {
		doHandleInvalid(event, scheduled);
	}

	/**
	 * Computations must define hashCode because they are stored in a set.
	 */
	public abstract int hashCode();

	private String mapToString(Map map) {
		StringBuffer result = new StringBuffer('{');
		for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			result.append(entry.getKey());
			result.append("->(");
			Set set = (Set) entry.getValue();
			for (Iterator it2 = set.iterator(); it2.hasNext();) {
				String name = (String) it2.next();
				result.append(name);
				if (it2.hasNext()) {
					result.append(",");
				}
			}
			result.append(")");
			if (it.hasNext()) {
				result.append(",");
			}
		}
		return result.toString();
	}

	/**
	 * Remove this computation from all contexts that are tracking it
	 */
	protected void removeAll(EclipseContext originatingContext) {
		for (Iterator it = dependencies.keySet().iterator(); it.hasNext();) {
			((EclipseContext) it.next()).listeners.remove(this);
		}
		dependencies.clear();
		// Bug 304859
		originatingContext.listeners.remove(this);
	}

	void startListening(EclipseContext originatingContext) {
		if (EclipseContext.DEBUG)
			System.out.println(toString() + " now listening to: " //$NON-NLS-1$
					+ mapToString(dependencies));
		for (Iterator it = dependencies.keySet().iterator(); it.hasNext();) {
			EclipseContext c = (EclipseContext) it.next(); // XXX IEclipseContex
			if (c.listeners.contains(this)) { // nested
				for (Iterator exitsting = c.listeners.iterator(); exitsting.hasNext();) {
					Computation existingComputation = (Computation) exitsting.next();
					if (!equals(existingComputation))
						continue;
					for (Iterator newDependencies = dependencies.keySet().iterator(); newDependencies
							.hasNext();) {
						IEclipseContext newDepenecyContext = (IEclipseContext) newDependencies
								.next();
						if (existingComputation.dependencies.containsKey(newDepenecyContext))
							((Set) existingComputation.dependencies.get(newDepenecyContext))
									.addAll((Set) dependencies.get(newDepenecyContext));
						else
							existingComputation.dependencies.put(newDepenecyContext, dependencies
									.get(newDepenecyContext));
					}
					break;
				}
			} else
				c.listeners.add(this);
		}
		// Bug 304859
		if (!dependencies.containsKey(originatingContext))
			originatingContext.listeners.remove(this);
	}

	protected void stopListening(IEclipseContext context, String name) {

		if (name == null) {
			if (EclipseContext.DEBUG)
				System.out.println(toString() + " no longer listening to " + context); //$NON-NLS-1$
			dependencies.remove(context);
			return;
		}
		Set properties = (Set) dependencies.get(context);
		if (properties != null) {
			if (EclipseContext.DEBUG)
				System.out.println(toString() + " no longer listening to " + context + "," + name); //$NON-NLS-1$
			// Bug 304859 - causes reordering of listeners
			// ((EclipseContext) context).listeners.remove(this); // XXX
			// IEclipseContext
			properties.remove(name);
			// if we no longer track any values in the context, remove dependency
			if (properties.isEmpty())
				dependencies.remove(context);
		}
	}

	public Set dependsOnNames(IEclipseContext context) {
		return (Set) dependencies.get(context);
	}

}