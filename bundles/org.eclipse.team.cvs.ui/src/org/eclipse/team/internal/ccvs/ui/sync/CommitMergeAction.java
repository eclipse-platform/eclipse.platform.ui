package org.eclipse.team.internal.ccvs.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ui.sync.ITeamNode;
import org.eclipse.team.internal.ui.sync.SyncSet;

public class CommitMergeAction extends MergeAction {
	public CommitMergeAction(CVSSyncCompareInput model, ISelectionProvider sp, int direction, String label, Shell shell) {
		super(model, sp, direction, label, shell);
	}
	/*
	 * @see MergeAction#isMatchingKind(int)
	 */
	protected boolean isMatchingKind(int kind) {
		if ((kind & IRemoteSyncElement.DIRECTION_MASK) != IRemoteSyncElement.OUTGOING) return false;
		int change = kind & IRemoteSyncElement.CHANGE_MASK;
		return (change == IRemoteSyncElement.CHANGE || change == IRemoteSyncElement.ADDITION);
	}

	protected SyncSet run(SyncSet syncSet, IProgressMonitor monitor) {
		ITeamNode[] changed = syncSet.getChangedNodes();
		IResource[] changedResources = new IResource[changed.length];
		for (int i = 0; i < changed.length; i++) {
			changedResources[i] = changed[i].getResource();
		}
		try {
			CVSUIPlugin.getPlugin().getRepositoryManager().commit(changedResources, getShell(), monitor);
		} catch (TeamException e) {
			// remove the change from the set, add an error
			CVSUIPlugin.getPlugin().log(e.getStatus());
			return null;
		}
		return syncSet;
	}
}
