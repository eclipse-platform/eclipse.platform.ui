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

package org.eclipse.jface.instrumentation;

/**
 * Manages instrumentation listeners which cannot be added to
 * existing listener lists in the workspace
 */
public class JFaceInstrumentationManager {

	public static IInstrumentationActionListener actionListener;
	public static IInstrumentationMenuListener menuListener;

	/**
	 * Add an IInstrumentationActionListener
	 */
	public static void addActionListener(IInstrumentationActionListener al) {		
		actionListener = al;
	}	

	/**
	 * Remove the IInstrumentationActionListener
	 */
	public static void removeActionListener() {		
		actionListener = null;
	}	

	/**
	 * Notify the listener that an action was fired
	 */
	public static void fireAction(String actionString, boolean contextMenu) {
		if (actionListener != null)
				actionListener.action(actionString, contextMenu);
	}

	/**
	 * Add an IInstrumentationMenuListener
	 */
	public static void addMenuListener(IInstrumentationMenuListener ml) {		
		menuListener = ml;
	}	

	/**
	 * Remove the IInstrumentationMenuListener
	 */
	public static void removeMenuListener() {		
		menuListener = null;
	}	

	/**
	 * Notify the listener that a menu was shown
	 */
	public static void menuShown(String menuString) {
		if (menuListener != null)
				menuListener.menuShown(menuString);
	}


}
