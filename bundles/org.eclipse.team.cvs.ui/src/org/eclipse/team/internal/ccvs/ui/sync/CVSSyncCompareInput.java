package org.eclipse.team.internal.ccvs.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.ILocalSyncElement;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRunnable;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.resources.CVSRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.AvoidableMessageDialog;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ui.sync.CatchupReleaseViewer;
import org.eclipse.team.internal.ui.sync.ChangedTeamContainer;
import org.eclipse.team.internal.ui.sync.ITeamNode;
import org.eclipse.team.internal.ui.sync.SyncCompareInput;
import org.eclipse.team.internal.ui.sync.TeamFile;

public class CVSSyncCompareInput extends SyncCompareInput {
	private IResource[] resources;
	private boolean onlyOutgoing = false;
	
	public CVSSyncCompareInput(IResource[] resources) {
		this(resources, false);
	}
	
	protected CVSSyncCompareInput(IResource[] resources, int granularity) {
		super(granularity);
		this.resources = resources;
	}
	
	public CVSSyncCompareInput(IResource[] resources, boolean onlyOutgoing) {
		super(CVSUIPlugin.getPlugin().getPreferenceStore().getBoolean(ICVSUIConstants.PREF_CONSIDER_CONTENTS) ? ILocalSyncElement.GRANULARITY_CONTENTS : ILocalSyncElement.GRANULARITY_TIMESTAMP);
		this.onlyOutgoing = onlyOutgoing;
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
		if (onlyOutgoing) {
			// Find the outgoing changes in each selected resource
			final List outgoing = new ArrayList();
			try {
				for (int i = 0; i < resources.length; i++) {
					resources[i].accept(new IResourceVisitor() {
						public boolean visit(IResource resource) throws CoreException {
							// if resource is a file and is dirty, add it to the list
							if (resource.getType() == IResource.FILE) {
								if (isDirty((IFile)resource)) {
									outgoing.add(resource);
								}
							} else {
								try {
									ICVSFolder folder = CVSWorkspaceRoot.getCVSFolderFor((IContainer)resource);
									if (folder.isCVSFolder()) {
										return true;
									} else {
										outgoing.add(resource);
										return false;
									}
								} catch (CVSException e) {
									CVSUIPlugin.log(e.getStatus());
									return false;
								}
							}	
							return true;
						}
					});
				}
			} catch (CoreException e) {
				ErrorDialog.openError(getShell(), Policy.bind("simpleInternal"), Policy.bind("internal"), e.getStatus()); //$NON-NLS-1$ //$NON-NLS-2$
				CVSUIPlugin.log(e.getStatus());
				return new IRemoteSyncElement[0];
			}
			
			final TeamException[] exception = new TeamException[1];
			final Map providerMapping = getProviderMapping((IResource[])outgoing.toArray(new IResource[outgoing.size()]));
			final IRemoteSyncElement[] trees = new IRemoteSyncElement[providerMapping.size()];
			Session.run(null, null, true, new ICVSRunnable() {
				public void run(IProgressMonitor monitor) throws CVSException {
					int i = 0;
					int work = 1000 * trees.length;
					monitor.beginTask(null, work);
					try {
						for (Iterator iter = providerMapping.keySet().iterator(); iter.hasNext();) {
							RepositoryProvider provider = (RepositoryProvider)iter.next();
							List resourceList = (List)providerMapping.get(provider);
							final TeamException[] exception = new TeamException[1];
							trees[i++] = CVSWorkspaceRoot.getRemoteSyncTree(
								provider.getProject(), 
								(IResource[]) resourceList.toArray(new IResource[resourceList.size()]),
								null /* tag */,
								Policy.subMonitorFor(monitor, 1000));
						}
					} catch (TeamException e) {
						exception[0] = e;
					} finally {
						monitor.done();
					}
				}
			}, monitor);
			if (exception[0] != null) throw exception[0];
			return trees;
		} else {
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
			// prompt
			if(source instanceof TeamFile) {
				TeamFile file = (TeamFile)source;						
				int direction = file.getChangeDirection();
				int type = file.getChangeType();
				if(direction == IRemoteSyncElement.INCOMING ||
				   direction == IRemoteSyncElement.CONFLICTING) {
					promptForConfirmMerge(getShell());
			    }
			}
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
						Policy.bind("CVSSyncCompareInput.confirmMergeMessageTitle"),  //$NON-NLS-1$
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
	 * 
	 * Also run with refresh prompting if one of the resources is out of sync with the local
	 * file system.
	 */
	public Object prepareInput(IProgressMonitor pm) throws InterruptedException, InvocationTargetException {
		final Object[] result = new Object[] { null };
		final Exception[] exception = new Exception[] {null};
		try {
			Session.run(null, null, false, new ICVSRunnable() {
				public void run(IProgressMonitor monitor) throws CVSException {
					try {
						CVSUIPlugin.runWithRefresh(getShell(), resources, new IRunnableWithProgress() {
							public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
								result[0] = CVSSyncCompareInput.super.prepareInput(monitor);
							}
						}, monitor);
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
	
	/**
	 * Adjust the sync info (to conflicting change) for locally deleted 
	 * folders (i.e. outgoing folder deletions)
	 * that have incoming or conflicting changes in one or more children.
	 * 
	 * @see MergeAction#removeNodes(ITeamNode[])
	 */
	protected IDiffElement collectResourceChanges(IDiffContainer parent, IRemoteSyncElement tree, IProgressMonitor pm) {
		IDiffElement element = super.collectResourceChanges(parent, tree, pm);
		int kind = element.getKind();
		if ((element instanceof ChangedTeamContainer) 
				&& ((kind & Differencer.CHANGE_TYPE_MASK) == Differencer.DELETION) 
				&& ((kind & Differencer.DIRECTION_MASK) == ITeamNode.OUTGOING)) {
			// Check the children to see if there are any incomming changes
			if (hasIncomingChanges((ChangedTeamContainer)element)) {
				((ChangedTeamContainer)element).setKind(ITeamNode.CONFLICTING | Differencer.CHANGE);
			}
		}
		return element;
	}
	
	private boolean hasIncomingChanges(ChangedTeamContainer container) {
		IDiffElement[] children = container.getChildren();
		for (int i = 0; i < children.length; i++) {
			IDiffElement element = children[i];
			int direction = element.getKind() & Differencer.DIRECTION_MASK;
			if (direction == ITeamNode.CONFLICTING || direction == ITeamNode.INCOMING) {
				return true;
			}
			if (element instanceof ChangedTeamContainer)  {
				boolean hasIncomingChanges = hasIncomingChanges((ChangedTeamContainer)element);
				if (hasIncomingChanges) return true;
			}
		}
		return false;
	}
	
	/*
	 * Copied from CVSDecorator
	 */
	private static boolean isDirty(ICVSFile cvsFile) {
		try {
			// file is dirty or file has been merged by an update
			if(!cvsFile.isIgnored()) {
				return cvsFile.isModified();
			} else {
				return false;
			} 
		} catch (CVSException e) {
			//if we get an error report it to the log but assume dirty
			CVSUIPlugin.log(e.getStatus());
			return true;
		}
	}

	/*
	 * Copied from CVSDecorator
	 */
	private static boolean isDirty(IFile file) {
		return isDirty(CVSWorkspaceRoot.getCVSFileFor(file));
	}
	
	/*
	 * Method copied from TeamAction. It should be put in a common place
	 */
	protected Map getProviderMapping(IResource[] resources) {
		Map result = new HashMap();
		for (int i = 0; i < resources.length; i++) {
			RepositoryProvider provider = RepositoryProvider.getProvider(resources[i].getProject());
			List list = (List)result.get(provider);
			if (list == null) {
				list = new ArrayList();
				result.put(provider, list);
			}
			list.add(resources[i]);
		}
		return result;
	}
}
