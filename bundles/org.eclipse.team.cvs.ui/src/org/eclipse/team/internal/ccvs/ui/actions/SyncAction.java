package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.ccvs.core.ICVSResource;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.sync.CVSSyncCompareInput;
import org.eclipse.team.ui.actions.TeamAction;
import org.eclipse.team.ui.sync.SyncView;
import org.eclipse.ui.PartInitException;

/**
 * Action for catchup/release in popup menus.
 */
public class SyncAction extends TeamAction {
	/*
	 * Method declared on IActionDelegate.
	 */
	public void run(IAction action) {
		IResource[] resources = getSelectedResources();
		SyncView view = (SyncView)CVSUIPlugin.getActivePage().findView(SyncView.VIEW_ID);
		if (view == null) {
			view = SyncView.findInActivePerspective();
		}
		if (view != null) {
			try {
				CVSUIPlugin.getActivePage().showView(SyncView.VIEW_ID);
			} catch (PartInitException e) {
				CVSUIPlugin.log(e.getStatus());
			}
			// What happens when resources from the same project are selected?
			view.showSync(new CVSSyncCompareInput(resources));
		}
	}
	
	/*
	 * Method declared on IActionDelegate.
	 */
	protected boolean isEnabled() throws TeamException {
		IResource[] resources = getSelectedResources();
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			if (!resource.isAccessible()) return false;
			if(resource.getType()==IResource.PROJECT) continue;
			// If the resource is not managed and its parent is not managed, disable.
			ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
			if (!cvsResource.isManaged()) {
				// The resource is not managed. See if its parent is managed.
				if (!cvsResource.getParent().isManaged()) return false;
			}
		}
		return true;
	}
}
