/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.views;

import java.lang.reflect.Field;

import org.eclipse.jface.window.Window;

/**
 * 
 * A class to access the last return code from org.eclipse.jface.window.Window.
 *
 */
/* package */ class WizardReturnCodeHack {

	/**
	 * Ensure the given view is a fast view only and follows the
	 * user as they switch perspectives.
	 * 
	 * @param aView - the view to ensure is a following fast view only
	 */
	/* package */
	static final int getLastWizardReturnCode() {
		Class window;
		try {
		      window = Class.forName("org.eclipse.jface.window.Window"); //$NON-NLS-1$
		} catch (ClassNotFoundException e) {
			return Window.CANCEL;
		}
		Field field;
		try {
		      field = window.getDeclaredField("globalReturnCode"); //$NON-NLS-1$
		} catch (NoSuchFieldException e) {
			return Window.CANCEL;
		}
		field.setAccessible(true);
		int returnCode;
		try {
		      returnCode = field.getInt(null);
		} catch (IllegalAccessException e) {
			return Window.CANCEL;
		}

		return returnCode;
	}	
}
