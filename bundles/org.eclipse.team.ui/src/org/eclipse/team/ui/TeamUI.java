/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
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
		TeamUIPlugin.getPlugin().addPropertyChangeListener(listener);
	}
	
	/**
	 * Deregister as a Team property changes.
	 * 
	 * @param listener  the listener to remove
	 */
	public static void removePropertyChangeListener(IPropertyChangeListener listener) {
		TeamUIPlugin.getPlugin().removePropertyChangeListener(listener);
	}
}
