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

import java.util.*;
import org.eclipse.e4.core.services.context.IComputedValue;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.*;

public class EclipseContext extends AbstractContext {

	static class TrackableComputation extends Computation implements Runnable {
		Runnable runnable;
		private final String name;

		TrackableComputation(Runnable runnable, String name) {
			this.runnable = runnable;
			this.name = name;
		}

		final protected void doHandleInvalid(IEclipseContext context) {
			if (EclipseContext.DEBUG)
				System.out.println("scheduling " + toString());
			((EclipseContext) context).schedule(this); // XXX conversion: should be IEclipseContext
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

	private static final Object[] NO_ARGUMENTS = new Object[0];

	private IEclipseContextStrategy strategy;

	// TODO replace with variable on bundle-specific class
	public static boolean DEBUG = false;

	/**
	 * TODO Can this really be static? Couldn't there be multiple computations ongoing
	 * in a single thread? For example a computation could recursively cause
	 * another context lookup and therefore a nested computation.
	 */
	static ThreadLocal currentComputation = new ThreadLocal();

	Set listeners = new HashSet();

	Map localValues = new HashMap();
	Map localValueComputations = new HashMap();

	IEclipseContext parent;
	private final String contextName;

	public EclipseContext(IEclipseContext parent, String name, IEclipseContextStrategy strategy) {
		this.parent = parent;
		this.contextName = name;
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

	public Object getLocal(String name) {
		return internalGet(this, name, null, true);
	}

	public Object get(String name) {
		return internalGet(this, name, NO_ARGUMENTS, false);
	}

	public Object get(String name, Object[] arguments) {
		return internalGet(this, name, arguments, false);
	}

	static class LookupKey {
		String name;
		Object[] arguments;

		public LookupKey(String name, Object[] arguments) {
			this.name = name;
			this.arguments = arguments;
		}

		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result;
			if (arguments != null) {
				for (int i = 0; i < arguments.length; i++) {
					Object arg = arguments[i];
					result = prime * result + (arg == null ? 0 : arg.hashCode());
				}
			}
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LookupKey other = (LookupKey) obj;
			if (!Arrays.equals(arguments, other.arguments))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
	}

	protected Object internalGet(EclipseContext originatingContext, String name, Object[] arguments, boolean local) {
		trackAccess(name);
		LookupKey lookupKey = new LookupKey(name, arguments);
		if (this == originatingContext) {
			ValueComputation valueComputation = (ValueComputation) localValueComputations.get(lookupKey);
			if (valueComputation != null) {
				return valueComputation.get(arguments);
			}
		}
		Object result = localValues.get(name);
		if (result != null) {
			if (result instanceof IComputedValue) {
				if (EclipseContext.DEBUG)
					System.out.println("creating new value computation for " + name + " in " + this + " from " + originatingContext);
				ValueComputation valueComputation = new ValueComputation(this, originatingContext, name, ((IComputedValue) result));
				originatingContext.localValueComputations.put(lookupKey, valueComputation);
				result = valueComputation.get(arguments);
			}
			return result;
		}
		if (!local && parent != null) {
			return ((EclipseContext) parent).internalGet(originatingContext, name, arguments, local); // XXX IEclipseContext
		}
		return null;
	}

	protected void invalidate(String name) {
		if (EclipseContext.DEBUG)
			System.out.println("invalidating " + this.contextName + "," + name);
		localValueComputations.remove(name);
		Computation[] ls = (Computation[]) listeners.toArray(new Computation[listeners.size()]);
		for (int i = 0; i < ls.length; i++) {
			ls[i].handleInvalid(this, name);
		}
	}

	public boolean containsKey(String name) {
		return isSetLocally(name) || (parent != null && parent.containsKey(name));
	}

	private boolean isSetLocally(String name) {
		trackAccess(name);
		return localValues.containsKey(name);
	}

	public void runAndTrack(final Runnable runnable, String name) {
		TrackableComputation computation = new TrackableComputation(runnable, name);
		schedule(computation);
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

	public void remove(String name) {
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
		return contextName;
	}
}
