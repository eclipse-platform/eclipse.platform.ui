package org.eclipse.team.internal.ccvs.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.ui.sync.CatchupReleaseViewer;
import org.eclipse.team.ui.sync.MergeResource;
import org.eclipse.team.ui.sync.SyncView;

public class CVSCatchupReleaseViewer extends CatchupReleaseViewer {
	// Actions
	private MergeAction getAction;
	private CommitMergeAction commitAction;

	public CVSCatchupReleaseViewer(Composite parent, CVSSyncCompareInput model) {
		super(parent, model);
		initializeActions(model);
	}
	
	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
		manager.add(new Separator());
		int syncMode = getSyncMode();
		if (syncMode == SyncView.SYNC_OUTGOING || syncMode == SyncView.SYNC_BOTH) {
			commitAction.update();
			manager.add(commitAction);
		}
		if (syncMode == SyncView.SYNC_INCOMING || syncMode == SyncView.SYNC_BOTH) {
			getAction.update();
			manager.add(getAction);
		}
	}
	
	/**
	 * Creates the actions for this viewer.
	 */
	private void initializeActions(final CVSSyncCompareInput diffModel) {
		Shell shell = getControl().getShell();
		commitAction = new CommitMergeAction(diffModel, this, IRemoteSyncElement.OUTGOING, Policy.bind("CVSCatchupReleaseViewer.checkIn"), shell);
		getAction = new GetMergeAction(diffModel, this, IRemoteSyncElement.INCOMING, Policy.bind("CVSCatchupReleaseViewer.get"), shell);
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
				CVSUIPlugin.log(e.getStatus());
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
				CVSUIPlugin.log(e.getStatus());
				config.setRightLabel(Policy.bind("CVSCatchupReleaseViewer.commonFile", name));
			}
		} else {
			config.setAncestorLabel(Policy.bind("CVSCatchupReleaseViewer.noCommonFile"));
		}
	}
}
