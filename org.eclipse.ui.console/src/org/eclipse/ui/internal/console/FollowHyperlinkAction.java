/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.console;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.TextConsoleViewer;

/**
 * Follows a hyperlink in the console
 * 
 * @since 3.1
 */
public class FollowHyperlinkAction extends Action {

	private TextConsoleViewer viewer;

    /**
	 * Constructs a follow link action
	 */
	public FollowHyperlinkAction(TextConsoleViewer consoleViewer) {
	    super(ConsoleMessages.FollowHyperlinkAction_0); 
		setToolTipText(ConsoleMessages.FollowHyperlinkAction_1); 
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IConsoleHelpContextIds.CONSOLE_OPEN_LINK_ACTION);
		this.viewer = consoleViewer;
	}

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IAction#isEnabled()
     */
    public boolean isEnabled() {
        return viewer.getHyperlink() != null;
    }
	
    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.action.IAction#run()
     */
	public void run() {
		IHyperlink link = viewer.getHyperlink();
		if (link != null) {
			link.linkActivated();
		}
	}

}
