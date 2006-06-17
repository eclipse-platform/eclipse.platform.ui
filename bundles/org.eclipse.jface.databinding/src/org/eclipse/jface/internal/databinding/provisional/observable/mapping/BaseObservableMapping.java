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

package org.eclipse.jface.internal.databinding.provisional.observable.mapping;

import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.jface.internal.databinding.provisional.observable.AbstractObservable;
import org.eclipse.jface.internal.databinding.provisional.observable.IChangeListener;

/**
 * @since 3.2
 *
 */
public abstract class BaseObservableMapping extends AbstractObservable {

	/**
	 * Points to an instance of IChangeListener or a Collection of
	 * IMappingChangeListener
	 */
	protected Object mappingChangeListeners = null;

	public void addMappingChangeListener(IMappingChangeListener listener) {
		if (mappingChangeListeners == null) {
			boolean hadListeners = hasListeners();
			mappingChangeListeners = listener;
			if (!hadListeners) {
				firstListenerAdded();
			}
			return;
		}
	
		Collection listenerList;
		if (mappingChangeListeners instanceof IMappingChangeListener) {
			IChangeListener l = (IChangeListener) mappingChangeListeners;
	
			listenerList = new ArrayList();
			listenerList.add(l);
		} else {
			listenerList = (Collection) mappingChangeListeners;
		}
	
		listenerList.add(listener);
	}

	public void removeMappingChangeListener(IMappingChangeListener listener) {
		if (mappingChangeListeners == listener) {
			mappingChangeListeners = null;
			if (!hasListeners()) {
				lastListenerRemoved();
			}
			return;
		}
	
		if (mappingChangeListeners instanceof Collection) {
			Collection listenerList = (Collection) mappingChangeListeners;
			listenerList.remove(listener);
			if (listenerList.isEmpty()) {
				mappingChangeListeners = null;
				if (!hasListeners()) {
					lastListenerRemoved();
				}
			}
		}
	}

	protected void fireMappingValueChange(MappingDiff diff) {
		if (mappingChangeListeners == null) {
			return;
		}
	
		if (mappingChangeListeners instanceof IMappingChangeListener) {
			((IMappingChangeListener) mappingChangeListeners).handleMappingValueChange(this, diff);
			return;
		}
	
		Collection changeListenerCollection = (Collection) mappingChangeListeners;
	
		IMappingChangeListener[] listeners = (IMappingChangeListener[]) (changeListenerCollection)
				.toArray(new IMappingChangeListener[changeListenerCollection.size()]);
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].handleMappingValueChange(this, diff);
		}
	}

}
