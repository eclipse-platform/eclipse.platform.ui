/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.preferences;

import java.io.*;
import java.util.Iterator;
import java.util.Properties;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.preferences.*;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeEvent;
import org.osgi.service.prefs.BackingStoreException;

/**
 * This class represents the backwards compatibility story between the Eclipse 3.0
 * preferences and pre-3.0 preferences. It acts as a bridge between the 
 * org.eclipse.core.runtime.Preferences object associated with a particular plug-in
 * object, and its corresponding preference node in the 3.0 preference node
 * hierarchy.
 * 
 * @since 3.0
 */
public class PreferenceForwarder extends Preferences implements IEclipsePreferences.IPreferenceChangeListener {

	private static final byte[] BYTE_ARRAY_DEFAULT_DEFAULT = new byte[0];

	private IEclipsePreferences preferences;
	private IEclipsePreferences defaults;

	/**
	 * Class to wrap property change events. Have to use a subclass since we
	 * don't want to increase the visibility of the property change event constructor
	 * to be public.
	 */
	class PropertyChangeEventWrapper extends Preferences.PropertyChangeEvent {
		PropertyChangeEventWrapper(Object source, String property, Object oldValue, Object newValue) {
			super(source, property, oldValue, newValue);
		}
	}

	public PreferenceForwarder(String pluginID) {
		super();
		IEclipsePreferences root = Platform.getPreferencesService().getRootNode();
		defaults = (IEclipsePreferences) root.node(DefaultScope.SCOPE).node(pluginID);
		preferences = (IEclipsePreferences) root.node(InstanceScope.SCOPE).node(pluginID);
		preferences.addPreferenceChangeListener(this);
	}

	/**
	 * Adds a property change listener to this preference object.
	 * Has no affect if the identical listener is already registered.
	 *
	 * @param listener a property change listener
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		if (listeners == null)
			listeners = new ListenerList();
		listeners.add(listener);
	}

	/*
	 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener#preferenceChange(org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeEvent)
	 */
	public void preferenceChange(IPreferenceChangeEvent event) {
		dirty = true;
		if (listeners == null)
			return;
		Object oldValue = event.getOldValue();
		Object newValue = event.getNewValue();
		String key = event.getKey();
		if (newValue == null)
			newValue = getDefault(key, oldValue);
		else if (oldValue == null)
			oldValue = getDefault(key, newValue);
		PropertyChangeEvent propertyChangeEvent = new PropertyChangeEventWrapper(this, key, oldValue, newValue);
		Object[] clients = listeners.getListeners();
		for (int i = 0; i < clients.length; i++)
			((IPropertyChangeListener) clients[i]).propertyChange(propertyChangeEvent);
	}

	/**
	 * Removes the given listener from this preference object.
	 * Has no affect if the listener is not registered.
	 *
	 * @param listener a property change listener
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		if (listeners == null)
			return;
		listeners.remove(listener);
		if (listeners.size() == 0) {
			listeners = null;
		}
	}

	/**
	 * Does its best at determining the default value for the given key. Checks the
	 * given object's type and then looks in the list of defaults to see if a value
	 * exists. If not or if there is a problem converting the value, the default default 
	 * value for that type is returned.
	 */
	private Object getDefault(String key, Object obj) {
		if (obj instanceof String)
			return defaults.get(key, STRING_DEFAULT_DEFAULT);
		else if (obj instanceof Integer)
			return new Integer(defaults.getInt(key, INT_DEFAULT_DEFAULT));
		else if (obj instanceof Double)
			return new Double(defaults.getDouble(key, DOUBLE_DEFAULT_DEFAULT));
		else if (obj instanceof Float)
			return new Float(defaults.getFloat(key, FLOAT_DEFAULT_DEFAULT));
		else if (obj instanceof Long)
			return new Long(defaults.getLong(key, LONG_DEFAULT_DEFAULT));
		else if (obj instanceof byte[])
			return defaults.getByteArray(key, BYTE_ARRAY_DEFAULT_DEFAULT);
		else if (obj instanceof Boolean)
			return new Boolean(defaults.getBoolean(key, BOOLEAN_DEFAULT_DEFAULT));
		else
			return null;
	}

