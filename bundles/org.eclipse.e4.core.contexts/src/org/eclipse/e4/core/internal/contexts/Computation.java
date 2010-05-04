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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.e4.core.contexts.ContextChangeEvent;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.internal.contexts.EclipseContext.Scheduled;

abstract class Computation {
	Map<IEclipseContext, Set<String>> dependencies = new HashMap<IEclipseContext, Set<String>>();

	void addDependency(IEclipseContext context, String name) {
		Set<String> properties = dependencies.get(context);
		if (properties == null) {
			properties = new HashSet<String>(4);
			dependencies.put(context, properties);
		}
		properties.add(name);
	}

	final void clear(IEclipseContext context, String name) {
		doClear();
		stopListening(context, name);
	}

	protected void doClear() {
		// nothing to do in default computation
	}

	protected void doHandleInvalid(ContextChangeEvent event, List<Scheduled> scheduled) {
		// nothing to do in default computation
	}

	/**
	 * Computations must define equals because they are stored in a set.
	 */
	public abstract boolean equals(Object arg0);

	final void handleInvalid(ContextChangeEvent event, List<Scheduled> scheduled) {
		IEclipseContext context = event.getContext();

		String name = event.getName();
		Set<String> names = dependencies.get(context);
		if (name == null && event.getEventType() == ContextChangeEvent.DISPOSE) {
			clear(context, null);
			doHandleInvalid(event, scheduled);
		} else if (names != null && names.contains(name)) {
			clear(context, name);
			doHandleInvalid(event, scheduled);
		}
	}

	final void handleUninjected(ContextChangeEvent event, List<Scheduled> scheduled) {
		doHandleInvalid(event, scheduled);
	}

	/**
	 * Computations must define hashCode because they are stored in a set.
	 */
	public abstract int hashCode();

	private String mapToString(Map<IEclipseContext, Set<String>> map) {
		StringBuffer result = new StringBuffer('{');
		for (Iterator<Map.Entry<IEclipseContext, Set<String>>> it = map.entrySet().iterator(); it.hasNext();) {
			Map.Entry<IEclipseContext, Set<String>> entry = it.next();
			result.append(entry.getKey());
			result.append("->("); //$NON-NLS-1$
			Set<String> set = entry.getValue();
			for (Iterator<String> it2 = set.iterator(); it2.hasNext();) {
				String name = it2.next();
				result.append(name);
				if (it2.hasNext()) {
					result.append(',');
				}
			}
			result.append(')');
			if (it.hasNext()) {
				result.append(',');
			}
		}
		return result.toString();
	}

	/**
	 * Remove this computation from all contexts that are tracking it
	 */
	protected void removeAll(EclipseContext originatingContext) {
		for (Iterator<IEclipseContext> it = dependencies.keySet().iterator(); it.hasNext();) {
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
		for (Iterator<IEclipseContext> it = dependencies.keySet().iterator(); it.hasNext();) {
			EclipseContext c = (EclipseContext) it.next();
			Computation existingComputation = c.listeners.get(this);
			if (existingComputation != null) {
				// if the existing computation is equal but not identical, we need to update
				if (this == existingComputation)
					continue;
				Set<String> existingDependencies = existingComputation.dependencies.get(c);
				if (existingDependencies != null)
					existingDependencies.addAll(dependencies.get(c));
				else
					existingComputation.dependencies.put(c, dependencies.get(c));
			} else
				c.listeners.put(this, this);
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
		Set<String> properties = dependencies.get(context);
		if (properties != null) {
			if (EclipseContext.DEBUG)
				System.out.println(toString() + " no longer listening to " + context + ',' + name); //$NON-NLS-1$
			// Bug 304859 - causes reordering of listeners
			// ((EclipseContext) context).listeners.remove(this);
			properties.remove(name);
			// if we no longer track any values in the context, remove dependency
			if (properties.isEmpty())
				dependencies.remove(context);
		}
	}

	public Set<String> dependsOnNames(IEclipseContext context) {
		return dependencies.get(context);
	}

}