/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.runtime;

import java.io.*;
import java.util.*;

import org.eclipse.core.internal.runtime.PreferenceExporter;

/**
 * A table of preference settings, mapping named properties to values. Property
 * names are non-empty strings; property values can be either booleans,
 * non-null strings, or values of one of the primitive number types.
 * The table consists of two, sparse, layers: the lower layer holds default values 
 * for properties, and the upper layer holds explicitly set values for properties.
 * Normal retrieval looks for an explicitly set value for the given property in
 * the upper layer; if there is nothing for that property in the upper layer, it
 * next looks for a default value for the given property in the lower layer; if
 * there is nothing for that property in the lower layer, it returns a standard
 * default-default value. The default-default values for the primitive types are
 * as follows:
 * <ul>
 * 	<li><code>boolean</code> = <code>false</code></li>
 * 	<li><code>double</code> = <code>0.0</code></li>
 * 	<li><code>float</code> = <code>0.0f</code></li>
 * 	<li><code>int</code> = <code>0</code></li>
 *  <li><code>long</code> = <code>0L</code></li>
 * 	<li><code>String</code> = <code>""</code> (the empty string)</li>
 * </ul>
 * <p>
 * Internally, all properties values (in both layers) are stored as strings.
 * Standard conversions to and from numeric and boolean types are performed on
 * demand.
 * </p>
 * <p>
 * The typical usage is to establish the defaults for all known properties
 * and then restore previously stored values for properties whose values 
 * were explicitly set. The existing settings can be changed and new properties
 * can be set (<code>setValue</code>). If the values specified is the same as 
 * the default value, the explicit setting is deleted from the top layer.
 * It is also possible to reset a property value back to the default value 
 * using <code>setToDefault</code>. After the properties have been modified, 
 * the properties with explicit settings are written to disk. The default values
 * are never saved. This two-tiered approach
 * to saving and restoring property setting minimizes the number of properties
 * that need to be persisted; indeed, the normal starting state does not require
 * storing any properties at all. It also makes it easy to use different 
 * default settings in different environments while maintaining just those
 * property settings the user has adjusted.
 * </p>
 * <p>
 * A property change event is reported whenever a property's value actually
 * changes (either through <code>setValue</code>, <code>setToDefault</code>).
 * Note, however, that manipulating default values (with <code>setDefault</code>)
 * does not cause any events to be reported.
 * </p>
 * <p>
 * Clients may instantiate this class. This class was not designed to be 
 * subclassed.
 * </p>
 * <p>
 * The implementation is based on a pair of internal 
 * <code>java.util.Properties</code> objects, one holding explicitly set values
 * (set using <code>setValue</code>), the other holding the default values
 * (set using <code>setDefaultValue</code>). The <code>load</code> and
 * <code>store</code> methods persist the non-default property values to 
 * streams (the default values are not saved).
 * </p>
 * 
 * @since 2.0
 */
public class Preferences {

	/**
	 * The default-default value for boolean properties (<code>false</code>).
	 */
	public static final boolean BOOLEAN_DEFAULT_DEFAULT = false;

	/**
	 * The default-default value for double properties (<code>0.0</code>).
	 */
	public static final double DOUBLE_DEFAULT_DEFAULT = 0.0;

	/**
	 * The default-default value for float properties (<code>0.0f</code>).
	 */
	public static final float FLOAT_DEFAULT_DEFAULT = 0.0f;

	/**
	 * The default-default value for int properties (<code>0</code>).
	 */
	public static final int INT_DEFAULT_DEFAULT = 0;

	/**
	 * The default-default value for long properties (<code>0L</code>).
	 */
	public static final long LONG_DEFAULT_DEFAULT = 0L;

	/**
	 * The default-default value for String properties (<code>""</code>).
	 */
	public static final String STRING_DEFAULT_DEFAULT = ""; //$NON-NLS-1$

	/**
	 * The string representation used for <code>true</code>
	 * (<code>"true"</code>).
	 */
	private static final String TRUE = "true"; //$NON-NLS-1$

	/**
	 * The string representation used for <code>false</code>
	 * (<code>"false"</code>).
	 */
	private static final String FALSE = "false"; //$NON-NLS-1$

