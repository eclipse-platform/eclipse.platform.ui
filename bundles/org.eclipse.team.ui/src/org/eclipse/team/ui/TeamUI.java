/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.synchronize.SynchronizeManager;
import org.eclipse.team.ui.synchronize.ISynchronizeManager;

/**
 * TeamUI contains public API for generic UI-based Team functionality
 */
public class TeamUI {

	// manages synchronize participants
	private static ISynchronizeManager synchronizeManager;

	/**
	 * Property constant indicating the global ignores list has changed. 
	 */
	public static String GLOBAL_IGNORES_CHANGED = TeamUIPlugin.ID + "global_ignores_changed"; //$NON-NLS-1$

	/**
	 * Return the synchronize manager.
	 * 
	 * @return the synchronize manager
	 * @since 3.0
	 */
	public static ISynchronizeManager getSynchronizeManager() {
		if (synchronizeManager == null) {
			synchronizeManager = new SynchronizeManager();
		}
		return synchronizeManager;
	}

	/**
	 * Register for changes made to Team properties.
	 * 
	 * @param listener the listener to add
	 */
	public static void addPropertyChangeListener(IPropertyChangeListener listener) {
		TeamUIPlugin.addPropertyChangeListener(listener);
	}

	/**
	 * Deregister as a Team property changes.
	 * 
	 * @param listener the listener to remove
	 */
	public static void removePropertyChangeListener(IPropertyChangeListener listener) {
		TeamUIPlugin.removePropertyChangeListener(listener);
	}
}
