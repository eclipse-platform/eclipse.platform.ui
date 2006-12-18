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
package org.eclipse.debug.internal.core.commands;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.commands.IBooleanCollector;
import org.eclipse.debug.core.commands.IDisconnectCommand;
import org.eclipse.debug.core.commands.IStatusCollector;
import org.eclipse.debug.core.model.IDisconnect;

/**
 * Default disconnect command for the standard debug model.
 * 
 * @since 3.3
 */
public class DisconnectCommand extends DebugCommand implements IDisconnectCommand {

	protected boolean isExecutable(Object target, IProgressMonitor monitor, IBooleanCollector collector) throws CoreException {
		return ((IDisconnect)target).canDisconnect();
	}

	protected void doExecute(Object target, IProgressMonitor monitor, IStatusCollector collector) throws CoreException {
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