	/**
	 * An event object describing a change to a named property.
	 * <p>
	 * The preferences object reports property change events for internal state
	 * changes that may be of interest to external parties. A special listener
	 * interface (<code>Preferences.IPropertyChangeListener</code>) is 
	 * defined for this purpose. Listeners are registered via the
	 * <code>Preferences.addPropertyChangeListener</code> method.
	 * </p>
	 * <p>
	 * Clients cannot instantiate or subclass this class.
	 * </p>
	 *
	 * @see #addPropertyChangeListener
	 * @see #IPropertyChangeListener
	 */
	public static class PropertyChangeEvent extends EventObject {

		/**
		 * The name of the changed property.
 		 */
		private String propertyName;

		/**
		 * The old value of the changed property, or <code>null</code> if
		 * not known or not relevant.
		 */
		private Object oldValue;

		/**
		 * The new value of the changed property, or <code>null</code> if
		 * not known or not relevant.
		 */
		private Object newValue;

		/**
		 * Creates a new property change event.
		 *
		 * @param source the object whose property has changed
		 * @param property the property that has changed (must not be 
		 *    <code>null</code>)
		 * @param oldValue the old value of the property, or 
		 *    <code>null</code> if none
		 * @param newValue the new value of the property, or 
		 *    <code>null</code> if none
		 */
		PropertyChangeEvent(
			Object source,
			String property,
			Object oldValue,
			Object newValue) {

			super(source);
			if (property == null) {
				throw new IllegalArgumentException();
			}
			this.propertyName = property;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		/**
		 * Returns the name of the property that changed.
		 * <p>
		 * Warning: there is no guarantee that the property name returned
		 * is a constant string.  Callers must compare property names using
		 * <code>equals</code>, not ==.
		 *
		 * @return the name of the property that changed
		 */
		public String getProperty() {
			return propertyName;
		}

		/**
		 * Returns the new value of the property.
		 *
		 * @return the new value, or <code>null</code> if not known
		 *  or not relevant
		 */
		public Object getNewValue() {
			return newValue;
		}

		/**
		 * Returns the old value of the property.
		 *
		 * @return the old value, or <code>null</code> if not known
		 *  or not relevant
		 */
		public Object getOldValue() {
			return oldValue;
		}
	}

	/**
	 * Listener for property changes.
	 * <p>
	 * Usage:
	 * <pre>
	 * Preferences.IPropertyChangeListener listener =
	 *   new Preferences.IPropertyChangeListener() {
	 *      public void propertyChange(Preferences.PropertyChangeEvent event) {
	 *         ... // code to deal with occurrence of property change
	 *      }
	 *   };
	 * emitter.addPropertyChangeListener(listener);
	 * ...
	 * emitter.removePropertyChangeListener(listener);
	 * </pre>
	 * </p>
	 */
	public interface IPropertyChangeListener extends EventListener {

		/**
		 * Notification that a property has changed.
		 * <p>
		 * This method gets called when the observed object fires a property
		 * change event.
		 * </p>
		 *
		 * @param event the property change event object describing which
		 *    property changed and how
		 */
		public void propertyChange(Preferences.PropertyChangeEvent event);
	}

	/**
	 * Internal class is used to maintain a list of listeners.
	 * It is a fairly lightweight object, occupying minimal space when
	 * no listeners are registered.
	 * <p>
	 * Note that the <code>add</code> method checks for and eliminates 
	 * duplicates based on identity (not equality).  Likewise, the
	 * <code>remove</code> method compares based on identity.
	 * </p>
	 * <p>
	 * Use the <code>getListeners</code> method when notifying listeners.
	 * Note that no garbage is created if no listeners are registered.
	 * The recommended code sequence for notifying all registered listeners
	 * of say, <code>FooListener.eventHappened</code>, is:
	 * <pre>
	 * Object[] listeners = myListenerList.getListeners();
	 * for (int i = 0; i &lt; listeners.length; ++i) {
	 *    ((FooListener) listeners[i]).eventHappened(event);
	 * }
	 * </pre>
	 * </p>
	 */
	private static class ListenerList {
		/**
		 * The initial capacity of the list. Always >= 1.
		 */
		private int capacity;

		/**
		 * The current number of listeners.
		 * Maintains invariant: 0 <= size <= listeners.length.
		 */
		private int size;

		/**
		 * The list of listeners.  Initially <code>null</code> but initialized
		 * to an array of size capacity the first time a listener is added.
		 * Maintains invariant: listeners != null IFF size != 0
		 */
		private Object[] listeners = null;

		/**
		 * The empty array singleton instance, returned by getListeners()
		 * when size == 0.
		 */
		private static final Object[] EmptyArray = new Object[0];

