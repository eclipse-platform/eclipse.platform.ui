/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.views;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.update.configuration.IInstallConfiguration;
import org.eclipse.update.configuration.ILocalSite;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.internal.ui.UpdateUI;

public class SaveConfigurationAction extends Action {
	private IInstallConfiguration target;

	public SaveConfigurationAction(String text) {
		super(text);
	}

	public void setConfiguration(IInstallConfiguration target) {
		this.target = target;
	}

	public void run() {
		if (target == null)
			return;
		try {
			ILocalSite localSite = SiteManager.getLocalSite();
			localSite.addToPreservedConfigurations(target);
			localSite.save();
		} catch (CoreException e) {
			UpdateUI.logException(e);
		}
	}
}
