package org.eclipse.team.internal.ccvs.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.team.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.ccvs.core.ILogEntry;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.resources.CVSRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.ui.sync.CatchupReleaseViewer;
import org.eclipse.team.ui.sync.ChangedTeamContainer;
import org.eclipse.team.ui.sync.ITeamNode;
import org.eclipse.team.ui.sync.SyncCompareInput;
import org.eclipse.team.ui.sync.TeamFile;

public class CVSSyncCompareInput extends SyncCompareInput {
	boolean dirty = false;
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
		catchupReleaseViewer.getTree().addMouseMoveListener(new MouseMoveListener() {
			/**
			 * @see MouseMoveListener#mouseMove(MouseEvent)
			 */
			public void mouseMove(MouseEvent e) {
				Tree tree = (Tree)e.widget;
				TreeItem item = tree.getItem(new Point(e.x, e.y));
				if (item != null) {
					// Hack: this is the only way to get an item from the tree viewer
					Object o = item.getData();
					if (o instanceof TeamFile) {
						TeamFile file = (TeamFile)o;
						if (file.getChangeDirection() != ITeamNode.OUTGOING) {
							IRemoteSyncElement element = file.getMergeResource().getSyncElement();
							ICVSRemoteFile remoteFile = (ICVSRemoteFile)element.getRemote();
							ILogEntry logEntry;
							if (remoteFile != null) {
								try {
									// XXX Should have real progress here
									logEntry = remoteFile.getLogEntry(new NullProgressMonitor());
								} catch (TeamException ex) {
									tree.setToolTipText(null);
									return;
								}
								if (logEntry != null) {
									String newText = logEntry.getComment();
									String oldText = tree.getToolTipText();
									if (!newText.equals(oldText)) {
										tree.setToolTipText(logEntry.getComment());
									}
									return;
								}
							}
						}
					}
				}
				tree.setToolTipText(null);
			}
		});
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
	
	/*
	 * Helper method to get cvs elements from the selection in the sync editoru input
	 */
	public static CVSRemoteSyncElement getSyncElementFrom(Object node) {
		CVSRemoteSyncElement element = null;
		if (node instanceof TeamFile) {
			element = (CVSRemoteSyncElement)((TeamFile)node).getMergeResource().getSyncElement();
		} else if (node instanceof ChangedTeamContainer) {
			element = (CVSRemoteSyncElement)((ChangedTeamContainer)node).getMergeResource().getSyncElement();
		}
		return element;
	}
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
		super.setDirty(dirty);
	}
	public boolean isDirty() {
		return dirty;
	}

}
