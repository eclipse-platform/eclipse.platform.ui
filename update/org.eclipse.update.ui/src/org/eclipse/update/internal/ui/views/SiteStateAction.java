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

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.update.configuration.IConfiguredSite;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.forms.ActivityConstraints;

/**
 * @author dejan
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */

public class SiteStateAction extends Action {
	private IConfiguredSite site;

	public SiteStateAction() {
	}

	public void setSite(IConfiguredSite site) {
		this.site = site;
		boolean state = site.isEnabled();
		setText(state ? UpdateUI.getString("SiteStateAction.disableLabel") : UpdateUI.getString("SiteStateAction.enableLabel")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void run() {
		if (site == null)
			return;
		boolean oldValue = site.isEnabled();
		boolean newValue = !oldValue;
		if (!confirm(newValue))
			return;
		site.setEnabled(newValue);
		IStatus status = ActivityConstraints.validateCurrentState();
		if (status != null) {
			ErrorDialog.openError(
				UpdateUI.getActiveWorkbenchShell(),
				null,
				null,
				status);
			site.setEnabled(oldValue);
			return;
		} else {
			// do a restart
			try {
				SiteManager.getLocalSite().save();
				UpdateUI.requestRestart();
				UpdateUI.getDefault().getUpdateModel().fireObjectChanged(site, "");
			} catch (CoreException e) {
				site.setEnabled(oldValue);
				UpdateUI.logException(e);
			}
		}
	}

	private boolean confirm(boolean newState) {
		String name = site.getSite().getURL().toString();
		String enableMessage = UpdateUI.getFormattedMessage("SiteStateAction.enableMessage", name); //$NON-NLS-1$ //$NON-NLS-2$
		String disableMessage = UpdateUI.getFormattedMessage("SiteStateAction.disableMessage", name); //$NON-NLS-1$ //$NON-NLS-2$

		String message = newState ? enableMessage : disableMessage;
		return MessageDialog.openConfirm(UpdateUI.getActiveWorkbenchShell(), UpdateUI.getString("SiteStateAction.dialogTitle"), message); //$NON-NLS-1$
	}
}
