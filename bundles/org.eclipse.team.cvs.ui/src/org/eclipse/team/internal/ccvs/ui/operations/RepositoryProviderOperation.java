/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.operations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;

/**
 * Performs a cvs operation on multiple repository providers
 */
public abstract class RepositoryProviderOperation extends CVSOperation {

	private IResource[] resources;

	/**
	 * @param shell
	 */
	public RepositoryProviderOperation(Shell shell, IResource[] resources) {
		super(shell);
		this.resources = resources;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void execute(IProgressMonitor monitor) throws CVSException, InterruptedException {
		Map table = getProviderMapping(getResources());
		Set keySet = table.keySet();
		monitor.beginTask("", keySet.size() * 1000);
		monitor.setTaskName(getTaskName());
		Iterator iterator = keySet.iterator();
		while (iterator.hasNext()) {
			IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1000);
			CVSTeamProvider provider = (CVSTeamProvider)iterator.next();
			List list = (List)table.get(provider);
			IResource[] providerResources = (IResource[])list.toArray(new IResource[list.size()]);
			execute(provider, providerResources, subMonitor);
		}

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
	 * Return the task name associated with the operation. This task name
	 * will appear in progress feedback presented to the user.
	 * @return
	 */
	protected abstract String getTaskName();

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
}
