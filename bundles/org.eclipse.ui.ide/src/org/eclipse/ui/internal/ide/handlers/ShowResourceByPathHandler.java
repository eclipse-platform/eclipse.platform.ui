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

package org.eclipse.ui.internal.ide.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.ISetSelectionTarget;

/**
 * A command handler to show a resource in the Navigator view given the resource
 * path.
 * 
 * @since 3.2
 */
public class ShowResourceByPathHandler extends AbstractHandler {

	private static final String PARAM_ID_RESOURCE_PATH = "resourcePath"; //$NON-NLS-1$

	public Object execute(ExecutionEvent event) throws ExecutionException {

		IResource resource = (IResource) event
				.getObjectParameterForExecution(PARAM_ID_RESOURCE_PATH);

		IWorkbenchWindow activeWindow = HandlerUtil
				.getActiveWorkbenchWindowChecked(event);

		IWorkbenchPage activePage = activeWindow.getActivePage();
		if (activePage == null) {
			throw new ExecutionException("no active workbench page"); //$NON-NLS-1$
		}

		try {
			IViewPart view = activePage.showView(IPageLayout.ID_RES_NAV);
			if (view instanceof ISetSelectionTarget) {
				ISelection selection = new StructuredSelection(resource);
				((ISetSelectionTarget) view).selectReveal(selection);
			}
		} catch (PartInitException e) {
			throw new ExecutionException("error showing resource in navigator"); //$NON-NLS-1$
		}

		return null;
	}

}
