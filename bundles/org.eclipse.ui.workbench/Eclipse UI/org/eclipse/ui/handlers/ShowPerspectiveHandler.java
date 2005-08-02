/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.handlers;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.SelectPerspectiveDialog;

/**
 * Shows the given perspective. If no perspective is specified in the
 * parameters, then this opens the perspective selection dialog.
 * 
 * @since 3.1
 */
public final class ShowPerspectiveHandler extends AbstractHandler {

	/**
	 * The name of the parameter providing the perspective identifier.
	 */
	private static final String PARAMETER_NAME_VIEW_ID = "org.eclipse.ui.perspectives.showPerspective.perspectiveId"; //$NON-NLS-1$

	public final Object execute(final ExecutionEvent event)
			throws ExecutionException {
		// Get the view identifier, if any.
		final Map parameters = event.getParameters();
		final Object value = parameters.get(PARAMETER_NAME_VIEW_ID);
		if (value == null) {
			openOther();
		} else {
			openPerspective((String) value);
		}

		return null;
	}

	/**
	 * Opens a view selection dialog, allowing the user to chose a view.
	 * 
	 * @throws ExecutionException
	 *             If the perspective could not be opened.
	 */
	private final void openOther() throws ExecutionException {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final IWorkbenchWindow activeWorkbenchWindow = workbench
				.getActiveWorkbenchWindow();
		final SelectPerspectiveDialog dialog = new SelectPerspectiveDialog(
				activeWorkbenchWindow.getShell(), WorkbenchPlugin.getDefault()
						.getPerspectiveRegistry());
		dialog.open();
		if (dialog.getReturnCode() == Window.CANCEL) {
			return;
		}

		final IPerspectiveDescriptor descriptor = dialog.getSelection();
		if (descriptor != null) {
			openPerspective(descriptor.getId());
		}
	}

	/**
	 * Opens the perspective with the given identifier.
	 * 
	 * @param perspectiveId
	 *            The perspective to open; must not be <code>null</code>
	 * @throws ExecutionException
	 *             If the perspective could not be opened.
	 */
	private final void openPerspective(final String perspectiveId)
			throws ExecutionException {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final IWorkbenchWindow activeWorkbenchWindow = workbench
				.getActiveWorkbenchWindow();
		if (activeWorkbenchWindow == null) {
			return;
		}

		IAdaptable input = null;

		final IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
		if (activePage != null)
			input = activePage.getInput();

		try {
			workbench.showPerspective(perspectiveId, activeWorkbenchWindow,
					input);
		} catch (WorkbenchException e) {
			throw new ExecutionException("Perspective could not be opened.", e); //$NON-NLS-1$
		}
	}
}
