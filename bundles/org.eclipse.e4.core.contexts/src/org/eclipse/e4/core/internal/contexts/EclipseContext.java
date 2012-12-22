/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.contexts;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.di.IInjector;
import org.eclipse.e4.core.internal.contexts.osgi.ContextDebugHelper;

/**
 * This implementation assumes that all contexts are of the class EclipseContext. The external
 * methods of it are exposed via IEclipseContext.
 */
public class EclipseContext implements IEclipseContext {

	/**
	 * A context key identifying the parent context.
	 */
	public static final String PARENT = "parentContext"; //$NON-NLS-1$

	/**
	 * A context key (value "debugString") identifying a value to use in debug statements for a
	 * context. A computed value can be used to embed more complex information in debug statements.
	 */
	public static final String DEBUG_STRING = "debugString"; //$NON-NLS-1$

	static class Scheduled {

		public TrackableComputationExt runnable;
		public ContextChangeEvent event;

		public Scheduled(TrackableComputationExt runnable, ContextChangeEvent event) {
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

	private WeakGroupedListenerList weakListeners = new WeakGroupedListenerList();
	private Map<String, ValueComputation> localValueComputations = Collections.synchronizedMap(new HashMap<String, ValueComputation>());

	final protected Map<String, Object> localValues = Collections.synchronizedMap(new HashMap<String, Object>());

	private ArrayList<String> modifiable;

	private List<Computation> waiting; // list of Computations; null for all non-root entries

	private Set<WeakReference<EclipseContext>> children = new HashSet<WeakReference<EclipseContext>>();

	private Set<IContextDisposalListener> notifyOnDisposal = new HashSet<IContextDisposalListener>();

	static private ThreadLocal<Stack<Computation>> currentComputation = new ThreadLocal<Stack<Computation>>();

	// I don't think we need to sync referenceQueue access
	private ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();

	private Map<Reference<?>, TrackableComputationExt> activeComputations = Collections.synchronizedMap(new HashMap<Reference<?>, TrackableComputationExt>());
	private Set<TrackableComputationExt> activeRATs = Collections.synchronizedSet(new HashSet<TrackableComputationExt>());

	private final static Object[] nullArgs = new Object[] {null};

	/**
	 * A context key (value "activeChildContext") that identifies another {@link IEclipseContext}
	 * that is a child of the context. The meaning of active is up to the application.
	 */
	public static final String ACTIVE_CHILD = "activeChildContext"; //$NON-NLS-1$

	static private final IEclipseContextDebugger debugAddOn = ContextDebugHelper.getDebugger();

	public EclipseContext(IEclipseContext parent) {
		setParent(parent);
		if (parent == null)
			waiting = Collections.synchronizedList(new ArrayList<Computation>());
		if (debugAddOn != null)
			debugAddOn.notify(this, IEclipseContextDebugger.EventType.CONSTRUCTED, null);
	}

	final static private Set<EclipseContext> noChildren = new HashSet<EclipseContext>(0);

	public Set<EclipseContext> getChildren() {
		Set<EclipseContext> result;
		synchronized (children) {
			if (children.size() == 0)
				return noChildren;
			result = new HashSet<EclipseContext>(children.size());
			for (Iterator<WeakReference<EclipseContext>> i = children.iterator(); i.hasNext();) {
				EclipseContext referredContext = i.next().get();
				if (referredContext == null) {
					i.remove();
					continue;
				}
				result.add(referredContext);
			}
		}
		return result;
	}

	public boolean containsKey(String name) {
		trackAccess(name);
		return containsKey(name, false);
	}

	public boolean containsKey(String name, boolean localOnly) {
		if (isSetLocally(name))
			return true;
		if (localOnly)
			return false;
		EclipseContext parent = getParent();
		if (parent != null && parent.containsKey(name, localOnly))
			return true;
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.core.services.context.IEclipseContext#dispose()
	 */
	public void dispose() {
		// dispose of child contexts first
		for (EclipseContext childContext : getChildren()) {
			childContext.dispose();
		}

		activeComputations.clear();
		activeRATs.clear();

		ContextChangeEvent event = new ContextChangeEvent(this, ContextChangeEvent.DISPOSE, null, null, null);
		Set<Scheduled> scheduled = new LinkedHashSet<Scheduled>();
		Set<Computation> allComputations = getListeners();
		weakListeners.clear();
		for (Computation computation : allComputations) {
			computation.handleInvalid(event, scheduled);
		}
		processScheduled(scheduled);

		synchronized (notifyOnDisposal) {
			for (IContextDisposalListener listener : notifyOnDisposal) {
				listener.disposed(this);
			}
		}

		for (ValueComputation computation : localValueComputations.values()) {
			computation.dipose();
		}
		localValueComputations.clear();

		// if this was the parent's active child, deactivate it
		EclipseContext parent = getParent();
		EclipseContext rootContext = null;
		if (parent != null) {
			rootContext = getRoot();
			if (this == parent.getActiveChild())
				parent.set(ACTIVE_CHILD, null);
		}

		localValues.clear();

		if (parent != null) {
			parent.removeChild(this);
			if (rootContext != null)
				rootContext.cleanup();
		}

		if (debugAddOn != null)
			debugAddOn.notify(this, IEclipseContextDebugger.EventType.DISPOSED, null);
	}

	public Object get(String name) {
		trackAccess(name);
		return internalGet(this, name, false);
	}

	public Object getLocal(String name) {
		trackAccess(name);
		return internalGet(this, name, true);
	}

	public Object internalGet(EclipseContext originatingContext, String name, boolean local) {
		if (this == originatingContext) {
			ValueComputation valueComputation = localValueComputations.get(name);
			if (valueComputation != null)
				return valueComputation.get();
		}

		Object result = null;
		// 1. try for local value
		if (localValues.containsKey(name)) {
			result = localValues.get(name);
			if (result == null)
				return null;
		} else
			result = lookup(name, originatingContext);

		// if we found something, compute the concrete value and return
		if (result != null) {
			if (result instanceof IContextFunction) {
				ValueComputation valueComputation = new ValueComputation(name, originatingContext, ((IContextFunction) result));
				// do calculations before adding listeners
				result = valueComputation.get();
				originatingContext.localValueComputations.put(name, valueComputation);
			}
			return result;
		}
		// 3. delegate to parent
		if (!local) {
			IEclipseContext parent = (IEclipseContext) localValues.get(PARENT);
			if (parent != null) {
				return ((EclipseContext) parent).internalGet(originatingContext, name, local);
			}
		}
		return null;
	}

	/**
	 * The given name has been modified or removed in this context. Invalidate all local value
	 * computations and listeners that depend on this name.
	 */
	public void invalidate(String name, int eventType, Object oldValue, Object newValue, Set<Scheduled> scheduled) {
		ContextChangeEvent event = null;
		ValueComputation computation = localValueComputations.get(name);
		if (computation != null) {
			event = new ContextChangeEvent(this, eventType, null, name, oldValue);
			if (computation.shouldRemove(event)) {
				localValueComputations.remove(name);
				weakListeners.remove(computation);
			}
			computation.handleInvalid(event, scheduled);
		}
		Set<Computation> namedComputations = weakListeners.getListeners(name);
		if (namedComputations != null && namedComputations.size() > 0) {
			if (event == null) {
				event = new ContextChangeEvent(this, eventType, null, name, oldValue);
			}
			for (Computation listener : namedComputations) {
				listener.handleInvalid(event, scheduled);
			}
		}

		// invalidate this name in child contexts
		for (EclipseContext childContext : getChildren()) {
			// unless it is already set to the same value
			if (eventType == ContextChangeEvent.ADDED && childContext.isLocalEquals(name, newValue))
				continue;
			childContext.invalidate(name, eventType, oldValue, newValue, scheduled);
		}
	}

	protected boolean isLocalEquals(String name, Object newValue) {
		if (!localValues.containsKey(name))
			return false;
		return (localValues.get(name) == newValue);
	}

	private boolean isSetLocally(String name) {
		return localValues.containsKey(name);
	}

	public void remove(String name) {
		if (isSetLocally(name)) {
			Object oldValue = localValues.remove(name);
			Set<Scheduled> scheduled = new LinkedHashSet<Scheduled>();
			invalidate(name, ContextChangeEvent.REMOVED, oldValue, IInjector.NOT_A_VALUE, scheduled);
			processScheduled(scheduled);
		}
	}

	public void runAndTrack(final RunAndTrack runnable) {
		TrackableComputationExt computation = new TrackableComputationExt(runnable, this);
		ContextChangeEvent event = new ContextChangeEvent(this, ContextChangeEvent.INITIAL, null, null, null);
		boolean result = computation.update(event);
		if (result) {
			Reference<Object> ref = computation.getReference();
			if (ref != null)
				activeComputations.put(ref, computation);
			else
				activeRATs.add(computation);
		}
	}

	public void removeRAT(Computation computation) {
		// remove from listeners
		weakListeners.remove(computation);
		activeRATs.remove(computation);
	}

	protected void processScheduled(Set<Scheduled> scheduledList) {
		for (Iterator<Scheduled> i = scheduledList.iterator(); i.hasNext();) {
			Scheduled scheduled = i.next();
			scheduled.runnable.update(scheduled.event);
		}
	}

	public void set(String name, Object value) {
		if (PARENT.equals(name)) {
			setParent((IEclipseContext) value);
			return;
		}
		boolean containsKey = localValues.containsKey(name);
		Object oldValue = localValues.put(name, value);
		if (!containsKey || value != oldValue) {
			Set<Scheduled> scheduled = new LinkedHashSet<Scheduled>();
			invalidate(name, ContextChangeEvent.ADDED, oldValue, value, scheduled);
			processScheduled(scheduled);
		}

		// cleanup unused computation listeners
		Reference<?> ref = referenceQueue.poll();
		if (ref != null) {
			ContextChangeEvent event = new ContextChangeEvent(this, ContextChangeEvent.UNINJECTED, nullArgs, null, null);
			for (; ref != null; ref = referenceQueue.poll()) {
				TrackableComputationExt obsoleteComputation = activeComputations.remove(ref);
				if (obsoleteComputation == null)
					continue;
				obsoleteComputation.update(event);
			}
		}
	}

	public void modify(String name, Object value) {
		Set<Scheduled> scheduled = new LinkedHashSet<Scheduled>();
		if (!internalModify(name, value, scheduled))
			set(name, value);
		processScheduled(scheduled);
	}

	public boolean internalModify(String name, Object value, Set<Scheduled> scheduled) {
		boolean containsKey = localValues.containsKey(name);
		if (containsKey) {
			if (!checkModifiable(name)) {
				String tmp = "Variable " + name + " is not modifiable in the context " + toString(); //$NON-NLS-1$ //$NON-NLS-2$
				throw new IllegalArgumentException(tmp);
			}
			Object oldValue = localValues.put(name, value);
			if (value != oldValue)
				invalidate(name, ContextChangeEvent.ADDED, oldValue, value, scheduled);
			return true;
		}

		EclipseContext parent = getParent();
		if (parent != null)
			return parent.internalModify(name, value, scheduled);
		return false;
	}

	public EclipseContext getParent() {
		return (EclipseContext) localValues.get(PARENT);
	}

	public void setParent(IEclipseContext parent) {
		EclipseContext parentContext = (EclipseContext) localValues.get(PARENT);
		if (parent == parentContext)
			return; // no-op
		if (parentContext != null)
			parentContext.removeChild(this);
		Set<Scheduled> scheduled = new LinkedHashSet<Scheduled>();
		handleReparent((EclipseContext) parent, scheduled);
		localValues.put(PARENT, parent);
		if (parent != null)
			((EclipseContext) parent).addChild(this);
		processScheduled(scheduled);
		return;
	}

	/**
	 * Returns a string representation of this context for debugging purposes only.
	 */
	public String toString() {
		Object debugString = localValues.get(DEBUG_STRING);
		return debugString instanceof String ? ((String) debugString) : "Anonymous Context"; //$NON-NLS-1$
	}

	private void trackAccess(String name) {
		Stack<Computation> current = getCalculatedComputations();
		if (current.isEmpty())
			return;
		Computation computation = current.peek(); // only track in the top-most one
		if (computation == null)
			return;
		addDependency(name, computation);
	}

	public void addDependency(String name, Computation computation) {
		weakListeners.add(name, computation);
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
		ContextChangeEvent event = new ContextChangeEvent(this, ContextChangeEvent.UNINJECTED, new Object[] {object}, null, null);
		Set<Computation> comps = getListeners();
		for (Computation computation : comps) {
			if (computation instanceof TrackableComputationExt)
				((TrackableComputationExt) computation).update(event);
		}
	}

	public Set<Computation> getListeners() {
		return weakListeners.getListeners();
	}

	private void handleReparent(EclipseContext newParent, Set<Scheduled> scheduled) {
		// TBD should we lock waiting list while doing reparent?
		// Add "boolean inReparent" on the root context and process right away?
		processWaiting();
		// 1) everybody who depends on me: I need to collect combined list of names injected
		Set<String> usedNames = new HashSet<String>();
		collectDependentNames(usedNames);

		// 2) for each used name:
		for (Iterator<String> i = usedNames.iterator(); i.hasNext();) {
			String name = i.next();
			if (localValues.containsKey(name))
				continue; // it is a local value
			Object oldValue = get(name);
			Object newValue = (newParent != null) ? newParent.get(name) : null;
			if (oldValue != newValue)
				invalidate(name, ContextChangeEvent.ADDED, oldValue, newValue, scheduled);
		}

		ContextChangeEvent event = new ContextChangeEvent(this, ContextChangeEvent.ADDED, null, null, null);
		for (Computation computation : localValueComputations.values()) {
			weakListeners.remove(computation);
			computation.handleInvalid(event, scheduled);
		}
		localValueComputations.clear();
	}

	private void collectDependentNames(Set<String> usedNames) {
		Set<String> names = weakListeners.getNames();
		usedNames.addAll(names);
		for (EclipseContext childContext : getChildren()) {
			childContext.collectDependentNames(usedNames);
		}
	}

	public void processWaiting() {
		// traverse to the root node
		EclipseContext parent = getParent();
		if (parent != null) {
			parent.processWaiting();
			return;
		}
		if (waiting == null || waiting.isEmpty())
			return;
		// create update notifications
		Computation[] ls = waiting.toArray(new Computation[waiting.size()]);
		waiting.clear();
		ContextChangeEvent event = new ContextChangeEvent(this, ContextChangeEvent.UPDATE, null, null, null);
		for (int i = 0; i < ls.length; i++) {
			if (ls[i] instanceof TrackableComputationExt)
				((TrackableComputationExt) ls[i]).update(event);
		}
	}

	public void addWaiting(Computation cp) {
		// traverse to the root node
		EclipseContext parent = getParent();
		if (parent != null) {
			parent.addWaiting(cp);
			return;
		}
		if (waiting == null) // could happen on re-parent
			waiting = Collections.synchronizedList(new ArrayList<Computation>());
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

	public void addChild(EclipseContext childContext) {
		synchronized (children) {
			children.add(new WeakReference<EclipseContext>(childContext));
		}
	}

	public void removeChild(EclipseContext childContext) {
		synchronized (children) {
			for (Iterator<WeakReference<EclipseContext>> i = children.iterator(); i.hasNext();) {
				EclipseContext referredContext = i.next().get();
				if (referredContext == null) {
					i.remove();
					continue;
				}
				if (referredContext == childContext) {
					i.remove();
					return;
				}
			}
		}
	}

	public <T> T get(Class<T> clazz) {
		return clazz.cast(get(clazz.getName()));
	}

	public boolean containsKey(Class<?> clazz) {
		return containsKey(clazz.getName());
	}

	public <T> void set(Class<T> clazz, T value) {
		set(clazz.getName(), value);
	}

	public void remove(Class<?> clazz) {
		remove(clazz.getName());
	}

	public <T> T getLocal(Class<T> clazz) {
		return clazz.cast(getLocal(clazz.getName()));
	}

	public <T> void modify(Class<T> clazz, T value) {
		modify(clazz.getName(), value);
	}

	public void declareModifiable(Class<?> clazz) {
		declareModifiable(clazz.getName());
	}

	public IEclipseContext createChild() {
		return new EclipseContext(this); // strategies are not inherited
	}

	public IEclipseContext createChild(String name) {
		IEclipseContext result = createChild();
		result.set(DEBUG_STRING, name);
		return result;
	}

	public void notifyOnDisposal(IContextDisposalListener listener) {
		synchronized (notifyOnDisposal) {
			notifyOnDisposal.add(listener);
		}
	}

	public IEclipseContext getActiveChild() {
		trackAccess(ACTIVE_CHILD);
		return (EclipseContext) internalGet(this, ACTIVE_CHILD, true);
	}

	public IEclipseContext getActiveLeaf() {
		IEclipseContext activeContext = this;
		IEclipseContext child = getActiveChild();
		while (child != null) {
			activeContext = child;
			child = child.getActiveChild();
		}
		return activeContext;
	}

	public void activate() {
		EclipseContext parent = getParent();
		if (parent == null)
			return;
		if (this == parent.getActiveChild())
			return;
		parent.set(ACTIVE_CHILD, this);
	}

	public void activateBranch() {
		for (IEclipseContext i = this; i != null; i = i.getParent()) {
			i.activate();
		}
	}

	public void deactivate() {
		EclipseContext parent = getParent();
		if (parent == null)
			return;
		if (this != parent.getActiveChild())
			return; // this is not an active context; return 
		parent.set(ACTIVE_CHILD, null);
	}

	// This method is for debug only, do not use externally
	public Map<String, Object> localData() {
		Map<String, Object> result = new HashMap<String, Object>(localValues.size());
		for (String string : localValues.keySet()) {
			Object value = localValues.get(string);
			if (value instanceof IContextFunction)
				continue;
			result.put(string, value);
		}
		return result;
	}

	// This method is for debug only, do not use externally
	public Map<String, Object> localContextFunction() {
		Map<String, Object> result = new HashMap<String, Object>(localValues.size());
		for (String string : localValues.keySet()) {
			Object value = localValues.get(string);
			if (value instanceof IContextFunction)
				result.put(string, value);
		}
		return result;
	}

	// This method is for debug only, do not use externally
	public Map<String, Object> cachedCachedContextFunctions() {
		Map<String, Object> result = new HashMap<String, Object>(localValueComputations.size());
		for (String string : localValueComputations.keySet()) {
			ValueComputation vc = localValueComputations.get(string);
			if (vc != null)
				result.put(string, vc.get());
		}
		return result;
	}

	// This method is for debug only, do not use externally
	public Set<String> getRawListenerNames() {
		return weakListeners.getNames();
	}

	// This method is for debug only, do not use externally
	public Set<Computation> getListeners(String name) {
		return weakListeners.getListeners(name);
	}

	static public Stack<Computation> getCalculatedComputations() {
		Stack<Computation> current = currentComputation.get();
		if (current == null) {
			current = new Stack<Computation>();
			currentComputation.set(current);
		}
		return current;
	}

	public void pushComputation(Computation comp) {
		Stack<Computation> current = getCalculatedComputations();
		current.push(comp);
	}

	public void popComputation(Computation comp) {
		Stack<Computation> current = getCalculatedComputations();
		Computation ended = current.pop();
		if (ended != comp)
			throw new IllegalArgumentException("Internal error: Invalid nested computation processing"); //$NON-NLS-1$
	}

	/**
	 * This method can be overriden to provide additional source for the requested data.
	 * The override is expected to take care of initiating dynamic updates.
	 */
	protected Object lookup(String name, EclipseContext originatingContext) {
		return null;
	}

	/**
	 * Prefix used to distinguish active variables
	 */
	static private final String ACTIVE_VARIABLE = "org.eclipse.ui.active_"; //$NON-NLS-1$

	public <T> T getActive(Class<T> clazz) {
		return clazz.cast(getActive(clazz.getName()));
	}

	public Object getActive(final String name) {
		final String internalName = ACTIVE_VARIABLE + name;
		if (containsKey(internalName, true)) {
			trackAccess(internalName);
			return internalGet(this, internalName, true);
		}

		final EclipseContext originatingContext = this;

		runAndTrack(new RunAndTrack() {
			public boolean changed(IEclipseContext context) {
				IEclipseContext activeContext = getActiveLeaf();
				Object result = activeContext.get(name);
				originatingContext.set(internalName, result);
				return true;
			}
		});
		trackAccess(internalName);
		return internalGet(this, internalName, true);
	}

	public WeakReference<Object> trackedWeakReference(Object object) {
		return new WeakReference<Object>(object, referenceQueue);
	}

	public void cleanup() {
		for (EclipseContext childContext : getChildren()) {
			childContext.cleanup();
		}
		weakListeners.cleanup();
	}
}
