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
package org.eclipse.team.internal.ccvs.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.SubscriberChangeEvent;
import org.eclipse.team.core.synchronize.IResourceVariant;
import org.eclipse.team.core.synchronize.IResourceVariantComparator;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.core.subscribers.caches.PersistantResourceVariantByteStore;
import org.eclipse.team.internal.core.subscribers.caches.ResourceVariantByteStore;
import org.eclipse.team.internal.core.subscribers.caches.ResourceVariantTree;
import org.eclipse.team.internal.core.subscribers.caches.SyncTreeSubscriber;

/**
 * This class provides common funtionality for three way sychronizing
 * for CVS.
 */
public abstract class CVSSyncTreeSubscriber extends SyncTreeSubscriber {
	
	public static final String SYNC_KEY_QUALIFIER = "org.eclipse.team.cvs"; //$NON-NLS-1$
	
	private IResourceVariantComparator comparisonCriteria;
	
	private QualifiedName id;
	private String name;
	private String description;
	
	CVSSyncTreeSubscriber(QualifiedName id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.comparisonCriteria = new CVSRevisionNumberCompareCriteria(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.ISyncTreeSubscriber#getId()
	 */
	public QualifiedName getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.ISyncTreeSubscriber#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.ISyncTreeSubscriber#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.ISyncTreeSubscriber#getSyncInfo(org.eclipse.core.resources.IResource)
	 */
	public SyncInfo getSyncInfo(IResource resource) throws TeamException {
		if (!isSupervised(resource)) return null;
		if(resource.getType() == IResource.FILE || !isThreeWay()) {
			return super.getSyncInfo(resource);
		} else {
			// In CVS, folders do not have a base. Hence, the remote is used as the base.
			IResourceVariant remoteResource = getRemoteResource(resource);
			return getSyncInfo(resource, remoteResource, remoteResource);
		}
	}
	
	protected boolean isThreeWay() {
		return true;
	}

	/**
	 * Method that creates an instance of SyncInfo for the provider local, base and remote.
	 * Can be overiden by subclasses.
	 * @param local
	 * @param base
	 * @param remote
	 * @param monitor
	 * @return
	 */
	protected SyncInfo getSyncInfo(IResource local, IResourceVariant base, IResourceVariant remote) throws TeamException {
		CVSSyncInfo info = new CVSSyncInfo(local, base, remote, this);
		info.init();
		return info;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.ISyncTreeSubscriber#refresh(org.eclipse.core.resources.IResource[], int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void refresh(IResource[] resources, int depth, IProgressMonitor monitor) throws TeamException {
		monitor = Policy.monitorFor(monitor);
		List errors = new ArrayList();
		try {
			monitor.beginTask(null, 100 * resources.length);
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				IStatus status = refresh(resource, depth, Policy.subMonitorFor(monitor, 100));
				if (!status.isOK()) {
					errors.add(status);
				}
			}
		} finally {
			monitor.done();
		} 
		if (!errors.isEmpty()) {
			throw new CVSException(new MultiStatus(CVSProviderPlugin.ID, 0, 
					(IStatus[]) errors.toArray(new IStatus[errors.size()]), 
					Policy.bind("CVSSyncTreeSubscriber.1", getName()), null)); //$NON-NLS-1$
		}
	}

	public IStatus refresh(IResource resource, int depth, IProgressMonitor monitor) {
		monitor = Policy.monitorFor(monitor);
		try {
			// Take a guess at the work involved for refreshing the base and remote tree
			int baseWork = isThreeWay() ? (getCacheFileContentsHint() ? 30 : 10) : 0;
			int remoteWork = 100;
			monitor.beginTask(null, baseWork + remoteWork);
			IResource[] baseChanges = refreshBase(new IResource[] {resource}, depth, Policy.subMonitorFor(monitor, baseWork));
			IResource[] remoteChanges = refreshRemote(new IResource[] {resource}, depth, Policy.subMonitorFor(monitor, remoteWork));
			
			Set allChanges = new HashSet();
			allChanges.addAll(Arrays.asList(remoteChanges));
			allChanges.addAll(Arrays.asList(baseChanges));
			IResource[] changedResources = (IResource[]) allChanges.toArray(new IResource[allChanges.size()]);
			fireTeamResourceChange(SubscriberChangeEvent.asSyncChangedDeltas(this, changedResources));
			return Status.OK_STATUS;
		} catch (TeamException e) {
			return new CVSStatus(IStatus.ERROR, Policy.bind("CVSSyncTreeSubscriber.2", resource.getFullPath().toString(), e.getMessage()), e); //$NON-NLS-1$
		} finally {
			monitor.done();
		} 
	}
	
	protected  boolean getCacheFileContentsHint() {
		return false;
	}

	protected IResource[] refreshBase(IResource[] resources, int depth, IProgressMonitor monitor) throws TeamException {
		if (isThreeWay()) {
			return getBaseTree().refresh(resources, depth, monitor);
		} else {
			return new IResource[0];
		}
	}

	protected IResource[] refreshRemote(IResource[] resources, int depth, IProgressMonitor monitor) throws TeamException {
		return getRemoteTree().refresh(resources, depth, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.ISyncTreeSubscriber#isSupervised(org.eclipse.core.resources.IResource)
	 */
	public boolean isSupervised(IResource resource) throws TeamException {
		try {
			RepositoryProvider provider = RepositoryProvider.getProvider(resource.getProject(), CVSProviderPlugin.getTypeId());
			if (provider == null) return false;
			// TODO: what happens for resources that don't exist?
			// TODO: is it proper to use ignored here?
			ICVSResource cvsThing = CVSWorkspaceRoot.getCVSResourceFor(resource);
			if (cvsThing.isIgnored()) {
				// An ignored resource could have an incoming addition (conflict)
				return hasRemote(resource);
			}
			return true;
		} catch (TeamException e) {
			// If there is no resource in coe this measn there is no local and no remote
			// so the resource is not supervised.
			if (e.getStatus().getCode() == IResourceStatus.RESOURCE_NOT_FOUND) {
				return false;
			}
			throw e;
		}
	}
	
	public IResourceVariant getRemoteResource(IResource resource) throws TeamException {
		return getRemoteTree().getResourceVariant(resource);
	}

	public IResourceVariant getBaseResource(IResource resource) throws TeamException {
		if (isThreeWay()) {
			return getBaseTree().getResourceVariant(resource);
		} else {
			return null;
		}
	}
	
	/**
	 * Return the base resource variant tree.
	 */
	protected abstract ResourceVariantTree getBaseTree();

	/**
	 * Return the remote resource variant tree.
	 */
	protected abstract ResourceVariantTree getRemoteTree();
	
	private String getSyncName(ResourceVariantByteStore cache) {
		if (cache instanceof PersistantResourceVariantByteStore) {
			return ((PersistantResourceVariantByteStore)cache).getSyncName().toString();
		}
		return cache.getClass().getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.helpers.SyncTreeSubscriber#hasRemote(org.eclipse.core.resources.IResource)
	 */
	protected boolean hasRemote(IResource resource) throws TeamException {
		return getRemoteTree().hasResourceVariant(resource);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.TeamSubscriber#getDefaultComparisonCriteria()
	 */
	public IResourceVariantComparator getResourceComparator() {
		return comparisonCriteria;
	}
	
	public IResource[] members(IResource resource) throws TeamException {
		if(resource.getType() == IResource.FILE) {
			return new IResource[0];
		}	
		try {
			Set allMembers = new HashSet();
			try {
				allMembers.addAll(Arrays.asList(((IContainer)resource).members()));
			} catch (CoreException e) {
				if (e.getStatus().getCode() == IResourceStatus.RESOURCE_NOT_FOUND) {
					// The resource is no longer exists so ignore the exception
				} else {
					throw e;
				}
			}
			allMembers.addAll(Arrays.asList(getMembers(getRemoteTree(), resource)));
			if (isThreeWay()) {
				allMembers.addAll(Arrays.asList(getMembers(getBaseTree(), resource)));
			}
			for (Iterator iterator = allMembers.iterator(); iterator.hasNext();) {
				IResource member = (IResource) iterator.next();
				if(!member.exists() && !hasRemote(member)) {
					// Remove deletion conflicts
					iterator.remove();
				} else if (!isSupervised(resource)) {
					// Remove unsupervised resources
					iterator.remove();
				}
			}
			return (IResource[]) allMembers.toArray(new IResource[allMembers.size()]);
		} catch (CoreException e) {
			throw TeamException.asTeamException(e);
		}
	}

	private IResource[] getMembers(ResourceVariantTree tree, IResource resource) throws TeamException, CoreException {
		// Filter and return only phantoms associated with the remote synchronizer.
		IResource[] members;
		try {
			members = tree.members(resource);
		} catch (CoreException e) {
			if (!isSupervised(resource) || e.getStatus().getCode() == IResourceStatus.RESOURCE_NOT_FOUND) {
				// The resource is no longer supervised or doesn't exist in any form
				// so ignore the exception and return that there are no members
				return new IResource[0];
			}
			throw e;
		}
		return members;
	}
}
