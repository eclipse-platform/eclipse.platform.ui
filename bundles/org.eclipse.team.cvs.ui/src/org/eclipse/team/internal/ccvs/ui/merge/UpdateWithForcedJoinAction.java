package org.eclipse.team.internal.ccvs.ui.merge;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.ui.RepositoryManager;
import org.eclipse.team.internal.ccvs.ui.sync.CVSSyncCompareInput;
import org.eclipse.team.internal.ui.sync.ITeamNode;
import org.eclipse.team.internal.ui.sync.SyncSet;

/*
 * Used only in the merge editor. This action allows the user to select a single conflict and use
 * the cvs update -j command to merge the changes. This is required because when building the
 * sync tree for a merge the cvs command 'cvs -n update -j -j' does not tell us which files
 * can be auto-merged. This action then allows the user to run the merge without having to
 * individually select each difference and use the 'copy right to left' buttons.
 */
public class UpdateWithForcedJoinAction extends UpdateMergeAction {
	public UpdateWithForcedJoinAction(CVSSyncCompareInput model, ISelectionProvider sp, String label, Shell shell) {
		super(model, sp, label, shell);		
	}
	
	/*
	 * @see UpdateSyncAction#runUpdateDeep(ITeamNode[], RepositoryManager, IProgressMonitor)
	 */
	protected void runUpdateDeep(ITeamNode[] nodes, RepositoryManager manager, IProgressMonitor monitor) throws TeamException {
		// cannot be called from this action
		Assert.isTrue(false);
	}

	/*
	 * @see UpdateSyncAction#runUpdateIgnoreLocalShallow(ITeamNode[], RepositoryManager, IProgressMonitor)
	 */
	protected void runUpdateIgnoreLocalShallow(ITeamNode[] nodes, RepositoryManager manager, IProgressMonitor monitor) throws TeamException {
		// force an update -j -j to be called on the conflict
		mergeWithLocal(nodes, manager, true, monitor);
	}

	/*
	 * @see UpdateSyncAction#runUpdateShallow(ITeamNode[], RepositoryManager, IProgressMonitor)
	 */
	protected void runUpdateShallow(ITeamNode[] nodes, RepositoryManager manager, IProgressMonitor monitor)	throws TeamException {
		// cannot be called from this action
		Assert.isTrue(false);
	}
	
	/*
	 * @see MergeAction#isEnabled(ITeamNode)
	 */
	protected boolean isEnabled(ITeamNode node) {
		int kind = node.getKind();
		if ((node.getChangeDirection() == IRemoteSyncElement.CONFLICTING) && 
		     (kind & IRemoteSyncElement.AUTOMERGE_CONFLICT) == 0) {
			 return true;
		} else {
			return false;
		}
	}
	/*
	 * @see UpdateSyncAction#promptForConflicts()
	 */
	protected boolean promptForConflicts() {
		// don't prompt for overriding conflicts, because this action is simply merging and creating a backup copy of the original file.
		return true;
	}

	/*
	 * Override removeNonApplicableNodes because conflicting nodes should not be removed from this set.
	 */	
	protected void removeNonApplicableNodes(SyncSet set, int syncMode) {
		set.removeOutgoingNodes();
		set.removeIncomingNodes();
	}
}
