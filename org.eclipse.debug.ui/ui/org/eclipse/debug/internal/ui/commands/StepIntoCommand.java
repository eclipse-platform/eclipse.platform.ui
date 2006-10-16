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
import org.eclipse.debug.core.model.IStep;
import org.eclipse.debug.internal.ui.commands.provisional.IStepIntoCommand;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor;

/**
 * Default step into command for the standard debug model.
 * 
 * @since 3.3
 */
public class StepIntoCommand extends StepCommand implements IStepIntoCommand {

	protected boolean isExecutable(Object target, IAsynchronousRequestMonitor monitor) throws CoreException {
		return ((IStep)target).canStepInto();
	}

	protected void doExecute(Object target, IAsynchronousRequestMonitor monitor) throws CoreException {
		((IStep)target).stepInto();
	}

}
