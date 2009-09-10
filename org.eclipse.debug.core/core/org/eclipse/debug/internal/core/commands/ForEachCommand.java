/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.core.commands.AbstractDebugCommand;
import org.eclipse.debug.core.commands.IEnabledStateRequest;

/**
 * A command that operates on each element individually.
 * 
 * @since 3.3
 */
public abstract class ForEachCommand extends AbstractDebugCommand {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.commands.DebugCommand#doExecute(java.lang.Object[], org.eclipse.core.runtime.IProgressMonitor, org.eclipse.debug.core.IRequest)
	 */
	protected void doExecute(Object[] targets, IProgressMonitor monitor, IRequest request) throws CoreException {
		for (int i = 0; i < targets.length; i++) {
			execute(targets[i]);
			monitor.worked(1);
		}
	}
	
	protected abstract void execute(Object target) throws CoreException;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.commands.DebugCommand#isExecutable(java.lang.Object[], org.eclipse.core.runtime.IProgressMonitor, org.eclipse.debug.core.commands.IEnabledStateRequest)
	 */
	protected boolean isExecutable(Object[] targets, IProgressMonitor monitor, IEnabledStateRequest request) throws CoreException {
		for (int i = 0; i < targets.length; i++) {
			if (!isExecutable(targets[i])) {
				return false;
			}
			monitor.worked(1);
		}
		return true;
	}
	
	protected abstract boolean isExecutable(Object target);

}
