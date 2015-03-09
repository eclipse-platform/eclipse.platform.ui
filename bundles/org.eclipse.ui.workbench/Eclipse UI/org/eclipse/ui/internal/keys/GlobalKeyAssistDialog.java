/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.keys;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.bindings.internal.KeyAssistDialog;
import org.eclipse.e4.ui.bindings.keys.KeyBindingDispatcher;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * Extends the key conflict popup dialog to provide a full list of global key
 * bindings for the workbench. If {@link #open()} is called while this dialog is
 * still open, the keys preference page is opened.
 *
 *
 */
public class GlobalKeyAssistDialog extends KeyAssistDialog {

	/**
	 * Context for this dialog, used to open services
	 */
	private IEclipseContext context;

	/**
	 * ID of the key binding preference page
	 */
	private final String keysPageId = "org.eclipse.ui.preferencePages.Keys"; //$NON-NLS-1$

	/**
	 * Whether this dialog is currently open, if the dialog is opened again, we
	 * open the preference page instead
	 */
	private boolean isOpen;

	/**
	 * @param context
	 * @param associatedKeyboard
	 * @param associatedState
	 */
	public GlobalKeyAssistDialog(IEclipseContext context, KeyBindingDispatcher associatedKeyboard) {
		super(context, associatedKeyboard);
		this.context = context;
		setInfoText(getInfoText());
	}

	@Override
	public int open() {
		if (isOpen) {
			return openPreferencePage();
		}
		isOpen = true;
		return super.open();
	}

	@Override
	public boolean close() {
		isOpen = false;
		return super.close();
	}

	/**
	 * Determines what keybinding was used to open this dialog and returns an
	 * info string using that binding. ex:
	 * "Press 'Ctrl-Shift-L') to open the preference page";
	 *
	 * @return info text for this dialog
	 */
	private String getInfoText() {
		ECommandService commandService = context.getActiveLeaf().get(ECommandService.class);
		Command cmd = commandService.getCommand(IWorkbenchCommandConstants.WINDOW_SHOW_KEY_ASSIST);

		if (cmd != null) {
			EBindingService bindingService = context.getActiveLeaf().get(EBindingService.class);
			TriggerSequence keySeq = bindingService.getBestSequenceFor(new ParameterizedCommand(
					cmd, null));

			if (keySeq != null) {
				return NLS.bind(KeyAssistMessages.openPreferencePage, keySeq.format());
			}
		}

		return ""; //$NON-NLS-1$
	}

	/**
	 * Opens the key binding preference page, closes this dialog
	 */
	private int openPreferencePage() {
		// Create a preference dialog on the keys preference page.

		Shell shell = getShell();
		if (shell.getParent() != null) {
			shell = shell.getParent().getShell();
		}

		PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(shell,
				keysPageId, null, getSelectedBinding());
		close();
		return dialog.open();
	}

}
