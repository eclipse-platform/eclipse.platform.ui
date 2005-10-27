/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.internal.extensions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.navigator.IExtensionStateModel;

public class ExtensionStateModel implements IExtensionStateModel {

	private final String id;

	private final String viewerId;

	private final Map values = new HashMap();

	private final ListenerList propertyListeners = new ListenerList(3);

	public ExtensionStateModel(String anId, String aViewerId) {
		id = anId;
		viewerId = aViewerId;
	}

	public String getId() {
		return id;
	}

	public String getViewerId() {
		return viewerId;
	}

	public String getStringProperty(String aPropertyName) {
		return (String) values.get(aPropertyName);
	}

	public boolean getBooleanProperty(String aPropertyName) {

		Boolean b = (Boolean) values.get(aPropertyName);
		return b != null ? b.booleanValue() : false;
	}

	public int getIntProperty(String aPropertyName) {
		Integer i = (Integer) values.get(aPropertyName);
		return i != null ? i.intValue() : -1;
	}

	public void setStringProperty(String aPropertyName, String aPropertyValue) {
		String oldValue = (String) values.get(aPropertyName);
		String newValue = aPropertyValue;
		if(hasPropertyChanged(oldValue, newValue)) {
			values.put(aPropertyName, newValue);
			firePropertyChangeEvent(new PropertyChangeEvent(this, aPropertyName, oldValue, newValue));
		}
	}

	public void setBooleanProperty(String aPropertyName, boolean aPropertyValue) {
		Boolean oldValue = (Boolean) values.get(aPropertyName);
		Boolean newValue = aPropertyValue ? Boolean.TRUE : Boolean.FALSE;
		if(hasPropertyChanged(oldValue, newValue)) {

			values
					.put(aPropertyName, aPropertyValue ? Boolean.TRUE
							: Boolean.FALSE);
			values.put(aPropertyName, newValue);
			firePropertyChangeEvent(new PropertyChangeEvent(this, aPropertyName, oldValue, newValue));
		}		
	}

	public void setIntProperty(String aPropertyName, int aPropertyValue) {
		Integer oldValue = (Integer) values.get(aPropertyName);
		Integer newValue = new Integer(aPropertyValue);
		if(hasPropertyChanged(oldValue, newValue)) {
			values.put(aPropertyName, newValue);
			firePropertyChangeEvent(new PropertyChangeEvent(this, aPropertyName, oldValue, newValue));
		}
	}

	public void addPropertyChangeListener(IPropertyChangeListener aListener) {
		synchronized (propertyListeners) {
			propertyListeners.add(aListener);
		}
	}

	public void removePropertyChangeListener(IPropertyChangeListener aListener) {
		synchronized (propertyListeners) {
			propertyListeners.remove(aListener);
		}
	}

	public Object getProperty(String aPropertyName) {
		return values.get(aPropertyName);
	}

	public void setProperty(String aPropertyName, Object aPropertyValue) {

		Object oldValue = values.get(aPropertyName);
		Object newValue = aPropertyValue;
		if(hasPropertyChanged(oldValue, newValue)) {
			values.put(aPropertyName, newValue);
			firePropertyChangeEvent(new PropertyChangeEvent(this, aPropertyName, oldValue, newValue));
		}
	}

	/**
	 * @param oldValue
	 * @param newValue
	 * @return
	 */
	private boolean hasPropertyChanged(Object oldValue, Object newValue) {
		return oldValue == null || !oldValue.equals(newValue);
	}

	protected void firePropertyChangeEvent(PropertyChangeEvent anEvent) {
		Object[] listeners = propertyListeners.getListeners();
		for (int i = 0; i < listeners.length; ++i)
			((IPropertyChangeListener) listeners[i]).propertyChange(anEvent);
	}

}
