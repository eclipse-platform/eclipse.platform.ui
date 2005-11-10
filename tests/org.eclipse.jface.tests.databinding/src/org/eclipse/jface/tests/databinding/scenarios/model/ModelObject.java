package org.eclipse.jface.tests.databinding.scenarios.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModelObject {
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
			this);
	private String id;

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(propertyName,
				listener);
	}

	protected void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		propertyChangeSupport.firePropertyChange(propertyName, oldValue,
				newValue);
	}

	public void setId(String string) {
		Object oldValue = id;
		id = string;
		firePropertyChange("id", oldValue, id);
	}

	protected Object[] append(Object[] array, Object object) {
		List newList = new ArrayList(Arrays.asList(array));
		newList.add(object);
		return newList.toArray((Object[]) Array.newInstance(array.getClass()
				.getComponentType(), newList.size()));
	}

	protected Object[] remove(Object[] array, Object object) {
		List newList = new ArrayList(Arrays.asList(array));
		newList.remove(object);
		return newList.toArray((Object[]) Array.newInstance(array.getClass()
				.getComponentType(), newList.size()));
	}
	
}
