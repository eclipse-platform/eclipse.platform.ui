package org.eclipse.team.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.internal.simpleAccess.SimpleAccessOperations;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Action for deleting the selected resources on the provider
 */
public class DeleteAction extends TeamAction {
	/*
	 * Method declared on IActionDelegate.
	 */
	public void run(IAction action) {
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {		
					final boolean[] okToContinue = {false};
					getShell().getDisplay().syncExec(new Runnable() {
						public void run() {
							okToContinue[0] = MessageDialog.openConfirm(getShell(), Policy.bind("DeleteAction.promptTitle"), Policy.bind("DeleteAction.promptMessage")); //$NON-NLS-1$ //$NON-NLS-2$
						}
					});
								
					if (okToContinue[0]) {
						Hashtable table = getProviderMapping();
						Set keySet = table.keySet();
						monitor.beginTask("", keySet.size() * 1000); //$NON-NLS-1$
						monitor.setTaskName(Policy.bind("DeleteAction.deleting")); //$NON-NLS-1$
						Iterator iterator = keySet.iterator();
						while (iterator.hasNext()) {
							IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1000);
							RepositoryProvider provider = (RepositoryProvider)iterator.next();
							List list = (List)table.get(provider);
							IResource[] providerResources = (IResource[])list.toArray(new IResource[list.size()]);
							provider.getSimpleAccess().delete(providerResources, subMonitor);
							for (int i = 0; i < providerResources.length; i++) {
								providerResources[i].delete(true, monitor);
							}							
						}
					}
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		}, Policy.bind("DeleteAction.delete"), this.PROGRESS_BUSYCURSOR); //$NON-NLS-1$
	}
	/**
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		IResource[] resources = getSelectedResources();
		if (resources.length == 0) return false;
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			RepositoryProvider provider = RepositoryProvider.getProvider(resource.getProject());
			SimpleAccessOperations ops = provider.getSimpleAccess();
			if (provider == null || ops == null) return false;
			if (!ops.hasRemote(resource)) return false;
		}
		return true;
	}
}