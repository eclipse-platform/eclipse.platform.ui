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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.ComparisonCriteria;
import org.eclipse.team.core.subscribers.ContentComparisonCriteria;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.core.subscribers.TeamSubscriber;
import org.eclipse.team.core.subscribers.TeamDelta;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSynchronizer;
import org.eclipse.team.internal.ccvs.core.Policy;

/**
 * This class provides common funtionality for three way sychronizing
 * for CVS.
 */
public abstract class CVSSyncTreeSubscriber extends TeamSubscriber {
	
	private QualifiedName id;
	private String name;
	private String description;
	
	// options this subscriber supports for determining the sync state of resources
	private Map comparisonCriterias = new HashMap();
	private String defaultCriteria;
	
	CVSSyncTreeSubscriber(QualifiedName id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
		initializeComparisonCriteria();
	}

	/**
	 * Method invoked from the constructor to initialize the comparison criteria
	 * and the default criteria.
	 * This method can be overriden by subclasses.
	 */
	protected void initializeComparisonCriteria() {				
		// setup comparison criteria
		ComparisonCriteria revisionNumberComparator = new CVSRevisionNumberCompareCriteria();
		ComparisonCriteria contentsComparator = new ContentComparisonCriteria(new ComparisonCriteria[] {revisionNumberComparator}, false /*consider whitespace */);
		ComparisonCriteria contentsComparatorIgnoreWhitespace = new ContentComparisonCriteria(new ComparisonCriteria[] {revisionNumberComparator}, true /* ignore whitespace */);
		
		addComparisonCriteria(revisionNumberComparator);
		addComparisonCriteria(contentsComparator);
		addComparisonCriteria(contentsComparatorIgnoreWhitespace);
		
		// default
		defaultCriteria = revisionNumberComparator.getId();
	}
	
