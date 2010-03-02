/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.preferences;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferenceNodeVisitor;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * Represents a working copy of a preference node, backed by the real node.
 * <p>
 * Note: Working copy nodes do not fire node change events.
 * </p>
 * <p>
 * Note: Preference change listeners registered on this node will only receive 
 * events from this node and not events based on the original backing node.
 * </p>
 * @since 3.1
 */
public class WorkingCopyPreferences extends EventManager implements
		IEclipsePreferences {

	private static final String TRUE = "true"; //$NON-NLS-1$

	private final Map temporarySettings;
	private final IEclipsePreferences original;
	private boolean removed = false;
	private org.eclipse.ui.preferences.WorkingCopyManager manager;

	/**
	 * @param original the underlying preference node
	 * @param manager the working copy manager
	 */
	public WorkingCopyPreferences(IEclipsePreferences original, org.eclipse.ui.preferences.WorkingCopyManager manager) {
		super();
		this.original = original;
		this.manager = manager;
		this.temporarySettings = new HashMap();
	}

	/*
	 * Convenience method for throwing an exception when methods
	 * are called on a removed node.
	 */
	private void checkRemoved() {
		if (removed) {
			String message = "Preference node: " + absolutePath() + " has been removed."; //$NON-NLS-1$ //$NON-NLS-2$
			throw new IllegalStateException(message);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences#addNodeChangeListener(org.eclipse.core.runtime.preferences.IEclipsePreferences.INodeChangeListener)
	 */
	public void addNodeChangeListener(INodeChangeListener listener) {
		// no-op - working copy nodes don't fire node change events
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences#removeNodeChangeListener(org.eclipse.core.runtime.preferences.IEclipsePreferences.INodeChangeListener)
	 */
	public void removeNodeChangeListener(INodeChangeListener listener) {
		// no-op - working copy nodes don't fire node change events
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences#addPreferenceChangeListener(org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener)
	 */
	public void addPreferenceChangeListener(IPreferenceChangeListener listener) {
		checkRemoved();
		addListenerObject(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences#removePreferenceChangeListener(org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener)
	 */
	public void removePreferenceChangeListener(IPreferenceChangeListener listener) {
		checkRemoved();
		removeListenerObject(listener);
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.prefs.Preferences#removeNode()
	 */
	public void removeNode() throws BackingStoreException {
		checkRemoved();

		// clear all values (long way so people get notified)
		String[] keys = keys();
		for (int i = 0; i < keys.length; i++) {
			remove(keys[i]);
		}

		// remove children
		String[] childNames = childrenNames();
		for (int i = 0; i < childNames.length; i++) {
			node(childNames[i]).removeNode();
		}

		// mark as removed
		removed = true;
	}


	/* (non-Javadoc)
	 * @see org.osgi.service.prefs.Preferences#node(java.lang.String)
	 */
	public Preferences node(String path) {
		checkRemoved();
		return manager.getWorkingCopy((IEclipsePreferences) getOriginal().node(path));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences#accept(org.eclipse.core.runtime.preferences.IPreferenceNodeVisitor)
	 */
	public void accept(IPreferenceNodeVisitor visitor) throws BackingStoreException {
		checkRemoved();
		if (!visitor.visit(this)) {
			return;
		}
		String[] childNames = childrenNames();
		for (int i = 0; i < childNames.length; i++) {
			((IEclipsePreferences) node(childNames[i])).accept(visitor);
		}
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.prefs.Preferences#put(java.lang.String, java.lang.String)
	 */
	public void put(String key, String value) {
		checkRemoved();
		if (key == null || value == null) {
			throw new NullPointerException();
		}
		String oldValue = null;
		if (temporarySettings.containsKey(key)) {
			oldValue = (String) temporarySettings.get(key);
		} else {
			oldValue = getOriginal().get(key, null);
		}
		temporarySettings.put(key, value);
		if (!value.equals(oldValue)) {
			firePropertyChangeEvent(key, oldValue, value);
		}
	}

	private void firePropertyChangeEvent(String key, Object oldValue, Object newValue) {
		Object[] listeners = getListeners();
		if (listeners.length == 0) {
			return;
		}
		PreferenceChangeEvent event = new PreferenceChangeEvent(this, key, oldValue, newValue);
		for (int i = 0; i < listeners.length; i++) {
			((IPreferenceChangeListener) listeners[i]).preferenceChange(event);
		}
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.prefs.Preferences#get(java.lang.String, java.lang.String)
	 */
	public String get(String key, String defaultValue) {
		checkRemoved();
		return internalGet(key, defaultValue);
	}

	private String internalGet(String key, String defaultValue) {
		if (key == null) {
			throw new NullPointerException();
		}
		if (temporarySettings.containsKey(key)) {
			Object value = temporarySettings.get(key);
			return value == null ? defaultValue : (String) value;
		}
		return getOriginal().get(key, defaultValue);
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.prefs.Preferences#remove(java.lang.String)
	 */
	public void remove(String key) {
		checkRemoved();
		if (key == null) {
			throw new NullPointerException();
		}
		Object oldValue = null;
		if (temporarySettings.containsKey(key)) {
			oldValue = temporarySettings.get(key);
		} else {
			oldValue = original.get(key, null);
		}
		if (oldValue == null) {
			return;
		}
		temporarySettings.put(key, null);
		firePropertyChangeEvent(key, oldValue, null);
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.prefs.Preferences#clear()
	 */
	public void clear() {
		checkRemoved();
		for (Iterator i = temporarySettings.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			Object value = temporarySettings.get(key);
			if (value != null) {
				temporarySettings.put(key, null);
				firePropertyChangeEvent(key, value, null);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.prefs.Preferences#putInt(java.lang.String, int)
	 */
	public void putInt(String key, int value) {
		checkRemoved();
		if (key == null) {
			throw new NullPointerException();
		}
		String oldValue = null;
		if (temporarySettings.containsKey(key)) {
			oldValue = (String) temporarySettings.get(key);
		} else {
			oldValue = getOriginal().get(key, null);
		}
		String newValue = Integer.toString(value);
		temporarySettings.put(key, newValue);
		if (!newValue.equals(oldValue)) {
			firePropertyChangeEvent(key, oldValue, newValue);
		}
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.prefs.Preferences#getInt(java.lang.String, int)
	 */
	public int getInt(String key, int defaultValue) {
		checkRemoved();
		String value = internalGet(key, null);
		int result = defaultValue;
		if (value != null) {
			try {
				result = Integer.parseInt(value);
			} catch (NumberFormatException e) {
				// use default
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.prefs.Preferences#putLong(java.lang.String, long)
	 */
	public void putLong(String key, long value) {
		checkRemoved();
		if (key == null) {
			throw new NullPointerException();
		}
		String oldValue = null;
		if (temporarySettings.containsKey(key)) {
			oldValue = (String) temporarySettings.get(key);
		} else {
			oldValue = getOriginal().get(key, null);
		}
		String newValue = Long.toString(value);
		temporarySettings.put(key, newValue);
		if (!newValue.equals(oldValue)) {
			firePropertyChangeEvent(key, oldValue, newValue);
		}
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.prefs.Preferences#getLong(java.lang.String, long)
	 */
	public long getLong(String key, long defaultValue) {
		checkRemoved();
		String value = internalGet(key, null);
		long result = defaultValue;
		if (value != null) {
			try {
				result = Long.parseLong(value);
			} catch (NumberFormatException e) {
				// use default
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.prefs.Preferences#putBoolean(java.lang.String, boolean)
	 */
	public void putBoolean(String key, boolean value) {
		checkRemoved();
		if (key == null) {
			throw new NullPointerException();
		}
		String oldValue = null;
		if (temporarySettings.containsKey(key)) {
			oldValue = (String) temporarySettings.get(key);
		} else {
			oldValue = getOriginal().get(key, null);
		}
		String newValue = String.valueOf(value);
		temporarySettings.put(key, newValue);
		if (!newValue.equalsIgnoreCase(oldValue)) {
			firePropertyChangeEvent(key, oldValue, newValue);
		}
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.prefs.Preferences#getBoolean(java.lang.String, boolean)
	 */
	public boolean getBoolean(String key, boolean defaultValue) {
		checkRemoved();
		String value = internalGet(key, null);
		return value == null ? defaultValue : TRUE.equalsIgnoreCase(value);
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.prefs.Preferences#putFloat(java.lang.String, float)
	 */
	public void putFloat(String key, float value) {
		checkRemoved();
		if (key == null) {
			throw new NullPointerException();
		}
		String oldValue = null;
		if (temporarySettings.containsKey(key)) {
			oldValue = (String) temporarySettings.get(key);
		} else {
			oldValue = getOriginal().get(key, null);
		}
		String newValue = Float.toString(value);
		temporarySettings.put(key, newValue);
		if (!newValue.equals(oldValue)) {
			firePropertyChangeEvent(key, oldValue, newValue);
		}
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.prefs.Preferences#getFloat(java.lang.String, float)
	 */
	public float getFloat(String key, float defaultValue) {
		checkRemoved();
		String value = internalGet(key, null);
		float result = defaultValue;
		if (value != null) {
			try {
				result = Float.parseFloat(value);
			} catch (NumberFormatException e) {
				// use default
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.prefs.Preferences#putDouble(java.lang.String, double)
	 */
	public void putDouble(String key, double value) {
		checkRemoved();
		if (key == null) {
			throw new NullPointerException();
		}
		String oldValue = null;
		if (temporarySettings.containsKey(key)) {
			oldValue = (String) temporarySettings.get(key);
		} else {
			oldValue = getOriginal().get(key, null);
		}
		String newValue = Double.toString(value);
		temporarySettings.put(key, newValue);
		if (!newValue.equals(oldValue)) {
			firePropertyChangeEvent(key, oldValue, newValue);
		}
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.prefs.Preferences#getDouble(java.lang.String, double)
	 */
	public double getDouble(String key, double defaultValue) {
		checkRemoved();
		String value = internalGet(key, null);
		double result = defaultValue;
		if (value != null) {
			try {
				result = Double.parseDouble(value);
			} catch (NumberFormatException e) {
				// use default
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.prefs.Preferences#putByteArray(java.lang.String, byte[])
	 */
	public void putByteArray(String key, byte[] value) {
		checkRemoved();
		if (key == null || value == null) {
			throw new NullPointerException();
		}
		String oldValue = null;
		if (temporarySettings.containsKey(key)) {
			oldValue = (String) temporarySettings.get(key);
		} else {
			oldValue = getOriginal().get(key, null);
		}
		String newValue = new String(Base64.encode(value));
		temporarySettings.put(key, newValue);
		if (!newValue.equals(oldValue)) {
			firePropertyChangeEvent(key, oldValue, newValue);
		}
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.prefs.Preferences#getByteArray(java.lang.String, byte[])
	 */
	public byte[] getByteArray(String key, byte[] defaultValue) {
		checkRemoved();
		String value = internalGet(key, null);
		return value == null ? defaultValue : Base64.decode(value.getBytes());
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.prefs.Preferences#keys()
	 */
	public String[] keys() throws BackingStoreException {
		checkRemoved();
		HashSet allKeys = new HashSet(Arrays.asList(getOriginal().keys()));
		allKeys.addAll(temporarySettings.keySet());
		return (String[]) allKeys.toArray(new String[allKeys.size()]);
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.prefs.Preferences#childrenNames()
	 */
	public String[] childrenNames() throws BackingStoreException {
		checkRemoved();
		return getOriginal().childrenNames();
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.prefs.Preferences#parent()
	 */
	public Preferences parent() {
		checkRemoved();
		return manager.getWorkingCopy((IEclipsePreferences) getOriginal().parent());
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.prefs.Preferences#nodeExists(java.lang.String)
	 */
	public boolean nodeExists(String pathName) throws BackingStoreException {
		// short circuit for this node
		if (pathName.length() == 0) {
			return removed ? false : getOriginal().nodeExists(pathName);
		}
		return getOriginal().nodeExists(pathName);
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.prefs.Preferences#name()
	 */
	public String name() {
		return getOriginal().name();
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.prefs.Preferences#absolutePath()
	 */
	public String absolutePath() {
		return getOriginal().absolutePath();
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.prefs.Preferences#flush()
	 */
	public void flush() throws BackingStoreException {
		if (removed) {
			getOriginal().removeNode();
			return;
		}
		checkRemoved();
		// update underlying preferences
		for (Iterator i = temporarySettings.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			String value = (String) temporarySettings.get(key);
			if (value == null) {
				getOriginal().remove(key);
			} else {
				getOriginal().put(key, value);
			}
		}
		// clear our settings
		temporarySettings.clear();

		// save the underlying preference store
		getOriginal().flush();
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.prefs.Preferences#sync()
	 */
	public void sync() throws BackingStoreException {
		checkRemoved();
		// forget our settings
		temporarySettings.clear();
		// load the underlying preference store
		getOriginal().sync();
	}

	/**
	 * @return Returns the original preference node.
	 */
	private IEclipsePreferences getOriginal() {
		return original;
	}
}
