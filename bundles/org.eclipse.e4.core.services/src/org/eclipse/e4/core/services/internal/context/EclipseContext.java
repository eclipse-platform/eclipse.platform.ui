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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.runtime.Assert;
import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.context.IContextFunction;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.core.services.context.spi.IEclipseContextStrategy;
import org.eclipse.e4.core.services.context.spi.ILookupStrategy;
import org.eclipse.e4.core.services.context.spi.IRunAndTrack;
import org.eclipse.e4.core.services.context.spi.ISchedulerStrategy;

public class EclipseContext implements IEclipseContext, IDisposable {
	/**
	 * A context key identifying the parent context, which can be retrieved with
	 * {@link IEclipseContext#get(String)}.
	 */
	public static final String PARENT = "PARENT_CONTEXT"; //$NON-NLS-1$

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

		/**
		 * String representation for debugging purposes only.
		 */
		public String toString() {
			return "Key(" + name + ',' + Arrays.asList(arguments) + ')'; //$NON-NLS-1$
		}
	}

	static class TrackableComputation extends Computation implements Runnable {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return runnable.hashCode();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			return this.runnable.equals(((TrackableComputation) obj).runnable);
		}

		final Runnable runnable;

		TrackableComputation(Runnable runnable) {
			this.runnable = runnable;
			Assert.isNotNull(runnable);
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
			return runnable.toString();
		}
	}

	static class TrackableComputationExt extends Computation implements IRunAndTrack {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return 31 + ((runnable == null) ? 0 : runnable.hashCode());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TrackableComputationExt other = (TrackableComputationExt) obj;
			if (runnable == null) {
				if (other.runnable != null)
					return false;
			} else if (!runnable.equals(other.runnable))
				return false;
			return true;
		}

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
	 * TODO Can this really be static? Couldn't there be multiple computations ongoing in a single
	 * thread? For example a computation could recursively cause another context lookup and
	 * therefore a nested computation.
	 */
	static ThreadLocal currentComputation = new ThreadLocal();

	// TODO replace with variable on bundle-specific class
	public static boolean DEBUG = false;

	private static final Object[] NO_ARGUMENTS = new Object[0];

	final Set listeners = new HashSet();

	final Map localValueComputations = Collections.synchronizedMap(new HashMap());
	final Map localValues = Collections.synchronizedMap(new HashMap());

	final IEclipseContext parent;
	private final IEclipseContextStrategy strategy;

	public EclipseContext(IEclipseContext parent, IEclipseContextStrategy strategy) {
		this.parent = parent;
		this.strategy = strategy;
		set(PARENT, parent);
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
	 * 
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

	protected Object internalGet(EclipseContext originatingContext, String name,
			Object[] arguments, boolean local) {
		trackAccess(name);
		LookupKey lookupKey = new LookupKey(name, arguments);
		if (this == originatingContext) {
			ValueComputation valueComputation = (ValueComputation) localValueComputations
					.get(lookupKey);
			if (valueComputation != null) {
				return valueComputation.get(arguments);
			}
		}
		// 1. try for local value
		Object result = localValues.get(name);

		// 2. try the local strategy
		if (result == null && strategy instanceof ILookupStrategy)
			result = ((ILookupStrategy) strategy).lookup(name, originatingContext);

		// if we found something, compute the concrete value and return
		if (result != null) {
			if (result instanceof IContextFunction) {
				ValueComputation valueComputation = new ValueComputation(this, originatingContext,
						name, ((IContextFunction) result));
				if (EclipseContext.DEBUG)
					System.out.println("created " + valueComputation);
				originatingContext.localValueComputations.put(lookupKey, valueComputation);
				result = valueComputation.get(arguments);
			}
			return result;
		}
		// 3. delegate to parent
		if (!local && parent != null) {
			return ((EclipseContext) parent)
					.internalGet(originatingContext, name, arguments, local); // XXX
			// IEclipseContext
		}
		return null;
	}

	protected void invalidate(String name, int eventType) {
		if (EclipseContext.DEBUG)
			System.out.println("invalidating " + get(IContextConstants.DEBUG_STRING) + ',' + name); //$NON-NLS-1$
		removeLocalValueComputations(name);
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
			invalidate(name, IRunAndTrack.REMOVED);
		}
	}

	/**
	 * Removes all local value computations associated with the given name.
	 * 
	 * @param name
	 *            The name to remove
	 */
	private void removeLocalValueComputations(String name) {
		synchronized (localValueComputations) {
			// remove all keys with a matching name
			for (Iterator it = localValueComputations.keySet().iterator(); it.hasNext();) {
				LookupKey key = (LookupKey) it.next();
				if (key.name.equals(name)) {
					Object removed = localValueComputations.get(key);
					if (removed instanceof ValueComputation) {
						((ValueComputation) removed).clear(this, name);
					}
					it.remove();
				}
			}
		}
	}

	public void runAndTrack(final IRunAndTrack runnable, Object[] args) {
		TrackableComputationExt computation = new TrackableComputationExt(runnable);
		schedule(computation, null, IRunAndTrack.INITIAL, args);
	}

	public void runAndTrack(final Runnable runnable) {
		TrackableComputation computation = new TrackableComputation(runnable);
		schedule(computation);
	}

	protected boolean schedule(IRunAndTrack runnable, String name, int eventType, Object[] args) {
		if (runnable == null)
			return false;
		if (eventType != IRunAndTrack.INITIAL && eventType != IRunAndTrack.DISPOSE
				&& strategy != null && strategy instanceof ISchedulerStrategy)
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
		boolean containsKey = localValues.containsKey(name);
		Object oldValue = localValues.put(name, value);
		if (!containsKey || value != oldValue) {
			invalidate(name, IRunAndTrack.ADDED);
		}
	}

	/**
	 * Returns a string representation of this context for debugging purposes only.
	 */
	public String toString() {
		Object debugString = localValues.get(IContextConstants.DEBUG_STRING);
		return debugString instanceof String ? ((String) debugString) : "Anonymous Context";
	}

	private void trackAccess(String name) {
		Computation computation = (Computation) currentComputation.get();
		if (computation != null) {
			computation.addDependency(this, name);
		}
	}
}
