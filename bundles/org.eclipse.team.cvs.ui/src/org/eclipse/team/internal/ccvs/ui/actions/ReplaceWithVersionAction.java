package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.core.resources.LocalFile;
import org.eclipse.team.internal.ccvs.core.resources.LocalFolder;
import org.eclipse.team.internal.ccvs.core.resources.LocalResource;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.VersionSelectionDialog;
import org.eclipse.team.ui.actions.TeamAction;

/**
 * Action for replace with version.
 */
public class ReplaceWithVersionAction extends TeamAction {
	/*
	 * Method declared on IActionDelegate.
	 */
	public void run(IAction action) {
		String title = Policy.bind("CompareWithVersionAction.compare");
		try {
			IResource[] resources = getSelectedResources();
			if (resources.length != 1) return;
			IResource resource = resources[0];

			CVSTeamProvider provider = (CVSTeamProvider)TeamPlugin.getManager().getProvider(resource.getProject());
			LocalResource cvsResource = null;
			if (resources[0].getType()==IResource.FILE) {
				cvsResource = new LocalFile(resource.getLocation().toFile());
			} else {
				cvsResource = new LocalFolder(resource.getLocation().toFile());
			}

			VersionSelectionDialog dialog = new VersionSelectionDialog(getShell(), resource);
			dialog.setBlockOnOpen(true);
			int result = dialog.open();
			if (result == Dialog.CANCEL || dialog.getResult() == null) {
				return;
			}
			CVSTag tag = dialog.getResult();
			ICVSRemoteResource remoteResource = (ICVSRemoteResource)provider.getRemoteTree(resource, tag, new NullProgressMonitor());
			System.out.println("Not implemented");
		} catch (TeamException e) {
			ErrorDialog.openError(getShell(), title, null, e.getStatus());
		}
	}
	
	protected boolean isEnabled() {
		return getSelectedResources().length == 1;
	}
}
