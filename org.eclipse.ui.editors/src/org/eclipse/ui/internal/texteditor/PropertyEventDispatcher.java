/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

public final class PropertyEventDispatcher {
	private final Map<Object, Object> fHandlerMap= new HashMap<>();
	private final Map<Object, Object> fReverseMap= new HashMap<>();
	private final IPreferenceStore fStore;
	private final IPropertyChangeListener fListener= new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			firePropertyChange(event);
		}
	};
	public PropertyEventDispatcher(IPreferenceStore store) {
		Assert.isLegal(store != null);
		fStore= store;
	}
	public void dispose() {
		if (!fReverseMap.isEmpty())
			fStore.removePropertyChangeListener(fListener);
		fReverseMap.clear();
		fHandlerMap.clear();
	}
	private void firePropertyChange(PropertyChangeEvent event) {
		Object value= fHandlerMap.get(event.getProperty());
		if (value instanceof IPropertyChangeListener)
			((IPropertyChangeListener) value).propertyChange(event);
		else if (value instanceof Set)
			for (@SuppressWarnings("unchecked") Iterator<IPropertyChangeListener> it= ((Set<IPropertyChangeListener>) value).iterator(); it.hasNext(); )
				it.next().propertyChange(event);
	}
	public void addPropertyChangeListener(String property, IPropertyChangeListener listener) {
		Assert.isLegal(property != null);
		Assert.isLegal(listener != null);

		if (fReverseMap.isEmpty())
			fStore.addPropertyChangeListener(fListener);

		multiMapPut(fHandlerMap, property, listener);
		multiMapPut(fReverseMap, listener, property);
	}
	private void multiMapPut(Map<Object, Object> map, Object key, Object value) {
		Object mapping= map.get(key);
		if (mapping == null) {
			map.put(key, value);
		} else if (mapping instanceof Set) {
			@SuppressWarnings("unchecked")
			Set<Object> set= (Set<Object>) mapping;
			set.add(value);
		} else {
			Set<Object> set= new LinkedHashSet<>();
			set.add(mapping);
			set.add(value);
			map.put(key, set);
		}
	}
	private void multiMapRemove(Map<Object, Object> map, Object key, Object value) {
		Object mapping= map.get(key);
		if (mapping instanceof Set) {
			@SuppressWarnings("unchecked")
			Set<Object> set= (Set<Object>) mapping;
			set.remove(value);
		} else if (mapping != null) {
			map.remove(key);
		}
	}
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		Object value= fReverseMap.get(listener);
		if (value == null)
			return;
		if (value instanceof String) {
			fReverseMap.remove(listener);
			multiMapRemove(fHandlerMap, value, listener);
		} else if (value instanceof Set) {
			fReverseMap.remove(listener);
			for (@SuppressWarnings("unchecked") Iterator<Object> it= ((Set<Object>) value).iterator(); it.hasNext();)
				multiMapRemove(fHandlerMap, it.next(), listener);
		}

		if (fReverseMap.isEmpty())
			fStore.removePropertyChangeListener(fListener);
	}
}