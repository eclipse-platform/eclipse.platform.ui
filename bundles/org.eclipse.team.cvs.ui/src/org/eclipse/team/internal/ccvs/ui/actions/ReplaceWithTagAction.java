package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.core.resources.LocalFile;
import org.eclipse.team.internal.ccvs.core.resources.LocalFolder;
import org.eclipse.team.internal.ccvs.core.resources.LocalResource;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.TagSelectionDialog;
import org.eclipse.team.ui.actions.TeamAction;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Action for replace with tag.
 */
public class ReplaceWithTagAction extends TeamAction {
	/*
	 * Method declared on IActionDelegate.
	 */
	public void run(IAction action) {
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					IResource[] resources = getSelectedResources();
					if (resources.length != 1) return;
					IResource resource = resources[0];
		
					CVSTeamProvider provider = (CVSTeamProvider)TeamPlugin.getManager().getProvider(resource.getProject());
					// To do: check for local changes and warn of overwriting.
					LocalResource cvsResource = null;
					if (resources[0].getType() == IResource.FILE) {
						cvsResource = new LocalFile(resource.getLocation().toFile());
					} else {
						cvsResource = new LocalFolder(resource.getLocation().toFile());
					}
		
					TagSelectionDialog dialog = new TagSelectionDialog(getShell(), resource);
					dialog.setBlockOnOpen(true);
					if (dialog.open() == Dialog.CANCEL) {
						return;
					}
					CVSTag tag = dialog.getResult();
					if (tag == null) {
						return;
					}
					provider.get(new IResource[] { resource }, IResource.DEPTH_INFINITE, tag, monitor);
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
			}
		}, Policy.bind("ReplaceWithTagAction.replace"), this.PROGRESS_BUSYCURSOR);			
	}
	
	protected boolean isEnabled() {
		return getSelectedResources().length == 1;
	}
}
