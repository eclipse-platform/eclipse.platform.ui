package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class ReplaceWithRemoteAction extends ReplaceWithAction {
	public void run(IAction action) {
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					// Check if any resource is dirty.
					IResource[] candidateResources = getSelectedResources();
					List targetResources = new ArrayList();
					for (int i = 0; i < candidateResources.length; i++) {
						IResource resource = candidateResources[i];
						if (isDirty(resource) && getConfirmOverwrite()) {
							if (confirmOverwrite(Policy.bind("ReplaceWithRemoteAction.localChanges", resource.getName()))) {
								targetResources.add(resource);
							}
						} else {
							targetResources.add(resource);
						}						
					}
					// Do the replace
					Hashtable table = getProviderMapping((IResource[])targetResources.toArray(new IResource[targetResources.size()]));
					Set keySet = table.keySet();
					monitor.beginTask("", keySet.size() * 1000);
					monitor.setTaskName(Policy.bind("ReplaceWithRemoteAction.replacing"));
					Iterator iterator = keySet.iterator();
					while (iterator.hasNext()) {
						IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1000);
						CVSTeamProvider provider = (CVSTeamProvider)iterator.next();
						List list = (List)table.get(provider);
						IResource[] providerResources = (IResource[])list.toArray(new IResource[list.size()]);
						provider.get(providerResources, IResource.DEPTH_INFINITE, subMonitor);
					}
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		}, Policy.bind("ReplaceWithRemoteAction.problemMessage"), PROGRESS_DIALOG);
	}
	protected boolean isEnabled() throws TeamException {
		IResource[] resources = getSelectedResources();
		if (resources.length == 0) return false;
		for (int i = 0; i < resources.length; i++) {
			CVSTeamProvider provider = (CVSTeamProvider)RepositoryProvider.getProvider(resources[i].getProject(), CVSProviderPlugin.getTypeId());
			if (provider == null) return false;
			if (!provider.hasRemote(resources[i])) return false;
		}
		return true;
	}
}
