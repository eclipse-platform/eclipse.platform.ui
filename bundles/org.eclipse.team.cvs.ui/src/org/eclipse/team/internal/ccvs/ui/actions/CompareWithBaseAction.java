package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareUI;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.CVSCompareEditorInput;
import org.eclipse.team.internal.ccvs.ui.CVSResourceNode;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.ResourceEditionNode;
import org.eclipse.team.ui.actions.TeamAction;

/**
 * Action for container compare with base.
 */
public class CompareWithBaseAction extends TeamAction {
	/*
	 * Method declared on IActionDelegate.
	 */
	public void run(IAction action) {
		
		// Setup the holders
		final IResource[] resource = new IResource[] {null};
		final ICVSRemoteResource[] remoteResource = new ICVSRemoteResource[] { null };
		
		// Fetch the remote tree
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					IResource[] resources = getSelectedResources();
					if (resources.length != 1) return;
					resource[0] = resources[0];
					ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource[0]);
					
					monitor.beginTask(Policy.bind("CompareWithRemoteAction.fetching"), 100); //$NON-NLS-1$
					remoteResource[0] = CVSWorkspaceRoot.getRemoteTree(resource[0], CVSTag.BASE, Policy.subMonitorFor(monitor, 100));
					monitor.done();
					
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
			}
		}, Policy.bind("CompareWithRemoteAction.compare"), PROGRESS_DIALOG); //$NON-NLS-1$
		
		// Just to be safe...
		if (remoteResource[0] == null) {
			MessageDialog.openInformation(getShell(), Policy.bind("CompareWithRemoteAction.noRemote"), Policy.bind("CompareWithRemoteAction.noRemoteLong")); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
					
		// Open the compare view
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				CompareUI.openCompareEditor(new CVSCompareEditorInput(new CVSResourceNode(resource[0]), new ResourceEditionNode(remoteResource[0])));
			}
		}, Policy.bind("CompareWithRemoteAction.compare"), PROGRESS_BUSYCURSOR); //$NON-NLS-1$
		
	}
	
	protected boolean isEnabled() {
		IResource[] resources = getSelectedResources();
		if (resources.length != 1) return false;
		CVSTeamProvider provider = (CVSTeamProvider)RepositoryProvider.getProvider(resources[0].getProject(), CVSProviderPlugin.getTypeId());
		if(provider==null) return false;
		return provider.hasRemote(resources[0]);
	}
}
