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
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.INodeChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.NodeChangeEvent;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;

import org.eclipse.ui.internal.misc.Assert;

/**
 * The ScopedPreferenceStore is an IPreferenceStore that uses the 
 * scopes provided in org.eclipse.core.runtime.preferences.
 * 
 * A ScopedPreferenceStore does the lookup of a preference based on 
 * it's search scopes and sets the value of the preference based on
 * its store scope.
 * The store scope and the default cope are always included in the 
 * search scopes.
 * @see org.eclipse.core.runtime.preferences
 */
public class ScopedPreferenceStore implements IPreferenceStore {

	/**
	 * Identity list of old listeners (element type: 
	 * <code>org.eclipse.jface.util.IPropertyChangeListener</code>).
	 */
	private ListenerList listeners = new ListenerList();

	/**
	 * The storeContext is the context where values will stored with the
	 * setValue methods. If there are no searchContexts this will be the
	 * search context.
	 */
	private IScopeContext storeContext;

	/**
	 * The searchContext is the array of contexts that will be used by the
	 * get methods for searching for values. 
	 */
	private IScopeContext[] searchContexts;

	/**
	 * The cachedContexts is the path used for searching. It will be
	 * either the searchContexts plus the defaultContext or the scopeContext
	 * plus the defaultContext.
	 */
	private IScopeContext[] cachedContexts;

	/**
	 * The listener on the IEclipsePreferences. This is used to forward
	 * updates to the property change listeners on the preference store.
	 */
	IEclipsePreferences.IPreferenceChangeListener preferencesListener;

	/**
	 * The default context is the context where getDefault and setDefault 
	 * methods will search. This context is also used in the search.
	 */
	private IScopeContext defaultContext = new DefaultScope();

	/**
	 * The nodeQualifer is the string used to look up the node in the 
	 * contexts.
	 */
	String nodeQualifier;

	/**
	 * Create a new instance of the receiver. Store the values in context
	 * in the node looked up by qualifier.
	 * @param context The scope to store and retrieve values from.
	 * @param qualifier The qualifer used to look up the preference node.
	 */
	public ScopedPreferenceStore(IScopeContext context, String qualifier) {
		storeContext = context;
		nodeQualifier = qualifier;
		//Set the search contexts to be the context by default
		setSearchContexts(new IScopeContext[] { context });

		preferencesListener = new IEclipsePreferences.IPreferenceChangeListener() {
			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener#preferenceChange(org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent)
			 */
			public void preferenceChange(PreferenceChangeEvent event) {

				Object oldValue = event.getOldValue();
				Object newValue = event.getNewValue();
				String key = event.getKey();
				if (newValue == null)
					newValue = getDefault(key, oldValue);
				else if (oldValue == null)
					oldValue = getDefault(key, newValue);

				firePropertyChangeEvent(event.getKey(), oldValue, newValue);
			}
		};

		getStorePreferences().addPreferenceChangeListener(preferencesListener);
		getDefaultPreferences().addPreferenceChangeListener(preferencesListener);

		Platform.getPreferencesService().getRootNode().addNodeChangeListener(
				getNodeChangeListener());

	}

	/**
	 * Return a node change listener that adds a removes the receiver when
	 * nodes change.
	 * @return INodeChangeListener
	 */
	private INodeChangeListener getNodeChangeListener() {
		return new IEclipsePreferences.INodeChangeListener() {
			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences.INodeChangeListener#added(org.eclipse.core.runtime.preferences.IEclipsePreferences.NodeChangeEvent)
			 */
			public void added(NodeChangeEvent event) {
				if (nodeQualifier.equals(event.getChild().name()))
					getStorePreferences().addPreferenceChangeListener(preferencesListener);

			}

			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences.INodeChangeListener#removed(org.eclipse.core.runtime.preferences.IEclipsePreferences.NodeChangeEvent)
			 */
			public void removed(NodeChangeEvent event) {
				if (nodeQualifier.equals(event.getChild().name()))
					getStorePreferences().removePreferenceChangeListener(preferencesListener);

			}
		};
	}

