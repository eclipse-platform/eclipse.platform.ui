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
package org.eclipse.ui.internal.forms;

import java.text.MessageFormat;
import java.util.*;

import org.eclipse.swt.SWT;

public class Policy {
	private static ResourceBundle msgs = null;

	/**
	 * Returns the NLS'ed message for the given argument. This is only being
	 * called from SWT.
	 * 
	 * @param key the key to look up
	 * @return the message for the given key
	 * 
	 */
	public static String getMessage(String key) {
		String answer = key;
		
		if (key == null) {
			SWT.error (SWT.ERROR_NULL_ARGUMENT);
		}	
		if (msgs == null) {
			try {
				msgs = ResourceBundle.getBundle("org.eclipse.ui.internal.forms.FormsMessages"); //$NON-NLS-1$
			} catch (MissingResourceException ex) {
				answer = key + " (no resource bundle)"; //$NON-NLS-1$
			}
		}
		if (msgs != null) {
			try {
				answer = msgs.getString(key);
			} catch (MissingResourceException ex2) {}
		}
		return answer;
	}
	
	public static String getMessage(String key, String arg) {
		return getMessage(key, new Object[] { arg });
	}

	public static String getMessage(String key, Object[] args) {
		String answer = key;
		
		if (key == null || args == null) {
			SWT.error (SWT.ERROR_NULL_ARGUMENT);
		}
		if (msgs == null) {
			try {
				msgs = ResourceBundle.getBundle("org.eclipse.ui.internal.forms.FormsMessages"); //$NON-NLS-1$
			} catch (MissingResourceException ex) {
				answer = key + " (no resource bundle)"; //$NON-NLS-1$
			}
		}
		if (msgs != null) {
			try {
				MessageFormat formatter = new MessageFormat("");			
				formatter.applyPattern(msgs.getString(key));			
				answer = formatter.format(args);
			} catch (MissingResourceException ex2) {}
		}
		return answer;
	}
}
