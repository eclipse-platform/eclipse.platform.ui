package org.eclipse.team.internal.ccvs.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
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
import org.eclipse.team.ccvs.core.ICVSFile;
import org.eclipse.team.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.ui.CVSDecorator;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.HistoryView;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;
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
	private CommitSyncAction commitAction;
	private UpdateMergeAction updateMergeAction;
	private UpdateWithForcedJoinAction updateWithJoinAction;
	private IgnoreAction ignoreAction;
	private HistoryAction showInHistory;
	
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
			if (selection.isEmpty()) return;
			HistoryView view = HistoryView.openInActivePerspective();
			if (view == null) return;
			ITeamNode node = (ITeamNode)selection.getFirstElement();
			IRemoteSyncElement remoteSyncElement = ((TeamFile)node).getMergeResource().getSyncElement();
			IResource resource = remoteSyncElement.getLocal();
			if (resource.exists()) {
				view.showHistory(resource);
			} else {
				ICVSRemoteFile remoteFile = (ICVSRemoteFile)remoteSyncElement.getRemote();
				view.showHistory(remoteFile);
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
				this.selection = ss;
				setEnabled(true);
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
				StringBuffer postfix = new StringBuffer();
				if (element instanceof ITeamNode) {					
					ITeamNode node = (ITeamNode)element;
					IResource resource = node.getResource();
					if (resource.exists() && resource.getType() == IResource.FILE) {
						try {
							ICVSFile cvsFile = CVSWorkspaceRoot.getCVSFileFor((IFile) resource);
							ResourceSyncInfo info = cvsFile.getSyncInfo();
							String kw;
							if (info!=null) {
								kw = CVSDecorator.getFileTypeString(resource.getName(), info.getKeywordMode());
							} else {
								kw = CVSDecorator.getFileTypeString(resource.getName(), null);
							}
							postfix.append("(" + kw + ")");
						} catch(CVSException e) {
							ErrorDialog.openError(getControl().getShell(), null, null, e.getStatus());
						}
					}
				}								
				return oldProvider.getText(element) + " " + postfix.toString() ;
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
				break;
			case SyncView.SYNC_OUTGOING:
				commitAction.update(SyncView.SYNC_OUTGOING);
				manager.add(commitAction);
				ignoreAction.update();
				manager.add(ignoreAction);
				manager.add(new Separator());
				updateAction.update(SyncView.SYNC_OUTGOING);
				manager.add(updateAction);
				break;
			case SyncView.SYNC_BOTH:
				commitAction.update(SyncView.SYNC_BOTH);
				manager.add(commitAction);
				manager.add(new Separator());
				updateAction.update(SyncView.SYNC_BOTH);
				manager.add(updateAction);
				break;
			case SyncView.SYNC_MERGE:
				updateMergeAction.update(SyncView.SYNC_INCOMING);
				updateWithJoinAction.update(SyncView.SYNC_INCOMING);
				manager.add(updateMergeAction);
				manager.add(updateWithJoinAction);
				break;
		}
	}
	
	/**
	 * Creates the actions for this viewer.
	 */
	private void initializeActions(final CVSSyncCompareInput diffModel) {
		Shell shell = getControl().getShell();
		commitAction = new CommitSyncAction(diffModel, this, Policy.bind("CVSCatchupReleaseViewer.commit"), shell);
		updateAction = new UpdateSyncAction(diffModel, this, Policy.bind("CVSCatchupReleaseViewer.update"), shell);
		updateMergeAction = new UpdateMergeAction(diffModel, this, Policy.bind("CVSCatchupReleaseViewer.update"), shell);
		ignoreAction = new IgnoreAction(diffModel, this, Policy.bind("CVSCatchupReleaseViewer.ignore"), shell);
		updateWithJoinAction = new UpdateWithForcedJoinAction(diffModel, this, Policy.bind("CVSCatchupReleaseViewer.mergeUpdate"), shell);
		
		// Show in history view
		showInHistory = new HistoryAction(Policy.bind("CVSCatchupReleaseViewer.showInHistory"));
		addSelectionChangedListener(showInHistory);	
	}
	
	/**
	 * Provide CVS-specific labels for the editors.
	 */
	protected void updateLabels(MergeResource resource) {
		CompareConfiguration config = getCompareConfiguration();
		String name = resource.getName();
		config.setLeftLabel(Policy.bind("CVSCatchupReleaseViewer.workspaceFile", name));
	
		IRemoteSyncElement syncTree = resource.getSyncElement();
		IRemoteResource remote = syncTree.getRemote();
		if (remote != null) {
			try {
				String revision = ((ICVSRemoteFile)remote).getRevision();
				config.setRightLabel(Policy.bind("CVSCatchupReleaseViewer.repositoryFileRevision", new Object[] {name, revision}));
			} catch (TeamException e) {
				ErrorDialog.openError(getControl().getShell(), null, null, e.getStatus());
				config.setRightLabel(Policy.bind("CVSCatchupReleaseViewer.repositoryFile", name));
			}
		} else {
			config.setRightLabel(Policy.bind("CVSCatchupReleaseViewer.noRepositoryFile"));
		}
	
		IRemoteResource base = syncTree.getBase();
		if (base != null) {
			try {
				String revision = ((ICVSRemoteFile)base).getRevision();
				config.setAncestorLabel(Policy.bind("CVSCatchupReleaseViewer.commonFileRevision", new Object[] {name, revision} ));
			} catch (TeamException e) {
				ErrorDialog.openError(getControl().getShell(), null, null, e.getStatus());
				config.setRightLabel(Policy.bind("CVSCatchupReleaseViewer.commonFile", name));
			}
		} else {
			config.setAncestorLabel(Policy.bind("CVSCatchupReleaseViewer.noCommonFile"));
		}
	}
}
