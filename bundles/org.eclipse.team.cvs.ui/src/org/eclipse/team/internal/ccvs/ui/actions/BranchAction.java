package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.wizards.BranchWizard;
import org.eclipse.team.ui.actions.TeamAction;

/**
 * BranchAction tags the selected resources with a branch tag specified by the user,
 * and optionally updates the local resources to point to the new branch.
 */
public class BranchAction extends TeamAction {
	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		final Shell shell = getShell();
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				BranchWizard wizard = new BranchWizard();
				wizard.setResources(getSelectedResources());
				WizardDialog dialog = new WizardDialog(shell, wizard);
				dialog.open();
			}
		});
	}
	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		IResource[] resources = getSelectedResources();
		if (resources.length == 0) return false;
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			RepositoryProvider provider = RepositoryProvider.getProvider(resource.getProject(), CVSProviderPlugin.getTypeId());
			if (provider == null) return false;
			ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
			if (resource.getType() == IResource.PROJECT && ((ICVSFolder)cvsResource).isCVSFolder()) return true;
			if ( ! cvsResource.isManaged()) return false;
		}
		return true;
	}
}

