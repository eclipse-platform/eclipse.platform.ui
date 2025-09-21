/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
package org.eclipse.ui.texteditor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;


/**
 * Preference store that composes multiple preference stores in a
 * chain and serves a preference value from the first preference store in the
 * chain that contains the preference.
 * <p>
 * This preference store is read-only i.e. write access
 * throws an {@link java.lang.UnsupportedOperationException}.</p>
 *
 * @since 3.0
 */
public class ChainedPreferenceStore implements IPreferenceStore {

	/** Child preference stores. */
	private final IPreferenceStore[] fPreferenceStores;

	/** Listeners on this chained preference store. */
	private final ListenerList<IPropertyChangeListener> fClientListeners= new ListenerList<>(ListenerList.IDENTITY);

	/** Listeners on the child preference stores. */
	private final List<PropertyChangeListener> fChildListeners= new ArrayList<>();

	/**
	 * Listener on the chained preference stores. Forwards only the events
	 * that are visible to clients.
	 */
	private class PropertyChangeListener implements IPropertyChangeListener {

		/** Preference store to listen too. */
		private IPreferenceStore fPreferenceStore;

		/**
		 * Initialize with the given preference store.
		 *
		 * @param preferenceStore the preference store
		 */
		public PropertyChangeListener(IPreferenceStore preferenceStore) {
			setPreferenceStore(preferenceStore);
		}

		@Override
		public void propertyChange(PropertyChangeEvent event) {
			IPreferenceStore childPreferenceStore= getPreferenceStore();
			handlePropertyChangeEvent(childPreferenceStore, event);
		}

		/**
		 * Registers this listener on the preference store.
		 */
		public void register() {
			getPreferenceStore().addPropertyChangeListener(this);
		}

		/**
		 * Unregisters this listener from the preference store.
		 */
		public void unregister() {
			getPreferenceStore().removePropertyChangeListener(this);
		}

		/**
		 * Returns the preference store.
		 *
		 * @return the preference store
		 */
		public IPreferenceStore getPreferenceStore() {
			return fPreferenceStore;
		}

		/**
		 * Sets the preference store.
		 *
		 * @param preferenceStore the preference store to set
		 */
		public void setPreferenceStore(IPreferenceStore preferenceStore) {
			fPreferenceStore= preferenceStore;
		}

	}

	/**
	 * Sets the chained preference stores.
	 *
	 * @param preferenceStores the chained preference stores to set
	 */
	public ChainedPreferenceStore(IPreferenceStore[] preferenceStores) {
		Assert.isTrue(preferenceStores != null && preferenceStores.length > 0);
		fPreferenceStores= new IPreferenceStore[preferenceStores.length];
		System.arraycopy(preferenceStores, 0, fPreferenceStores, 0, preferenceStores.length);
		// Create listeners
		for (IPreferenceStore store : fPreferenceStores) {
			PropertyChangeListener listener= new PropertyChangeListener(store);
			fChildListeners.add(listener);
		}
	}

