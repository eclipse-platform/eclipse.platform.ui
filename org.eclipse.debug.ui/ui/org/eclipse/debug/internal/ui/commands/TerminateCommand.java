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
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.internal.ui.commands.provisional.ITerminateCommand;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor;

/**
 * Default terminate command for the standard debug model.
 * 
 * @since 3.3
 */
public class TerminateCommand extends DebugCommand implements ITerminateCommand {

	protected boolean isExecutable(Object target, IAsynchronousRequestMonitor monitor) throws CoreException {
		return ((ITerminate)target).canTerminate();
	}

	protected void doExecute(Object target, IAsynchronousRequestMonitor monitor) throws CoreException {
		((ITerminate)target).terminate();
	}

	protected Object getTarget(Object element) {
		if (element instanceof ITerminate) {
			return element;
		} else if (element instanceof IAdaptable) {
			return ((IAdaptable) element).getAdapter(ITerminate.class);
		}
		return null;
	}
}
