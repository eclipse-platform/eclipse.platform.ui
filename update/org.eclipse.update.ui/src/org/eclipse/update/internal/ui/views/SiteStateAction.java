/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.views;

import java.lang.reflect.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.operations.*;

/**
 * Toggles a site's enabled state
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
		try {
			if (site == null)
				return;
			
			IStatus status = OperationsManager.getValidator().validatePlatformConfigValid();
			if (status != null) {
				ErrorDialog.openError(
						UpdateUI.getActiveWorkbenchShell(),
						null,
						null,
						status);
				return;
			}
			
			boolean oldValue = site.isEnabled();
			if (!confirm(!oldValue))
				return;
			
			IOperation toggleSiteOperation = OperationsManager.getOperationFactory().createToggleSiteOperation(site);
			boolean restartNeeded = toggleSiteOperation.execute(null, null);
					
			UpdateUI.requestRestart(restartNeeded);

		} catch (CoreException e) {
			ErrorDialog.openError(
				UpdateUI.getActiveWorkbenchShell(),
				null,
				null,
				e.getStatus());
		} catch (InvocationTargetException e) {
			UpdateUI.logException(e);
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
