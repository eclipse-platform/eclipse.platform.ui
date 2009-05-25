/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 146397
 *******************************************************************************/

package org.eclipse.core.databinding.observable;

/**
 * @since 1.0
 *
 */
public abstract class ChangeSupport extends ChangeManager {

	/**
	 * @param realm 
	 */
	public ChangeSupport(Realm realm) {
		super(realm);
	}
	
	public void addListener(Object listenerType,
			IObservablesListener listener) {
		super.addListener(listenerType, listener);
	}
	
	public void removeListener(Object listenerType,
			IObservablesListener listener) {
		super.removeListener(listenerType, listener);
	}
	
	public void fireEvent(ObservableEvent event) {
		super.fireEvent(event);
	}
	
	/**
	 * 
	 */
	protected abstract void firstListenerAdded();
	
	/**
	 * 
	 */
	protected abstract void lastListenerRemoved();

	/**
	 * @param listener
	 */
	public void addChangeListener(IChangeListener listener) {
		addListener(ChangeEvent.TYPE, listener);
	}
	
	/**
	 * @param listener
	 */
	public void removeChangeListener(IChangeListener listener) {
		removeListener(ChangeEvent.TYPE, listener);
	}

	/**
	 * @param listener
	 */
	public void addStaleListener(IStaleListener listener) {
		addListener(StaleEvent.TYPE, listener);
	}
	
	/**
	 * @param listener
	 */
	public void removeStaleListener(IStaleListener listener) {
		removeListener(StaleEvent.TYPE, listener);
	}

	/**
	 * @param listener 
	 * @since 1.2
	 */
	public void addDisposeListener(IDisposeListener listener) {
		addListener(DisposeEvent.TYPE, listener);
	}

	/**
	 * @param listener
	 * @since 1.2
	 */
	public void removeDisposeListener(IDisposeListener listener) {
		removeListener(DisposeEvent.TYPE, listener);
	}
}
