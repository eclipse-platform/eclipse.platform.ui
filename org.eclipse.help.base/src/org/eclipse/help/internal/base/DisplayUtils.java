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
package org.eclipse.help.internal.base;

import java.lang.reflect.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.ILiveHelpAction;
import org.osgi.framework.*;

/**
 * Utility class to control SWT Display and event loop run in
 * org.eclipse.help.ui plug-in
 */
public class DisplayUtils {
	private static final String HELP_UI_PLUGIN_ID = "org.eclipse.help.ui"; //$NON-NLS-1$
	private static final String LOOP_CLASS_NAME = "org.eclipse.help.ui.internal.HelpUIEventLoop"; //$NON-NLS-1$

	static void runUI() {
		invoke("run"); //$NON-NLS-1$
	}
	static void wakeupUI() {
		invoke("wakeup"); //$NON-NLS-1$
	}

	static void waitForDisplay() {
		invoke("waitFor"); //$NON-NLS-1$
	}

	private static void invoke(String method) {
		try {
			Bundle bundle = Platform.getBundle(HELP_UI_PLUGIN_ID);
			if (bundle == null) {
				return;
			}
			Class c = bundle.loadClass(LOOP_CLASS_NAME);
			Method m = c.getMethod(method, new Class[]{}); //$NON-NLS-1$
			m.invoke(null, new Object[]{});
		} catch (Exception e) {
		}
	}
	
	public static void runLiveHelp(String pluginID, String className, String arg) {	
		Bundle bundle = Platform.getBundle(pluginID);
		if (bundle == null) {
			return;
		}

		try {
			Class c = bundle.loadClass(className);
			Object o = c.newInstance();
			if (o != null && o instanceof ILiveHelpAction) {
				ILiveHelpAction helpExt = (ILiveHelpAction) o;
				if (arg != null)
					helpExt.setInitializationString(arg);
				Thread runnableLiveHelp = new Thread(helpExt);
				runnableLiveHelp.setDaemon(true);
				runnableLiveHelp.start();
			}
		} catch (ThreadDeath td) {
			throw td;
		} catch (Exception e) {
		}
	}
}
