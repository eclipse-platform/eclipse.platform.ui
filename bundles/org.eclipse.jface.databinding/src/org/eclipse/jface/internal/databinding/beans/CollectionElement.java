package org.eclipse.jface.internal.databinding.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Represents an element in a collection.
 */
public class CollectionElement {
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
			this);

	/**
	 * @param object
	 */
	public CollectionElement(Object object) {
		this.value = object;
	}

	/**
	 * @param listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	/**
	 * @param propertyName
	 * @param listener
	 */
	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	/**
	 * @param listener
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	/**
	 * @param propertyName
	 * @param listener
	 */
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
	
	protected void firePropertyChange(String propertyName, int oldValue,
			int newValue) {
		propertyChangeSupport.firePropertyChange(propertyName, oldValue,
				newValue);
	}
	
	Object value = null;

	/**
	 * @return the "value"'s value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param value
	 */
	public void setValue(Object value) {
		Object oldValue = this.value;
		this.value = value;
		firePropertyChange("value", oldValue, value); //$NON-NLS-1$
	}
	
}