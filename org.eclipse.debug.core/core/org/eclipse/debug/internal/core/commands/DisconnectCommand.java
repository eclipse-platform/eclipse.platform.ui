/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
import org.eclipse.debug.core.commands.IDisconnectHandler;
import org.eclipse.debug.core.model.IDisconnect;

/**
 * Default disconnect command for the standard debug model.
 * 
 * @since 3.3
 */
public class DisconnectCommand extends ForEachCommand implements IDisconnectHandler {

	protected Object getTarget(Object element) {
		return getAdapter(element, IDisconnect.class);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.commands.ForEachCommand#execute(java.lang.Object)
	 */
	protected void execute(Object target) throws CoreException {
		((IDisconnect)target).disconnect();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.commands.ForEachCommand#isExecutable(java.lang.Object)
	 */
	protected boolean isExecutable(Object target) {
		return ((IDisconnect)target).canDisconnect();
	}
}
