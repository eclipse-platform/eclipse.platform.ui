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
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
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
		final IResource[] resource = new IResource[] {null};
		final CVSTag[] tag = new CVSTag[] {null};
		final ICVSRemoteResource[] remoteResource = new ICVSRemoteResource[] { null };
		
		// Show a busy curesor while popping up the Tag selection dialog
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				IResource[] resources = getSelectedResources();
				if (resources.length != 1) return;
				resource[0] = resources[0];
	
				CVSTeamProvider provider = (CVSTeamProvider)TeamPlugin.getManager().getProvider(resource[0].getProject());
				TagSelectionDialog dialog = new TagSelectionDialog(getShell(), resource[0]);
				dialog.setBlockOnOpen(true);
				int result = dialog.open();
				if (result == Dialog.CANCEL || dialog.getResult() == null) {
					return;
				}
				tag[0] = dialog.getResult();
			}
		}, Policy.bind("CompareWithTagAction.compare"), PROGRESS_BUSYCURSOR);
		
		if (tag[0] == null) return;
		
		// Show a progress dialog while fethcing the remote tree
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					// This is the only use of the monitor so no submonitor is created
					remoteResource[0] = CVSWorkspaceRoot.getRemoteTree(resource[0], tag[0], monitor);
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
				CompareUI.openCompareEditor(new CVSCompareEditorInput(new CVSResourceNode(resource[0]), new ResourceEditionNode(remoteResource[0])));
			}
		}, Policy.bind("CompareWithTagAction.compare"), PROGRESS_BUSYCURSOR);
	}
	
	protected boolean isEnabled() {
		return getSelectedResources().length == 1;
	}
}
