package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareUI;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Action for compare with tag.
 */
public class CompareWithTagAction extends TeamAction {
	/*
	 * Method declared on IActionDelegate.
	 */
	public void run(IAction action) {
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				String title = Policy.bind("CompareWithTagAction.compare");
				try {
					IResource[] resources = getSelectedResources();
					if (resources.length != 1) return;
					IResource resource = resources[0];
		
					CVSTeamProvider provider = (CVSTeamProvider)TeamPlugin.getManager().getProvider(resource.getProject());
					TagSelectionDialog dialog = new TagSelectionDialog(getShell(), resource);
					dialog.setBlockOnOpen(true);
					int result = dialog.open();
					if (result == Dialog.CANCEL || dialog.getResult() == null) {
						return;
					}
					CVSTag tag = dialog.getResult();
					ICVSRemoteResource remoteResource = CVSWorkspaceRoot.getRemoteTree(resource, tag, new NullProgressMonitor());
					CompareUI.openCompareEditor(new CVSCompareEditorInput(new CVSResourceNode(resource), new ResourceEditionNode(remoteResource)));
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
			}
		}, Policy.bind("CompareWithTagAction.compare"), PROGRESS_BUSYCURSOR);
			
	}
	
	protected boolean isEnabled() {
		return getSelectedResources().length == 1;
	}
}