	@Override
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		if (fClientListeners.size() == 0) {
			registerChildListeners();
		}
		fClientListeners.add(listener);
	}

	@Override
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		fClientListeners.remove(listener);
		if (fClientListeners.size() == 0) {
			unregisterChildListeners();
		}
	}

	@Override
	public boolean contains(String name) {
		return getVisibleStore(name) != null;
	}

	@Override
	public void firePropertyChangeEvent(String name, Object oldValue, Object newValue) {
		firePropertyChangeEvent(new PropertyChangeEvent(this, name, oldValue, newValue));
	}

	/**
	 * Fire the given property change event.
	 *
	 * @param event the property change event
	 */
	private void firePropertyChangeEvent(PropertyChangeEvent event) {
		for (IPropertyChangeListener listener : fClientListeners) {
			listener.propertyChange(event);
		}
	}

	@Override
	public boolean getBoolean(String name) {
		IPreferenceStore visibleStore= getVisibleStore(name);
		if (visibleStore != null) {
			return visibleStore.getBoolean(name);
		}
		return BOOLEAN_DEFAULT_DEFAULT;
	}

	@Override
	public boolean getDefaultBoolean(String name) {
		IPreferenceStore visibleStore= getVisibleStore(name);
		if (visibleStore != null) {
			return visibleStore.getDefaultBoolean(name);
		}
		return BOOLEAN_DEFAULT_DEFAULT;
	}

	@Override
	public double getDefaultDouble(String name) {
		IPreferenceStore visibleStore= getVisibleStore(name);
		if (visibleStore != null) {
			return visibleStore.getDefaultDouble(name);
		}
		return DOUBLE_DEFAULT_DEFAULT;
	}

	@Override
	public float getDefaultFloat(String name) {
		IPreferenceStore visibleStore= getVisibleStore(name);
		if (visibleStore != null) {
			return visibleStore.getDefaultFloat(name);
		}
		return FLOAT_DEFAULT_DEFAULT;
	}

	@Override
	public int getDefaultInt(String name) {
		IPreferenceStore visibleStore= getVisibleStore(name);
		if (visibleStore != null) {
			return visibleStore.getDefaultInt(name);
		}
		return INT_DEFAULT_DEFAULT;
	}

	@Override
	public long getDefaultLong(String name) {
		IPreferenceStore visibleStore= getVisibleStore(name);
		if (visibleStore != null) {
			return visibleStore.getDefaultLong(name);
		}
		return LONG_DEFAULT_DEFAULT;
	}

	@Override
	public String getDefaultString(String name) {
		IPreferenceStore visibleStore= getVisibleStore(name);
		if (visibleStore != null) {
			return visibleStore.getDefaultString(name);
		}
		return STRING_DEFAULT_DEFAULT;
	}

	@Override
	public double getDouble(String name) {
		IPreferenceStore visibleStore= getVisibleStore(name);
		if (visibleStore != null) {
			return visibleStore.getDouble(name);
		}
		return DOUBLE_DEFAULT_DEFAULT;
	}

	@Override
	public float getFloat(String name) {
		IPreferenceStore visibleStore= getVisibleStore(name);
		if (visibleStore != null) {
			return visibleStore.getFloat(name);
		}
		return FLOAT_DEFAULT_DEFAULT;
	}

	@Override
	public int getInt(String name) {
		IPreferenceStore visibleStore= getVisibleStore(name);
		if (visibleStore != null) {
			return visibleStore.getInt(name);
		}
		return INT_DEFAULT_DEFAULT;
	}

	@Override
	public long getLong(String name) {
		IPreferenceStore visibleStore= getVisibleStore(name);
		if (visibleStore != null) {
			return visibleStore.getLong(name);
		}
		return LONG_DEFAULT_DEFAULT;
	}

	@Override
	public String getString(String name) {
		IPreferenceStore visibleStore= getVisibleStore(name);
		if (visibleStore != null) {
			return visibleStore.getString(name);
		}
		return STRING_DEFAULT_DEFAULT;
	}

	@Override
	public boolean isDefault(String name) {
		IPreferenceStore visibleStore= getVisibleStore(name);
		if (visibleStore != null) {
			return visibleStore.isDefault(name);
		}
		return false;
	}

	@Override
	public boolean needsSaving() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void putValue(String name, String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDefault(String name, double value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDefault(String name, float value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDefault(String name, int value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDefault(String name, long value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDefault(String name, String defaultObject) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDefault(String name, boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setToDefault(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setValue(String name, double value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setValue(String name, float value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setValue(String name, int value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setValue(String name, long value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setValue(String name, String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setValue(String name, boolean value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Handle property change event from the child listener with the given child preference store.
	 *
	 * @param childPreferenceStore the child preference store
	 * @param event the event
	 */
	private void handlePropertyChangeEvent(IPreferenceStore childPreferenceStore, PropertyChangeEvent event) {
		String property= event.getProperty();
		Object oldValue= event.getOldValue();
		Object newValue= event.getNewValue();

		IPreferenceStore visibleStore= getVisibleStore(property);

		/*
		 * Assume that the property is there but has no default value (its owner relies on the default-default value)
		 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=52827
		 */
		if (visibleStore == null && newValue != null) {
			visibleStore= childPreferenceStore;
		}

		if (visibleStore == null) {
			// no visible store
			if (oldValue != null) {
				// removal in child, last in chain -> removal in this chained preference store
				firePropertyChangeEvent(event);
			}
		} else if (visibleStore == childPreferenceStore) {
			// event from visible store
			if (oldValue != null) {
				// change in child, visible store -> change in this chained preference store
				firePropertyChangeEvent(event);
			} else {
				// insertion in child
				IPreferenceStore oldVisibleStore= null;
				int i= 0;
				int length= fPreferenceStores.length;
				while (i < length && fPreferenceStores[i++] != visibleStore) {
					// do nothing
				}
				while (oldVisibleStore == null && i < length) {
					if (fPreferenceStores[i].contains(property)) {
						oldVisibleStore= fPreferenceStores[i];
					}
					i++;
				}

				if (oldVisibleStore == null) {
					// insertion in child, first in chain -> insertion in this chained preference store
					firePropertyChangeEvent(event);
				} else {
					// insertion in child, not first in chain
					oldValue= getOtherValue(property, oldVisibleStore, newValue);
					if (!oldValue.equals(newValue)) {
						// insertion in child, different old value -> change in this chained preference store
						firePropertyChangeEvent(property, oldValue, newValue);
					// else: insertion in child, same old value -> no change in this chained preference store
					}
				}
			}
		} else {
			// event from other than the visible store
			boolean eventBeforeVisibleStore= false;
			for (IPreferenceStore store : fPreferenceStores) {
				if (store == visibleStore) {
					break;
				}
				if (store == childPreferenceStore) {
					eventBeforeVisibleStore= true;
					break;
				}
			}

			if (eventBeforeVisibleStore) {
				// removal in child, before visible store

				/*
				 * The original event's new value can be non-null (removed assertion).
				 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=69419
				 */

				newValue= getOtherValue(property, visibleStore, oldValue);
				if (!newValue.equals(oldValue)) {
					// removal in child, before visible store, different old value -> change in this chained preference store
					firePropertyChangeEvent(property, oldValue, newValue);
				// else: removal in child, before visible store, same old value -> no change in this chained preference store
				}
			}
			// else: event behind visible store -> no change in this chained preference store
		}
	}

	/**
	 * Returns an object of the same dynamic type as <code>thisValue</code>, the returned object
	 * encapsulates the value of the <code>property</code> from the preference <code>store</code>.
	 *
	 * @param property the name of the considered property
	 * @param store the preference store
	 * @param thisValue the given value
	 * @return the other value
	 */
	private Object getOtherValue(String property, IPreferenceStore store, Object thisValue) {

		if (thisValue instanceof Boolean) {
			return store.getBoolean(property) ? Boolean.TRUE : Boolean.FALSE;
		} else if (thisValue instanceof Double) {
			return Double.valueOf(store.getDouble(property));
		} else if (thisValue instanceof Float) {
			return Float.valueOf(store.getFloat(property));
		} else if (thisValue instanceof Integer) {
			return Integer.valueOf(store.getInt(property));
		} else if (thisValue instanceof Long) {
			return Long.valueOf(store.getLong(property));
		} else if (thisValue instanceof String) {
			return store.getString(property);
		}

		return store.getString(property);
	}

	/**
	 * Returns the preference store from which the given property's value
	 * is visible.
	 *
	 * @param property the name of the property
	 * @return the preference store from which the property's value is visible,
	 * 	<code>null</code> if the property is unknown
	 */
	private IPreferenceStore getVisibleStore(String property) {
		IPreferenceStore visibleStore= null;

		for (int i= 0, length= fPreferenceStores.length; i < length && visibleStore == null; i++) {
			IPreferenceStore store= fPreferenceStores[i];
			if (store.contains(property)) {
				visibleStore= store;
			}
		}
		return visibleStore;
	}

	/**
	 * Register the child listeners on the child preference stores.
	 */
	private void registerChildListeners() {
		Iterator<PropertyChangeListener> iter= fChildListeners.iterator();
		while (iter.hasNext()) {
			PropertyChangeListener listener= iter.next();
			listener.register();
		}
	}

	/**
	 * Unregister the child listeners from the child preference stores.
	 */
	private void unregisterChildListeners() {
		Iterator<PropertyChangeListener> iter= fChildListeners.iterator();
		while (iter.hasNext()) {
			PropertyChangeListener listener= iter.next();
			listener.unregister();
		}
	}
}