	/**
	 * Returns whether the given property is known to this preference object,
	 * either by having an explicit setting or by having a default
	 * setting.
	 *
	 * @param name the name of the property
	 * @return <code>true</code> if either a current value or a default
	 *  value is known for the named property, and <code>false</code>otherwise
	 */
	public boolean contains(String name) {
		String[] names = new String[0];
		try {
			names = preferences.keys();
		} catch (BackingStoreException e) {
			logError(e.getMessage(), e);
		}
		for (int i = 0; i < names.length; i++)
			if (names[i].equals(name))
				return true;
		names = new String[0];
		try {
			names = defaults.keys();
		} catch (BackingStoreException e) {
			logError(e.getMessage(), e);
		}
		for (int i = 0; i < names.length; i++)
			if (names[i].equals(name))
				return true;
		return false;
	}

	/**
	 * Returns the current value of the boolean-valued property with the
	 * given name.
	 * Returns the default-default value (<code>false</code>) if there
	 * is no property with the given name, or if the current value 
	 * cannot be treated as a boolean.
	 *
	 * @param name the name of the property
	 * @return the boolean-valued property
	 */
	public boolean getBoolean(String name) {
		return preferences.getBoolean(name, defaults.getBoolean(name, BOOLEAN_DEFAULT_DEFAULT));
	}

	/**
	 * Sets the current value of the boolean-valued property with the
	 * given name.
	 * <p>
	 * A property change event is reported if the current value of the 
	 * property actually changes from its previous value. In the event
	 * object, the property name is the name of the property, and the
	 * old and new values are wrapped as objects.
	 * </p>
	 * <p>
	 * If the given value is the same as the corresponding default value
	 * for the given property, the explicit setting is deleted.
	 * Note that the recommended way of re-initializing a property to its
	 * default value is to call <code>setToDefault</code>.
	 * </p>
	 *
	 * @param name the name of the property
	 * @param value the new current value of the property
	 */
	public void setValue(String name, boolean value) {
		if (getDefaultBoolean(name) == value)
			preferences.remove(name);
		else
			preferences.putBoolean(name, value);
	}

	/**
	 * Returns the default value for the boolean-valued property
	 * with the given name.
	 * Returns the default-default value (<code>false</code>) if there
	 * is no default property with the given name, or if the default 
	 * value cannot be treated as a boolean.
	 *
	 * @param name the name of the property
	 * @return the default value of the named property
	 */
	public boolean getDefaultBoolean(String name) {
		return defaults.getBoolean(name, BOOLEAN_DEFAULT_DEFAULT);
	}

	/**
	 * Sets the default value for the boolean-valued property with the
	 * given name. 
	 * <p>
	 * Note that the current value of the property is affected if
	 * the property's current value was its old default value, in which
	 * case it changes to the new default value. If the property's current
	 * is different from its old default value, its current value is
	 * unaffected. No property change events are reported by changing default
	 * values.
	 * </p>
	 *
	 * @param name the name of the property
	 * @param value the new default value for the property
	 */
	public void setDefault(String name, boolean value) {
		defaults.putBoolean(name, value);
	}

	/**
	 * Returns the current value of the double-valued property with the
	 * given name.
	 * Returns the default-default value (<code>0.0</code>) if there
	 * is no property with the given name, or if the current value 
	 * cannot be treated as a double.
	 *
	 * @param name the name of the property
	 * @return the double-valued property
	 */
	public double getDouble(String name) {
		return preferences.getDouble(name, defaults.getDouble(name, DOUBLE_DEFAULT_DEFAULT));
	}

	/**
	 * Sets the current value of the double-valued property with the
	 * given name.
	 * <p>
	 * A property change event is reported if the current value of the 
	 * property actually changes from its previous value. In the event
	 * object, the property name is the name of the property, and the
	 * old and new values are wrapped as objects.
	 * </p>
	 * <p>
	 * If the given value is the same as the corresponding default value
	 * for the given property, the explicit setting is deleted.
	 * Note that the recommended way of re-initializing a property to its
	 * default value is to call <code>setToDefault</code>.
	 * </p>
	 *
	 * @param name the name of the property
	 * @param value the new current value of the property; must be 
	 *   a number (not a NaN)
	 */
	public void setValue(String name, double value) {
		if (Double.isNaN(value))
			throw new IllegalArgumentException();
		if (getDefaultDouble(name) == value)
			preferences.remove(name);
		else
			preferences.putDouble(name, value);
	}

	/**
	 * Returns the default value for the double-valued property
	 * with the given name.
	 * Returns the default-default value (<code>0.0</code>) if there
	 * is no default property with the given name, or if the default 
	 * value cannot be treated as a double.
	 *
	 * @param name the name of the property
	 * @return the default value of the named property
	 */
	public double getDefaultDouble(String name) {
		return defaults.getDouble(name, DOUBLE_DEFAULT_DEFAULT);
	}

