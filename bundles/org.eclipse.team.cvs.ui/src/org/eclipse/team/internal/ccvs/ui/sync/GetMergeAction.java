package org.eclipse.team.internal.ccvs.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.List;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.RepositoryManager;
import org.eclipse.team.ui.sync.ChangedTeamContainer;
import org.eclipse.team.ui.sync.ITeamNode;
import org.eclipse.team.ui.sync.SyncSet;
import org.eclipse.team.ui.sync.TeamFile;
import org.eclipse.team.ui.sync.UnchangedTeamContainer;

public class GetMergeAction extends MergeAction {
	public GetMergeAction(CVSSyncCompareInput model, ISelectionProvider sp, int direction, String label, Shell shell) {
		super(model, sp, direction, label, shell);
	}

	protected SyncSet run(SyncSet syncSet, IProgressMonitor monitor) {
		// If there is a conflict in the syncSet, we need to prompt the user before proceeding.
		if (syncSet.hasConflicts()) {
			String[] buttons = new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL};
			String question = Policy.bind("GetMergeAction.questionCatchup");
			String title = Policy.bind("GetMergeAction.titleCatchup");
			String[] tips = new String[] {
				Policy.bind("GetMergeAction.catchupAll"),
				Policy.bind("GetMergeAction.catchupPart"),
				Policy.bind("GetMergeAction.cancelCatchup")
			};
			Shell shell = getShell();
			final ToolTipMessageDialog dialog = new ToolTipMessageDialog(shell, title, null, question, MessageDialog.QUESTION, buttons, tips, 0);
			shell.getDisplay().syncExec(new Runnable() {
				public void run() {
					dialog.open();
				}
			});
			switch (dialog.getReturnCode()) {
				case 0:
					// Yes, synchronize conflicts as well
					break;
				case 1:
					// No, only synchronize non-conflicting changes.
					syncSet.removeConflictingNodes();
					break;
				case 2:
				default:
					// Cancel
					return null;
			}	
		}
		ITeamNode[] changed = syncSet.getChangedNodes();
		List changedResources = new ArrayList();
		List addedResources = new ArrayList();
		for (int i = 0; i < changed.length; i++) {
			if ((changed[i].getKind() & Differencer.CHANGE_TYPE_MASK) == Differencer.ADDITION) {
				addedResources.add(changed[i].getResource());
			} else {
				changedResources.add(changed[i].getResource());
			}
		}
		try {
			RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
			if (addedResources.size() > 0) {
				manager.update((IResource[])addedResources.toArray(new IResource[0]), monitor);
			}
			if (changedResources.size() > 0) {
				manager.get((IResource[])changedResources.toArray(new IResource[0]), monitor);
			}
		} catch (TeamException e) {
			// remove the change from the set, add an error
			CVSUIPlugin.getPlugin().log(e.getStatus());
			return null;
		}
		return syncSet;
	}
	
	protected boolean isEnabled(ITeamNode node) {
		int kind = node.getKind();
		if (node instanceof TeamFile) {
			int direction = kind & Differencer.DIRECTION_MASK;
			if (direction == ITeamNode.INCOMING || direction == Differencer.CONFLICTING) {
					return true;
			}
			// allow to catchup outgoing deletions
			return (kind & Differencer.CHANGE_TYPE_MASK) == Differencer.DELETION;
		}
		if (node instanceof ChangedTeamContainer) {
			// first check for changes to this folder
			int direction = kind & Differencer.DIRECTION_MASK;
			if (direction == ITeamNode.INCOMING || direction == Differencer.CONFLICTING) {
				return true;
			}
			// Fall through to the UnchangedTeamContainer code
		}
		if (node instanceof UnchangedTeamContainer) {
			IDiffElement[] children = ((UnchangedTeamContainer)node).getChildren();
			for (int i = 0; i < children.length; i++) {
				ITeamNode child = (ITeamNode)children[i];
				if (isEnabled(child)) {
					return true;
				}
			}
			return false;
		}
		return false;
	}
}
