package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class ShowCVSPerspectiveAction extends Action {
	public void run() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();
		if (activeWindow == null) {
			return;
		}
		IWorkbenchPage activePage = activeWindow.getActivePage();
		if (activePage == null) {
			return;
		}
		IPerspectiveDescriptor cvsPerspective = workbench.getPerspectiveRegistry().findPerspectiveWithId("org.eclipse.team.cvs.ui.cvsPerspective"); //$NON-NLS-1$
		if(cvsPerspective!=null) {
			activePage.setPerspective(cvsPerspective);
		}
	}
}