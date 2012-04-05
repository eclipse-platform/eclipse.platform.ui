/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.views;

import org.eclipse.core.commands.*;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.update.ui.UpdateManagerUI;

/**
 * This handler is hooked to a command provided by the application (such
 * as org.eclipse.ui.ide). This allows RCP applications to control how update
 * functionality is surfaced.
 */
public class FindAndInstallHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		UpdateManagerUI.openInstaller(HandlerUtil.getActiveShell(event));
		return null;
	}

}
