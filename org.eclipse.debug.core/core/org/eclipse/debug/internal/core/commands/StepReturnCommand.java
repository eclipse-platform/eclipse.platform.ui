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
import org.eclipse.debug.core.commands.IStepReturnHandler;
import org.eclipse.debug.core.model.IStep;

/**
 * Default step return command for the standard debug model.
 * 
 * @since 3.3
 */
public class StepReturnCommand extends StepCommand implements IStepReturnHandler {

	protected boolean isSteppable(Object target) {
		return ((IStep)target).canStepReturn();
	}

	protected void step(Object target) throws CoreException {
		((IStep)target).stepReturn();
	}


	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.commands.AbstractDebugCommand#getEnabledStateJobFamily(org.eclipse.debug.core.commands.IDebugCommandRequest)
	 */
	protected Object getEnabledStateJobFamily(IDebugCommandRequest request) {
		return IStepReturnHandler.class;
	}

}
