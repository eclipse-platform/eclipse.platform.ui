/*******************************************************************************
 * Copyright (c) 2005, 2021 IBM Corporation and others.
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
package org.eclipse.jface.examples.databinding;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModelObject {
	private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	private String id;

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
	}

	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
	}

	protected void firePropertyChange(String propertyName, int oldValue, int newValue) {
		propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
	}

	protected void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
		propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void setId(String string) {
		Object oldValue = id;
		id = string;
		firePropertyChange("id", oldValue, id);
	}

	protected Object[] append(Object[] array, Object object) {
		List<Object> newList = new ArrayList<>(Arrays.asList(array));
		newList.add(object);
		return newList.toArray((Object[]) Array.newInstance(array.getClass().getComponentType(), newList.size()));
	}

	protected Object[] remove(Object[] array, Object object) {
		List<Object> newList = new ArrayList<>(Arrays.asList(array));
		newList.remove(object);
		return newList.toArray((Object[]) Array.newInstance(array.getClass().getComponentType(), newList.size()));
	}

}
