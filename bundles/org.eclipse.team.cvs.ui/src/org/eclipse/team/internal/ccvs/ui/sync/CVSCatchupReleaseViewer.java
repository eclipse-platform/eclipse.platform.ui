package org.eclipse.team.internal.ccvs.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.resources.LocalFile;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.merge.UpdateMergeAction;
import org.eclipse.team.ui.sync.CatchupReleaseViewer;
import org.eclipse.team.ui.sync.ITeamNode;
import org.eclipse.team.ui.sync.MergeResource;
import org.eclipse.team.ui.sync.SyncView;

public class CVSCatchupReleaseViewer extends CatchupReleaseViewer {
	// Actions
	private UpdateSyncAction updateAction;
	private CommitSyncAction commitAction;
	private UpdateMergeAction updateMergeAction;
	
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
								if (new LocalFile(((IFile)resource).getLocation().toFile()).getSyncInfo() == null) {
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
				return oldProvider.getText(element);
			}
		});
	}
	
	protected void fillContextMenu(IMenuManager manager) {
		switch (getSyncMode()) {
			case SyncView.SYNC_INCOMING:
				updateAction.update(SyncView.SYNC_INCOMING);
				manager.add(updateAction);
				break;
			case SyncView.SYNC_OUTGOING:
				commitAction.update(SyncView.SYNC_OUTGOING);
				manager.add(commitAction);
				updateAction.update(SyncView.SYNC_OUTGOING);
				manager.add(updateAction);
				break;
			case SyncView.SYNC_BOTH:
				commitAction.update(SyncView.SYNC_BOTH);
				manager.add(commitAction);
				updateAction.update(SyncView.SYNC_BOTH);
				manager.add(updateAction);
				break;
			case SyncView.SYNC_MERGE:
				updateMergeAction.update(SyncView.SYNC_INCOMING);
				manager.add(updateMergeAction);
				break;
		}
		manager.add(new Separator());
		super.fillContextMenu(manager);
	}
	
	/**
	 * Creates the actions for this viewer.
	 */
	private void initializeActions(final CVSSyncCompareInput diffModel) {
		Shell shell = getControl().getShell();
		commitAction = new CommitSyncAction(diffModel, this, Policy.bind("CVSCatchupReleaseViewer.commit"), shell);
		updateAction = new UpdateSyncAction(diffModel, this, Policy.bind("CVSCatchupReleaseViewer.update"), shell);
		updateMergeAction = new UpdateMergeAction(diffModel, this, Policy.bind("CVSCatchupReleaseViewer.update"), shell);
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
