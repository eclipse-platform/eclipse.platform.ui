package org.eclipse.team.internal.ccvs.ui.merge;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.resources.CVSRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.sync.CVSSyncCompareInput;
import org.eclipse.team.ui.sync.CatchupReleaseViewer;
import org.eclipse.team.ui.sync.SyncView;
import org.eclipse.team.ui.sync.TeamFile;

public class MergeEditorInput extends CVSSyncCompareInput {
	IProject project;
	CVSTag start;
	CVSTag end;
	
	public MergeEditorInput(IProject project, CVSTag start, CVSTag end) {
		super(new IResource[] {project});
		this.project = project;
		this.start = start;
		this.end = end;
	}
	public Viewer createDiffViewer(Composite parent) {
		Viewer viewer = super.createDiffViewer(parent);
		getViewer().syncModeChanged(SyncView.SYNC_MERGE);
		return viewer;
	}
	protected IRemoteSyncElement[] createSyncElements(IProgressMonitor monitor) throws TeamException {
		monitor.beginTask(null, 100);
		try {
			IRemoteResource base = CVSWorkspaceRoot.getRemoteTree(project, start, Policy.subMonitorFor(monitor, 50));
			IRemoteResource remote = CVSWorkspaceRoot.getRemoteTree(project, end, Policy.subMonitorFor(monitor, 50));
			return new IRemoteSyncElement[] {new CVSRemoteSyncElement(true /*three way*/, project, base, remote)};
		} finally {
			monitor.done();
		}
	}
	public CVSTag getStartTag() {
		return start;
	}
	public CVSTag getEndTag() {
		return end;
	}
	public String getTitle() {
		return Policy.bind("MergeEditorInput.title", start.getName(), end.getName());
	}
	public boolean isSaveNeeded() {
		return false;
	}
	protected void contentsChanged(ICompareInput source) {
	}
	/*
	 * @see SyncCompareInput#getSyncGranularity()
	 */
	protected int getSyncGranularity() {
		// we have to perform content comparison since files in different branches
		// may have different revisions but the same contents. Consider these files
		// for merge purposes as equal.
		return IRemoteSyncElement.GRANULARITY_CONTENTS;
	}
}