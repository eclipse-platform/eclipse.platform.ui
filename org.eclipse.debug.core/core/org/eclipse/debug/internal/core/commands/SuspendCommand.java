/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
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
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.ISuspendHandler;
import org.eclipse.debug.core.model.ISuspendResume;

/**
 * Default suspend command for the standard debug model.
 * 
 * @since 3.3
 */
public class SuspendCommand extends ForEachCommand implements ISuspendHandler {

	protected Object getTarget(Object element) {
		return getAdapter(element, ISuspendResume.class);
	}

	protected void execute(Object target) throws CoreException {
		((ISuspendResume)target).suspend();
	}
	
	protected boolean isExecutable(Object target) {
		return ((ISuspendResume)target).canSuspend();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.commands.AbstractDebugCommand#getEnabledStateJobFamily(org.eclipse.debug.core.commands.IDebugCommandRequest)
	 */
	protected Object getEnabledStateJobFamily(IDebugCommandRequest request) {
		return ISuspendHandler.class;
	}
}
