package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.core.ITeamProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.sync.SyncView;
import org.eclipse.team.ui.TeamUIPlugin;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;

/**
 * Action for catchup/release in popup menus.
 */
public class SyncAction implements IObjectActionDelegate {
	private ISelection selection;
	
	/**
	 * Convenience method: extract all <code>IResources</code> from given selection.
	 * Never returns null.
	 */
	public static IResource[] getResources(ISelection selection) {
		ArrayList tmp = new ArrayList();
		if (selection instanceof IStructuredSelection) {
			Object[] s = ((IStructuredSelection) selection).toArray();
			for (int i = 0; i < s.length; i++) {
				Object o = s[i];
				if (o instanceof IResource) {
					tmp.add(o);
					continue;
				}
				if (o instanceof IAdaptable) {
					IAdaptable a = (IAdaptable) o;
					Object adapter = a.getAdapter(IResource.class);
					if (adapter instanceof IResource)
						tmp.add(adapter);
					continue;
				}
			}
		}
		IResource[] resourceSelection = new IResource[tmp.size()];
		tmp.toArray(resourceSelection);
		return resourceSelection;
	}
	
	/**
	 * Convenience method for getting the current shell.
	 */
	protected Shell getShell() {
		return TeamUIPlugin.getPlugin().getWorkbench().getActiveWorkbenchWindow().getShell();
	}

	/*
	 * Method declared on IActionDelegate.
	 */
	public void run(IAction action) {
		String title = Policy.bind("SyncAction.sync");
		try {
			IResource[] resources = getResources(selection);
			SyncView view = (SyncView)TeamUIPlugin.getActivePage().findView(SyncView.VIEW_ID);
			if (view == null) {
				TeamUIPlugin plugin = TeamUIPlugin.getPlugin();
				view = SyncView.findInActivePerspective();
			}
			if (view != null) {
				try {
					TeamUIPlugin.getActivePage().showView(SyncView.VIEW_ID);
				} catch (PartInitException e) {
					TeamUIPlugin.log(e.getStatus());
				}
				// What happens when resources from the same project are selected?
				IRemoteSyncElement[] input = new IRemoteSyncElement[resources.length];
				for (int i = 0; i < input.length; i++) {
					CVSTeamProvider provider = (CVSTeamProvider)TeamPlugin.getManager().getProvider(resources[i].getProject());
					input[i] = provider.getRemoteSyncTree(resources[i], CVSTag.DEFAULT, new NullProgressMonitor());
				}
				view.showSync(input);
			}
		} catch (TeamException e) {
			ErrorDialog.openError(getShell(), title, null, e.getStatus());
		}
	}
	
	/*
	 * Method declared on IActionDelegate.
	 */
	public void selectionChanged(IAction action, ISelection s) {
		selection = s;
		IResource[] resources = getResources(s);
		for (int i = 0; i < resources.length; i++) {
			if (!resources[i].isAccessible()) {
				action.setEnabled(false);
				return;
			}
			ITeamProvider provider = TeamPlugin.getManager().getProvider(resources[i].getProject());
			if (provider == null) {
				action.setEnabled(false);
				return;
			}
			if (!(provider instanceof CVSTeamProvider)) {
				action.setEnabled(false);
				return;
			}
		}
		action.setEnabled(true);
	}
	
	/*
	 * Method declared on IObjectActionDelegate.
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}
}
