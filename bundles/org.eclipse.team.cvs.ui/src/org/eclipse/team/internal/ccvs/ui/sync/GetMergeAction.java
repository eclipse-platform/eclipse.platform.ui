package org.eclipse.team.internal.ccvs.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.resources.CVSRemoteSyncElement;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.RepositoryManager;
import org.eclipse.team.ui.sync.ChangedTeamContainer;
import org.eclipse.team.ui.sync.ITeamNode;
import org.eclipse.team.ui.sync.SyncSet;
import org.eclipse.team.ui.sync.TeamFile;
import org.eclipse.team.ui.sync.UnchangedTeamContainer;

/**
 * GetMergeAction is run on a set of sync nodes when the "Get" menu item is performed
 * in the Synchronize view.
 */
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
		// A list of diff elements in the sync set which are incoming folder additions
		List parentCreationElements = new ArrayList();
		// A list of diff elements in the sync set which are folder conflicts
		List parentConflictElements = new ArrayList();
		for (int i = 0; i < changed.length; i++) {
			IDiffContainer parent = changed[i].getParent();
			if (parent != null) {
				int parentKind = changed[i].getParent().getKind();
				if (((parentKind & Differencer.CHANGE_TYPE_MASK) == Differencer.ADDITION) &&
					((parentKind & Differencer.DIRECTION_MASK) == ITeamNode.INCOMING)) {
					parentCreationElements.add(parent);
				} else if ((parentKind & Differencer.DIRECTION_MASK) == ITeamNode.CONFLICTING) {
					parentConflictElements.add(parent);
				}
			}
			if ((changed[i].getKind() & Differencer.CHANGE_TYPE_MASK) == Differencer.ADDITION) {
				addedResources.add(changed[i].getResource());
			} else {
				changedResources.add(changed[i].getResource());
			}
		}
		try {
			RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
			if (parentCreationElements.size() > 0) {
				// If a node has a parent that is an incoming folder creation, we have to 
				// create that folder locally and set its sync info before we can get the
				// node itself. We must do this for all incoming folder creations (recursively)
				// in the case where there are multiple levels of incoming folder creations.
				Iterator it = parentCreationElements.iterator();
				while (it.hasNext()) {
					makeInSync((IDiffElement)it.next());
				}				
			}
			if (parentConflictElements.size() > 0) {
				// If a node has a parent that is a folder conflict, that means that the folder
				// exists locally but has no sync info. In order to get the node, we have to 
				// create the sync info for the folder (and any applicable parents) before we
				// get the node itself.
				Iterator it = parentConflictElements.iterator();
				while (it.hasNext()) {
					makeInSync((IDiffElement)it.next());
				}				
			}
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
	
	private void makeInSync(IDiffElement parentElement) throws TeamException {
		// Recursively make the parent element (and its parents) in sync.
		// Walk up and find the parents which need to be made in sync too. (For
		// each parent that doesn't already have sync info.
		Vector v = new Vector();
		int parentKind = parentElement.getKind();
		while (((parentKind & Differencer.CHANGE_TYPE_MASK) == Differencer.ADDITION) &&
			((parentKind & Differencer.DIRECTION_MASK) == ITeamNode.INCOMING)) {
			v.add(0, parentElement);
			parentElement = parentElement.getParent();
			parentKind = parentElement == null ? 0 : parentElement.getKind();
		}
		Iterator parentIt = v.iterator();
		while (parentIt.hasNext()) {
			IDiffElement next = (IDiffElement)parentIt.next();
			if (next instanceof ChangedTeamContainer) {
				CVSRemoteSyncElement syncElement = (CVSRemoteSyncElement)((ChangedTeamContainer)next).getMergeResource().getSyncElement();
				// Create the sync info
				syncElement.makeInSync(new NullProgressMonitor());
			}
		}
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
