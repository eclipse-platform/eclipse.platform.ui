/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * Open the preferences dialog
 */
public class OpenPreferencesAction extends Action implements ActionFactory.IWorkbenchAction {

	/**
	 * The workbench window; or <code>null</code> if this action has been
	 * <code>dispose</code>d.
	 */
	private IWorkbenchWindow workbenchWindow;

	/**
	 * Create a new <code>OpenPreferenceAction</code> This default constructor
	 * allows the the action to be called from the welcome page.
	 */
	public OpenPreferencesAction() {
		this(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
	}

	/**
	 * Create a new <code>OpenPreferenceAction</code> and initialize it from the
	 * given resource bundle.
	 */
	public OpenPreferencesAction(IWorkbenchWindow window) {
		super(WorkbenchMessages.OpenPreferences_text);
		if (window == null) {
			throw new IllegalArgumentException();
		}
		this.workbenchWindow = window;
		// @issue action id not set
		setToolTipText(WorkbenchMessages.OpenPreferences_toolTip);
		window.getWorkbench().getHelpSystem().setHelp(this, IWorkbenchHelpContextIds.OPEN_PREFERENCES_ACTION);
	}

	@Override
	public void run() {
		if (workbenchWindow == null) {
			// action has been dispose
			return;
		}
		PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(null, null, null, null);
		dialog.open();
	}

	@Override
	public void dispose() {
		workbenchWindow = null;
	}

}
