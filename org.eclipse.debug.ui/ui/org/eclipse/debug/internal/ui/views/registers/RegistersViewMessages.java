/**********************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 * IBM Corporation - Bug 90318
 ***********************************************************************/
package org.eclipse.debug.internal.ui.views.registers;

import org.eclipse.osgi.util.NLS;

public class RegistersViewMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.debug.internal.ui.views.registers.RegistersViewMessages";//$NON-NLS-1$

    public static String RegistersView_0;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, RegistersViewMessages.class);
	}
}