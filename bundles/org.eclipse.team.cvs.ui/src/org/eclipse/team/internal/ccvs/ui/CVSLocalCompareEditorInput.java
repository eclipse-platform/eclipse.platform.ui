package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.resources.CVSRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.ui.sync.CVSSyncCompareInput;
import org.eclipse.team.internal.ui.sync.SyncView;

public class CVSLocalCompareEditorInput extends CVSSyncCompareInput {
	CVSTag[] tags;
	
	public CVSLocalCompareEditorInput(IResource[] resources, CVSTag[] tags) {
		super(resources);
		Assert.isTrue(resources.length == tags.length);
		this.tags = tags;
	}
	
	public CVSLocalCompareEditorInput(IResource[] resources, CVSTag tag) {
		super(resources);
		Assert.isTrue(tag != null);
		this.tags = new CVSTag[] {tag};
	}
	
	public Viewer createDiffViewer(Composite parent) {
		Viewer viewer = super.createDiffViewer(parent);
		getViewer().syncModeChanged(SyncView.SYNC_COMPARE);
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
				CVSTag tag;			
				if(tags.length != resources.length) {
					tag = tags[0];
				} else {
					tag = tags[i];
				}
				IRemoteResource remote = CVSWorkspaceRoot.getRemoteTree(resource, tag, Policy.subMonitorFor(monitor, 50));
				trees[i] = new CVSRemoteSyncElement(false /* two-way */, resource, null, remote);				 
			}
		} finally {
			monitor.done();
		}
		//getViewer().resetFilters();
		return trees;
	}
	
	public String getTitle() {
		return Policy.bind("CVSLocalCompareEditorInput.title", tags[0].getName()); //$NON-NLS-1$
	}
	
	public boolean isSaveNeeded() {
		return false;
	}
	
	protected void contentsChanged(ICompareInput source) {
	}
}