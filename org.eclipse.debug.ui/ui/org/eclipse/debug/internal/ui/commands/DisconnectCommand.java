/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.commands;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.ui.commands.IDisconnectCommand;
import org.eclipse.debug.ui.commands.IStatusMonitor;

/**
 * Default disconnect command for the standard debug model.
 * 
 * @since 3.3
 */
public class DisconnectCommand extends DebugCommand implements IDisconnectCommand {

	protected boolean isExecutable(Object target, IStatusMonitor monitor) throws CoreException {
		return ((IDisconnect)target).canDisconnect();
	}

	protected void doExecute(Object target, IStatusMonitor monitor) throws CoreException {
		((IDisconnect)target).disconnect();
	}

	protected Object getTarget(Object element) {
		if (element instanceof IDisconnect) {
			return element;
		} else if (element instanceof IAdaptable) {
			return ((IAdaptable) element).getAdapter(IDisconnect.class);
		}
		return null;
	}
}
