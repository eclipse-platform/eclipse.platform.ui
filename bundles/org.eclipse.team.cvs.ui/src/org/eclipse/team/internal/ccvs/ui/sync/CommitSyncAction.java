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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.resources.CVSRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.RepositoryManager;
import org.eclipse.team.internal.ui.sync.ChangedTeamContainer;
import org.eclipse.team.internal.ui.sync.ITeamNode;
import org.eclipse.team.internal.ui.sync.SyncSet;
import org.eclipse.team.internal.ui.sync.TeamFile;

public class CommitSyncAction extends MergeAction {
	public CommitSyncAction(CVSSyncCompareInput model, ISelectionProvider sp, String label, Shell shell) {
		super(model, sp, label, shell);
	}

	protected SyncSet run(SyncSet syncSet, IProgressMonitor monitor) {
		boolean result = saveIfNecessary();
		if (!result) return null;
		
		// If there is a conflict in the syncSet, we need to prompt the user before proceeding.
		if (syncSet.hasConflicts() || syncSet.hasIncomingChanges()) {
			switch (promptForConflicts(syncSet)) {
				case 0:
					// Yes, synchronize conflicts as well
					break;
				case 1:
					// No, only synchronize non-conflicting changes.
					syncSet.removeConflictingNodes();
					syncSet.removeIncomingNodes();
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
		List toMerge = new ArrayList();
		List incoming = new ArrayList();

		// A list of diff elements in the sync set which are incoming folder additions
		List parentCreationElements = new ArrayList();
		// A list of diff elements in the sync set which are folder conflicts
		List parentConflictElements = new ArrayList();
		
		for (int i = 0; i < changed.length; i++) {
			changedResources[i] = changed[i].getResource();
			int kind = changed[i].getKind();
			IResource resource = changed[i].getResource();
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
			switch (kind & Differencer.DIRECTION_MASK) {
				case ITeamNode.INCOMING:
					// Incoming change. Make it outgoing before committing.
					incoming.add(changed[i]);
					break;
				case ITeamNode.OUTGOING:
					switch (kind & Differencer.CHANGE_TYPE_MASK) {
						case Differencer.ADDITION:
							// Outgoing addition. 'add' it before committing.
							additions.add(resource);
							break;
						case Differencer.DELETION:
							// Outgoing deletion. 'delete' it before committing.
							deletions.add(resource);
							break;
						case Differencer.CHANGE:
							// Outgoing change. Just commit it.
							break;
					}
					break;
				case ITeamNode.CONFLICTING:
					if (changed[i] instanceof TeamFile) {
						toMerge.add(((TeamFile)changed[i]).getMergeResource().getSyncElement());
					}
					break;
			}
		}
		try {
			RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
			String comment = promptForComment(manager);
			if (comment == null) {
				// User cancelled. Remove the nodes from the sync set.
				return null;
			}
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

			// Make any incoming file changes or deletions into outgoing changes before committing.
			Iterator it = incoming.iterator();
			while (it.hasNext()) {
				ITeamNode node = (ITeamNode)it.next();
				if (node instanceof TeamFile) {
					CVSRemoteSyncElement element = (CVSRemoteSyncElement)((TeamFile)node).getMergeResource().getSyncElement();
					element.makeOutgoing(monitor);
				}
			}
			
			if (additions.size() != 0) {
				manager.add((IResource[])additions.toArray(new IResource[0]), monitor);
			}
			if (deletions.size() != 0) {
				manager.delete((IResource[])deletions.toArray(new IResource[0]), monitor);
			}
			if (toMerge.size() != 0) {
				manager.merged((IRemoteSyncElement[])toMerge.toArray(new IRemoteSyncElement[0]));
			}
			manager.commit(changedResources, comment, monitor);
			
			it = incoming.iterator();
			while (it.hasNext()) {
				ITeamNode node = (ITeamNode)it.next();
				if (node instanceof ChangedTeamContainer) {
					CVSRemoteSyncElement element = (CVSRemoteSyncElement)((ChangedTeamContainer)node).getMergeResource().getSyncElement();
					element.makeIncoming(monitor);
					element.getLocal().delete(true, monitor);
				}
			}
			
			// Reset the timestamps for any files that were not committed
			// because their contents match that of the server
			for (int i = 0; i < changedResources.length; i++) {
				IResource resource = changedResources[i];
				if (resource.getType() == IResource.FILE) {
					ICVSFile cvsFile = CVSWorkspaceRoot.getCVSFileFor((IFile)resource);
					// If the file is still modified after the commit, it probably is a pseudo change
					if (cvsFile.exists() && cvsFile.isModified()) {
						cvsFile.setTimeStamp(cvsFile.getSyncInfo().getTimeStamp());
					}
				}
			}
			
		} catch (final TeamException e) {
			getShell().getDisplay().syncExec(new Runnable() {
				public void run() {
					ErrorDialog.openError(getShell(), null, null, e.getStatus());
				}
			});
			return null;
		} catch (final CoreException e) {
			getShell().getDisplay().syncExec(new Runnable() {
				public void run() {
					ErrorDialog.openError(getShell(), Policy.bind("simpleInternal"), Policy.bind("internal"), e.getStatus()); //$NON-NLS-1$ //$NON-NLS-2$
					CVSUIPlugin.log(e.getStatus());
				}
			});
			return null;
		}
		return syncSet;
	}
	
	protected void makeInSync(IDiffElement parentElement) throws TeamException {
		// Recursively make the parent element (and its parents) in sync.
		// Walk up and find the parents which need to be made in sync too. (For
		// each parent that doesn't already have sync info).
		Vector v = new Vector();
		int parentKind = parentElement.getKind();
		while (((parentKind & Differencer.CHANGE_TYPE_MASK) == Differencer.ADDITION) &&
			((parentKind & Differencer.DIRECTION_MASK) == ITeamNode.INCOMING) ||
			 ((parentKind & Differencer.DIRECTION_MASK) == ITeamNode.CONFLICTING)) {
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
		// The commit action is enabled only for non-conflicting outgoing changes
		return new SyncSet(new StructuredSelection(node)).hasOutgoingChanges();
	}	
	
	/**
	 * Prompts the user to determine how conflicting changes should be handled.
	 * Note: This method is designed to be overridden by test cases.
	 * @return 0 to sync conflicts, 1 to sync all non-conflicts, 2 to cancel
	 */
	protected int promptForConflicts(SyncSet syncSet) {
		String[] buttons = new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL};
		String question = Policy.bind("CommitSyncAction.questionRelease"); //$NON-NLS-1$
		String title = Policy.bind("CommitSyncAction.titleRelease"); //$NON-NLS-1$
		String[] tips = new String[] {
			Policy.bind("CommitSyncAction.releaseAll"), //$NON-NLS-1$
			Policy.bind("CommitSyncAction.releasePart"), //$NON-NLS-1$
			Policy.bind("CommitSyncAction.cancelRelease") //$NON-NLS-1$
		};
		Shell shell = getShell();
		final ToolTipMessageDialog dialog = new ToolTipMessageDialog(shell, title, null, question, MessageDialog.QUESTION, buttons, tips, 0);
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				dialog.open();
			}
		});
		return dialog.getReturnCode();
	}
	
	/**
	 * Prompts the user for a release comment.
	 * Note: This method is designed to be overridden by test cases.
	 * @return the comment, or null to cancel
	 */
	protected String promptForComment(RepositoryManager manager) {
		return manager.promptForComment(getShell());
	}
	
	protected void removeNonApplicableNodes(SyncSet set, int syncMode) {
		set.removeConflictingNodes();
		set.removeIncomingNodes();
	}
}
