/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.net;

import java.util.Date;

public class Policy {
	
	// general debug flag
	public static boolean DEBUG = false;

	public static boolean DEBUG_SYSTEM_PROVIDERS = false;

	static {
		// init debug options
		if (Activator.getInstance().isDebugging()) {
			DEBUG = true;
			DEBUG_SYSTEM_PROVIDERS = "true".equalsIgnoreCase(Activator.getInstance().getDebugOption(Activator.ID + "/systemproviders")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Print a debug message to the console. Pre-pend the message with the
	 * current date and the name of the current thread.
	 * 
	 * @param message
	 */
	public static void debug(String message) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(new Date(System.currentTimeMillis()));
		buffer.append(" - ["); //$NON-NLS-1$
		buffer.append(Thread.currentThread().getName());
		buffer.append("] "); //$NON-NLS-1$
		buffer.append(message);
		System.out.println(buffer.toString());
	}
}
