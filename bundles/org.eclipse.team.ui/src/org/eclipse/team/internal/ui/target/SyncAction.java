package org.eclipse.team.internal.ui.target;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.core.target.TargetManager;
import org.eclipse.team.core.target.TargetProvider;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.team.internal.ui.sync.SyncCompareInput;
import org.eclipse.team.internal.ui.sync.SyncView;
import org.eclipse.ui.PartInitException;

/**
 * Action for catchup/release in popup menus.
 */
public class SyncAction extends TeamAction {
	
	public void run(IAction action) {
		IResource[] resources = getSelectedResources();
		SyncView view = (SyncView)TeamUIPlugin.getActivePage().findView(SyncView.VIEW_ID);
		if (view == null) {
			view = SyncView.findInActivePerspective();
		}
		if (view != null) {
			try {
				TeamUIPlugin.getActivePage().showView(SyncView.VIEW_ID);
			} catch (PartInitException e) {
				TeamUIPlugin.log(e.getStatus());
			}
			view.showSync(getCompareInput(resources));
		}
	}
	
	protected boolean isEnabled() throws TeamException {
		IResource[] resources = getSelectedResources();
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			if (!resource.isAccessible()) return false;
			TargetProvider provider = TargetManager.getProvider(resource.getProject());
			if(provider == null) return false;
		}
		return true;
	}

	protected SyncCompareInput getCompareInput(IResource[] resources) {
		return new TargetSyncCompareInput(resources, IRemoteSyncElement.GRANULARITY_TIMESTAMP);
	}
}