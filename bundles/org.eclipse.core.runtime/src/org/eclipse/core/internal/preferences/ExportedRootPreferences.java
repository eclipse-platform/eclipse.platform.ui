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

import org.eclipse.core.runtime.preferences.IExportedPreferences;

/**
 * @since 3.1
 */
public class ExportedRootPreferences extends ExportedPreferences {

	private ListenerRegistry nodeChangeListeners = new ListenerRegistry();
	private ListenerRegistry preferenceChangeListeners = new ListenerRegistry();

	public static IExportedPreferences newRoot() {
		return new ExportedRootPreferences(null, ""); //$NON-NLS-1$
	}

	protected ExportedRootPreferences(EclipsePreferences parent, String name) {
		super(parent, name);
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
