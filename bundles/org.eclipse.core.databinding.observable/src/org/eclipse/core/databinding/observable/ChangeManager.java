/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bugs 118516, 255734
 *     Chris Audley - bug 273265
 *******************************************************************************/

package org.eclipse.core.databinding.observable;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;

/**
 * Listener management implementation. Exposed to subclasses in form of
 * {@link AbstractObservable} and {@link ChangeSupport}.
 * 
 * @since 1.0
 * 
 */
/* package */class ChangeManager {

	ListenerList[] listenerLists = null;
	Object listenerTypes[] = null;
	private final Realm realm;

	/**
	 * @param realm
	 * 
	 */
	/* package */ChangeManager(Realm realm) {
		Assert.isNotNull(realm, "Realm cannot be null"); //$NON-NLS-1$
		this.realm = realm;
	}

	/**
	 * @param listenerType
	 * @param listener
	 */
	protected void addListener(Object listenerType,
			IObservablesListener listener) {
		int listenerTypeIndex = findListenerTypeIndex(listenerType);
		if (listenerTypeIndex == -1) {
			int length;
			if (listenerTypes == null) {
				length = 0;
				listenerTypes = new Object[1];
				listenerLists = new ListenerList[1];
			} else {
				length = listenerTypes.length;
				System.arraycopy(listenerTypes, 0,
						listenerTypes = new Object[length + 1], 0, length);
				System
						.arraycopy(listenerLists, 0,
								listenerLists = new ListenerList[length + 1],
								0, length);
			}
			listenerTypes[length] = listenerType;
			listenerLists[length] = new ListenerList();
			listenerTypeIndex = length;
		}
		boolean hadListeners = hasListeners();
		listenerLists[listenerTypeIndex].add(listener);
		if (!hadListeners && hasListeners()) {
			firstListenerAdded();
		}
	}

	/**
	 * @param listenerType
	 * @param listener
	 */
	protected void removeListener(Object listenerType,
			IObservablesListener listener) {
		int listenerTypeIndex = findListenerTypeIndex(listenerType);
		if (listenerTypeIndex != -1) {
			boolean hadListeners = hasListeners();
			listenerLists[listenerTypeIndex].remove(listener);
			if (listenerLists[listenerTypeIndex].size() == 0) {
				if (hadListeners && !hasListeners()) {
					this.lastListenerRemoved();
				}
			}
		}
	}

	protected boolean hasListeners() {
		if (listenerTypes != null)
			for (int i = 0; i < listenerTypes.length; i++)
				if (listenerTypes[i] != DisposeEvent.TYPE)
					if (listenerLists[i].size() > 0)
						return true;
		return false;
	}

	private int findListenerTypeIndex(Object listenerType) {
		if (listenerTypes != null) {
			for (int i = 0; i < listenerTypes.length; i++) {
				if (listenerTypes[i] == listenerType) {
					return i;
				}
			}
		}
		return -1;
	}

	protected void fireEvent(ObservableEvent event) {
		Object listenerType = event.getListenerType();
		int listenerTypeIndex = findListenerTypeIndex(listenerType);
		if (listenerTypeIndex != -1) {
			Object[] listeners = listenerLists[listenerTypeIndex]
					.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				event.dispatch((IObservablesListener) listeners[i]);
			}
		}
	}

	/**
	 * 
	 */
	protected void firstListenerAdded() {
	}

	/**
	 * 
	 */
	protected void lastListenerRemoved() {
	}

	/**
	 * 
	 */
	public void dispose() {
		listenerLists = null;
		listenerTypes = null;
	}

	/**
	 * @return Returns the realm.
	 */
	public Realm getRealm() {
		return realm;
	}

	protected Object clone() throws CloneNotSupportedException {
		ChangeManager duplicate = (ChangeManager) super.clone();
		duplicate.listenerLists = null;
		duplicate.listenerTypes = null;
		return duplicate;
	}
}
