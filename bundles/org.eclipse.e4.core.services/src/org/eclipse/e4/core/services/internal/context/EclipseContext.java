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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

	static class TrackableComputationExt extends Computation implements IRunAndTrack {

		private ContextChangeEvent cachedEvent;

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

		public Object getObject() {
			if (runnable instanceof IRunAndTrackObject)
				return ((IRunAndTrackObject) runnable).getObject();
			return null;
		}

		final protected void doHandleInvalid(ContextChangeEvent event, List<Scheduled> scheduledList) {
			int eventType = event.getEventType();
			if (eventType == ContextChangeEvent.INITIAL || eventType == ContextChangeEvent.DISPOSE) {
				// process right away
				notify(event);
			} else {
				// schedule processing
				Scheduled toBeScheduled = new Scheduled(this, event);
				for (Iterator<Scheduled> i = scheduledList.iterator(); i.hasNext();) {
					Scheduled scheduled = i.next();
					if (scheduled.equals(toBeScheduled)) // eliminate duplicates
						return;
				}
				scheduledList.add(toBeScheduled);
			}
		}

		public boolean notify(ContextChangeEvent event) {
			// is this a structural event?
			// structural changes: INITIAL, DISPOSE, UNINJECTED are always processed right away
			int eventType = event.getEventType();
			if ((runnable instanceof IRunAndTrackObject)
					&& ((IRunAndTrackObject) runnable).batchProcess()) {
				if ((eventType == ContextChangeEvent.ADDED)
						|| (eventType == ContextChangeEvent.REMOVED)) {
					cachedEvent = event;
					EclipseContext eventsContext = (EclipseContext) ((ObjectProviderContext) event
							.getContext()).getContext();
					eventsContext.addWaiting(this);
					// eventsContext.getRoot().waiting.add(this);
					return true;
				}
			}
			Computation oldComputation = currentComputation.get();
			currentComputation.set(this);
			boolean result = true;
			try {
				if (cachedEvent != null) {
					result = runnable.notify(cachedEvent);
					cachedEvent = null;
				}
				if (eventType != ContextChangeEvent.UPDATE)
					result = runnable.notify(event);
			} finally {
				currentComputation.set(oldComputation);
			}
			EclipseContext eventsContext = (EclipseContext) ((ObjectProviderContext) event
					.getContext()).getContext();
			if (result)
				startListening(eventsContext);
			else
				removeAll(eventsContext);
			return result;
		}

		public String toString() {
			return "TrackableComputationExt(" + runnable + ')'; //$NON-NLS-1$
		}
	}

	static class Scheduled {

		public IRunAndTrack runnable;
		public ContextChangeEvent event;

		public Scheduled(IRunAndTrack runnable, ContextChangeEvent event) {
			this.runnable = runnable;
			this.event = event;
		}

		public int hashCode() {
			return 31 * (31 + event.hashCode()) + runnable.hashCode();
		}

		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Scheduled other = (Scheduled) obj;
			if (!event.equals(other.event))
				return false;
			return runnable.equals(other.runnable);
		}
	}

	static class DebugSnap {
		Set<Computation> listeners = new HashSet<Computation>();
		Map<LookupKey, ValueComputation> localValueComputations = Collections
				.synchronizedMap(new HashMap<LookupKey, ValueComputation>());
		Map<String, Object> localValues = Collections
				.synchronizedMap(new HashMap<String, Object>());
	}

	static ThreadLocal<Computation> currentComputation = new ThreadLocal<Computation>();

	// TODO replace with variable on bundle-specific class
	public static boolean DEBUG = false;
	public static boolean DEBUG_VERBOSE = false;
	public static String DEBUG_VERBOSE_NAME = null;

	private DebugSnap snapshot;

	private static final Object[] NO_ARGUMENTS = new Object[0];

	final Set<Computation> listeners = Collections
			.synchronizedSet(new LinkedHashSet<Computation>());

	final Map<LookupKey, ValueComputation> localValueComputations = Collections
			.synchronizedMap(new HashMap<LookupKey, ValueComputation>());
	final Map<String, Object> localValues = Collections
			.synchronizedMap(new HashMap<String, Object>());

	private final IEclipseContextStrategy strategy;

	private ArrayList<String> modifiable;

	private ArrayList<Computation> waiting; // list of Computations; null for all non-root entries

	public EclipseContext(IEclipseContext parent, IEclipseContextStrategy strategy) {
		this.strategy = strategy;
		set(IContextConstants.PARENT, parent);
		if (parent == null)
			waiting = new ArrayList<Computation>();
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
		snapshot.listeners = new HashSet<Computation>(listeners);
		snapshot.localValueComputations = new HashMap<LookupKey, ValueComputation>(
				localValueComputations);
		snapshot.localValues = new HashMap<String, Object>(localValues);
	}

	/**
	 * Print a diff between the current context state and the last snapshot state
	 */
	public void debugDiff() {
		if (snapshot == null)
			return;
		Set<Computation> listenerDiff = new HashSet<Computation>(listeners);
		listenerDiff.removeAll(snapshot.listeners);
		listenerDiff = new HashSet<Computation>(listenerDiff);// shrink the set
		System.out.println("Listener diff: "); //$NON-NLS-1$
		for (Iterator<Computation> it = listenerDiff.iterator(); it.hasNext();) {
			System.out.println("\t" + it.next()); //$NON-NLS-1$
		}

		Set<ValueComputation> computationDiff = new HashSet<ValueComputation>(
				localValueComputations.values());
		computationDiff.removeAll(snapshot.localValueComputations.values());
		System.out.println("localValueComputations diff:"); //$NON-NLS-1$
		for (Iterator<ValueComputation> it = computationDiff.iterator(); it.hasNext();) {
			System.out.println("\t" + it.next()); //$NON-NLS-1$
		}

		Set<Object> valuesDiff = new HashSet<Object>(localValues.values());
		valuesDiff.removeAll(snapshot.localValues.values());
		System.out.println("localValues diff:"); //$NON-NLS-1$
		for (Iterator<Object> it = valuesDiff.iterator(); it.hasNext();) {
			System.out.println("\t" + it.next()); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.core.services.context.IEclipseContext#dispose()
	 */
	public void dispose() {
		Computation[] ls = listeners.toArray(new Computation[listeners.size()]);
		ContextChangeEvent event = EclipseContextFactory.createContextEvent(this,
				ContextChangeEvent.DISPOSE, null, null, null);
		// reverse order of listeners
		for (int i = ls.length - 1; i >= 0; i--) {
			List<Scheduled> scheduled = new ArrayList<Scheduled>();
			ls[i].handleInvalid(event, scheduled);
			processScheduled(scheduled);
		}

		// TBD used by OSGI Context strategy - is this needed? Looks like @PreDestroy
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

	public Object internalGet(EclipseContext originatingContext, String name, Object[] arguments,
			boolean local) {
		trackAccess(name);
		if (DEBUG_VERBOSE) {
			System.out.println("IEC.get(" + name + ", " + arguments + ", " + local + "):" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					+ originatingContext + " for " + toString()); //$NON-NLS-1$
		}
		LookupKey lookupKey = null;
		if (this == originatingContext) {
			lookupKey = new LookupKey(name, arguments);
			ValueComputation valueComputation = localValueComputations.get(lookupKey);
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
				if (lookupKey == null)
					lookupKey = new LookupKey(name, arguments);
				originatingContext.localValueComputations.put(lookupKey, valueComputation);
				// value computation depends on parent if function is defined in a parent
				if (this != originatingContext)
					valueComputation.addDependency(originatingContext, IContextConstants.PARENT);
				result = valueComputation.get(arguments);
			}
			if (DEBUG_VERBOSE) {
				System.out.println("IEC.get(" + name + "): " + result); //$NON-NLS-1$ //$NON-NLS-2$
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

	protected void invalidate(String name, int eventType, Object oldValue, List<Scheduled> scheduled) {
		if (EclipseContext.DEBUG)
			System.out.println("invalidating " + this + ',' + name); //$NON-NLS-1$
		removeLocalValueComputations(name);
		Computation[] ls = listeners.toArray(new Computation[listeners.size()]);
		ContextChangeEvent event = EclipseContextFactory.createContextEvent(this, eventType, null,
				name, oldValue);
		for (int i = 0; i < ls.length; i++) {
			ls[i].handleInvalid(event, scheduled);
		}
	}

	private boolean isSetLocally(String name) {
		trackAccess(name);
		return localValues.containsKey(name);
	}

	public void remove(String name) {
		if (isSetLocally(name)) {
			Object oldValue = localValues.remove(name);
			List<Scheduled> scheduled = new ArrayList<Scheduled>();
			invalidate(name, ContextChangeEvent.REMOVED, oldValue, scheduled);
			processScheduled(scheduled);
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
			for (Iterator<LookupKey> it = localValueComputations.keySet().iterator(); it.hasNext();) {
				LookupKey key = it.next();
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
		ContextChangeEvent event = EclipseContextFactory.createContextEvent(this,
				ContextChangeEvent.INITIAL, args, null, null);
		TrackableComputationExt computation = new TrackableComputationExt(runnable);
		computation.notify(event);
	}

	protected void processScheduled(List<Scheduled> scheduledList) {
		boolean useScheduler = (strategy != null && strategy instanceof ISchedulerStrategy);
		for (Iterator<Scheduled> i = scheduledList.iterator(); i.hasNext();) {
			Scheduled scheduled = i.next();
			if (useScheduler)
				((ISchedulerStrategy) strategy).schedule(scheduled.runnable, scheduled.event);
			else
				scheduled.runnable.notify(scheduled.event);
		}
	}

	public void set(String name, Object value) {
		if (IContextConstants.PARENT.equals(name)) {
			// TBD make setting parent a separate operation
			List<Scheduled> scheduled = new ArrayList<Scheduled>();
			handleReparent((EclipseContext) value, scheduled);
			localValues.put(IContextConstants.PARENT, value);
			processScheduled(scheduled);
			return;
		}
		boolean containsKey = localValues.containsKey(name);
		Object oldValue = localValues.put(name, value);
		if (!containsKey || value != oldValue) {
			if (DEBUG_VERBOSE) {
				System.out.println("IEC.set(" + name + ',' + value + "):" + oldValue + " for " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						+ toString());
			}
			List<Scheduled> scheduled = new ArrayList<Scheduled>();
			invalidate(name, ContextChangeEvent.ADDED, oldValue, scheduled);
			processScheduled(scheduled);
		}
	}

	public void modify(String name, Object value) {
		List<Scheduled> scheduled = new ArrayList<Scheduled>();
		if (!internalModify(name, value, scheduled))
			set(name, value);
		processScheduled(scheduled);
	}

	public boolean internalModify(String name, Object value, List<Scheduled> scheduled) {
		boolean containsKey = localValues.containsKey(name);
		if (containsKey) {
			if (!checkModifiable(name)) {
				String tmp = "Variable " + name + " is not modifiable in the context " + toString(); //$NON-NLS-1$ //$NON-NLS-2$
				throw new IllegalArgumentException(tmp);
			}
			Object oldValue = localValues.put(name, value);
			if (value != oldValue) {
				if (DEBUG_VERBOSE)
					System.out.println("IEC.set(" + name + ',' + value + "):" + oldValue + " for " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							+ toString());
				invalidate(name, ContextChangeEvent.ADDED, oldValue, scheduled);
			}
			return true;
		}

		EclipseContext parent = getParent();
		if (parent != null)
			return parent.internalModify(name, value, scheduled);
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
		Computation computation = currentComputation.get();
		if (computation != null) {
			computation.addDependency(this, name);
		}
	}

	public void declareModifiable(String name) {
		if (name == null)
			return;
		if (modifiable == null)
			modifiable = new ArrayList<String>(3);
		modifiable.add(name);
		if (localValues.containsKey(name))
			return;
		localValues.put(name, null);
	}

	private boolean checkModifiable(String name) {
		if (modifiable == null)
			return false;
		for (Iterator<String> i = modifiable.iterator(); i.hasNext();) {
			String candidate = i.next();
			if (candidate.equals(name))
				return true;
		}
		return false;
	}

	public void removeListenersTo(Object object) {
		if (object == null)
			return;
		synchronized (listeners) {
			ContextChangeEvent event = EclipseContextFactory.createContextEvent(this,
					ContextChangeEvent.UNINJECTED, null, null, null);
			for (Iterator<Computation> i = listeners.iterator(); i.hasNext();) {
				Computation computation = i.next();
				if (computation instanceof TrackableComputationExt) {
					if (object == ((TrackableComputationExt) computation).getObject()) {
						((IRunAndTrack) computation).notify(event);
						i.remove();
					}
				}
			}
		}
	}

	private void handleReparent(EclipseContext newParent, List<Scheduled> scheduled) {
		// TBD should we lock waiting list while doing reparent?
		// Add "boolean inReparent" on the root context and process right away?
		processWaiting();
		// 1) everybody who depends on me: I need to collect combined list of names injected
		Computation[] ls = listeners.toArray(new Computation[listeners.size()]);
		Set<String> usedNames = new HashSet<String>();
		for (int i = 0; i < ls.length; i++) {
			Set<String> listenerNames = ls[i].dependsOnNames(this);
			if (listenerNames == null)
				continue; // should not happen?
			usedNames.addAll(listenerNames); // also removes duplicates
		}
		// 2) for each used name:
		for (Iterator<String> i = usedNames.iterator(); i.hasNext();) {
			String name = i.next();
			if (localValues.containsKey(name))
				continue; // it is a local value
			Object oldValue = get(name);
			Object newValue = (newParent != null) ? newParent.get(name) : null;
			if (oldValue != newValue)
				invalidate(name, ContextChangeEvent.ADDED, oldValue, scheduled);
		}
		localValueComputations.clear();
	}

	public void processWaiting() {
		// traverse to the root node
		EclipseContext parent = getParent();
		if (parent != null) {
			parent.processWaiting();
			return;
		}
		if (waiting == null)
			return;
		// create update notifications
		Computation[] ls = waiting.toArray(new Computation[waiting.size()]);
		waiting.clear();
		ContextChangeEvent event = EclipseContextFactory.createContextEvent(this,
				ContextChangeEvent.UPDATE, null, null, null);
		for (int i = 0; i < ls.length; i++) {
			if (ls[i] instanceof TrackableComputationExt)
				((TrackableComputationExt) ls[i]).notify(event);
		}
	}

	public void addWaiting(Computation cp) {
		// traverse to the root node
		EclipseContext parent = getParent();
		if (parent != null) {
			parent.addWaiting(cp);
			return;
		}
		if (waiting == null)
			waiting = new ArrayList<Computation>();
		waiting.add(cp);
	}

	protected EclipseContext getRoot() {
		EclipseContext current = this;
		EclipseContext root;
		do {
			root = current;
			current = current.getParent();
		} while (current != null);
		return root;
	}

}
