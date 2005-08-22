/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class Messages {
	
	private static final String BUNDLE_NAME= "org.eclipse.ui.views.markers.internal.Messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE= 
		ResourceBundle.getBundle(BUNDLE_NAME);

 
    private Messages() {
        super();
    }

    public static String format(String key, Object[] args) {
        return MessageFormat.format(getString(key), args);
    }

    public static String getString(String key) {
        return Util.getString(RESOURCE_BUNDLE, key);
    }
}
