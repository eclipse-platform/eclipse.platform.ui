package org.eclipse.team.internal.ccvs.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.resources.CVSRemoteSyncElement;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.RepositoryManager;
import org.eclipse.team.ui.sync.ChangedTeamContainer;
import org.eclipse.team.ui.sync.ITeamNode;
import org.eclipse.team.ui.sync.SyncSet;
import org.eclipse.team.ui.sync.SyncView;
import org.eclipse.team.ui.sync.TeamFile;
import org.eclipse.team.ui.sync.UnchangedTeamContainer;

public class CommitMergeAction extends MergeAction {
	public CommitMergeAction(CVSSyncCompareInput model, ISelectionProvider sp, int direction, String label, Shell shell) {
		super(model, sp, direction, label, shell);
	}

	protected SyncSet run(SyncSet syncSet, IProgressMonitor monitor) {
		// If there is a conflict in the syncSet, we need to prompt the user before proceeding.
		if (syncSet.hasConflicts() || syncSet.hasIncomingChanges()) {
			String[] buttons = new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL};
			String question = Policy.bind("CommitMergeAction.questionRelease");
			String title = Policy.bind("CommitMergeAction.titleRelease");
			String[] tips = new String[] {
				Policy.bind("CommitMergeAction.releaseAll"),
				Policy.bind("CommitMergeAction.releasePart"),
				Policy.bind("CommitMergeAction.cancelRelease")
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
		if (changed.length == 0) {
			return syncSet;
		}
		IResource[] changedResources = new IResource[changed.length];
		List additions = new ArrayList();
		List deletions = new ArrayList();
		List conflicts = new ArrayList();
		List incoming = new ArrayList();
		
		for (int i = 0; i < changed.length; i++) {
			changedResources[i] = changed[i].getResource();
			int kind = changed[i].getKind();
			switch (kind & Differencer.CHANGE_TYPE_MASK) {
				case Differencer.ADDITION:
					// Outgoing addition. 'add' it before committing.
					additions.add(changed[i].getResource());
					break;
				case Differencer.DELETION:
					if ((kind & Differencer.DIRECTION_MASK) == ITeamNode.INCOMING) {
						// Incoming deletion. makeOutgoing before committing.
						incoming.add(changed[i]);
					} else {
						// Outgoing deletion. 'delete' it before committing.
						deletions.add(changed[i].getResource());
					}
					break;
			}
			// If it's a conflicting change we need to mark it as merged before committing.
			if ((changed[i].getKind() & Differencer.DIRECTION_MASK) == Differencer.CONFLICTING) {
				if (changed[i] instanceof TeamFile) {
					conflicts.add(((TeamFile)changed[i]).getMergeResource().getSyncElement());
				}
			}
		}
		try {
			RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
			String comment = manager.promptForComment(getShell());
			if (comment == null) {
				// User cancelled. Remove the nodes from the sync set.
				return null;
			} else {
				// Make any incoming deletions into outgoing changes before committing.
				Iterator it = incoming.iterator();
				while (it.hasNext()) {
					ITeamNode node = (ITeamNode)it.next();
					if (node instanceof TeamFile) {
						CVSRemoteSyncElement element = (CVSRemoteSyncElement)((TeamFile)node).getMergeResource().getSyncElement();
						element.makeOutgoing(monitor);
					} else if (node instanceof ChangedTeamContainer) {
						CVSRemoteSyncElement element = (CVSRemoteSyncElement)((ChangedTeamContainer)node).getMergeResource().getSyncElement();
						element.makeOutgoing(monitor);
					}
				}
				
				if (additions.size() != 0) {
					manager.add((IResource[])additions.toArray(new IResource[0]), monitor);
				}
				if (deletions.size() != 0) {
					manager.delete((IResource[])deletions.toArray(new IResource[0]), monitor);
				}
				if (conflicts.size() != 0) {
					manager.merged((IRemoteSyncElement[])conflicts.toArray(new IRemoteSyncElement[0]));
				}
				manager.commit(changedResources, comment, getShell(), monitor);
			}
		} catch (final TeamException e) {
			getShell().getDisplay().syncExec(new Runnable() {
				public void run() {
					ErrorDialog.openError(getShell(), null, null, e.getStatus());
				}
			});
			return null;
		}
		return syncSet;
	}
	
	protected boolean isEnabled(ITeamNode node) {
		int kind = node.getKind();
		if (node instanceof TeamFile) {
			int direction = kind & Differencer.DIRECTION_MASK;
			if (direction == ITeamNode.OUTGOING || direction == Differencer.CONFLICTING) {
				return true;
			}
			// allow to release over incoming deletions
			return (kind & Differencer.CHANGE_TYPE_MASK) == Differencer.DELETION;
		}
		if (node instanceof ChangedTeamContainer) {
			if ((kind & Differencer.DIRECTION_MASK) == ITeamNode.OUTGOING) {
				return true;
			}
			// Fall through to UnchangedTeamContainer code
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