		/**
		 * Creates a listener list with an initial capacity of 3.
		 */
		public ListenerList() {
			this(3);
		}

		/**
		 * Creates a listener list with the given initial capacity.
		 *
		 * @param capacity the number of listeners which this list can initially 
		 *    accept without growing its internal representation; must be at
		 *    least 1
		 */
		public ListenerList(int capacity) {
			if (capacity < 1) {
				throw new IllegalArgumentException();
			}
			this.capacity = capacity;
		}

		/**
		 * Adds the given listener to this list. Has no effect if an identical 
		 * listener is already registered.
		 *
		 * @param listener the listener
		 */
		public void add(Object listener) {
			if (listener == null) {
				throw new IllegalArgumentException();
			}
			if (size == 0) {
				listeners = new Object[capacity];
			} else {
				// check for duplicates using identity
				for (int i = 0; i < size; ++i) {
					if (listeners[i] == listener) {
						return;
					}
				}
				// grow array if necessary
				if (size == listeners.length) {
					System.arraycopy(listeners, 0, listeners = new Object[size * 2 + 1], 0, size);
				}
			}
			listeners[size++] = listener;
		}

		/**
		 * Returns an array containing all the registered listeners.
		 * The resulting array is unaffected by subsequent adds or removes.
		 * If there are no listeners registered, the result is an empty array
		 * singleton instance (no garbage is created).
		 * Use this method when notifying listeners, so that any modifications
		 * to the listener list during the notification will have no effect on 
		 * the notification itself.
		 *
		 * @return the list of registered listeners
		 */
		public Object[] getListeners() {
			if (size == 0)
				return EmptyArray;
			Object[] result = new Object[size];
			System.arraycopy(listeners, 0, result, 0, size);
			return result;
		}

		/**
		 * Returns whether this listener list is empty.
		 *
		 * @return <code>true</code> if there are no registered listeners, and
		 *   <code>false</code> otherwise
		 */
		public boolean isEmpty() {
			return size == 0;
		}

		/**
		 * Removes the given listener from this list. Has no effect if an 
		 * identical listener was not already registered.
		 *
		 * @param listener the listener
		 */
		public void remove(Object listener) {
			if (listener == null) {
				throw new IllegalArgumentException();
			}
			for (int i = 0; i < size; ++i) {
				if (listeners[i] == listener) {
					if (size == 1) {
						listeners = null;
						size = 0;
					} else {
						System.arraycopy(listeners, i + 1, listeners, i, --size - i);
						listeners[size] = null;
					}
					return;
				}
			}
		}

		/**
		 * Returns the number of registered listeners.
		 *
		 * @return the number of registered listeners
		 */
		public int size() {
			return size;
		}
	}

	/** 
	 * List of registered listeners (element type: 
	 * <code>IPropertyChangeListener</code>).
	 * These listeners are to be informed when the current value of a property
	 * changes.
	 */
	private ListenerList listeners = new ListenerList();

	/**
	 * The mapping from property name to
	 * property value (represented as strings).
	 */
	private Properties properties;

	/**
	 * The mapping from property name to
	 * default property value (represented as strings);
	 * <code>null</code> if none.
	 */
	private Properties defaultProperties;

	/**
	 * Indicates whether a value has been changed by <code>setToDefault</code>
	 * or <code>setValue</code>; initially <code>false</code>.
	 */
	private boolean dirty = false;

