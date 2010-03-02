/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.registry;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Command handler to show a particular view.
 * 
 * @since 3.0
 */
public final class ShowViewHandler extends AbstractHandler {
	
	/**
	 * The identifier of the view this handler should open. This value should
	 * never be <code>null</code>.
	 */
	private final String viewId;

	/**
	 * Constructs a new instance of <code>ShowViewHandler</code>.
	 * 
	 * @param viewId
	 *            The identifier of the view this handler should open; must not
	 *            be <code>null</code>.
	 */
	public ShowViewHandler(final String viewId) {
		this.viewId = viewId;
	}

	public final Object execute(final ExecutionEvent event) 
			throws ExecutionException {
		final IWorkbenchWindow activeWorkbenchWindow = HandlerUtil
				.getActiveWorkbenchWindowChecked(event);

		final IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
		if (activePage == null) {
			return null;
		}

		try {
			activePage.showView(viewId);
		} catch (PartInitException e) {
			IStatus status = StatusUtil
					.newStatus(e.getStatus(), e.getMessage());
			StatusManager.getManager().handle(status, StatusManager.SHOW);
		}

		return null;
	}
}
