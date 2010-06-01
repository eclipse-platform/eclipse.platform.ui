/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
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
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchCommandConstants;
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


	public final Object execute(final ExecutionEvent event) {
		final String preferencePageId = event
				.getParameter(IWorkbenchCommandConstants.WINDOW_PREFERENCES_PARM_PAGEID);
		final IWorkbenchWindow activeWorkbenchWindow = HandlerUtil
				.getActiveWorkbenchWindow(event);

		final Shell shell;
		if (activeWorkbenchWindow == null) {
			shell = null;
		} else {
			shell = activeWorkbenchWindow.getShell();
		}

		final PreferenceDialog dialog = PreferencesUtil
				.createPreferenceDialogOn(shell, preferencePageId, null, null);
		dialog.open();

		return null;
	}

}
