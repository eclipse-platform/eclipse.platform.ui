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
package org.eclipse.ui.internal.console;

import org.eclipse.ui.console.ConsolePlugin;
 
public interface IInternalConsoleConstants {
    public static final String PREF_CONSOLE_SCROLL_LOCK = ConsolePlugin.getUniqueIdentifier() + ".PREF_CONSOLE_LOCK"; //$NON-NLS-1$
    
	// tool images
	public static final String IMG_LCL_PIN = "IMG_LCL_PIN"; //$NON-NLS-1$
	public static final String IMG_LCL_LOCK = "IMG_LCL_LOCK"; //$NON-NLS-1$
	
	// disabled local tool images
	public static final String IMG_DLCL_PIN = "IMG_DLCL_PIN"; //$NON-NLS-1$
	public static final String IMG_DLCL_CLEAR= "IMG_DLCL_CLEAR"; //$NON-NLS-1$
	public static final String IMG_DLCL_LOCK = "IMG_DLCL_LOCK"; //$NON-NLS-1$
	
	// enabled local tool images	
	public static final String IMG_ELCL_PIN = "IMG_ELCL_PIN"; //$NON-NLS-1$
	public static final String IMG_ELCL_CLEAR= "IMG_ELCL_CLEAR"; //$NON-NLS-1$
	public static final String IMG_ELCL_LOCK = "IMG_ELCL_LOCK"; //$NON-NLS-1$
    public static final String IMG_ELCL_NEW_CON = "IMG_ELCL_NEW_CON"; //$NON-NLS-1$
}
