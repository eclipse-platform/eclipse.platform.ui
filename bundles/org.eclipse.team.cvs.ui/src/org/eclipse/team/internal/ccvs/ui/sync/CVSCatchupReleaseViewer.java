package org.eclipse.team.internal.ccvs.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ILogEntry;
import org.eclipse.team.internal.ccvs.core.client.Command.KSubstOption;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.HistoryView;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.merge.OverrideUpdateMergeAction;
import org.eclipse.team.internal.ccvs.ui.merge.UpdateMergeAction;
import org.eclipse.team.internal.ccvs.ui.merge.UpdateWithForcedJoinAction;
import org.eclipse.team.ui.sync.CatchupReleaseViewer;
import org.eclipse.team.ui.sync.ITeamNode;
import org.eclipse.team.ui.sync.MergeResource;
import org.eclipse.team.ui.sync.SyncView;
import org.eclipse.team.ui.sync.TeamFile;

public class CVSCatchupReleaseViewer extends CatchupReleaseViewer {
	// Actions
	private UpdateSyncAction updateAction;
	private ForceUpdateSyncAction forceUpdateAction;
	private CommitSyncAction commitAction;
	private ForceCommitSyncAction forceCommitAction;
	private UpdateMergeAction updateMergeAction;
	private UpdateWithForcedJoinAction updateWithJoinAction;
	private IgnoreAction ignoreAction;
	private HistoryAction showInHistory;
	private OverrideUpdateMergeAction forceUpdateMergeAction;
	
	class DiffImage extends CompositeImageDescriptor {
		private static final int HEIGHT= 16;
		private static final int WIDTH= 22;
		
		Image baseImage;
		ImageDescriptor overlay;
		
		public DiffImage(Image baseImage, ImageDescriptor overlay) {
			this.baseImage = baseImage;
			this.overlay = overlay;
		}
		
		/*
		 * @see CompositeImageDescriptor#drawCompositeImage(int, int)
		 */
		protected void drawCompositeImage(int width, int height) {
			drawImage(baseImage.getImageData(), 0, 0);
			ImageData overlayData = overlay.getImageData();
			drawImage(overlayData, WIDTH - overlayData.width, (HEIGHT - overlayData.height) / 2);
		}

		/*
		 * @see CompositeImageDescriptor#getSize()
		 */
		protected Point getSize() {
			return new Point(WIDTH, HEIGHT);
		}
	}
	class HistoryAction extends Action implements ISelectionChangedListener {
		IStructuredSelection selection;
		public HistoryAction(String label) {
			super(label);
		}
		public void run() {
			if (selection.isEmpty()) {
				return;
			}
			HistoryView view = HistoryView.openInActivePerspective();
			if (view == null) {
				return;
			}
			ITeamNode node = (ITeamNode)selection.getFirstElement();
			IRemoteSyncElement remoteSyncElement = ((TeamFile)node).getMergeResource().getSyncElement();
			ICVSRemoteFile remoteFile = (ICVSRemoteFile)remoteSyncElement.getRemote();
			IResource local = remoteSyncElement.getLocal();
			ICVSRemoteFile baseFile = (ICVSRemoteFile)remoteSyncElement.getBase();
			
			// can only show history if remote exists or local has a base.
			if(remoteFile!=null) {
				view.showHistory(remoteFile);
			} else if(baseFile!=null) {
				view.showHistory(baseFile);
			}
		}
		public void selectionChanged(SelectionChangedEvent event) {
			ISelection selection = event.getSelection();
			if (!(selection instanceof IStructuredSelection)) {
				setEnabled(false);
				return;
			}
			IStructuredSelection ss = (IStructuredSelection)selection;
			if (ss.size() != 1) {
				setEnabled(false);
				return;
			}
			ITeamNode first = (ITeamNode)ss.getFirstElement();
			if (first instanceof TeamFile) {
				// can only show history on elements that have a remote file
				this.selection = ss;
				IRemoteSyncElement remoteSyncElement = ((TeamFile)first).getMergeResource().getSyncElement();
				if(remoteSyncElement.getRemote() != null || remoteSyncElement.getBase() != null) {
					setEnabled(true);
				} else {
					setEnabled(false);
				}
			} else {
				this.selection = null;
				setEnabled(false);
			}
		}
	}
	
