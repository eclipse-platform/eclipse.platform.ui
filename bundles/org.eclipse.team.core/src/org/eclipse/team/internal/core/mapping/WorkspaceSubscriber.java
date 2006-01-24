/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.core.mapping;

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.*;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.diff.IDiffNode;
import org.eclipse.team.core.diff.IDiffVisitor;
import org.eclipse.team.core.subscribers.*;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;
import org.eclipse.team.internal.core.*;

/**
 * A workspace subscriber is a special subscriber tat delegates to
 * the subscribers associated with each project through the
 * {@link RepositoryProvider} API.
 * @since 3.2
 */
public class WorkspaceSubscriber extends Subscriber implements ISubscriberChangeListener, IRepositoryProviderListener {
	
	private static WorkspaceSubscriber instance;
	private Map projects = new HashMap();
	
	public static synchronized WorkspaceSubscriber getInstance() {
		if (instance == null) {
			instance = new WorkspaceSubscriber();
		}
		return instance;
	}

	public WorkspaceSubscriber() {
		// Add subscribers for all projects that have them
		RepositoryProviderManager.getInstance().addListener(this);
		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < allProjects.length; i++) {
			IProject project = allProjects[i];
			if (RepositoryProvider.isShared(project)) {
				// TODO This will instantiate all providers
				RepositoryProvider provider = RepositoryProvider.getProvider(project);
				Subscriber subscriber = provider.getSubscriber();
				if (subscriber != null) {
					subscriber.addListener(this);
					projects.put(project, subscriber);
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.Subscriber#getName()
	 */
	public String getName() {
		return "Workspace";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.Subscriber#isSupervised(org.eclipse.core.resources.IResource)
	 */
	public boolean isSupervised(IResource resource) throws TeamException {
		Subscriber subscriber = getSubscriber(resource);
		if (subscriber != null)
			return subscriber.isSupervised(resource);
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.Subscriber#members(org.eclipse.core.resources.IResource)
	 */
	public IResource[] members(IResource resource) throws TeamException {
		Subscriber subscriber = getSubscriber(resource);
		if (subscriber != null)
			return subscriber.members(resource);
		if (resource instanceof IContainer) {
			IContainer container = (IContainer) resource;
			try {
				return container.members();
			} catch (CoreException e) {
				throw TeamException.asTeamException(e);
			}
		}
		return new IResource[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.Subscriber#roots()
	 */
	public IResource[] roots() {
		return (IProject[]) projects.keySet().toArray(new IProject[projects.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.Subscriber#getDiff(org.eclipse.core.resources.IResource)
	 */
	public IDiffNode getDiff(IResource resource) throws CoreException {
		Subscriber subscriber = getSubscriber(resource);
		if (subscriber != null)
			return subscriber.getDiff(resource);
		return super.getDiff(resource);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.Subscriber#getSyncInfo(org.eclipse.core.resources.IResource)
	 */
	public SyncInfo getSyncInfo(IResource resource) throws TeamException {
		Subscriber subscriber = getSubscriber(resource);
		if (subscriber != null)
			return subscriber.getSyncInfo(resource);
		return null;
	}

	/**
	 * Return a dummy comparator. The comparator should not be used by clients.
	 * 
	 * @see org.eclipse.team.core.subscribers.Subscriber#getResourceComparator()
	 */
	public IResourceVariantComparator getResourceComparator() {
		return new IResourceVariantComparator() {
			public boolean isThreeWay() {
				return true;
			}
			public boolean compare(IResourceVariant base, IResourceVariant remote) {
				return false;
			}
			public boolean compare(IResource local, IResourceVariant remote) {
				return false;
			}
		
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.Subscriber#refresh(org.eclipse.core.resources.mapping.ResourceTraversal[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void refresh(ResourceTraversal[] traversals, IProgressMonitor monitor) throws TeamException {
		try {
			List errors = new ArrayList();
			Subscriber[] subscribers = getSubscribers();
			monitor.beginTask(null, subscribers.length * 100);
			for (int i = 0; i < subscribers.length; i++) {
				Subscriber subscriber = subscribers[i];
				try {
					subscriber.refresh(traversals, Policy.subMonitorFor(monitor, 100));
				} catch (TeamException e) {
					errors.add(e);
				}
			}
			try {
				handleErrors((CoreException[]) errors.toArray(new CoreException[errors.size()]));
			} catch (CoreException e) {
				throw TeamException.asTeamException(e);
			}
		} finally {
			monitor.done();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.Subscriber#refresh(org.eclipse.core.resources.IResource[], int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void refresh(IResource[] resources, int depth,
			IProgressMonitor monitor) throws TeamException {
		refresh(new ResourceTraversal[] { new ResourceTraversal(resources, depth, IResource.NONE)}, monitor);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.Subscriber#accept(org.eclipse.core.resources.mapping.ResourceTraversal[], org.eclipse.team.core.diff.IDiffVisitor)
	 */
	public void accept(ResourceTraversal[] traversals, IDiffVisitor visitor) throws CoreException {
		List errors = new ArrayList();
		Subscriber[] subscribers = getSubscribers();
		for (int i = 0; i < subscribers.length; i++) {
			Subscriber subscriber = subscribers[i];
			try {
				subscriber.accept(traversals, visitor);
			} catch (CoreException e) {
				errors.add(e);
			}
		}
		handleErrors((CoreException[]) errors.toArray(new CoreException[errors.size()]));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.Subscriber#accept(org.eclipse.core.resources.IResource[], int, org.eclipse.team.core.diff.IDiffVisitor)
	 */
	public void accept(IResource[] resources, int depth, IDiffVisitor visitor) throws CoreException {
		accept(new ResourceTraversal[] { new ResourceTraversal(resources, depth, IResource.NONE)}, visitor);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.Subscriber#getState(org.eclipse.core.resources.mapping.ResourceMapping, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public int getState(ResourceMapping mapping, int stateMask, IProgressMonitor monitor) throws CoreException {
		int state = 0;
		try {
			List errors = new ArrayList();
			Subscriber[] subscribers = getSubscribers();
			monitor.beginTask(null, subscribers.length * 100);
			for (int i = 0; i < subscribers.length; i++) {
				Subscriber subscriber = subscribers[i];
				try {
					int subscriberState = subscriber.getState(mapping, stateMask, Policy.subMonitorFor(monitor, 100));
					state |= subscriberState;
				} catch (TeamException e) {
					errors.add(e);
				}
			}
			handleErrors((CoreException[]) errors.toArray(new CoreException[errors.size()]));
		} finally {
			monitor.done();
		}
		return state & stateMask;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.Subscriber#collectOutOfSync(org.eclipse.core.resources.IResource[], int, org.eclipse.team.core.synchronize.SyncInfoSet, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void collectOutOfSync(IResource[] resources, int depth, SyncInfoSet set, IProgressMonitor monitor) {
		try {
			Subscriber[] subscribers = getSubscribers();
			monitor.beginTask(null, subscribers.length * 100);
			for (int i = 0; i < subscribers.length; i++) {
				Subscriber subscriber = subscribers[i];
				subscriber.collectOutOfSync(resources, depth, set, Policy.subMonitorFor(monitor, 100));
			}
		} finally {
			monitor.done();
		}
	}
	
	private Subscriber[] getSubscribers() {
		Set result = new HashSet();
		for (Iterator iter = projects.values().iterator(); iter.hasNext();) {
			Subscriber subscriber = (Subscriber) iter.next();
			result.add(subscriber);
		}
		return (Subscriber[]) result.toArray(new Subscriber[result.size()]);
	}

	/*
	 * Return the subscriber for the given resource if the resource 
	 * is in the scope of this subscriber.
	 */
	private Subscriber getSubscriber(IResource resource) {
		return (Subscriber)projects.get(resource.getProject());
	}
	
	private void handleErrors(CoreException[] exceptions) throws CoreException {
		if (exceptions.length == 0)
			return;
		if (exceptions.length == 1)
			throw exceptions[0];
		MultiStatus result = new MultiStatus(TeamPlugin.ID, 0, "Mulitple errors occurred", null);
		for (int i = 0; i < exceptions.length; i++) {
			CoreException exception = exceptions[i];
			IStatus status = new Status(
					exception.getStatus().getSeverity(),
					exception.getStatus().getPlugin(),
					exception.getStatus().getCode(),
					exception.getStatus().getMessage(),
					exception);
			result.add(status);
		}
		throw new TeamException(result);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.ISubscriberChangeListener#subscriberResourceChanged(org.eclipse.team.core.subscribers.ISubscriberChangeEvent[])
	 */
	public void subscriberResourceChanged(ISubscriberChangeEvent[] deltas) {
		fireTeamResourceChange(deltas);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.IRepositoryProviderListener#providerMapped(org.eclipse.team.core.RepositoryProvider)
	 */
	public void providerMapped(RepositoryProvider provider) {
		// Record the subscriber. No need to fire an event since the subscriber should
		Subscriber subscriber = provider.getSubscriber();
		if (subscriber != null)
			projects.put(provider.getProject(), subscriber);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.IRepositoryProviderListener#providerUnmapped(org.eclipse.core.resources.IProject)
	 */
	public void providerUnmapped(IProject project) {
		// We'll remove the project. No need to fire an event since the subscriber should have done that
		projects.remove(project);
	}
}