	/**
	 * Sets the default value for the double-valued property with the
	 * given name. 
	 * <p>
	 * Note that the current value of the property is affected if
	 * the property's current value was its old default value, in which
	 * case it changes to the new default value. If the property's current
	 * is different from its old default value, its current value is
	 * unaffected. No property change events are reported by changing default
	 * values.
	 * </p>
	 *
	 * @param name the name of the property
	 * @param value the new default value for the property; must be 
	 *   a number (not a NaN)
	 */
	public void setDefault(String name, double value) {
		if (Double.isNaN(value))
			throw new IllegalArgumentException();
		defaults.putDouble(name, value);
	}

	/**
	 * Returns the current value of the float-valued property with the
	 * given name.
	 * Returns the default-default value (<code>0.0f</code>) if there
	 * is no property with the given name, or if the current value 
	 * cannot be treated as a float.
	 *
	 * @param name the name of the property
	 * @return the float-valued property
	 */
	public float getFloat(String name) {
		return preferences.getFloat(name, defaults.getFloat(name, FLOAT_DEFAULT_DEFAULT));
	}

	/**
	 * Sets the current value of the float-valued property with the
	 * given name.
	 * <p>
	 * A property change event is reported if the current value of the 
	 * property actually changes from its previous value. In the event
	 * object, the property name is the name of the property, and the
	 * old and new values are wrapped as objects.
	 * </p>
	 * <p>
	 * If the given value is the same as the corresponding default value
	 * for the given property, the explicit setting is deleted.
	 * Note that the recommended way of re-initializing a property to its
	 * default value is to call <code>setToDefault</code>.
	 * </p>
	 *
	 * @param name the name of the property
	 * @param value the new current value of the property; must be 
	 *   a number (not a NaN)
	 */
	public void setValue(String name, float value) {
		if (Float.isNaN(value))
			throw new IllegalArgumentException();
		if (getDefaultFloat(name) == value)
			preferences.remove(name);
		else
			preferences.putFloat(name, value);
	}

	/**
	 * Returns the default value for the float-valued property
	 * with the given name.
	 * Returns the default-default value (<code>0.0f</code>) if there
	 * is no default property with the given name, or if the default 
	 * value cannot be treated as a float.
	 *
	 * @param name the name of the property
	 * @return the default value of the named property
	 */
	public float getDefaultFloat(String name) {
		return defaults.getFloat(name, FLOAT_DEFAULT_DEFAULT);
	}

	/**
	 * Sets the default value for the float-valued property with the
	 * given name. 
	 * <p>
	 * Note that the current value of the property is affected if
	 * the property's current value was its old default value, in which
	 * case it changes to the new default value. If the property's current
	 * is different from its old default value, its current value is
	 * unaffected. No property change events are reported by changing default
	 * values.
	 * </p>
	 *
	 * @param name the name of the property
	 * @param value the new default value for the property; must be 
	 *   a number (not a NaN)
	 */
	public void setDefault(String name, float value) {
		if (Float.isNaN(value))
			throw new IllegalArgumentException();
		defaults.putFloat(name, value);
	}

	/**
	 * Returns the current value of the integer-valued property with the
	 * given name.
	 * Returns the default-default value (<code>0</code>) if there
	 * is no property with the given name, or if the current value 
	 * cannot be treated as an integter.
	 *
	 * @param name the name of the property
	 * @return the int-valued property
	 */
	public int getInt(String name) {
		return preferences.getInt(name, defaults.getInt(name, INT_DEFAULT_DEFAULT));
	}

	/**
	 * Sets the current value of the integer-valued property with the
	 * given name.
	 * <p>
	 * A property change event is reported if the current value of the 
	 * property actually changes from its previous value. In the event
	 * object, the property name is the name of the property, and the
	 * old and new values are wrapped as objects.
	 * </p>
	 * <p>
	 * If the given value is the same as the corresponding default value
	 * for the given property, the explicit setting is deleted.
	 * Note that the recommended way of re-initializing a property to its
	 * default value is to call <code>setToDefault</code>.
	 * </p>
	 *
	 * @param name the name of the property
	 * @param value the new current value of the property
	 */
	public void setValue(String name, int value) {
		if (getDefaultInt(name) == value)
			preferences.remove(name);
		else
			preferences.putInt(name, value);
	}

	/**
	 * Returns the default value for the integer-valued property
	 * with the given name.
	 * Returns the default-default value (<code>0</code>) if there
	 * is no default property with the given name, or if the default 
	 * value cannot be treated as an integer.
	 *
	 * @param name the name of the property
	 * @return the default value of the named property
	 */
	public int getDefaultInt(String name) {
		return defaults.getInt(name, INT_DEFAULT_DEFAULT);
	}

