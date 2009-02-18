/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.about;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.about.IInstallationPageSources;
import org.eclipse.ui.about.InstallationPage;
import org.eclipse.ui.handlers.HandlerUtil;

abstract class ProductInfoPageHandler extends AbstractHandler {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands
	 * .ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		InstallationPage page = getInstallationPage(event.getApplicationContext());
		if (page != null)
			return execute(page, event);
		return null;
	}

	protected InstallationPage getInstallationPage(Object executionContext) {
		// First look for an open product info dialog and use its page.
		InstallationPage page = (InstallationPage) HandlerUtil.getVariable(
				executionContext,
				InstallationDialogSourceProvider.ACTIVE_PRODUCT_DIALOG_PAGE);
		if (page == null) {
			// Look for the active page in the installation dialog
			page = (InstallationPage) HandlerUtil.getVariable(executionContext,
					IInstallationPageSources.ACTIVE_PAGE);
		}
		return page;
	}

	protected abstract Object execute(InstallationPage page,
			ExecutionEvent event);
}