	/**
	 * Exports all non-default-valued preferences for all installed plugins to the 
	 * provided file. if a file already exists at the given location, it will be deleted.
	 * If there are no preferences to export, no file will be written.
	 * <p>
	 * The file that is written can be read later using the importPreferences method.
	 * </p>
	 * @param file The absolute filesystem path of the file to export preferences to.
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> The file could not be written.</li>
	 * </ul>
	 * @see #importPreferences
	 * @see #validatePreferenceVersions
	 */
	public static void exportPreferences(IPath file) throws CoreException {
		PreferenceExporter.exportPreferences(file);
	}
	/**
	 * Loads the plugin preferences from the given file, and replaces all 
	 * non-default-valued preferences for all plugins with the values from this file.
	 * <p>
	 * If the file contains preferences for plug-ins that don't exist in the current
	 * install, they are ignored.  This method does not validate if the plug-in
	 * versions in the preference file match the currently installed plug-ins.
	 * Clients should first call validatePreferenceVersions on the file to ensure
	 * that the versions are compatible.
	 * </p>
	 * <p>
	 * The file must have been written by the exportPreferences method.
	 * </p>
	 * @param file The absolute filesystem path of the file to import preferences from.
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> The file does not exist.</li>
	 * <li> The file could not be read.</li>
	 * </ul>
	 * @see #exportPreferences
	 * @see #validatePreferenceVersions
	 */
	public static void importPreferences(IPath file) throws CoreException {
		PreferenceExporter.importPreferences(file);
	}
	/**
	 * Validates that the preference versions in the given file match the versions
	 * of the currently installed plugins.  Returns an OK status if all preferences match 
	 * the currently installed plugins, otherwise a MultiStatus describing what 
	 * plugins have preferences that don't match.  
	 * <p>
	 * If the returned status has a <code>IStatus.WARNING</code> severity, 
	 * it means that some preferences may not be applicable but for the most 
	 * part they will be compatible.  If the returned status has a 
	 * <code>IStatus.ERROR</code> severity, it means that the preferences 
	 * will probably not be compatible.
	 * <p>
	 * If the file contains preferences for plug-ins that don't exist in the current
	 * install, they are ignored.
	 * </p>
	 * <p>
	 * The file must have been written by the exportPreferences method.
	 * </p>
	 * @param file The absolute filesystem path of the preference file to validate.
	 * @see #exportPreferences
	 * @see #importPreferences
	 */
	public static IStatus validatePreferenceVersions(IPath file) {
		return PreferenceExporter.validatePreferenceVersions(file);
	}
	/**
	 * Creates an empty preference table.
	 * <p>
	 * Use the methods <code>load(InputStream)</code> and
	 * <code>store(InputStream)</code> to load and store these preferences.
	 * </p>
	 * @see #load
	 * @see #store
	 */
	public Preferences() {
		defaultProperties = new Properties();
		properties = new Properties(defaultProperties);
	}

	/**
	 * Adds a property change listener to this preference object.
	 * Has no affect if the identical listener is already registered.
	 *
	 * @param listener a property change listener
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes the given listener from this preference object.
	 * Has no affect if the listener is not registered.
	 *
	 * @param listener a property change listener
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		listeners.remove(listener);
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
		return (properties.containsKey(name) || defaultProperties.containsKey(name));
	}

	/**
	 * Fires a property change event corresponding to a change to the
	 * current value of the property with the given name.
	 *
	 * @param name the name of the property, to be used as the property
	 *  in the event object
	 * @param oldValue the old value, or <code>null</code> if not known or not
	 *    relevant
	 * @param newValue the new value, or <code>null</code> if not known or not
	 *    relevant
	 */
	private void firePropertyChangeEvent(
		String name,
		Object oldValue,
		Object newValue) {

		if (name == null) {
			throw new IllegalArgumentException();
		}
		Object[] listeners = this.listeners.getListeners();
		// Do we even need to fire an event?
		if (listeners.length > 0) {
			PropertyChangeEvent pe =
				new PropertyChangeEvent(this, name, oldValue, newValue);
			for (int i = 0; i < listeners.length; ++i) {
				IPropertyChangeListener l = (IPropertyChangeListener) listeners[i];
				l.propertyChange(pe);
			}
		}
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
		String value = properties.getProperty(name);
		if (value == null) {
			return BOOLEAN_DEFAULT_DEFAULT;
		}
		return value.equals(Preferences.TRUE);
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
		boolean defaultValue = getDefaultBoolean(name);
		boolean oldValue = getBoolean(name);
		if (value == defaultValue) {
			Object removed = properties.remove(name);
			if (removed != null) {
				// removed an explicit setting
				dirty = true;
			}
		} else {
			properties.put(name, value ? Preferences.TRUE : Preferences.FALSE);
		}
		if (oldValue != value) {
			// mark as dirty since value did really change
			dirty = true;
			// report property change if getValue now returns different value
			firePropertyChangeEvent(name, new Boolean(oldValue), new Boolean(value));
		}
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
		String value = defaultProperties.getProperty(name);
		if (value == null) {
			return BOOLEAN_DEFAULT_DEFAULT;
		}
		return value.equals(Preferences.TRUE);
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
		defaultProperties.put(name, value ? Preferences.TRUE : Preferences.FALSE);
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
		return convertToDouble(properties.getProperty(name), DOUBLE_DEFAULT_DEFAULT);
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
		if (Double.isNaN(value)) {
			throw new IllegalArgumentException();
		}
		double defaultValue = getDefaultDouble(name);
		double oldValue = getDouble(name);
		if (value == defaultValue) {
			Object removed = properties.remove(name);
			if (removed != null) {
				// removed an explicit setting
				dirty = true;
			}
		} else {
			properties.put(name, Double.toString(value));
		}
		if (oldValue != value) {
			// mark as dirty since value did really change
			dirty = true;
			// report property change if getValue now returns different value
			firePropertyChangeEvent(name, new Double(oldValue), new Double(value));
		}
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
		return convertToDouble(
			defaultProperties.getProperty(name),
			DOUBLE_DEFAULT_DEFAULT);
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
		if (Double.isNaN(value)) {
			throw new IllegalArgumentException();
		}
		defaultProperties.put(name, Double.toString(value));
	}

