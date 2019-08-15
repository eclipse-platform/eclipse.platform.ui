/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
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
	 * Constructs a follow link action.
	 * 
	 * @param consoleViewer the viewer containing the link
	 */
	public FollowHyperlinkAction(TextConsoleViewer consoleViewer) {
		super(ConsoleMessages.FollowHyperlinkAction_0);
		setToolTipText(ConsoleMessages.FollowHyperlinkAction_1);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IConsoleHelpContextIds.CONSOLE_OPEN_LINK_ACTION);
		this.viewer = consoleViewer;
	}

	@Override
	public boolean isEnabled() {
		return viewer.getHyperlink() != null;
	}

	@Override
	public void run() {
		IHyperlink link = viewer.getHyperlink();
		if (link != null) {
			link.linkActivated();
		}
	}

}
