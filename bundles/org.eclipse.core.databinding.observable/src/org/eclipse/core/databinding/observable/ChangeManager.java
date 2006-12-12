/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.databinding.observable;

import org.eclipse.core.runtime.ListenerList;

/**
 * @since 3.3
 * 
 */
public class ChangeManager extends ListenerManager {

	ListenerList[] listenerLists = null;
	Object listenerTypes[] = null;
	private ListenerManager listenerManager;

	/**
	 * @param listenerSupport
	 * 
	 */
	public ChangeManager(ListenerManager listenerSupport) {
		this.listenerManager = listenerSupport == null ? this : listenerSupport;
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
			listenerLists[length].add(listener);
			if (length == 0) {
				listenerManager.firstListenerAdded();
			}
			return;
		}
		listenerLists[listenerTypeIndex].add(listener);
	}

	/**
	 * @param listenerType
	 * @param listener
	 */
	protected void removeListener(Object listenerType,
			IObservablesListener listener) {
		int listenerTypeIndex = findListenerTypeIndex(listenerType);
		if (listenerTypeIndex != -1) {
			listenerLists[listenerTypeIndex].remove(listener);
			if (listenerLists[listenerTypeIndex].size() == 0) {
				if (!hasListeners()) {
					listenerManager.lastListenerRemoved();
				}
			}
		}
	}

	protected boolean hasListeners() {
		if (listenerTypes == null) {
			return false;
		}
		for (int i = 0; i < listenerTypes.length; i++) {
			if (listenerLists[i].size() > 0) {
				return true;
			}
		}
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
	public void dispose() {
		listenerLists = null;
		listenerTypes = null;
		listenerManager = null;
	}

}
