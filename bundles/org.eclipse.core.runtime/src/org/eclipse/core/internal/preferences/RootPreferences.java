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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

/**
 * @since 3.0
 */
public class RootPreferences extends EclipsePreferences {

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
	 * @see EclipsePreferences#getChild(String)
	 */
	protected synchronized IEclipsePreferences getChild(String key) {
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
			childNodes[i] = getChild(childNames[i]);
		return childNodes;
	}

	/*
	 * @see IEclipsePreferences#node(IPath)
	 */
	public IEclipsePreferences node(IPath path) {
		if (path.segmentCount() == 0)
			return this;
		String scope = path.segment(0);
		IEclipsePreferences child = getChild(scope);
		if (child == null) {
			child = new EclipsePreferences(this, scope);
			addChild(scope, child);
		}
		return child.node(path.removeFirstSegments(1));
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
}