/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.operations;

import java.util.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Performs a cvs operation on multiple repository providers
 */
public abstract class RepositoryProviderOperation extends CVSOperation {

	private IResource[] resources;

	public RepositoryProviderOperation(IWorkbenchPart part, IResource[] resources) {
		super(part);
		this.resources = resources;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void execute(IProgressMonitor monitor) throws CVSException, InterruptedException {
		Map table = getProviderMapping(getResources());
		Set keySet = table.keySet();
		monitor.beginTask(null, keySet.size() * 1000);
		Iterator iterator = keySet.iterator();
		while (iterator.hasNext()) {
			IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1000);
			CVSTeamProvider provider = (CVSTeamProvider)iterator.next();
			List list = (List)table.get(provider);
			IResource[] providerResources = (IResource[])list.toArray(new IResource[list.size()]);
			ISchedulingRule rule = getSchedulingRule(provider);
			try {
				Platform.getJobManager().beginRule(rule, monitor);
				monitor.setTaskName(getTaskName(provider));
				execute(provider, providerResources, subMonitor);
			} finally {
				Platform.getJobManager().endRule(rule);
			}
		}
	}
	
	/**
	 * Return the taskname to be shown in the progress monitor while operating
	 * on the given provider.
	 * @param provider the provider being processed
	 * @return the taskname to be shown in the progress monitor
	 */
	protected abstract String getTaskName(CVSTeamProvider provider);

	/**
	 * Retgurn the scheduling rule to be obtained before work
	 * begins on the given provider. By default, it is the provider's project.
	 * This can be changed by subclasses.
	 * @param provider
	 * @return
	 */
	protected ISchedulingRule getSchedulingRule(CVSTeamProvider provider) {
		return provider.getProject();
	}

	/*
	 * Helper method. Return a Map mapping provider to a list of resources
	 * shared with that provider.
	 */
	private Map getProviderMapping(IResource[] resources) {
		Map result = new HashMap();
		for (int i = 0; i < resources.length; i++) {
			RepositoryProvider provider = RepositoryProvider.getProvider(resources[i].getProject(), CVSProviderPlugin.getTypeId());
			List list = (List)result.get(provider);
			if (list == null) {
				list = new ArrayList();
				result.put(provider, list);
			}
			list.add(resources[i]);
		}
		return result;
	}
	
	/**
	 * Return the resources that the operation is being performed on
	 * @return
	 */
	protected IResource[] getResources() {
		return resources;
	}

	/**
	 * Set the resources that the operation is to be performed on
	 * @param resources
	 */
	protected void setResources(IResource[] resources) {
		this.resources = resources;
	}

	/**
	 * Execute the operation on the resources for the given provider.
	 * @param provider
	 * @param providerResources
	 * @param subMonitor
	 * @throws CVSException
	 * @throws InterruptedException
	 */
	protected abstract void execute(CVSTeamProvider provider, IResource[] resources, IProgressMonitor monitor) throws CVSException, InterruptedException;

	protected ICVSResource[] getCVSArguments(IResource[] resources) {
		ICVSResource[] cvsResources = new ICVSResource[resources.length];
		for (int i = 0; i < cvsResources.length; i++) {
			cvsResources[i] = CVSWorkspaceRoot.getCVSResourceFor(resources[i]);
		}
		return cvsResources;
	}
	
	/*
	 * Get the arguments to be passed to a commit or update
	 */
	protected String[] getStringArguments(IResource[] resources) throws CVSException {
		List arguments = new ArrayList(resources.length);
		for (int i=0;i<resources.length;i++) {
			IPath cvsPath = resources[i].getFullPath().removeFirstSegments(1);
			if (cvsPath.segmentCount() == 0) {
				arguments.add(Session.CURRENT_LOCAL_FOLDER);
			} else {
				arguments.add(cvsPath.toString());
			}
		}
		return (String[])arguments.toArray(new String[arguments.size()]);
	}
	
	public ICVSResource[] getCVSResources() {
		ICVSResource[] cvsResources = new ICVSResource[resources.length];
		for (int i = 0; i < resources.length; i++) {
			cvsResources[i] = CVSWorkspaceRoot.getCVSResourceFor(resources[i]);
		}
		return cvsResources;
	}
	
	protected ICVSRepositoryLocation getRemoteLocation(CVSTeamProvider provider) throws CVSException {
		CVSWorkspaceRoot workspaceRoot = provider.getCVSWorkspaceRoot();
		return workspaceRoot.getRemoteLocation();
	}
	
	protected ICVSFolder getLocalRoot(CVSTeamProvider provider) throws CVSException {
		CVSWorkspaceRoot workspaceRoot = provider.getCVSWorkspaceRoot();
		return workspaceRoot.getLocalRoot();
	}

	/**
	 * Update the workspace subscriber for an update operation performed on the 
	 * given resources. After an update, the remote tree is flushed in order
	 * to ensure that stale incoming additions are removed. This need only
	 * be done for folders. At the time of writting, all update operations
	 * are deep so the flush is deep as well.
	 * @param provider the provider (projedct) for all the given resources
	 * @param resources the resources that were updated
	 * @param monitor a progress monitor
	 */
	protected void updateWorkspaceSubscriber(CVSTeamProvider provider, ICVSResource[] resources, IProgressMonitor monitor) {
		CVSWorkspaceSubscriber s = CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber();
		monitor.beginTask(null, 100 * resources.length);
		for (int i = 0; i < resources.length; i++) {
			ICVSResource resource = resources[i];
			if (resource.isFolder()) {
				try {
					s.updateRemote(provider, (ICVSFolder)resource, Policy.subMonitorFor(monitor, 100));
				} catch (TeamException e) {
					// Just log the error and continue
					CVSUIPlugin.log(e);
				}
			} else {
				monitor.worked(100);
			}
		}
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.team.ui.TeamOperation#isKeepOneProgressServiceEntry()
     */
    public boolean isKeepOneProgressServiceEntry() {
        // Keep the last repository provider operation in the progress service
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.ui.TeamOperation#getGotoAction()
     */
    protected IAction getGotoAction() {
        return getShowConsoleAction();
    }
}
