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
package org.eclipse.debug.internal.core;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;

/**
 * Tests if an object is launchable.
 */
public class LaunchablePropertyTester extends PropertyTester {

	/**
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if ("launchable".equals(property)) { //$NON-NLS-1$
				if (((LaunchManager)(DebugPlugin.getDefault().getLaunchManager())).launchModeAvailable((String)expectedValue)) {
					return Platform.getAdapterManager().hasAdapter(receiver, "org.eclipse.debug.ui.actions.ILaunchable"); //$NON-NLS-1$
				}
		}
		return false;
	}

}
