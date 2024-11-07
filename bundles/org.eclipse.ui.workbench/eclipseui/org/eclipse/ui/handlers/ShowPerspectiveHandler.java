/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Remy Chi Jian Suen <remy.suen@gmail.com> - Bug 218540 [Perspectives] Perspectives always open in same window, regardless of preference
 *******************************************************************************/
package org.eclipse.ui.handlers;

import java.util.Map;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.SelectPerspectiveDialog;

/**
 * Shows the given perspective. If no perspective is specified in the
 * parameters, then this opens the perspective selection dialog.
 *
 * @since 3.1
 */
public final class ShowPerspectiveHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

		// Get the view identifier, if any.
		final Map parameters = event.getParameters();
		final Object value = parameters.get(IWorkbenchCommandConstants.PERSPECTIVES_SHOW_PERSPECTIVE_PARM_ID);
		final String newWindow = (String) parameters
				.get(IWorkbenchCommandConstants.PERSPECTIVES_SHOW_PERSPECTIVE_PARM_NEWWINDOW);

		if (value == null) {
			openOther(window);
		} else {
			ExecutionException[] exception = new ExecutionException[1];
			BusyIndicator.showWhile(null, () -> {
				try {
					if (newWindow == null || newWindow.equalsIgnoreCase("false")) { //$NON-NLS-1$
						openPerspective((String) value, window);
					} else {
						openNewWindowPerspective((String) value, window);
					}
				} catch (ExecutionException e) {
					exception[0] = e;
				}
			});
			if (exception[0] != null) {
				throw exception[0];
			}
		}
		return null;
	}

	/**
	 * Opens the specified perspective in a new window.
	 *
	 * @param perspectiveId The perspective to open; must not be <code>null</code>
	 * @throws ExecutionException If the perspective could not be opened.
	 */
	private void openNewWindowPerspective(String perspectiveId, IWorkbenchWindow activeWorkbenchWindow)
			throws ExecutionException {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		try {
			IAdaptable input = ((Workbench) workbench).getDefaultPageInput();
			workbench.openWorkbenchWindow(perspectiveId, input);
		} catch (WorkbenchException e) {
			ErrorDialog.openError(activeWorkbenchWindow.getShell(),
					WorkbenchMessages.ChangeToPerspectiveMenu_errorTitle, e.getMessage(), e.getStatus());
		}
	}

	/**
	 * Opens a view selection dialog, allowing the user to chose a view.
	 *
	 * @throws ExecutionException If the perspective could not be opened.
	 */
	private void openOther(final IWorkbenchWindow activeWorkbenchWindow) throws ExecutionException {
		final SelectPerspectiveDialog dialog = new SelectPerspectiveDialog(activeWorkbenchWindow.getShell(),
				WorkbenchPlugin.getDefault().getPerspectiveRegistry());
		dialog.open();
		if (dialog.getReturnCode() == Window.CANCEL) {
			return;
		}

		final IPerspectiveDescriptor descriptor = dialog.getSelection();
		if (descriptor != null) {
			int openPerspMode = WorkbenchPlugin.getDefault().getPreferenceStore()
					.getInt(IPreferenceConstants.OPEN_PERSP_MODE);
			IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
			IPerspectiveDescriptor persp = page == null ? null : page.getPerspective();
			String perspectiveId = descriptor.getId();
			// only open it in a new window if the preference is set and the
			// current workbench page doesn't have an active perspective
			ExecutionException[] exception = new ExecutionException[1];
			BusyIndicator.showWhile(null, () -> {
				try {
					if (IPreferenceConstants.OPM_NEW_WINDOW == openPerspMode && persp != null) {
						openNewWindowPerspective(perspectiveId, activeWorkbenchWindow);
					} else {
						openPerspective(perspectiveId, activeWorkbenchWindow);
					}
				} catch (ExecutionException e) {
					exception[0] = e;
				}
			});
			if (exception[0] != null) {
				throw exception[0];
			}
		}
	}

	/**
	 * Opens the perspective with the given identifier.
	 *
	 * @param perspectiveId The perspective to open; must not be <code>null</code>
	 * @throws ExecutionException If the perspective could not be opened.
	 */
	private void openPerspective(final String perspectiveId, final IWorkbenchWindow activeWorkbenchWindow)
			throws ExecutionException {
		final IWorkbench workbench = PlatformUI.getWorkbench();

		final IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
		IPerspectiveDescriptor desc = activeWorkbenchWindow.getWorkbench().getPerspectiveRegistry()
				.findPerspectiveWithId(perspectiveId);
		if (desc == null) {
			throw new ExecutionException("Perspective " + perspectiveId //$NON-NLS-1$
					+ " cannot be found."); //$NON-NLS-1$
		}

		try {
			if (activePage != null) {
				activePage.setPerspective(desc);
			} else {
				IAdaptable input = ((Workbench) workbench).getDefaultPageInput();
				activeWorkbenchWindow.openPage(perspectiveId, input);
			}
		} catch (WorkbenchException e) {
			throw new ExecutionException("Perspective could not be opened.", e); //$NON-NLS-1$
		}
	}
}