	/**
	 * Add the comparison criteria to the subscriber
	 * 
	 * @param comparator
	 */
	protected void addComparisonCriteria(ComparisonCriteria comparator) {
		comparisonCriterias.put(comparator.getId(), comparator);
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
	 * @see org.eclipse.team.core.sync.TeamSubscriber#members(org.eclipse.core.resources.IResource)
	 */
	public IResource[] members(IResource resource) throws TeamException {
		if(resource.getType() == IResource.FILE) {
			return new IResource[0];
		}	
		try {
			// Filter and return only phantoms associated with the remote synchronizer.
			IResource[] members = ((IContainer)resource).members(true /* include phantoms */);
			List filteredMembers = new ArrayList(members.length);
			for (int i = 0; i < members.length; i++) {
				IResource member = members[i];
				
				// TODO: consider that there may be several sync states on this resource. There
				// should instead be a method to check for the existance of a set of sync types on
				// a resource.
				if(member.isPhantom() && getRemoteSynchronizer().getSyncBytes(member) == null) {
					continue;
				}
				
				// TODO: Is this a valid use of isSupervised
				if (isSupervised(resource)) {
					filteredMembers.add(member);
				}
			}
			return (IResource[]) filteredMembers.toArray(new IResource[filteredMembers.size()]);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.TeamSubscriber#roots()
	 */
	public IResource[] roots() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.ISyncTreeSubscriber#getRemoteResource(org.eclipse.core.resources.IResource)
	 */
	public IRemoteResource getRemoteResource(IResource resource) throws TeamException {
		return getRemoteSynchronizer().getRemoteResource(resource);
	}

	public IRemoteResource getBaseResource(IResource resource) throws TeamException {
		return getBaseSynchronizer().getRemoteResource(resource);
	}

	/**
	 * Return the synchronizer that provides the remote resources
	 */
	protected abstract ResourceSynchronizer getRemoteSynchronizer();
	/**
	 * Return the synchronizer that provides the base resources
	 */
	protected abstract ResourceSynchronizer getBaseSynchronizer();
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.ISyncTreeSubscriber#getSyncInfo(org.eclipse.core.resources.IResource)
	 */
	public SyncInfo getSyncInfo(IResource resource, IProgressMonitor monitor) throws TeamException {
		if (!isSupervised(resource)) return null;
		IRemoteResource remoteResource = getRemoteResource(resource);
		if(resource.getType() == IResource.FILE) {
			IRemoteResource baseResource = getBaseResource(resource);
			return getSyncInfo(resource, baseResource, remoteResource, monitor);
		} else {
			// In CVS, folders do not have a base. Hence, the remote is used as the base.
			return getSyncInfo(resource, remoteResource, remoteResource, monitor);
		}
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
	protected SyncInfo getSyncInfo(IResource local, IRemoteResource base, IRemoteResource remote, IProgressMonitor monitor) throws TeamException {
		try {
			monitor = Policy.monitorFor(monitor);
			monitor.beginTask(null, 100);
			CVSSyncInfo info = new CVSSyncInfo(local, base, remote, this, Policy.subMonitorFor(monitor, 100));
			
			// if it's out of sync, then cache the contents
			//if(info.getKind() != SyncInfo.IN_SYNC && remote != null) {
			//	remote.getContents(Policy.subMonitorFor(monitor, 30));
			//}
			return info;
		} finally {
			monitor.done();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.ISyncTreeSubscriber#refresh(org.eclipse.core.resources.IResource[], int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void refresh(IResource[] resources, int depth, IProgressMonitor monitor) throws TeamException {
		monitor = Policy.monitorFor(monitor);
		try {
			monitor.beginTask(null, 100);
			IResource[] remoteChanges = refreshRemote(resources, depth, Policy.subMonitorFor(monitor, 60));
			IResource[] baseChanges = getBaseSynchronizer().refresh(resources, depth, getCacheFileContentsHint(), Policy.subMonitorFor(monitor, 40));
		
			Set allChanges = new HashSet();
			allChanges.addAll(Arrays.asList(remoteChanges));
			allChanges.addAll(Arrays.asList(baseChanges));
			IResource[] changedResources = (IResource[]) allChanges.toArray(new IResource[allChanges.size()]);
			fireTeamResourceChange(TeamDelta.asSyncChangedDeltas(this, changedResources));
		} finally {
			monitor.done();
		} 
	}

	protected IResource[] refreshRemote(IResource[] resources, int depth, IProgressMonitor monitor) throws TeamException {
		return getRemoteSynchronizer().refresh(resources, depth,  getCacheFileContentsHint(), monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.ISyncTreeSubscriber#getCurrentComparisonCriteria()
	 */
	public ComparisonCriteria getCurrentComparisonCriteria() {		
		return (ComparisonCriteria)comparisonCriterias.get(defaultCriteria);
	}

	private boolean getCacheFileContentsHint() {
		return getCurrentComparisonCriteria().usesFileContents();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.ISyncTreeSubscriber#setCurrentComparisonCriteria(java.lang.String)
	 */
	public void setCurrentComparisonCriteria(String id) throws TeamException {
		if(! comparisonCriterias.containsKey(id)) {
			throw new CVSException(Policy.bind("CVSSyncTreeSubscriber.0", id, getName())); //$NON-NLS-1$
		}
		this.defaultCriteria = id;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.ISyncTreeSubscriber#getComparisonCriterias()
	 */
	public ComparisonCriteria[] getComparisonCriterias() {
		return (ComparisonCriteria[]) comparisonCriterias.values().toArray(new ComparisonCriteria[comparisonCriterias.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.ISyncTreeSubscriber#isSupervised(org.eclipse.core.resources.IResource)
	 */
	public boolean isSupervised(IResource resource) throws TeamException {
		RepositoryProvider provider = RepositoryProvider.getProvider(resource.getProject(), CVSProviderPlugin.getTypeId());
		if (provider == null) return false;
		// TODO: what happens for resources that don't exist?
		// TODO: is it proper to use ignored here?
		ICVSResource cvsThing = CVSWorkspaceRoot.getCVSResourceFor(resource);
		if (cvsThing.isIgnored()) {
			// An ignored resource could have an incoming addition (conflict)
			return getRemoteSynchronizer().getSyncBytes(resource) != null;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.TeamSubscriber#isThreeWay()
	 */
	public boolean isThreeWay() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.TeamSubscriber#isCancellable()
	 */
	public boolean isCancellable() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.TeamSubscriber#cancel()
	 */
	public void cancel() {
		// noop
	}
}