	/**
	 * Converts the given raw property value string to a double.
	 * 
	 * @param rawPropertyValue the raw property value, or <code>null</code>
	 *   if none
	 * @param defaultValue the default value
	 * @return the raw value converted to a double, or the given 
	 *    <code>defaultValue</code> if the raw value is <code>null</code> or
	 *    cannot be parsed as a double
	 */
	private double convertToDouble(String rawPropertyValue, double defaultValue) {
		double result = defaultValue;
		if (rawPropertyValue != null) {
			try {
				result = new Double(rawPropertyValue).doubleValue();
			} catch (NumberFormatException e) {
				// raw value cannot be treated as one of these
			}
		}
		return result;
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
		return convertToFloat(properties.getProperty(name), FLOAT_DEFAULT_DEFAULT);
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
		if (Float.isNaN(value)) {
			throw new IllegalArgumentException();
		}
		float defaultValue = getDefaultFloat(name);
		float oldValue = getFloat(name);
		if (value == defaultValue) {
			Object removed = properties.remove(name);
			if (removed != null) {
				// removed an explicit setting
				dirty = true;
			}
		} else {
			properties.put(name, Float.toString(value));
		}
		if (oldValue != value) {
			// mark as dirty since value did really change
			dirty = true;
			// report property change if getValue now returns different value
			firePropertyChangeEvent(name, new Float(oldValue), new Float(value));
		}
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
		return convertToFloat(
			defaultProperties.getProperty(name),
			FLOAT_DEFAULT_DEFAULT);
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
		if (Float.isNaN(value)) {
			throw new IllegalArgumentException();
		}
		defaultProperties.put(name, Float.toString(value));
	}

	/**
	 * Converts the given raw property value string to a float.
	 * 
	 * @param rawPropertyValue the raw property value, or <code>null</code>
	 *   if none
	 * @param defaultValue the default value
	 * @return the raw value converted to a float, or the given 
	 *    <code>defaultValue</code> if the raw value is <code>null</code> or
	 *    cannot be parsed as a float
	 */
	private float convertToFloat(String rawPropertyValue, float defaultValue) {
		float result = defaultValue;
		if (rawPropertyValue != null) {
			try {
				result = new Float(rawPropertyValue).floatValue();
			} catch (NumberFormatException e) {
				// raw value cannot be treated as one of these
			}
		}
		return result;
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
		return convertToInt(properties.getProperty(name), INT_DEFAULT_DEFAULT);
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
		int defaultValue = getDefaultInt(name);
		int oldValue = getInt(name);
		if (value == defaultValue) {
			Object removed = properties.remove(name);
			if (removed != null) {
				// removed an explicit setting
				dirty = true;
			}
		} else {
			properties.put(name, Integer.toString(value));
		}
		if (oldValue != value) {
			// mark as dirty since value did really change
			dirty = true;
			// report property change if getValue now returns different value
			firePropertyChangeEvent(name, new Integer(oldValue), new Integer(value));
		}
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
		return convertToInt(defaultProperties.getProperty(name), INT_DEFAULT_DEFAULT);
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
		defaultProperties.put(name, Integer.toString(value));
	}

