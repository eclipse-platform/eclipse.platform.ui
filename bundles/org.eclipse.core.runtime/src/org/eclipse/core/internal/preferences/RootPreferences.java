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

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * @since 3.0
 */
public class RootPreferences extends EclipsePreferences {

	ListenerRegistry nodeChangeListeners = new ListenerRegistry();
	ListenerRegistry preferenceChangeListeners = new ListenerRegistry();

	/**
	 * Default constructor.
	 */
	public RootPreferences() {
		super(null, ""); //$NON-NLS-1$
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#flush()
	 */
	public void flush() throws BackingStoreException {
		// flush all children
		BackingStoreException exception = null;
		String[] names = childrenNames();
		for (int i = 0; i < names.length; i++) {
			try {
				node(names[i]).flush();
			} catch (BackingStoreException e) {
				// store the first exception we get and still try and flush
				// the rest of the children.
				if (exception != null)
					exception = e;
			}
		}
		if (exception != null)
			throw exception;
	}

	/*
	 * @see EclipsePreferences#getChild(String, Plugin)
	 */
	protected synchronized IEclipsePreferences getChild(String key, Plugin context) {
		Object value = null;
		IEclipsePreferences child = null;
		if (children != null)
			value = children.get(key);
		if (value != null) {
			if (value instanceof IEclipsePreferences)
				return (IEclipsePreferences) value;
			//lazy initialization
			child = ((PreferencesService) Platform.getPreferencesService()).createNode(key);
			addChild(key, child);
		}
		return child;
	}

	/*
	 * @see EclipsePreferences#getChildren()
	 */
	protected synchronized IEclipsePreferences[] getChildren() {
		//must perform lazy initialization of child nodes
		String[] childNames = childrenNames();
		IEclipsePreferences[] childNodes = new IEclipsePreferences[childNames.length];
		for (int i = 0; i < childNames.length; i++)
			childNodes[i] = getChild(childNames[i], null);
		return childNodes;
	}

	/*
	 * @see Preferences#node(String)
	 */
	public Preferences node(String path) {
		if (path.length() == 0 || (path.length() == 1 && path.charAt(0) == IPath.SEPARATOR))
			return this;
		int startIndex = path.charAt(0) == IPath.SEPARATOR ? 1 : 0;
		int endIndex = path.indexOf(IPath.SEPARATOR, startIndex + 1);
		String scope = path.substring(startIndex, endIndex == -1 ? path.length() : endIndex);
		IEclipsePreferences child = getChild(scope, null);
		if (child == null) {
			child = new EclipsePreferences(this, scope);
			addChild(scope, child);
		}
		return child.node(endIndex == -1 ? "" : path.substring(endIndex + 1)); //$NON-NLS-1$
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#sync()
	 */
	public void sync() throws BackingStoreException {
		// sync all children
		BackingStoreException exception = null;
		String[] names = childrenNames();
		for (int i = 0; i < names.length; i++) {
			try {
				node(names[i]).sync();
			} catch (BackingStoreException e) {
				// store the first exception we get and still try and sync
				// the rest of the children.
				if (exception != null)
					exception = e;
			}
		}
		if (exception != null)
			throw exception;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.internal.preferences.EclipsePreferences#getNodeChangeListenerRegistry()
	 */
	protected ListenerRegistry getNodeChangeListenerRegistry() {
		return nodeChangeListeners;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.internal.preferences.EclipsePreferences#getPreferenceChangeListenerRegistry()
	 */
	protected ListenerRegistry getPreferenceChangeListenerRegistry() {
		return preferenceChangeListeners;
	}
}