	/**
	 * Does its best at determining the default value for the given key. Checks the
	 * given object's type and then looks in the list of defaults to see if a value
	 * exists. If not or if there is a problem converting the value, the default default 
	 * value for that type is returned.
	 * @param key the key to seatrch
	 * @param obj The object who default we are looking for.
	 * @return Object or <code>null</code>
	 */
	Object getDefault(String key, Object obj) {
		IEclipsePreferences defaults = getDefaultPreferences();
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
		else if (obj instanceof Boolean)
			return new Boolean(defaults.getBoolean(key, BOOLEAN_DEFAULT_DEFAULT));
		else
			return null;
	}

	/**
	 * Return the IEclipsePreferences for the store.
	 * @return IEclipsePreferences
	 */
	IEclipsePreferences getStorePreferences() {
		return storeContext.getNode(nodeQualifier);
	}

	/**
	 * Return the default IEclipsePreferences for the store.
	 * @return IEclipsePreferences
	 */
	private IEclipsePreferences getDefaultPreferences() {
		return defaultContext.getNode(nodeQualifier);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * Return the preference path to search preferences on. Return the search contexts
	 * plus the default if there are search contexts, otherwise return the
	 * store contexts plus the default.
	 * @return IEclipsePreferences[] 
	 * @see #getSearchPath()
	 */
	private IEclipsePreferences[] getCachedSearchPath() {
		return getPreferencesFor(cachedContexts);
	}

	/**
	 * Return the preferencepath to search preferences on. Return the search contexts
	 * if there are search contexts, otherwise return the
	 * store context.
	 * @return IEclipsePreferences[] 
	 * @see #getCachedSearchPath() for the version of this method that 
	 * includes the defaults.
	 */
	private IEclipsePreferences[] getSearchPath() {
		return getPreferencesFor(searchContexts);
	}

	/**
	 * Get the preferences that are dervied the qualified values for the scopes.
	 * @param scopes
	 * @return IEclipsePreferences[] 
	 */
	private IEclipsePreferences[] getPreferencesFor(IScopeContext[] scopes) {
		IEclipsePreferences[] preferences = new IEclipsePreferences[scopes.length];
		for (int i = 0; i < scopes.length; i++) {
			preferences[i] = scopes[i].getNode(nodeQualifier);
		}
		return preferences;
	}

	/**
	 * Set the search contexts to scopes. When searching for a value the
	 * seach will be done in the order of scopes and will not search the
	 * storeContext unless it is in this list. The defaultContext will always
	 * be searched whether or not it is added here.
	 * @param scopes the scopes to search. This should not include the defaultScope
	 * as it will be used for the isDefault method.
	 */
	public void setSearchContexts(IScopeContext[] scopes) {

		searchContexts = scopes;

		//Assert that the default was not included
		for (int i = 0; i < scopes.length; i++) {
			if (scopes[i].equals(defaultContext))
				Assert.isTrue(false, WorkbenchMessages
						.getString("ScopedPreferenceStore.DefaultAddedError")); //$NON-NLS-1$
		}

		//Add the default to the search contexts
		IScopeContext[] newScopes = new IScopeContext[scopes.length + 1];
		System.arraycopy(scopes, 0, newScopes, 0, scopes.length);
		newScopes[scopes.length] = defaultContext;
		cachedContexts = newScopes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#contains(java.lang.String)
	 */
	public boolean contains(String name) {
		if (name == null)
			return false;
		return (Platform.getPreferencesService().get(name, null, getCachedSearchPath())) != null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#firePropertyChangeEvent(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	public void firePropertyChangeEvent(String name, Object oldValue, Object newValue) {

		// efficiently handle case of 0 listeners
		if (listeners.isEmpty()) {
			// no one interested
			return;
		}

		// important: create intermediate array to protect against listeners 
		// being added/removed during the notification
		final Object[] list = listeners.getListeners();
		final PropertyChangeEvent event = new PropertyChangeEvent(this, name, oldValue, newValue);
		for (int i = 0; i < list.length; i++) {
			final IPropertyChangeListener listener = (IPropertyChangeListener) list[i];
			Platform.run(new SafeRunnable(JFaceResources.getString("PreferenceStore.changeError")) { //$NON-NLS-1$
						public void run() {
							listener.propertyChange(event);
						}
					});
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getBoolean(java.lang.String)
	 */
	public boolean getBoolean(String name) {
		return Platform.getPreferencesService().getBoolean(nodeQualifier, name,
				getDefaultBoolean(name), cachedContexts);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultBoolean(java.lang.String)
	 */
	public boolean getDefaultBoolean(String name) {
		return getDefaultPreferences().getBoolean(name, Preferences.BOOLEAN_DEFAULT_DEFAULT);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultDouble(java.lang.String)
	 */
	public double getDefaultDouble(String name) {
		return getDefaultPreferences().getDouble(name, Preferences.DOUBLE_DEFAULT_DEFAULT);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultFloat(java.lang.String)
	 */
	public float getDefaultFloat(String name) {
		return getDefaultPreferences().getFloat(name, Preferences.FLOAT_DEFAULT_DEFAULT);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultInt(java.lang.String)
	 */
	public int getDefaultInt(String name) {
		return getDefaultPreferences().getInt(name, Preferences.INT_DEFAULT_DEFAULT);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultLong(java.lang.String)
	 */
	public long getDefaultLong(String name) {
		return getDefaultPreferences().getLong(name, Preferences.LONG_DEFAULT_DEFAULT);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultString(java.lang.String)
	 */
	public String getDefaultString(String name) {
		return getDefaultPreferences().get(name, Preferences.STRING_DEFAULT_DEFAULT);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDouble(java.lang.String)
	 */
	public double getDouble(String name) {
		return Platform.getPreferencesService().getDouble(nodeQualifier, name,
				getDefaultDouble(name), cachedContexts);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getFloat(java.lang.String)
	 */
	public float getFloat(String name) {
		return Platform.getPreferencesService().getFloat(nodeQualifier, name,
				getDefaultFloat(name), cachedContexts);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getInt(java.lang.String)
	 */
	public int getInt(String name) {
		return Platform.getPreferencesService().getInt(nodeQualifier, name, getDefaultInt(name),
				cachedContexts);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getLong(java.lang.String)
	 */
	public long getLong(String name) {
		return Platform.getPreferencesService().getLong(nodeQualifier, name, getDefaultLong(name),
				cachedContexts);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getString(java.lang.String)
	 */
	public String getString(String name) {
		return Platform.getPreferencesService().getString(nodeQualifier, name,
				getDefaultString(name), cachedContexts);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#isDefault(java.lang.String)
	 */
	public boolean isDefault(String name) {
		if (name == null)
			return false;
		return (Platform.getPreferencesService().get(name, null, getSearchPath())) == null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#needsSaving()
	 */
	public boolean needsSaving() {
		//Always return true as there is no API on the IEclipsePreferences
		//to check this. The internal implementation will not save if there
		//is a nothing to save.
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#putValue(java.lang.String, java.lang.String)
	 */
	public void putValue(String name, String value) {
		getStorePreferences().put(name, value);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		listeners.remove(listener);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, double)
	 */
	public void setDefault(String name, double value) {
		getDefaultPreferences().putDouble(name, value);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, float)
	 */
	public void setDefault(String name, float value) {
		getDefaultPreferences().putFloat(name, value);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, int)
	 */
	public void setDefault(String name, int value) {
		getDefaultPreferences().putInt(name, value);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, long)
	 */
	public void setDefault(String name, long value) {
		getDefaultPreferences().putLong(name, value);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, java.lang.String)
	 */
	public void setDefault(String name, String defaultObject) {
		getDefaultPreferences().put(name, defaultObject);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, boolean)
	 */
	public void setDefault(String name, boolean value) {
		getDefaultPreferences().putBoolean(name, value);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setToDefault(java.lang.String)
	 */
	public void setToDefault(String name) {
		IEclipsePreferences preferences = getStorePreferences();
		Object oldValue = preferences.get(name, null);
		if (oldValue != null)
			preferences.remove(name);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, double)
	 */
	public void setValue(String name, double value) {
		getStorePreferences().putDouble(name, value);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, float)
	 */
	public void setValue(String name, float value) {
		getStorePreferences().putFloat(name, value);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, int)
	 */
	public void setValue(String name, int value) {
		getStorePreferences().putInt(name, value);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, long)
	 */
	public void setValue(String name, long value) {
		getStorePreferences().putLong(name, value);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, java.lang.String)
	 */
	public void setValue(String name, String value) {
		getStorePreferences().put(name, value);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, boolean)
	 */
	public void setValue(String name, boolean value) {
		getStorePreferences().putBoolean(name, value);

	}

}