	/**
	 * Sets the default value for the integer-valued property with the
	 * given name. 
	 * <p>
	 * Note that the current value of the property is affected if
	 * the property's current value was its old default value, in which
	 * case it changes to the new default value. If the property's current
	 * is different from its old default value, its current value is
	 * unaffected. No property change events are reported by changing default
	 * values.
	 * </p>
	 *
	 * @param name the name of the property
	 * @param value the new default value for the property
	 */
	public void setDefault(String name, int value) {
		defaults.putInt(name, value);
	}

	/**
	 * Returns the current value of the long-valued property with the
	 * given name.
	 * Returns the default-default value (<code>0L</code>) if there
	 * is no property with the given name, or if the current value 
	 * cannot be treated as a long.
	 *
	 * @param name the name of the property
	 * @return the long-valued property
	 */
	public long getLong(String name) {
		return preferences.getLong(name, defaults.getLong(name, LONG_DEFAULT_DEFAULT));
	}

	/**
	 * Sets the current value of the long-valued property with the
	 * given name.
	 * <p>
	 * A property change event is reported if the current value of the 
	 * property actually changes from its previous value. In the event
	 * object, the property name is the name of the property, and the
	 * old and new values are wrapped as objects.
	 * </p>
	 * <p>
	 * If the given value is the same as the corresponding default value
	 * for the given property, the explicit setting is deleted.
	 * Note that the recommended way of re-initializing a property to its
	 * default value is to call <code>setToDefault</code>.
	 * </p>
	 *
	 * @param name the name of the property
	 * @param value the new current value of the property
	 */
	public void setValue(String name, long value) {
		if (getDefaultLong(name) == value)
			preferences.remove(name);
		else
			preferences.putLong(name, value);
	}

	/**
	 * Returns the default value for the long-valued property
	 * with the given name.
	 * Returns the default-default value (<code>0L</code>) if there
	 * is no default property with the given name, or if the default 
	 * value cannot be treated as a long.
	 *
	 * @param name the name of the property
	 * @return the default value of the named property
	 */
	public long getDefaultLong(String name) {
		return defaults.getLong(name, LONG_DEFAULT_DEFAULT);
	}

	/**
	 * Sets the default value for the long-valued property with the
	 * given name. 
	 * <p>
	 * Note that the current value of the property is affected if
	 * the property's current value was its old default value, in which
	 * case it changes to the new default value. If the property's current
	 * is different from its old default value, its current value is
	 * unaffected. No property change events are reported by changing default
	 * values.
	 * </p>
	 *
	 * @param name the name of the property
	 * @param value the new default value for the property
	 */
	public void setDefault(String name, long value) {
		defaults.putLong(name, value);
	}

	/**
	 * Returns the current value of the string-valued property with the
	 * given name.
	 * Returns the default-default value (the empty string <code>""</code>)
	 * if there is no property with the given name.
	 *
	 * @param name the name of the property
	 * @return the string-valued property
	 */
	public String getString(String name) {
		return preferences.get(name, defaults.get(name, STRING_DEFAULT_DEFAULT));
	}

	/**
	 * Sets the current value of the string-valued property with the
	 * given name.
	 * <p>
	 * A property change event is reported if the current value of the 
	 * property actually changes from its previous value. In the event
	 * object, the property name is the name of the property, and the
	 * old and new values are wrapped as objects.
	 * </p>
	 * <p>
	 * If the given value is the same as the corresponding default value
	 * for the given property, the explicit setting is deleted.
	 * Note that the recommended way of re-initializing a property to its
	 * default value is to call <code>setToDefault</code>.
	 * </p>
	 *
	 * @param name the name of the property
	 * @param value the new current value of the property
	 */
	public void setValue(String name, String value) {
		if (value == null)
			throw new IllegalArgumentException();
		if (getDefaultString(name).equals(value))
			preferences.remove(name);
		else
			preferences.put(name, value);
	}

	/**
	 * Returns the default value for the string-valued property
	 * with the given name.
	 * Returns the default-default value (the empty string <code>""</code>) 
	 * is no default property with the given name, or if the default 
	 * value cannot be treated as a string.
	 *
	 * @param name the name of the property
	 * @return the default value of the named property
	 */
	public String getDefaultString(String name) {
		return defaults.get(name, STRING_DEFAULT_DEFAULT);
	}

