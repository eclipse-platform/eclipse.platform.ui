package org.eclipse.team.internal.ui.target;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.target.TargetManager;
import org.eclipse.team.core.target.TargetProvider;
import org.eclipse.team.internal.core.TeamPlugin;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Action for getting the contents of the selected resources
 */
public class GetAction extends TeamAction {	public void run(IAction action) {
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					Hashtable table = getTargetProviderMapping();
					Set keySet = table.keySet();
					monitor.beginTask("", keySet.size() * 1000); //$NON-NLS-1$
					monitor.setTaskName(Policy.bind("GetAction.working")); 
					Iterator iterator = keySet.iterator();
					while (iterator.hasNext()) {					
						IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1000);
						TargetProvider provider = (TargetProvider)iterator.next();
						List list = (List)table.get(provider);
						IResource[] providerResources = (IResource[])list.toArray(new IResource[list.size()]);
						
						provider.get(providerResources, subMonitor);
					}
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		}, Policy.bind("GetAction.title"), this.PROGRESS_DIALOG); //$NON-NLS-1$
	}
	/**
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() {
		IResource[] resources = getSelectedResources();
		if (resources.length == 0) return false;
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			try {
				TargetProvider provider = TargetManager.getProvider(resource.getProject());			
				if(provider == null)
					return false;
				if(! provider.canPut(resource))
					return false;	//if one can't don't allow for any
			} catch (TeamException e) {
				TeamPlugin.log(IStatus.ERROR, "Exception getting provider", e);
				return false;
			}
		}
		return true;
	}
}