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

import java.util.HashMap;
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
		// no-op for the root...only applicable for scope roots
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#sync()
	 */
	public void sync() throws BackingStoreException {
		// no-op for the root...only applicable for scope roots
	}

	public void addChild(IEclipsePreferences child) {
		if (children == null)
			children = new HashMap();
		children.put(child.name(), child);
	}

	public void addChild(String scope) {
		if (children == null)
			children = new HashMap();
		children.put(scope, scope);
	}

	/*
	 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences#node(org.eclipse.core.runtime.IPath)
	 */
	public IEclipsePreferences node(IPath path) {
		if (path.isEmpty())
			return this;
		IEclipsePreferences child = null;
		Object value = null;
		String scope = path.segment(0);
		if (children != null)
			value = children.get(scope);
		if (value == null) {
			child = new EclipsePreferences(this, scope);
			addChild(child);
		} else {
			if (value instanceof IEclipsePreferences)
				child = (IEclipsePreferences) value;
			else {
				child = ((PreferencesService) Platform.getPreferencesService()).createNode(scope);
				addChild(child);
			}
		}
		return child.node(path.removeFirstSegments(1));
	}
}