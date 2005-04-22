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
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class ShowCapabilitiesPreferenceAction implements ILiveHelpAction {

	public void setInitializationString(String data) {
	}

	public void run() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				PreferenceDialog dialog = PreferencesUtil
						.createPreferenceDialogOn(null, getCapabilityPageId(),
								null, null);
				dialog.open();
			}
		});
	}

	private String getCapabilityPageId() {
		// TODO must allow product to override this!!
		return "org.eclipse.sdk.capabilities";
	}
}