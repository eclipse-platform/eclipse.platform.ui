package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareUI;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.CVSCompareEditorInput;
import org.eclipse.team.internal.ccvs.ui.CVSResourceNode;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.ResourceEditionNode;
import org.eclipse.team.internal.ccvs.ui.TagSelectionDialog;
import org.eclipse.team.ui.actions.TeamAction;

/**
 * Action for compare with tag.
 */
public class CompareWithTagAction extends TeamAction {
	/*
	 * Method declared on IActionDelegate.
	 */
	public void run(IAction action) {
		
		// Setup the holders
		final CVSTag[] tag = new CVSTag[] {null};
		final ICVSRemoteResource[] remoteResource = new ICVSRemoteResource[] { null };
		final IResource[] resources = getSelectedResources();

		
		// Show a busy curesor while popping up the Tag selection dialog
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				IProject[] projects = new IProject[resources.length];
				for (int i = 0; i < resources.length; i++) {
					projects[i] = resources[i].getProject();
				}
				TagSelectionDialog dialog = new TagSelectionDialog(getShell(), projects);
				dialog.setBlockOnOpen(true);
				int result = dialog.open();
				if (result == Dialog.CANCEL || dialog.getResult() == null) {
					return;
				}
				tag[0] = dialog.getResult();
			}
		}, Policy.bind("CompareWithTagAction.compare"), PROGRESS_BUSYCURSOR);
		
		if (tag[0] == null) return;
		
		
		for (int i = 0; i < resources.length; i++) {
			// Show a progress dialog while fethcing the remote tree
			final IResource resource = resources[i];
			run(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
					try {
						monitor.beginTask(Policy.bind("CompareWithTagAction.fetching", tag[0].getName()), 100);
						remoteResource[0] = CVSWorkspaceRoot.getRemoteTree(resource, tag[0], Policy.subMonitorFor(monitor, 100));
						monitor.done();
					} catch (TeamException e) {
						throw new InvocationTargetException(e);
					}
				}
			}, Policy.bind("CompareWithTagAction.compare"), PROGRESS_DIALOG);
			
			// Just to be safe...
			if (remoteResource[0] == null) {
				MessageDialog.openInformation(getShell(), Policy.bind("CompareWithTagAction.noRemote"), Policy.bind("CompareWithTagAction.noRemoteLong"));
				return;
			}
			
			// Show a busy cursor while opening the compare view
			run(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
					CompareUI.openCompareEditor(new CVSCompareEditorInput(new CVSResourceNode(resource), new ResourceEditionNode(remoteResource[0])));
				}
			}, Policy.bind("CompareWithTagAction.compare"), PROGRESS_BUSYCURSOR);
		}
	}
	
	protected boolean isEnabled() {
		IResource[] resources = getSelectedResources();
		// allow operation for homegeneous multiple selections
		if(resources.length>0) {
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				if(RepositoryProvider.getProvider(resource.getProject(), CVSProviderPlugin.getTypeId()) == null) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