	public CVSCatchupReleaseViewer(Composite parent, CVSSyncCompareInput model) {
		super(parent, model);
		initializeActions(model);
		initializeLabelProvider();
	}
	
	private void initializeLabelProvider() {
		final ImageDescriptor conflictDescriptor = CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_MERGEABLE_CONFLICT);
		final ImageDescriptor questionableDescriptor = CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_QUESTIONABLE);
		final LabelProvider oldProvider = (LabelProvider)getLabelProvider();
		setLabelProvider(new LabelProvider() {
			public Image getImage(Object element) {
				Image image = oldProvider.getImage(element);
				if (element instanceof ITeamNode) {
					ITeamNode node = (ITeamNode)element;
					int kind = node.getKind();
					if ((kind & IRemoteSyncElement.AUTOMERGE_CONFLICT) != 0) {
						DiffImage diffImage = new DiffImage(image, conflictDescriptor);
						return diffImage.createImage();
					}
					if (kind == (IRemoteSyncElement.OUTGOING | IRemoteSyncElement.ADDITION)) {
						IResource resource = node.getResource();
						if (resource.getType() == IResource.FILE) {
							try {
								ICVSFile cvsFile = CVSWorkspaceRoot.getCVSFileFor((IFile) resource);
								if (cvsFile.getSyncInfo() == null) {
									DiffImage diffImage = new DiffImage(image, questionableDescriptor);
									return diffImage.createImage();
								}
							} catch (TeamException e) {
								ErrorDialog.openError(getControl().getShell(), null, null, e.getStatus());
								// Fall through and return the default image
							}
						}
					}
				}
				return image;
			}
			public String getText(Object element) {
				if (element instanceof ITeamNode) {					
					ITeamNode node = (ITeamNode)element;
					IResource resource = node.getResource();
					if (resource.exists()) {
						try {
							if (resource.getType() == IResource.FILE) {
								ICVSFile cvsFile = CVSWorkspaceRoot.getCVSFileFor((IFile)resource);
								ResourceSyncInfo info = cvsFile.getSyncInfo();
								KSubstOption option = info != null && info.getKeywordMode() != null ?
									info.getKeywordMode() :
									KSubstOption.fromFile((IFile)resource);
								return Policy.bind("CVSCatchupReleaseViewer.fileDecoration", oldProvider.getText(element), option.getShortDisplayText()); //$NON-NLS-1$
							} else if (resource instanceof IContainer) {
								ICVSFolder cvsFolder = CVSWorkspaceRoot.getCVSFolderFor((IContainer)resource);
								FolderSyncInfo info = cvsFolder.getFolderSyncInfo();
								if (info != null) {
									CVSTag tag = info.getTag();
									if (tag != null) {
										return Policy.bind("CVSCatchupReleaseViewer.folderDecoration", oldProvider.getText(element), tag.getName()); //$NON-NLS-1$
									}
								}
							}
							
						} catch (CVSException e) {
							ErrorDialog.openError(getControl().getShell(), null, null, e.getStatus());
						}
					}
				}								
				return oldProvider.getText(element);
			}
		});
	}
	
	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
		if (showInHistory != null) {
			manager.add(showInHistory);
		}
		manager.add(new Separator());
		switch (getSyncMode()) {
			case SyncView.SYNC_INCOMING:
				updateAction.update(SyncView.SYNC_INCOMING);
				manager.add(updateAction);
				forceUpdateAction.update(SyncView.SYNC_INCOMING);
				manager.add(forceUpdateAction);
				break;
			case SyncView.SYNC_OUTGOING:
				commitAction.update(SyncView.SYNC_OUTGOING);
				manager.add(commitAction);
				forceCommitAction.update(SyncView.SYNC_OUTGOING);
				manager.add(forceCommitAction);
				ignoreAction.update();
				manager.add(ignoreAction);
				break;
			case SyncView.SYNC_BOTH:
				commitAction.update(SyncView.SYNC_BOTH);
				manager.add(commitAction);
				updateAction.update(SyncView.SYNC_BOTH);
				manager.add(updateAction);
				manager.add(new Separator());
				forceCommitAction.update(SyncView.SYNC_BOTH);
				manager.add(forceCommitAction);
				forceUpdateAction.update(SyncView.SYNC_BOTH);
				manager.add(forceUpdateAction);				
				break;
			case SyncView.SYNC_MERGE:
				updateMergeAction.update(SyncView.SYNC_INCOMING);
				forceUpdateMergeAction.update(SyncView.SYNC_INCOMING);
				updateWithJoinAction.update(SyncView.SYNC_INCOMING);
				manager.add(updateMergeAction);
				manager.add(forceUpdateMergeAction);
				manager.add(updateWithJoinAction);
				break;
		}
	}
	
	/**
	 * Creates the actions for this viewer.
	 */
	private void initializeActions(final CVSSyncCompareInput diffModel) {
		Shell shell = getControl().getShell();
		commitAction = new CommitSyncAction(diffModel, this, Policy.bind("CVSCatchupReleaseViewer.commit"), shell); //$NON-NLS-1$
		forceCommitAction = new ForceCommitSyncAction(diffModel, this, Policy.bind("CVSCatchupReleaseViewer.forceCommit"), shell); //$NON-NLS-1$
		updateAction = new UpdateSyncAction(diffModel, this, Policy.bind("CVSCatchupReleaseViewer.update"), shell); //$NON-NLS-1$
		forceUpdateAction = new ForceUpdateSyncAction(diffModel, this, Policy.bind("CVSCatchupReleaseViewer.forceUpdate"), shell); //$NON-NLS-1$
		updateMergeAction = new UpdateMergeAction(diffModel, this, Policy.bind("CVSCatchupReleaseViewer.update"), shell); //$NON-NLS-1$
		ignoreAction = new IgnoreAction(diffModel, this, Policy.bind("CVSCatchupReleaseViewer.ignore"), shell); //$NON-NLS-1$
		updateWithJoinAction = new UpdateWithForcedJoinAction(diffModel, this, Policy.bind("CVSCatchupReleaseViewer.mergeUpdate"), shell); //$NON-NLS-1$
		forceUpdateMergeAction = new OverrideUpdateMergeAction(diffModel, this, Policy.bind("CVSCatchupReleaseViewer.forceUpdate"), shell); //$NON-NLS-1$
		
		// Show in history view
		showInHistory = new HistoryAction(Policy.bind("CVSCatchupReleaseViewer.showInHistory")); //$NON-NLS-1$
		addSelectionChangedListener(showInHistory);	
	}
	
	/**
	 * Provide CVS-specific labels for the editors.
	 */
	protected void updateLabels(MergeResource resource) {
		CompareConfiguration config = getCompareConfiguration();
		String name = resource.getName();
		config.setLeftLabel(Policy.bind("CVSCatchupReleaseViewer.workspaceFile", name)); //$NON-NLS-1$
	
		IRemoteSyncElement syncTree = resource.getSyncElement();
		IRemoteResource remote = syncTree.getRemote();
		if (remote != null) {
			try {
				ICVSRemoteFile remoteFile = (ICVSRemoteFile)remote;
				String revision = remoteFile.getRevision();
				// XXX Should have real progress
				ILogEntry logEntry = remoteFile.getLogEntry(new NullProgressMonitor());
				String author = logEntry.getAuthor();
				config.setRightLabel(Policy.bind("CVSCatchupReleaseViewer.repositoryFileRevision", new Object[] {name, revision, author})); //$NON-NLS-1$
			} catch (TeamException e) {
				ErrorDialog.openError(getControl().getShell(), null, null, e.getStatus());
				config.setRightLabel(Policy.bind("CVSCatchupReleaseViewer.repositoryFile", name)); //$NON-NLS-1$
			}
		} else {
			config.setRightLabel(Policy.bind("CVSCatchupReleaseViewer.noRepositoryFile")); //$NON-NLS-1$
		}
	
		IRemoteResource base = syncTree.getBase();
		if (base != null) {
			try {
				String revision = ((ICVSRemoteFile)base).getRevision();
				config.setAncestorLabel(Policy.bind("CVSCatchupReleaseViewer.commonFileRevision", new Object[] {name, revision} )); //$NON-NLS-1$
			} catch (TeamException e) {
				ErrorDialog.openError(getControl().getShell(), null, null, e.getStatus());
				config.setRightLabel(Policy.bind("CVSCatchupReleaseViewer.commonFile", name)); //$NON-NLS-1$
			}
		} else {
			config.setAncestorLabel(Policy.bind("CVSCatchupReleaseViewer.noCommonFile")); //$NON-NLS-1$
		}
	}
}
