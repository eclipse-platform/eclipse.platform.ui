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

package org.eclipse.ui.instrumentation;

/**
 * Manages instrumentation listeners which cannot be added to
 * existing listener lists in the workspace
 */
public class WorkbenchInstrumentationManager {

	public static IInstrumentationHelpListener helpListener;

	/**
	 * Add a help listener
	 **/
	public static void addHelpListener(IInstrumentationHelpListener hl) {		
		helpListener = hl;
	}	

	/**
	 * Remove the help listener
	 **/
	public static void removeHelpListener() {
		helpListener = null;
	}

	/**
	 * Notify the listener that help was activated
	 **/
	public static void fireHelpActivated(String helpString) {
		if (helpListener != null)
			helpListener.helpRequested(helpString);
	}
}
