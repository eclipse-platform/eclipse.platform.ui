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

package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * <p>
 * Shows the given preference page. If no preference page id is specified in the
 * parameters, then this opens the preferences dialog to whatever page was
 * active the last time the dialog was shown.
 * </p>
 * <p>
 * This class is not intended for use outside of the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 * 
 * @since 3.2
 */
public final class ShowPreferencePageHandler extends AbstractHandler {

	/**
	 * The name of the parameter providing the view identifier.
	 */
	private static final String PARAMETER_ID_PREFERENCE_PAGE_ID = "preferencePageId"; //$NON-NLS-1$

	public final Object execute(final ExecutionEvent event)
			throws ExecutionException {
		final String preferencePageId = event
				.getParameter(PARAMETER_ID_PREFERENCE_PAGE_ID);
		final IWorkbenchWindow activeWorkbenchWindow = HandlerUtil
				.getActiveWorkbenchWindowChecked(event);

		final Shell shell = activeWorkbenchWindow.getShell();
		if (shell == null) {
			throw new ExecutionException("no shell for active workbench window"); //$NON-NLS-1$
		}

		final PreferenceDialog dialog = PreferencesUtil
				.createPreferenceDialogOn(shell, preferencePageId, null, null);
		dialog.open();

		return null;
	}

}
