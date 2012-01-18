/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * 
 * @author Prakash G.R.
 * 
 * @since 3.7
 * 
 */
public class LockToolBarHandler extends AbstractHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
	 * ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {

		WorkbenchWindow workbenchWindow = (WorkbenchWindow) HandlerUtil
				.getActiveWorkbenchWindow(event);
		if (workbenchWindow != null) {
			ICoolBarManager coolBarManager = workbenchWindow.getCoolBarManager2();
			if (coolBarManager != null) {
				boolean oldValue = HandlerUtil.toggleCommandState(event.getCommand());
				coolBarManager.setLockLayout(!oldValue);
			}
		}
		return null;
	}

}
