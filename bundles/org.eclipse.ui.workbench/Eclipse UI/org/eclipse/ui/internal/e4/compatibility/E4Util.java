/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 *
 */
public class E4Util {

	// debug tracing
	private static final String OPTION_DEBUG_E4 = "org.eclipse.ui.workbench/debug/e4"; //$NON-NLS-1$;

	public final static boolean DEBUG_E4;

	static {
		WorkbenchPlugin activator = WorkbenchPlugin.getDefault();
		if (activator == null)
			DEBUG_E4 = false;
		else {
			DebugOptions debugOptions = activator.getDebugOptions();
			if (debugOptions == null)
				DEBUG_E4 = false;
			else
				DEBUG_E4 = debugOptions.getBooleanOption(OPTION_DEBUG_E4, false);
		}
	}

	public static void unsupported(String msg) throws UnsupportedOperationException {
		if (DEBUG_E4)
			WorkbenchPlugin.log("unsupported: " + msg); //$NON-NLS-1$
	}

	public static void message(String msg) throws UnsupportedOperationException {
		if (DEBUG_E4)
			WorkbenchPlugin.log(msg);
	}
}
