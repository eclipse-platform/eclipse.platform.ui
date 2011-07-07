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

	protected Map<EclipseContext, Set<String>> dependencies = new HashMap<EclipseContext, Set<String>>();

	public void addDependency(EclipseContext context, String name) {
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
		EclipseContext context = (EclipseContext) event.getContext();

		stopListening(context, name);
		doHandleInvalid(event, scheduled);
	}

	/**
	 * Remove this computation from all contexts that are tracking it
	 */
	protected void removeAll() {
		for (EclipseContext c : dependencies.keySet()) {
			c.removeListener(this);
		}
		dependencies.clear();
	}

	public void startListening() {
		for (EclipseContext c : dependencies.keySet()) {
			c.addListener(this, dependencies.get(c));
		}
	}

	public void stopListening(EclipseContext context, String name) {
		if (context == null) {
			Set<EclipseContext> dependentContexts = dependencies.keySet();
			for (EclipseContext dependentContext : dependentContexts) {
				dependentContext.removeListener(this);
			}
			return;
		}
		if (name == null) {
			dependencies.remove(context);
			context.removeListener(this);
			return;
		}
		Set<String> properties = dependencies.get(context);
		if (properties != null) {
			properties.remove(name);
			// if we no longer track any values in the context, remove dependency
			if (properties.isEmpty()) {
				dependencies.remove(context);
				context.removeListener(this);
			}
		}
	}

	public Set<String> dependsOnNames(IEclipseContext context) {
		return dependencies.get(context);
	}

}