/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.actions.keybindings;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class Messages {

	private static ResourceBundle resourceBundle = ResourceBundle.getBundle(Messages.class.getName());
	
	public static String getString(String key) {
		try {
			return resourceBundle.getString(key);
		} catch (MissingResourceException eMissingResource) {
			System.err.println(eMissingResource);
			return key;
		}
	}

	private Messages() {
		super();
	}
}
