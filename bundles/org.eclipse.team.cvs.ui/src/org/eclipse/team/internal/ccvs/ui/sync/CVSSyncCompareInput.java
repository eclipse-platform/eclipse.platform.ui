package org.eclipse.team.internal.ccvs.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.core.ITeamProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.ui.sync.CatchupReleaseViewer;
import org.eclipse.team.ui.sync.ChangedTeamContainer;
import org.eclipse.team.ui.sync.ITeamNode;
import org.eclipse.team.ui.sync.SyncCompareInput;
import org.eclipse.team.ui.sync.SyncSet;
import org.eclipse.team.ui.sync.TeamFile;
import org.eclipse.team.ui.sync.UnchangedTeamContainer;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class CVSSyncCompareInput extends SyncCompareInput {
	IResource[] resources;
	/**
	 * Creates a new catchup or release operation.
	 */
	public CVSSyncCompareInput(IResource[] resources) {
		super();
		this.resources = resources;
	}
	/**
	 * Overridden to create a custom DiffTreeViewer in the top left pane of the CompareProvider.
	 * 
	 * Subclasses must create and return a new CatchupReleaseViewer, and set the viewer
	 * using setViewer().
	 */
	public Viewer createDiffViewer(Composite parent) {
		CatchupReleaseViewer catchupReleaseViewer = new CVSCatchupReleaseViewer(parent, this);
		setViewer(catchupReleaseViewer);
		return catchupReleaseViewer;
	}

	protected IRemoteSyncElement[] createSyncElements(IProgressMonitor monitor) throws TeamException {
		IRemoteSyncElement[] trees = new IRemoteSyncElement[resources.length];
		int work = 1000 * resources.length;
		monitor.beginTask(null, work);
		try {
			for (int i = 0; i < trees.length; i++) {
				trees[i] = CVSWorkspaceRoot.getRemoteSyncTree(resources[i], null, Policy.subMonitorFor(monitor, 1000));
			}
		} finally {
			monitor.done();
		}
		return trees;
	}

	protected void updateView() {
		// Update the view
		if (getDiffRoot().hasChildren()) {
			getViewer().refresh();
		} else {
			getViewer().setInput(null);
		}
		
		// Update the status line
		updateStatusLine();
	}
	
	/**
	 * Overridden to mark the source as merged.
	 */
	protected void compareInputChanged(ICompareInput source) {
		super.compareInputChanged(source);
		contentsChanged(source);
	}
	protected void contentsChanged(ICompareInput source) {
		// Mark the source as merged.
		if (source instanceof TeamFile) {
			IRemoteSyncElement element = ((TeamFile)source).getMergeResource().getSyncElement();
			try {
				CVSUIPlugin.getPlugin().getRepositoryManager().merged(new IRemoteSyncElement[] {element});
			} catch (TeamException e) {
				ErrorDialog.openError(getShell(), null, null, e.getStatus());
			}
		}
	}
	/*
	 * @see SyncCompareInput#getSyncGranularity()
	 */
	protected int getSyncGranularity() {
		// assuming that sync is always performed relative to the current branch. In
		// these cases the server will perform the content comparison for us.
		return IRemoteSyncElement.GRANULARITY_TIMESTAMP;
	}
}
