package org.eclipse.team.internal.ccvs.ui.merge;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRunnable;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.resources.CVSRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFile;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.sync.CVSSyncCompareInput;
import org.eclipse.team.ui.sync.MergeResource;
import org.eclipse.team.ui.sync.SyncView;
import org.eclipse.team.ui.sync.TeamFile;

public class MergeEditorInput extends CVSSyncCompareInput {
	CVSTag start;
	CVSTag end;
	
	public MergeEditorInput(IResource[] resources, CVSTag start, CVSTag end) {
		super(resources);
		this.start = start;
		this.end = end;
	}
	public Viewer createDiffViewer(Composite parent) {
		Viewer viewer = super.createDiffViewer(parent);
		getViewer().syncModeChanged(SyncView.SYNC_MERGE);
		return viewer;
	}
	protected IRemoteSyncElement[] createSyncElements(IProgressMonitor monitor) throws TeamException {
		IResource[] resources = getResources();
		IRemoteSyncElement[] trees = new IRemoteSyncElement[resources.length];
		int work = 100 * resources.length;
		monitor.beginTask(null, work);
		try {
			for (int i = 0; i < trees.length; i++) {
				IResource resource = resources[i];
				IRemoteResource base = CVSWorkspaceRoot.getRemoteTree(resource, start, Policy.subMonitorFor(monitor, 50));
				IRemoteResource remote = CVSWorkspaceRoot.getRemoteTree(resource, end, Policy.subMonitorFor(monitor, 50));
				trees[i] = new CVSRemoteSyncElement(true /*three way*/, resource, base, remote);				 
			}
		} finally {
			monitor.done();
		}
		return trees;
	}
	public CVSTag getStartTag() {
		return start;
	}
	public CVSTag getEndTag() {
		return end;
	}
	public String getTitle() {
		return Policy.bind("MergeEditorInput.title", start.getName(), end.getName()); //$NON-NLS-1$
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
	
	/**
	 * Wrap the input preparation in a CVS session run so open sessions will be reused
	 */
	public Object prepareInput(IProgressMonitor pm) throws InterruptedException, InvocationTargetException {
		final Object[] result = new Object[] { null };
		final Exception[] exception = new Exception[] {null};
		try {
			Session.run(null, null, false, new ICVSRunnable() {
				public void run(IProgressMonitor monitor) throws CVSException {
					try {
						result[0] = MergeEditorInput.super.prepareInput(monitor);
					} catch (InterruptedException e) {
						exception[0] = e;
					} catch (InvocationTargetException e) {
						exception[0] = e;
					}
				}
			}, pm);
		} catch (CVSException e) {
			throw new InvocationTargetException(e);
		}
		
		if (exception[0] != null) {
			if (exception[0] instanceof InvocationTargetException) {
				throw (InvocationTargetException)exception[0];
			} else {
				throw (InterruptedException)exception[0];
			}
		}
			
		return result[0];
	}
	
	/*
	 * Override collectResourceChanges to only determine the true sync state for incomming changes
	 */
	protected IDiffElement collectResourceChanges(IDiffContainer parent, IRemoteSyncElement tree, IProgressMonitor pm) {
		if ( ! tree.isContainer()) {
			CVSRemoteSyncElement cvsTree = (CVSRemoteSyncElement)tree;
			RemoteFile base = (RemoteFile)cvsTree.getBase();
			RemoteFile remote = (RemoteFile)cvsTree.getRemote();
			if (base != null && remote != null && base.getRevision().equals(remote.getRevision())) {
				// If the base and remote are the same, we don't have an incomming change
				MergeResource mergeResource = new MergeResource(tree);
				TeamFile file = new TeamFile(parent, mergeResource, IRemoteSyncElement.IN_SYNC, getShell());
				return file;
			}
		}
		return super.collectResourceChanges(parent, tree, pm);
	}
}