package org.eclipse.team.internal.ccvs.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.List;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.RepositoryManager;
import org.eclipse.team.ui.sync.ITeamNode;
import org.eclipse.team.ui.sync.SyncSet;

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
		List additions = new ArrayList();
		List deletions = new ArrayList();
		for (int i = 0; i < changed.length; i++) {
			changedResources[i] = changed[i].getResource();
			// If it's an outgoing addition we need to 'add' it before comitting.
			// If it's an outgoing deletion we need to 'delete' it before committing.
			switch (changed[i].getKind() & Differencer.CHANGE_TYPE_MASK) {
				case Differencer.ADDITION:
					additions.add(changed[i].getResource());
					break;
				case Differencer.DELETION:
					deletions.add(changed[i].getResource());
					break;
			}
		}
		try {
			RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
			if (additions.size() != 0) {
				manager.add((IResource[])additions.toArray(new IResource[0]), monitor);
			}
			if (deletions.size() != 0) {
				manager.delete((IResource[])deletions.toArray(new IResource[0]), monitor);
			}
			manager.commit(changedResources, getShell(), monitor);
		} catch (TeamException e) {
			// remove the change from the set, add an error
			CVSUIPlugin.getPlugin().log(e.getStatus());
			return null;
		}
		return syncSet;
	}
}
