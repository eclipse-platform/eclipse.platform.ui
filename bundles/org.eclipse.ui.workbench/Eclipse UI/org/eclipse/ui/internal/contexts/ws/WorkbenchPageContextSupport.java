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

import org.eclipse.ui.contexts.ContextActivationServiceFactory;
import org.eclipse.ui.contexts.ICompoundContextActivationService;
import org.eclipse.ui.contexts.IWorkbenchPageContextSupport;
import org.eclipse.ui.internal.Perspective;
import org.eclipse.ui.internal.WorkbenchPage;

public class WorkbenchPageContextSupport
	implements IWorkbenchPageContextSupport {
	private ICompoundContextActivationService compoundContextActivationService;
	private WorkbenchPage workbenchPage;

	public WorkbenchPageContextSupport(WorkbenchPage workbenchPage) {
		if (workbenchPage == null)
			throw new NullPointerException();

		this.workbenchPage = workbenchPage;
		compoundContextActivationService =
			ContextActivationServiceFactory
				.getCompoundContextActivationService();
	}

	public ICompoundContextActivationService getCompoundContextActivationService() {
		Perspective perspective = workbenchPage.getActivePerspective();

		if (perspective != null)
			return perspective.getCompoundContextActivationService();
		else
			return compoundContextActivationService;
	}
}
