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

package org.eclipse.ui.views.internal.markers.bookmarks;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.eclipse.ui.views.internal.markers.Util;

class Messages {

	private final static ResourceBundle bundle = ResourceBundle.getBundle(Messages.class.getName());
	
	private Messages() {
		super();
	}

	static String format(String key, Object[] args) {
		return MessageFormat.format(getString(key), args);
	}

	static String getString(String key) {
		return Util.getString(bundle, key);
	}
}
