package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.compare.CompareUI;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.ui.CVSLocalCompareEditorInput;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.TagSelectionDialog;

public class CompareWithTagAction extends CVSAction {

	public void execute(IAction action) {
		final CVSTag tag;
		final ICVSRemoteResource[] remoteResource = new ICVSRemoteResource[] { null };
		final IResource[] resources = getSelectedResources();
		
		IProject[] projects = new IProject[resources.length];
		for (int i = 0; i < resources.length; i++) {
			projects[i] = resources[i].getProject();
		}
		TagSelectionDialog dialog = new TagSelectionDialog(getShell(), projects, Policy.bind("CompareWithTagAction.message"), 
			Policy.bind("TagSelectionDialog.Select_a_Tag_1"), TagSelectionDialog.INCLUDE_ALL_TAGS, false); //$NON-NLS-1$ //$NON-NLS-2$
		dialog.setBlockOnOpen(true);
		int result = dialog.open();
		if (result == Dialog.CANCEL || dialog.getResult() == null) {
			return;
		}
		tag = dialog.getResult();
		if (tag == null) return;
		CompareUI.openCompareEditor(new CVSLocalCompareEditorInput(resources, tag));
	}
	
	
	
	protected boolean isEnabled() {
		try {
			return isSelectionNonOverlapping();
		} catch(TeamException e) {
			CVSUIPlugin.log(e.getStatus());
			return false;
		}
	}

	/*
	 * @see CVSAction#needsToSaveDirtyEditors()
	 */
	protected boolean needsToSaveDirtyEditors() {
		return false;
	}
}
