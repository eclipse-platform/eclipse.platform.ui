/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
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

/**
 * TeamUI contains public API for generic UI-based Team functionality
 */
public class TeamUI {
	// property change types
	public static String GLOBAL_IGNORES_CHANGED = "global_ignores_changed"; //$NON-NLS-1$
		
	/**
	 * Register for changes made to Team properties.
	 * 
	 * @param listener  the listener to add
	 */
	public static void addPropertyChangeListener(IPropertyChangeListener listener) {
		TeamUIPlugin.addPropertyChangeListener(listener);
	}
	
	/**
	 * Deregister as a Team property changes.
	 * 
	 * @param listener  the listener to remove
	 */
	public static void removePropertyChangeListener(IPropertyChangeListener listener) {
		TeamUIPlugin.removePropertyChangeListener(listener);
	}
}
