package org.eclipse.team.internal.ccvs.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.ILocalSyncElement;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSRunnable;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.resources.CVSRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.AvoidableMessageDialog;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.ui.sync.CatchupReleaseViewer;
import org.eclipse.team.ui.sync.ChangedTeamContainer;
import org.eclipse.team.ui.sync.SyncCompareInput;
import org.eclipse.team.ui.sync.TeamFile;

public class CVSSyncCompareInput extends SyncCompareInput {
	private IResource[] resources;
	private TeamFile previousTeamFile = null;	
	
	/**
	 * Creates a new catchup or release operation.
	 */
	public CVSSyncCompareInput(IResource[] resources) {
		super(CVSUIPlugin.getPlugin().getPreferenceStore().getBoolean(ICVSUIConstants.PREF_CONSIDER_CONTENTS) ? ILocalSyncElement.GRANULARITY_CONTENTS : ILocalSyncElement.GRANULARITY_TIMESTAMP);
		this.resources = resources;
	}
	
	protected CVSSyncCompareInput(IResource[] resources, int granularity) {
		super(granularity);
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
//		catchupReleaseViewer.getTree().addMouseMoveListener(new MouseMoveListener() {
//			/**
//			 * @see MouseMoveListener#mouseMove(MouseEvent)
//			 */
//			public void mouseMove(MouseEvent e) {
//				final Tree tree = (Tree)e.widget;
//				TreeItem item = tree.getItem(new Point(e.x, e.y));
//				final TeamFile file;
//				if (item != null) {
//					// Hack: this is the only way to get an item from the tree viewer
//					Object o = item.getData();
//					if (o instanceof TeamFile) {
//						file = (TeamFile)o;
//					} else file = null;
//				} else file = null;
//
//				// avoid redundant updates -- identity test is good enough here
// 				if (file == previousTeamFile) return;
//				previousTeamFile = file;
//				getShell().getDisplay().asyncExec(new Runnable() {
//					public void run() {
//						updateToolTip(tree, file);
//					}
//				});
//			}
//		});
		return catchupReleaseViewer;
	}
	
//	protected void updateToolTip(Tree tree, TeamFile file) {
//		String newText = null;
//		if (file != null && file.getChangeDirection() != ITeamNode.OUTGOING) {
//			IRemoteSyncElement element = file.getMergeResource().getSyncElement();
//			final ICVSRemoteFile remoteFile = (ICVSRemoteFile)element.getRemote();
//			final ILogEntry[] logEntry = new ILogEntry[1];
//			if (remoteFile != null) {
//				try {
//					CVSUIPlugin.runWithProgress(getViewer().getTree().getShell(), true /*cancelable*/,
//						new IRunnableWithProgress() {
//						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
//							try {
//								logEntry[0] = remoteFile.getLogEntry(monitor);
//							} catch (TeamException ex) {
//								throw new InvocationTargetException(ex);
//							}
//						}
//					});
//				} catch (InterruptedException ex) {
//					// ignore cancellation
//				} catch (InvocationTargetException ex) {
//					// ignore the exception
//				}
//			}
//			if (logEntry[0] != null) {
//				newText = logEntry[0].getComment();
//			}
//		}
//		if (tree.isDisposed()) return;
//		String oldText = tree.getToolTipText();
//		if (newText == oldText || newText != null && newText.equals(oldText)) return;
//		tree.setToolTipText(newText);
//	}
	
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
		updateView();
		
		// prompt user with warning
		Shell shell = getShell();
		if(shell != null) {
			promptForConfirmMerge(getShell());
		}
	}
	
	/*
	 * Helper method to get cvs elements from the selection in the sync editor input
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
	
	/*
	 * Returns the resources in this input.
	 */
	public IResource[] getResources() {
		return resources;
	}
	
	/*
	 * Inform user that when changes are merged in the sync view that confirm
	 * merge should be called to finish the merge.
	 */
	private void promptForConfirmMerge(final Shell shell) {
		final IPreferenceStore store = CVSUIPlugin.getPlugin().getPreferenceStore();
		if(!store.getBoolean(ICVSUIConstants.PREF_PROMPT_ON_SAVING_IN_SYNC)) {
			return;
		};

		shell.getDisplay().syncExec(new Runnable() {
			public void run() {							
				AvoidableMessageDialog dialog = new AvoidableMessageDialog(
						shell,
						Policy.bind("CVSSyncCompareInput.confirmMergeTitle"),  //$NON-NLS-1$
						null,	// accept the default window icon
						Policy.bind("CVSSyncCompareInput.confirmMergeMessage"),  //$NON-NLS-1$
						MessageDialog.INFORMATION, 
						new String[] {IDialogConstants.OK_LABEL}, 
						0);
				dialog.open();		
				if(dialog.isDontShowAgain()) {
					store.setValue(ICVSUIConstants.PREF_PROMPT_ON_SAVING_IN_SYNC, false);
				}																				
			}
		});
	}
	
	/**
	 * Wrap the input preparation in a CVS session run so open sessions will be reused and
	 * file contents under the same remote root folder will be fetched using the same connection.
	 */
	public Object prepareInput(IProgressMonitor pm) throws InterruptedException, InvocationTargetException {
		final Object[] result = new Object[] { null };
		final Exception[] exception = new Exception[] {null};
		try {
			Session.run(null, null, false, new ICVSRunnable() {
				public void run(IProgressMonitor monitor) throws CVSException {
					try {
						result[0] = CVSSyncCompareInput.super.prepareInput(monitor);
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
}
