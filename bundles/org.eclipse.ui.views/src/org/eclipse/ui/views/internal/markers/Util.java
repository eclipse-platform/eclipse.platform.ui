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

package org.eclipse.ui.views.internal.markers;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class Util {

	public static String getString(ResourceBundle resourceBundle, String key)
		throws IllegalArgumentException {
		if (resourceBundle == null || key == null)
			throw new IllegalArgumentException();

		String value = key;

		try {
			value = resourceBundle.getString(key);
		} catch (MissingResourceException eMissingResource) {
			System.err.println(eMissingResource);
		}
		
		return value != null ? value.trim() : null;
	}

	private Util() {
		super();
	}
}
