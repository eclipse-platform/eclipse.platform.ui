/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.synchronize;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.ui.IMemento;

/**
 * Abstract superclass of resource scopes for <code>SubscriberParticipant</code>
 * instances.
 * <p>
 * Clients are not expected to subclass this class.
 * </p>
 * @see SubscriberParticipant
 * @since 3.0
 */
public abstract class AbstractSynchronizeScope implements ISynchronizeScope {
	
	/*
	 * Key for scope in memento
	 */
	private static final String CTX_SUBSCRIBER_SCOPE_TYPE = TeamUIPlugin.ID + ".SCOPE_TYPE"; //$NON-NLS-1$
	
	/*
	 * Scope change listeners
	 */
	private ListenerList listeners = new ListenerList();
	
	/**
	 * Save the scope to the given memento
	 * 
	 * @param scope a scope
	 * @param settings a memento
	 */
	protected static void saveScope(ISynchronizeScope scope, IMemento settings) {
		settings.putString(CTX_SUBSCRIBER_SCOPE_TYPE, getType(scope));
		((AbstractSynchronizeScope)scope).saveState(settings);
	}
	
	/**
	 * Restore a scope from the given memento
	 * 
	 * @param settings a memento
	 * @return the scope restored from the given memento
	 */
	protected static ISynchronizeScope createScope(IMemento settings) {
		String type = settings.getString(CTX_SUBSCRIBER_SCOPE_TYPE);
		if (type == null) {
			return new WorkspaceScope();
		}
		if (type.equals("ResourceScope")) { //$NON-NLS-1$
			return new ResourceScope(settings);
		}
		if (type.equals("WorkingSetScope")) { //$NON-NLS-1$
			return new WorkingSetScope(settings);
		}
		return new WorkspaceScope();
	}
	
	private static String getType(ISynchronizeScope scope) {
		String name = scope.getClass().getName();
		int lastDot = name.lastIndexOf("."); //$NON-NLS-1$
		if (lastDot == -1) {
			return name;
		}
		return name.substring(lastDot + 1); 
	}
	
	/**
	 * Constructor a scope from scratch
	 */
	protected AbstractSynchronizeScope() {
	}
	
	/**
	 * Constructor a scope from a previously saved state
	 */
	protected AbstractSynchronizeScope(IMemento memento) {
		init(memento);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.ScopableSubscriberParticipant.ISynchronizeScope#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		synchronized(listeners) {
			listeners.add(listener);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.ScopableSubscriberParticipant.ISynchronizeScope#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		synchronized(listeners) {
			listeners.remove(listeners);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizeScope#dispose()
	 */
	public void dispose() {
		// Do nothing by default
	}
	
	/**
	 * Fires the given property change event to all registered listeners.
	 * 
	 * @param event the property change event to be fired
	 */
	protected void firePropertyChangedEvent(final PropertyChangeEvent event) {
		Object[] allListeners;
		synchronized(listeners) {
			allListeners = listeners.getListeners();
		}
		for (int i = 0; i < allListeners.length; i++) {
			final IPropertyChangeListener listener = (IPropertyChangeListener)allListeners[i];
			Platform.run(new SafeRunnable() {
				public void run() throws Exception {
					listener.propertyChange(event);
				}
			});
		}
	}
	/**
	 * Fires a change event for property <code>ISynchronizeScope.ROOTS</code> 
	 * containing the new roots. The old roots are not provided in the event.
	 */
	protected void fireRootsChanges() {
		firePropertyChangedEvent(new PropertyChangeEvent(this, ROOTS, new IResource[0], getRoots()));
	}
	
	/**
	 * Persist the state of this scope. Clients must persist enough additional
	 * state to know what type (i.e. subclass) of scope to be recreated.
	 * 
	 * @param memento the memento into which the scope is to be saved
	 */
	public void saveState(IMemento memento) {
		// Do nothing by default
	}
	
	/**
	 * Method invoked from the constructor which repopulates the fields of this scope
	 * 
	 * @param memento the memento into which the scope was previously saved
	 */
	protected void init(IMemento memento) {
		// Do nothing by default
	}
}
