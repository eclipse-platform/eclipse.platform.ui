/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal;

import org.eclipse.help.ILiveHelpAction;
import org.eclipse.help.browser.IBrowser;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.browser.CurrentBrowser;
import org.eclipse.help.ui.internal.browser.embedded.EmbeddedBrowserAdapter;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class ShowCapabilitiesPreferenceAction implements ILiveHelpAction {

	public void setInitializationString(String data) {
	}

	public void run() {
		final Display display = PlatformUI.getWorkbench().getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				Shell[] shells = display.getShells();
				Shell shell = shells.length>0?shells[0]:null;
				if (shell!=null)
					shell.setActive();
				display.update();
				PreferenceDialog dialog = PreferencesUtil
						.createPreferenceDialogOn(shell, getCapabilityPageId(),
								null, null);
				dialog.open();
			}
		});
	}

	private String getCapabilityPageId() {
		return "org.eclipse.sdk.capabilities";
	}
}