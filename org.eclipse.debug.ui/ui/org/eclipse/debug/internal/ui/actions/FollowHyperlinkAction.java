package org.eclipse.debug.internal.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.views.console.ConsoleViewer;
import org.eclipse.debug.ui.console.IConsoleHyperlink;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Follows a hyperlink in the console
 */
public class FollowHyperlinkAction extends SelectionProviderAction {

	/**
	 * Constructs a follow link action
	 */
	public FollowHyperlinkAction(ISelectionProvider selectionProvider) {
		super(selectionProvider, ActionMessages.getString("FollowHyperlinkAction.&Open_Link_1")); //$NON-NLS-1$
		setToolTipText(ActionMessages.getString("FollowHyperlinkAction.Follow_the_selected_hyperlink._2")); //$NON-NLS-1$
		ISharedImages images= DebugUIPlugin.getDefault().getWorkbench().getSharedImages();
		setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_OPEN_MARKER));
		WorkbenchHelp.setHelp(
			this,
			IDebugHelpContextIds.FOLLOW_CONSOLE_HYPERLINK_ACTION);
		setEnabled(false);
	}
	
	public IConsoleHyperlink getHyperLink() {
		ISelectionProvider selectionProvider = getSelectionProvider();
		if (selectionProvider instanceof ConsoleViewer) {
			ITextSelection textSelection = (ITextSelection)selectionProvider.getSelection();
			ConsoleViewer consoleViewer = (ConsoleViewer)selectionProvider;
			if (textSelection != null) {
				return consoleViewer.getHyperlink(textSelection.getOffset());
			}
		}
		return null;
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		IConsoleHyperlink link = getHyperLink();
		if (link != null) {
			link.linkActivated();
		}
	}

}