	/**
	 * Sets the default value for the string-valued property with the
	 * given name. 
	 * <p>
	 * Note that the current value of the property is affected if
	 * the property's current value was its old default value, in which
	 * case it changes to the new default value. If the property's current
	 * is different from its old default value, its current value is
	 * unaffected. No property change events are reported by changing default
	 * values.
	 * </p>
	 *
	 * @param name the name of the property
	 * @param value the new default value for the property
	 */
	public void setDefault(String name, String value) {
		if (value == null)
			throw new IllegalArgumentException();
		defaults.put(name, value);
	}

	/**
	 * Returns whether the property with the given name has the default value in
	 * virtue of having no explicitly set value.
	 *
	 * @param name the name of the property
	 * @return <code>true</code> if the property has no explicitly set value,
	 * and <code>false</code> otherwise (including the case where the property
	 * is unknown to this object)
	 */
	public boolean isDefault(String name) {
		String[] keys = null;
		try {
			keys = preferences.keys();
		} catch (BackingStoreException e) {
			logError(e.getMessage(), e);
			return true;
		}
		for (int i = 0; i < keys.length; i++)
			if (keys[i].equals(name))
				return false;
		return true;
	}

	/**
	 * Sets the current value of the property with the given name back
	 * to its default value. Has no effect if the property does not have
	 * its own current value.
	 * <p>
	 * Note that the recommended way of re-initializing a property to the
	 * appropriate default value is to call <code>setToDefault</code>.
	 * This is implemented by removing the named value from the object, 
	 * thereby exposing the default value.
	 * </p>
	 * <p>
	 * A property change event is always reported. In the event
	 * object, the property name is the name of the property, and the
	 * old and new values are either strings, or <code>null</code> 
	 * indicating the default-default value.
	 * </p>
	 *
	 * @param name the name of the property
	 */
	public void setToDefault(String name) {
		Object oldValue = preferences.get(name, null);
		if (oldValue != null)
			preferences.remove(name);
	}

	/**
	 * Returns a list of all properties known to this preference object which
	 * have current values other than their default value.
	 *
	 * @return an array of property names 
	 */
	public String[] propertyNames() {
		try {
			return preferences.keys();
		} catch (BackingStoreException e) {
			logError(e.getMessage(), e);
			return new String[0];
		}
	}

	/**
	 * Returns a list of all properties known to this preference object which
	 * have default values other than their default-default value.
	 *
	 * @return an array of property names 
	 */
	public String[] defaultPropertyNames() {
		try {
			return defaults.keys();
		} catch (BackingStoreException e) {
			logError(e.getMessage(), e);
			return new String[0];
		}
	}

	/**
	 * Returns whether the current values in this preference object
	 * require saving.
	 *
	 * @return <code>true</code> if at least one of the properties
	 *  known to this preference object has a current value different from its
	 *  default value, and <code>false</code> otherwise
	 */
	public boolean needsSaving() {
		return dirty;
	}

	/**
	 * Flush the values of these plug-in preferences to disk.
	 * 
	 * @throws BackingStoreException
	 */
	public void flush() throws BackingStoreException {
		if (dirty)
			preferences.flush();
		dirty = false;
	}

	/**
	 *  Sync the values in memory with those which are persisted.
	 * 
	 * @throws BackingStoreException
	 */
	public void sync() throws BackingStoreException {
		// don't check the dirty flag first because there could be changes
		// on disk that we want
		preferences.sync();
		dirty = false;
	}

	/*
	 * Something bad happened so log it. 
	 */
	private void logError(String message, Exception e) {
		IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.ERROR, message, e);
		InternalPlatform.getDefault().log(status);
	}

	/*
	 * @see org.eclipse.core.runtime.Preferences#load(java.io.InputStream)
	 */
	public void load(InputStream in) throws IOException {
		Properties result = new Properties();
		result.load(in);
		convertFromProperties(result);
		dirty = false;
	}

	/*
	 * @see org.eclipse.core.runtime.Preferences#store(java.io.OutputStream, java.lang.String)
	 */
	public void store(OutputStream out, String header) throws IOException {
		Properties result = convertToProperties();
		result.store(out, header);
		dirty = false;
	}

	private void convertFromProperties(Properties props) {
		for (Iterator i = props.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			String value = props.getProperty(key);
			if (value != null)
				preferences.put(key, value);
		}
	}

	/*
	 * Convert the preferences in this node to a properties file
	 * suitable for persistence.
	 */
	private Properties convertToProperties() {
		Properties result = new Properties();
		String[] keys = propertyNames();
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			String value = getString(key);
			if (!"".equals(value)) //$NON-NLS-1$
				result.put(key, value);
		}
		return result;
	}
}
