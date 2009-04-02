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
import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.context.IContextFunction;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.*;

public class EclipseContext implements IEclipseContext, IDisposable {

	static class LookupKey {
		Object[] arguments;
		String name;

		public LookupKey(String name, Object[] arguments) {
			this.name = name;
			this.arguments = arguments;
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
	}

	static class TrackableComputation extends Computation implements Runnable {
		private final String name;
		Runnable runnable;

		TrackableComputation(Runnable runnable, String name) {
			this.runnable = runnable;
			this.name = name;
		}

		final protected void doHandleInvalid(IEclipseContext context, String name, int eventType) {
			if (eventType == IRunAndTrack.DISPOSE) {
				return;
			}
			if (EclipseContext.DEBUG)
				System.out.println("scheduling " + toString()); //$NON-NLS-1$
			((EclipseContext) context).schedule(this); // XXX conversion: should
			// be IEclipseContext
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

	static class TrackableComputationExt extends Computation implements IRunAndTrack {

		private IRunAndTrack runnable;

		public TrackableComputationExt(IRunAndTrack runnable) {
			this.runnable = runnable;
		}

		final protected void doHandleInvalid(IEclipseContext context, String name, int eventType) {
			((EclipseContext) context).schedule(this, name, eventType, null); // XXX
			// IEclipseContext
		}

		public boolean notify(IEclipseContext context, String name, int eventType, Object[] args) {
			Computation oldComputation = (Computation) currentComputation.get();
			currentComputation.set(this);
			boolean result = true;
			try {
				result = runnable.notify(context, name, eventType, args);
			} finally {
				currentComputation.set(oldComputation);
			}
			startListening();
			return result;
		}
	}

	/**
	 * TODO Can this really be static? Couldn't there be multiple computations
	 * ongoing in a single thread? For example a computation could recursively
	 * cause another context lookup and therefore a nested computation.
	 */
	static ThreadLocal currentComputation = new ThreadLocal();

	// TODO replace with variable on bundle-specific class
	public static boolean DEBUG = false;

	private static final Object[] NO_ARGUMENTS = new Object[0];

	Set listeners = new HashSet();

	Map localValueComputations = new HashMap();
	Map localValues = new HashMap();

	IEclipseContext parent;

	private IEclipseContextStrategy strategy;

	public EclipseContext(IEclipseContext parent, IEclipseContextStrategy strategy) {
		this.parent = parent;
		this.strategy = strategy;
		set(IContextConstants.DEBUG_STRING, "Anonymous Context"); //$NON-NLS-1$
	}

	public boolean containsKey(String name) {
		if (isSetLocally(name))
			return true;
		if (parent != null && parent.containsKey(name))
			return true;
		if (strategy instanceof ILookupStrategy) {
			if (((ILookupStrategy) strategy).containsKey(name, this))
				return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.e4.core.services.context.IEclipseContext#dispose()
	 */
	public void dispose() {
		Computation[] ls = (Computation[]) listeners.toArray(new Computation[listeners.size()]);
		for (int i = 0; i < ls.length; i++) {
			ls[i].handleInvalid(this, null, IRunAndTrack.DISPOSE);
		}
		if (strategy instanceof IDisposable)
			((IDisposable) strategy).dispose();
	}

	public Object get(String name) {
		return internalGet(this, name, NO_ARGUMENTS, false);
	}

	public Object get(String name, Object[] arguments) {
		return internalGet(this, name, arguments, false);
	}

	public Object getLocal(String name) {
		return internalGet(this, name, null, true);
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
		//1. try for local value
		Object result = localValues.get(name);

		//2. try the local strategy
		if (result == null && strategy instanceof ILookupStrategy)
			result = ((ILookupStrategy) strategy).lookup(name, originatingContext);

		//if we found something, compute the concrete value and return
		if (result != null) {
			if (result instanceof IContextFunction) {
				if (EclipseContext.DEBUG)
					System.out.println("creating new value computation for " + name + " in " + this + " from " + originatingContext); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				ValueComputation valueComputation = new ValueComputation(this, originatingContext, name, ((IContextFunction) result));
				originatingContext.localValueComputations.put(lookupKey, valueComputation);
				result = valueComputation.get(arguments);
			}
			return result;
		}
		//3. delegate to parent
		if (!local && parent != null) {
			return ((EclipseContext) parent).internalGet(originatingContext, name, arguments, local); // XXX IEclipseContext
		}
		return null;
	}

	protected void invalidate(String name, int eventType) {
		if (EclipseContext.DEBUG)
			System.out.println("invalidating " + get(IContextConstants.DEBUG_STRING) + ',' + name); //$NON-NLS-1$
		localValueComputations.remove(name);
		Computation[] ls = (Computation[]) listeners.toArray(new Computation[listeners.size()]);
		for (int i = 0; i < ls.length; i++) {
			ls[i].handleInvalid(this, name, eventType);
		}
	}

	private boolean isSetLocally(String name) {
		trackAccess(name);
		return localValues.containsKey(name);
	}

	public void remove(String name) {
		if (isSetLocally(name)) {
			localValues.remove(name);
			Object removed = localValueComputations.remove(name);
			if (removed instanceof ValueComputation) {
				ValueComputation valueComputation = (ValueComputation) removed;
				valueComputation.clear(this, name);
			}
			invalidate(name, IRunAndTrack.REMOVED);
		}
	}

	public void runAndTrack(final IRunAndTrack runnable, Object[] args) {
		TrackableComputationExt computation = new TrackableComputationExt(runnable);
		schedule(computation, null, IRunAndTrack.INITIAL, args);
	}

	public void runAndTrack(final Runnable runnable, String name) {
		TrackableComputation computation = new TrackableComputation(runnable, name);
		schedule(computation);
	}

	protected boolean schedule(IRunAndTrack runnable, String name, int eventType, Object[] args) {
		if (runnable == null)
			return false;
		if (eventType != IRunAndTrack.INITIAL && eventType != IRunAndTrack.DISPOSE && strategy != null && strategy instanceof ISchedulerStrategy)
			return ((ISchedulerStrategy) strategy).schedule(this, runnable, name, eventType, args);
		return runnable.notify(this, name, eventType, args);
	}

	protected void schedule(Runnable runnable) {
		if (runnable == null)
			return;
		if (strategy != null && strategy instanceof ISchedulerStrategy)
			((ISchedulerStrategy) strategy).schedule(runnable);
		else
			runnable.run();
	}

	public void set(String name, Object value) {
		localValues.put(name, value);
		Object old = localValueComputations.remove(name);
		if (old instanceof ValueComputation) {
			ValueComputation valueComputation = (ValueComputation) old;
			valueComputation.clear(this, name);
		}
		invalidate(name, IRunAndTrack.ADDED);
	}

	/**
	 * Returns a string representation of this context for debugging purposes only.
	 */
	public String toString() {
		return (String) get(IContextConstants.DEBUG_STRING);
	}

	private void trackAccess(String name) {
		Computation computation = (Computation) currentComputation.get();
		if (computation != null) {
			computation.addDependency(this, name);
		}
	}
}
