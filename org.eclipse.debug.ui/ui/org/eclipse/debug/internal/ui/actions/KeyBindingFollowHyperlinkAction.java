package org.eclipse.debug.internal.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.internal.ui.views.console.ConsoleView;
import org.eclipse.debug.ui.console.IConsoleHyperlink;
import org.eclipse.jface.action.IStatusLineManager;

/**
 * A follow hyperlink action that is always enabled but reports problems
 * when a run fails. Bound to the open editor action definition by the
 * ConsoleView.
 */
public class KeyBindingFollowHyperlinkAction extends FollowHyperlinkAction {

	ConsoleView fView;
	
	public KeyBindingFollowHyperlinkAction(ConsoleView view) {
		super(view.getConsoleViewer());
		fView= view;
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		IConsoleHyperlink link = getHyperLink();
		if (link == null) {		
			IStatusLineManager statusLine= fView.getViewSite().getActionBars().getStatusLineManager();
			if (statusLine != null) {
				statusLine.setErrorMessage(ActionMessages.getString("KeyBindingFollowHyperLinkAction.No_hyperlink")); //$NON-NLS-1$
			}
			fView.getSite().getShell().getDisplay().beep();
		} else {
			link.linkActivated();
		}
	}
}
