/*******************************************************************************
 * Copyright (c) 2007, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	@Override
	protected void doExecute(Object[] targets, IProgressMonitor monitor, IRequest request) throws CoreException {
		for (Object target : targets) {
			execute(target);
			monitor.worked(1);
		}
	}

	protected abstract void execute(Object target) throws CoreException;

	@Override
	protected boolean isExecutable(Object[] targets, IProgressMonitor monitor, IEnabledStateRequest request) throws CoreException {
		for (Object target : targets) {
			if (!isExecutable(target)) {
				return false;
			}
			monitor.worked(1);
		}
		return true;
	}

	protected abstract boolean isExecutable(Object target);

}
