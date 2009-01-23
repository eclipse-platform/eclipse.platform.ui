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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IComputedValue;
import org.eclipse.e4.core.services.context.spi.IEclipseContextScheduler;
import org.eclipse.e4.core.services.context.spi.IEclipseContextStrategy;

public class EclipseContext implements IEclipseContext {

	static class TrackableComputation extends Computation implements Runnable {
		Runnable runnable;
		private final String name;

		TrackableComputation(Runnable runnable, String name) {
			this.runnable = runnable;
			this.name = name;
		}

		final protected void doHandleInvalid(EclipseContext context) {
			if (EclipseContext.DEBUG) System.out.println("scheduling " + toString());
			context.schedule(this);
		}

		public void run() {
			Computation oldComputation = (Computation) currentComputation.get();
			currentComputation.set(this);
			try {
				runnable.run();
			} finally {
				currentComputation.set(oldComputation);
			}
			startListening();
		}

		public String toString() {
			return name;
		}
	}
	
	private IEclipseContextStrategy strategy;

	// XXX replace with variable on bundle-specific class
	public static final boolean DEBUG = false;

	static ThreadLocal currentComputation = new ThreadLocal();

	Set listeners = new HashSet();

	Map localValues = new HashMap();
	Map localValueComputations = new HashMap();

	IEclipseContext parent;

	private final String name;

	public EclipseContext(IEclipseContext parent, String name, IEclipseContextStrategy strategy) {
		this.parent = parent;
		this.name = name;
		this.strategy = strategy;
	}

	protected void schedule(Runnable runnable) {
		if (runnable == null)
			return;
		if (strategy != null && strategy instanceof IEclipseContextScheduler)
			((IEclipseContextScheduler) strategy).schedule(runnable);
		else
			runnable.run();
	}

	public Object get(String name) {
		return internalGet(this, name);
	}

	protected Object internalGet(EclipseContext originatingContext, String name) {
		trackAccess(name);
		Object result = localValues.get(name);
		if (result != null) {
			if (result instanceof IComputedValue) {
				if (EclipseContext.DEBUG) System.out.println("creating new value computation for " + name + " in " + this + " from " + originatingContext);
				ValueComputation valueComputation = new ValueComputation(
						this, originatingContext, name, ((IComputedValue) result));
				originatingContext.localValueComputations.put(name, valueComputation);
				result = valueComputation.get();
			}
			return result;
		}
		ValueComputation valueComputation = (ValueComputation) localValueComputations.get(name);
		if (valueComputation != null) {
			return valueComputation.get();
		}
		if (parent != null) {
			return ((EclipseContext) parent).internalGet(originatingContext, name); // XXX IEclipseContext
		}
		return null;
	}

	protected void invalidate(String name) {
		if (EclipseContext.DEBUG) System.out.println("invalidating " + this.name + "," + name);
		localValueComputations.remove(name);
		Computation[] ls = (Computation[]) listeners
				.toArray(new Computation[listeners.size()]);
		for (int i = 0; i < ls.length; i++) {
			ls[i].handleInvalid(this, name);
		}
	}

	public boolean isSet(String name) {
		Object value = get(name);
		return value != null;
	}

	public boolean isSetLocally(String name) {
		return localValues.containsKey(name);
	}

	public void runAndTrack(final Runnable runnable, String name) {
		TrackableComputation computation = new TrackableComputation(runnable, name);
		computation.run();
	}

	public void set(String name, IComputedValue computedValue) {
		set(name, (Object) computedValue);
	}

	public void set(String name, Object value) {
		localValues.put(name, value);
		Object old = localValueComputations.remove(name);
		if (old instanceof ValueComputation) {
			ValueComputation valueComputation = (ValueComputation) old;
			valueComputation.clear();
		}
		invalidate(name);
	}

	private void trackAccess(String name) {
		Computation computation = (Computation) currentComputation.get();
		if (computation != null) {
			computation.addDependency(this, name);
		}
	}

	public void unset(String name) {
		if (isSetLocally(name)) {
			localValues.remove(name);
			Object removed = localValueComputations.remove(name);
			if (removed instanceof ValueComputation) {
				ValueComputation valueComputation = (ValueComputation) removed;
				valueComputation.clear();
			}
			invalidate(name);
		}
	}

	public String toString() {
		return name;
	}
}
