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
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.internal.contexts.EclipseContext.Scheduled;

abstract public class Computation {

	/**
	 * Computations must define equals because they are stored in a set.
	 */
	public abstract boolean equals(Object arg0);

	/**
	 * Computations must define hashCode because they are stored in a set.
	 */
	public abstract int hashCode();

	protected Map<IEclipseContext, Set<String>> dependencies = new HashMap<IEclipseContext, Set<String>>();

	public void addDependency(IEclipseContext context, String name) {
		Set<String> properties = dependencies.get(context);
		if (properties == null) {
			properties = new HashSet<String>(4);
			dependencies.put(context, properties);
		}
		properties.add(name);
	}

	protected void doHandleInvalid(ContextChangeEvent event, List<Scheduled> scheduled) {
		// nothing to do in default computation
	}

	public void handleInvalid(ContextChangeEvent event, List<Scheduled> scheduled) {
		String name = event.getName();
		IEclipseContext context = event.getContext();
		Set<String> names = dependencies.get(context);

		boolean contextDisposed = (event.getEventType() == ContextChangeEvent.DISPOSE);
		boolean affected = (names == null) ? false : names.contains(name);

		if (contextDisposed || affected) {
			stopListening(context, name);
			doHandleInvalid(event, scheduled);
		}
	}

	/**
	 * Remove this computation from all contexts that are tracking it
	 */
	protected void removeAll() {
		for (Iterator<IEclipseContext> it = dependencies.keySet().iterator(); it.hasNext();) {
			((EclipseContext) it.next()).listeners.remove(this);
		}
		dependencies.clear();
	}

	public void startListening() {
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
	}

	public void stopListening(IEclipseContext context, String name) {
		if (name == null) {
			dependencies.remove(context);
			((EclipseContext) context).listeners.remove(this);
			return;
		}
		Set<String> properties = dependencies.get(context);
		if (properties != null) {
			properties.remove(name);
			// if we no longer track any values in the context, remove dependency
			if (properties.isEmpty()) {
				dependencies.remove(context);
				((EclipseContext) context).listeners.remove(this);
			}
		}
	}

	public Set<String> dependsOnNames(IEclipseContext context) {
		return dependencies.get(context);
	}

}