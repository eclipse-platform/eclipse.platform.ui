package org.eclipse.team.internal.ccvs.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.ui.sync.CatchupReleaseViewer;
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
		int syncMode = getSyncMode();
		if (syncMode == SyncView.SYNC_OUTGOING) {
			manager.add(new Separator());
			commitAction.update();
//			delRemoteAction.update();
			manager.add(commitAction);
//			manager.add(delRemoteAction);
		}
		if (syncMode == SyncView.SYNC_INCOMING) {
			manager.add(new Separator());
			getAction.update();
//			delLocalAction.update();
			manager.add(getAction);
//			manager.add(delLocalAction);
		}
	}
	
	/**
	 * Creates the actions for this viewer.
	 */
	private void initializeActions(final CVSSyncCompareInput diffModel) {
		Shell shell = getControl().getShell();
		commitAction = new CommitMergeAction(diffModel, this, IRemoteSyncElement.OUTGOING, Policy.bind("CVSCatchupReleaseViewer.checkIn"), shell);
		getAction = new GetMergeAction(diffModel, this, IRemoteSyncElement.INCOMING, Policy.bind("CVSCatchupReleaseViewer.get"), shell);
/*		delRemoteAction = new MergeAction(diffModel, this, MergeAction.DELETE_REMOTE, IRemoteSyncElement.OUTGOING, "Delete Remote") {
			protected boolean isMatchingKind(int kind) {
				return kind == (IRemoteSyncElement.OUTGOING | IRemoteSyncElement.DELETION);
			}
	 	};
		delLocalAction = new MergeAction(diffModel, this, MergeAction.DELETE_LOCAL, IRemoteSyncElement.INCOMING, "Delete Local") {
			protected boolean isMatchingKind(int kind) {
				return kind == (IRemoteSyncElement.INCOMING | IRemoteSyncElement.DELETION);
			}
	 	};*/
	}
}