	/**
	 * Converts the given raw property value string to an int.
	 * 
	 * @param rawPropertyValue the raw property value, or <code>null</code>
	 *   if none
	 * @param defaultValue the default value
	 * @return the raw value converted to an int, or the given 
	 *    <code>defaultValue</code> if the raw value is <code>null</code> or
	 *    cannot be parsed as an int
	 */
	private int convertToInt(String rawPropertyValue, int defaultValue) {
		int result = defaultValue;
		if (rawPropertyValue != null) {
			try {
				result = Integer.parseInt(rawPropertyValue);
			} catch (NumberFormatException e) {
				// raw value cannot be treated as one of these
			}
		}
		return result;
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
		return convertToLong(properties.getProperty(name), LONG_DEFAULT_DEFAULT);
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
		long defaultValue = getDefaultLong(name);
		long oldValue = getLong(name);
		if (value == defaultValue) {
			Object removed = properties.remove(name);
			if (removed != null) {
				// removed an explicit setting
				dirty = true;
			}
		} else {
			properties.put(name, Long.toString(value));
		}
		if (oldValue != value) {
			// mark as dirty since value did really change
			dirty = true;
			// report property change if getValue now returns different value
			firePropertyChangeEvent(name, new Long(oldValue), new Long(value));
		}
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
		return convertToLong(defaultProperties.getProperty(name), LONG_DEFAULT_DEFAULT);
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
		defaultProperties.put(name, Long.toString(value));
	}

	/**
	 * Converts the given raw property value string to a long.
	 * 
	 * @param rawPropertyValue the raw property value, or <code>null</code>
	 *   if none
	 * @param defaultValue the default value
	 * @return the raw value converted to a long, or the given 
	 *    <code>defaultValue</code> if the raw value is <code>null</code> or
	 *    cannot be parsed as a long
	 */
	private long convertToLong(String rawPropertyValue, long defaultValue) {
		long result = defaultValue;
		if (rawPropertyValue != null) {
			try {
				result = Long.parseLong(rawPropertyValue);
			} catch (NumberFormatException e) {
				// raw value cannot be treated as one of these
			}
		}
		return result;
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
		String value = properties.getProperty(name);
		return (value != null ? value : STRING_DEFAULT_DEFAULT);
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
		if (value == null) {
			throw new IllegalArgumentException();
		}
		String defaultValue = getDefaultString(name);
		String oldValue = getString(name);
		if (value.equals(defaultValue)) {
			Object removed = properties.remove(name);
			if (removed != null) {
				// removed an explicit setting
				dirty = true;
			}
		} else {
			properties.put(name, value);
		}
		if (!oldValue.equals(value)) {
			// mark as dirty since value did really change
			dirty = true;
			// report property change if getValue now returns different value
			firePropertyChangeEvent(name, oldValue, value);
		}
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
		String value = defaultProperties.getProperty(name);
		return (value != null ? value : STRING_DEFAULT_DEFAULT);
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
		if (value == null) {
			throw new IllegalArgumentException();
		}
		defaultProperties.put(name, value);
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
		return !properties.containsKey(name);
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
		Object oldPropertyValue = properties.remove(name);
		if (oldPropertyValue != null) {
			dirty = true;
		}
		String newValue = defaultProperties.getProperty(name, null);
		// n.b. newValue == null if there is no default value
		// can't determine correct default-default without knowing type
		firePropertyChangeEvent(name, oldPropertyValue, newValue);
	}

	/**
	 * Returns a list of all properties known to this preference object which
	 * have current values other than their default value.
	 *
	 * @return an array of property names 
	 */
	public String[] propertyNames() {
		String[] result = new String[properties.size()];
		properties.keySet().toArray(result);
		return result;
	}

	/**
	 * Returns a list of all properties known to this preference object which
	 * have default values other than their default-default value.
	 *
	 * @return an array of property names 
	 */
	public String[] defaultPropertyNames() {
		String[] result = new String[defaultProperties.size()];
		defaultProperties.keySet().toArray(result);
		return result;
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
	 * Saves the non-default-valued properties known to this preference object to
	 * the given output stream using 
	 * <code>Properties.store(OutputStream,String)</code>.
	 * <p>
	 * Note that the output is unconditionally written, even when
	 * <code>needsSaving</code> is <code>false</code>.
	 * </p>
	 *
	 * @param out the output stream 
	 * @param header a comment to be included in the output, or 
	 *    <code>null</code> if none
	 * @exception IOException if there is a problem saving this preference object
	 * @see Properties#store(OutputStream,String)
	 */
	public void store(OutputStream out, String header) throws IOException {
		properties.store(out, header);
		dirty = false;
	}

	/**
	 * Loads the non-default-valued properties for this preference object from the
	 * given input stream using 
	 * <code>java.util.Properties.load(InputStream)</code>. Default property
	 * values are not affected.
	 *
	 * @param in the input stream
	 * @exception IOException if there is a problem loading this preference
	 *    object
	 * @see java.util.Properties#load(InputStream)
	 */
	public void load(InputStream in) throws IOException {
		properties.load(in);
		dirty = false;
	}
}