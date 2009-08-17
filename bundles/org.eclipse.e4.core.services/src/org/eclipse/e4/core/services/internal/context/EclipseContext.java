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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.runtime.Assert;
import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.context.ContextChangeEvent;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IContextFunction;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.IRunAndTrack;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.core.services.context.spi.IEclipseContextStrategy;
import org.eclipse.e4.core.services.context.spi.ILookupStrategy;
import org.eclipse.e4.core.services.context.spi.ISchedulerStrategy;

/**
 * This implementation assumes that all contexts are of the class EclipseContext. The external
 * methods of it are exposed via IEclipseContext.
 */
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

		final protected void doHandleInvalid(ContextChangeEvent event) {
			if (event.getEventType() == ContextChangeEvent.DISPOSE) {
				return;
			}
			if (EclipseContext.DEBUG)
				System.out.println("scheduling " + this); //$NON-NLS-1$
			((EclipseContext) event.getContext()).schedule(this);
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

		protected void stopListening(IEclipseContext context, String name) {
			// trackable computation should always stop listening to
			// everything since dependencies are recomputed on each run
			removeAll();
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

		final protected void doHandleInvalid(ContextChangeEvent event) {
			((EclipseContext) event.getContext()).schedule(this, event);
		}

		public boolean notify(ContextChangeEvent event) {
			Computation oldComputation = (Computation) currentComputation.get();
			currentComputation.set(this);
			boolean result = true;
			try {
				result = runnable.notify(event);
			} finally {
				currentComputation.set(oldComputation);
			}
			startListening();
			return result;
		}

		public String toString() {
			return "TrackableComputationExt(" + runnable + ')'; //$NON-NLS-1$
		}
	}

	static class DebugSnap {
		Set listeners = new HashSet();
		Map localValueComputations = Collections.synchronizedMap(new HashMap());
		Map localValues = Collections.synchronizedMap(new HashMap());
	}

	static ThreadLocal currentComputation = new ThreadLocal();

	// TODO replace with variable on bundle-specific class
	public static boolean DEBUG = false;
	public static boolean DEBUG_VERBOSE = false;
	public static String DEBUG_VERBOSE_NAME = null;

	private DebugSnap snapshot;

	private static final Object[] NO_ARGUMENTS = new Object[0];

	final Set listeners = new HashSet();

	final Map localValueComputations = Collections.synchronizedMap(new HashMap());
	final Map localValues = Collections.synchronizedMap(new HashMap());

	private final IEclipseContextStrategy strategy;

	private ArrayList modifiable;

	public EclipseContext(IEclipseContext parent, IEclipseContextStrategy strategy) {
		this.strategy = strategy;
		set(IContextConstants.PARENT, parent);
	}

	public boolean containsKey(String name) {
		return containsKey(name, false);
	}

	public boolean containsKey(String name, boolean localOnly) {
		if (isSetLocally(name))
			return true;
		if (localOnly)
			return false;
		IEclipseContext parent = (IEclipseContext) getLocal(IContextConstants.PARENT);
		if (parent != null && parent.containsKey(name))
			return true;
		if (strategy instanceof ILookupStrategy) {
			if (((ILookupStrategy) strategy).containsKey(name, this))
				return true;
		}
		return false;
	}

	/**
	 * Remember a snapshot of this context state for debugging purposes.
	 */
	public void debugSnap() {
		snapshot = new DebugSnap();
		snapshot.listeners = new HashSet(listeners);
		snapshot.localValueComputations = new HashMap(localValueComputations);
		snapshot.localValues = new HashMap(localValues);
	}

	/**
	 * Print a diff between the current context state and the last snapshot state
	 */
	public void debugDiff() {
		if (snapshot == null)
			return;
		Set listenerDiff = new HashSet(listeners);
		listenerDiff.removeAll(snapshot.listeners);
		listenerDiff = new HashSet(listenerDiff);// shrink the set
		System.out.println("Listener diff: ");
		for (Iterator it = listenerDiff.iterator(); it.hasNext();) {
			System.out.println("\t" + it.next());
		}

		Set computationDiff = new HashSet(localValueComputations.values());
		computationDiff.removeAll(snapshot.localValueComputations.values());
		System.out.println("localValueComputations diff:");
		for (Iterator it = computationDiff.iterator(); it.hasNext();) {
			System.out.println("\t" + it.next());
		}

		Set valuesDiff = new HashSet(localValues.values());
		valuesDiff.removeAll(snapshot.localValues.values());
		System.out.println("localValues diff:");
		for (Iterator it = valuesDiff.iterator(); it.hasNext();) {
			System.out.println("\t" + it.next());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.core.services.context.IEclipseContext#dispose()
	 */
	public void dispose() {
		Computation[] ls = (Computation[]) listeners.toArray(new Computation[listeners.size()]);
		ContextChangeEvent event = EclipseContextFactory.createContextEvent(this,
				ContextChangeEvent.DISPOSE, null, null, null);
		for (int i = 0; i < ls.length; i++) {
			ls[i].handleInvalid(event);
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
		if (DEBUG_VERBOSE) {
			System.out.println("IEC.get(" + name + ", " + arguments + ", " + local + "):"
					+ originatingContext + " for " + toString());
		}
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
					System.out.println("created " + valueComputation); //$NON-NLS-1$
				originatingContext.localValueComputations.put(lookupKey, valueComputation);
				// value computation depends on parent if function is defined in a parent
				if (this != originatingContext)
					valueComputation.addDependency(originatingContext, IContextConstants.PARENT);
				result = valueComputation.get(arguments);
			}
			if (DEBUG_VERBOSE) {
				System.out.println("IEC.get(" + name + "): " + result);
			}
			return result;
		}
		// 3. delegate to parent
		if (!local) {
			IEclipseContext parent = (IEclipseContext) getLocal(IContextConstants.PARENT);
			if (parent != null) {
				return ((EclipseContext) parent).internalGet(originatingContext, name, arguments,
						local); // XXX
				// IEclipseContext
			}
		}
		return null;
	}

	protected void invalidate(String name, int eventType, Object oldValue) {
		if (EclipseContext.DEBUG)
			System.out.println("invalidating " + this + ',' + name); //$NON-NLS-1$
		removeLocalValueComputations(name);
		Computation[] ls = (Computation[]) listeners.toArray(new Computation[listeners.size()]);
		ContextChangeEvent event = EclipseContextFactory.createContextEvent(this, eventType, null,
				name, oldValue);
		for (int i = 0; i < ls.length; i++) {
			ls[i].handleInvalid(event);
		}
	}

	private boolean isSetLocally(String name) {
		trackAccess(name);
		return localValues.containsKey(name);
	}

	public void remove(String name) {
		if (isSetLocally(name)) {
			Object oldValue = localValues.remove(name);
			invalidate(name, ContextChangeEvent.REMOVED, oldValue);
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
		schedule(computation, EclipseContextFactory.createContextEvent(this,
				ContextChangeEvent.INITIAL, args, null, null));
	}

	public void runAndTrack(final Runnable runnable) {
		TrackableComputation computation = new TrackableComputation(runnable);
		schedule(computation);
	}

	protected boolean schedule(IRunAndTrack runnable, ContextChangeEvent event) {
		if (runnable == null)
			return false;
		int eventType = event.getEventType();
		if (eventType != ContextChangeEvent.INITIAL && eventType != ContextChangeEvent.DISPOSE
				&& strategy != null && strategy instanceof ISchedulerStrategy)
			return ((ISchedulerStrategy) strategy).schedule(runnable, event);
		return runnable.notify(event);
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
			if (DEBUG_VERBOSE) {
				System.out.println("IEC.set(" + name + "," + value + "):" + oldValue + " for "
						+ toString());
			}
			invalidate(name, ContextChangeEvent.ADDED, oldValue);
		}
	}

	public void modify(String name, Object value) {
		if (!internalModify(name, value))
			set(name, value);
	}

	public boolean internalModify(String name, Object value) {
		boolean containsKey = localValues.containsKey(name);
		if (containsKey) {
			if (!checkModifiable(name)) {
				String tmp = "Variable " + name + " is not modifiable in the context " + toString();
				throw new IllegalArgumentException(tmp);
			}
			Object oldValue = localValues.put(name, value);
			if (value != oldValue) {
				if (DEBUG_VERBOSE)
					System.out.println("IEC.set(" + name + "," + value + "):" + oldValue + " for "
							+ toString());
				invalidate(name, ContextChangeEvent.ADDED, oldValue);
			}
			return true;
		}

		EclipseContext parent = getParent();
		if (parent != null)
			return parent.internalModify(name, value);
		return false;
	}

	// TBD should this method be an API?
	public EclipseContext getParent() {
		return (EclipseContext) localValues.get(IContextConstants.PARENT);
	}

	/**
	 * Returns a string representation of this context for debugging purposes only.
	 */
	public String toString() {
		Object debugString = localValues.get(IContextConstants.DEBUG_STRING);
		return debugString instanceof String ? ((String) debugString) : "Anonymous Context"; //$NON-NLS-1$
	}

	private void trackAccess(String name) {
		Computation computation = (Computation) currentComputation.get();
		if (computation != null) {
			computation.addDependency(this, name);
		}
	}

	public void declareModifiable(String name) {
		if (name == null)
			return;
		if (modifiable == null)
			modifiable = new ArrayList(3);
		modifiable.add(name);
		if (localValues.containsKey(name))
			return;
		localValues.put(name, null);
	}

	private boolean checkModifiable(String name) {
		if (modifiable == null)
			return false;
		for (Iterator i = modifiable.iterator(); i.hasNext();) {
			String candidate = (String) i.next();
			if (candidate.equals(name))
				return true;
		}
		return false;
	}
}
