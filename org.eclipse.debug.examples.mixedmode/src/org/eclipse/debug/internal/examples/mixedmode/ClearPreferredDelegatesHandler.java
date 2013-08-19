/*******************************************************************************
 * Copyright (c) Aug 19, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.examples.mixedmode;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;

/**
 * Handler for the Clear Preferred Delegates command
 */
public class ClearPreferredDelegatesHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType[] types = lm.getLaunchConfigurationTypes();
		Set<Set<String>> modes = null;
		Set<String> mode = null;
		for (int i = 0; i < types.length; i++) {
			modes = types[i].getSupportedModeCombinations();
			for (Iterator<Set<String>> iter = modes.iterator(); iter.hasNext();) {
				mode = iter.next();
				try {
					types[i].setPreferredDelegate(mode, null);
				} catch (CoreException ce) {
					// /do nothing
				}
			}
		}
		return null;
	}
}
