/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.contexts.ws;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.contexts.IContextActivationService;
import org.eclipse.ui.contexts.IWorkbenchWindowContextSupport;

public class WorkbenchWindowContextSupport
	implements IWorkbenchWindowContextSupport {
	private WorkbenchWindowContextActivationService workbenchWindowContextActivationService;

	public WorkbenchWindowContextSupport(IWorkbenchWindow workbenchWindow) {
		if (workbenchWindow == null)
			throw new NullPointerException();

		workbenchWindowContextActivationService =
			new WorkbenchWindowContextActivationService(workbenchWindow);
	}

	public IContextActivationService getContextActivationService() {
		return workbenchWindowContextActivationService;
	}
}
