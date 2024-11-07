/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.util.PrefUtil;

/**
 * Action to open the help contents.
 *
 * @since 3.0
 */
public class HelpContentsAction extends Action implements IWorkbenchAction {
	/**
	 * The workbench window; or <code>null</code> if this action has been
	 * <code>dispose</code>d.
	 */
	private IWorkbenchWindow workbenchWindow;

	/**
	 * Zero-arg constructor to allow cheat sheets to reuse this action.
	 */
	public HelpContentsAction() {
		this(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
	}

	/**
	 * Constructor for use by ActionFactory.
	 *
	 * @param window the window
	 */
	public HelpContentsAction(IWorkbenchWindow window) {
		if (window == null) {
			throw new IllegalArgumentException();
		}
		this.workbenchWindow = window;
		setActionDefinitionId(IWorkbenchCommandConstants.HELP_HELP_CONTENTS);

		// support for allowing a product to override the text for the action
		String overrideText = PrefUtil.getAPIPreferenceStore()
				.getString(IWorkbenchPreferenceConstants.HELP_CONTENTS_ACTION_TEXT);
		if ("".equals(overrideText)) { //$NON-NLS-1$
			setText(WorkbenchMessages.HelpContentsAction_text);
			setToolTipText(WorkbenchMessages.HelpContentsAction_toolTip);
		} else {
			setText(overrideText);
			setToolTipText(Action.removeMnemonics(overrideText));
		}
		setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_HELP_CONTENTS));
		window.getWorkbench().getHelpSystem().setHelp(this, IWorkbenchHelpContextIds.HELP_CONTENTS_ACTION);
	}

	@Override
	public void run() {
		if (workbenchWindow == null) {
			// action has been disposed
			return;
		}
		// This may take a while, so use the busy indicator
		BusyIndicator.showWhile(null, () -> workbenchWindow.getWorkbench().getHelpSystem().displayHelp());
	}

	@Override
	public void dispose() {
		workbenchWindow = null;
	}

}
