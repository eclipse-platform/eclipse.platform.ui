package org.eclipse.team.internal.ui.target;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ui.sync.CatchupReleaseViewer;
import org.eclipse.team.internal.ui.sync.SyncCompareInput;
import org.eclipse.team.internal.ui.sync.SyncView;

public class TargetCatchupReleaseViewer extends CatchupReleaseViewer {
	private GetSyncAction getAction;
	private PutSyncAction putAction;
	
	public TargetCatchupReleaseViewer(Composite parent, TargetSyncCompareInput input) {
		super(parent, input);
		initializeActions(input);
	}
	
	/**
	 * Creates the actions for this viewer.
	 */
	private void initializeActions(final TargetSyncCompareInput diffModel) {
		Shell shell = getControl().getShell();
		getAction = new GetSyncAction(diffModel, this, "Get", shell);
		putAction = new PutSyncAction(diffModel, this, "Put", shell);
	}
	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
		manager.add(new Separator());
		switch (getSyncMode()) {
			case SyncView.SYNC_INCOMING:
				getAction.update(SyncView.SYNC_INCOMING);
				manager.add(getAction);
				break;
			case SyncView.SYNC_OUTGOING:
				putAction.update(SyncView.SYNC_INCOMING);
				manager.add(putAction);
				break;
			case SyncView.SYNC_BOTH:
				getAction.update(SyncView.SYNC_INCOMING);
				manager.add(getAction);
				putAction.update(SyncView.SYNC_INCOMING);
				manager.add(putAction);
				break;
		}
	}
}
