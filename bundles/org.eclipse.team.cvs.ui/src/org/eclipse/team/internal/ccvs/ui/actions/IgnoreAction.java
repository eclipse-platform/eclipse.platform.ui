package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.LocalFile;
import org.eclipse.team.internal.ccvs.core.resources.LocalFolder;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.ui.actions.TeamAction;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class IgnoreAction extends TeamAction {
	protected boolean isEnabled() throws TeamException {
		IResource[] resources = getSelectedResources();
		if (resources.length == 0) return false;
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			ICVSResource cvsResource = null;
			switch (resource.getType()) {
				case IResource.FILE:
					cvsResource = new LocalFile(resource.getLocation().toFile());
					break;
				case IResource.FOLDER:
					cvsResource = new LocalFolder(resource.getLocation().toFile());
					break;
				default:
					return false;
			}
			if (cvsResource.isManaged()) return false;
			if (cvsResource.isIgnored()) return false;
		}
		return true;
	}
	public void run(final IAction action) {
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				IResource[] resources = getSelectedResources();
				for (int i = 0; i < resources.length; i++) {
					IResource resource = resources[i];
					ICVSResource cvsResource = null;
					switch (resource.getType()) {
						case IResource.FILE:
							cvsResource = new LocalFile(resource.getLocation().toFile());
							break;
						case IResource.FOLDER:
							cvsResource = new LocalFolder(resource.getLocation().toFile());
							break;
					}
					if (cvsResource == null) return;
					try {
						cvsResource.setIgnored();
						if (action != null) {
							action.setEnabled(false);
						}
					} catch (CVSException e) {
						ErrorDialog.openError(getShell(), null, null, e.getStatus());
						return;
					}
				}
			}
		}, Policy.bind("IgnoreAction.ignore"), PROGRESS_BUSYCURSOR);
	}
}
