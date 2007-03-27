/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
import org.eclipse.debug.core.commands.IResumeHandler;
import org.eclipse.debug.core.model.ISuspendResume;

/**
 * Default resume command for the standard debug model.
 * 
 * @since 3.3
 */
public class ResumeCommand extends SuspendCommand implements IResumeHandler {

	protected void execute(Object target) throws CoreException {
		((ISuspendResume)target).resume();
	}

	protected boolean isExecutable(Object target) {
		return ((ISuspendResume)target).canResume();
	}

}
