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
package org.eclipse.team.core.subscribers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.core.*;

/**
 * A TeamSubscriber is connected to a remote location that has incoming changes
 * to be merged into a workspace. It maintains the synchronization state of the incoming
 * changes based on those in the workspace.
 * 
 * [Note: How can we allow the refresh() operation to optimize the sync calculation based
 * on the currently configured compare criteria?]
 */
abstract public class TeamSubscriber {
	
	private List listeners = new ArrayList(1);
	
	/**
	 * Return the subscriber manager which provides access and control of the
	 * team subscribers.
	 * @return the subscriber manager
	 */
	public static synchronized ISubscriberManager getSubscriberManager() {
		return SubscriberManager.getInstance();
	}
	
	/**
	 * Return the unique id that identified this subscriber.
	 */
	abstract public QualifiedName getId();
	
	/**
	 * Return the name of this subscription, in a format that is suitable for 
	 * display to an end user.
	 * 
	 * @return String representing the name of this subscription. 
	 */
	abstract public String getName();
	
	/**
	 * Return the description of this subscription, in a format that is suitable for 
	 * display to an end user. The description should contain enough details to
	 * understand the connection type of this subscriber. 
	 * 
	 * @return String representing the description of this subscription. 
	 */
	abstract public String getDescription();
	
	/**
	 * Returns <code>true</code> if this resource is supervised by this subscriber. 
	 * A supervised resource is one for which this subscriber maintains the synchronization
	 * state.  Returns <code>false</code> in all other cases. 
	 * 
	 * @return <code>true</code> if this resource is supervised, and 
	 *   <code>false</code> otherwise
	 */
	abstract public boolean isSupervised(IResource resource) throws TeamException;
	
	/**
	 * Returns all non-transient member resources of the given resource.
	 * The result will include entries for resources that exist 
	 * either in the workspace or are implicated in an incoming change. 
	 * Returns an empty list if the given resource exists neither in
	 * the workspace nor in the corresponding team stream, or if the
	 * given resource is transient.
	 * <p>
	 * This is a fast operation; the repository is not contacted.
	 * </p>
	 * <p>
	 * [Issue1 : Is there any filtering on the members? Just the ones
	 *  that changed in some way, or *every member*?
	 * ]</p>
	 *
	 * @param resource the resource
	 * @return a list of member resources
	 * @exception CoreException if this request fails. Reasons include:
	 */
	abstract  public IResource[] members(IResource resource) throws TeamException;
	
	/**
	 * Returns the list of  root resources this subscriber considers for synchronization.
	 * A client should call this method first then can safely call <code>members</code>
	 * to navigate the resources managed by this subscriber.
	 *   
	 * @return a list of resources
	 * @throws TeamException
	 */
	abstract public IResource[] roots();
	
	/**
	 * Returns a handle to the remote resource corresponding to the given
	 * resource, or <code>null</code> if there is no corresponding resource
	 * edition. 
	 * <p> 
	 * This is a fast operation; the repository is not contacted.
	 * </p>
	 *
	 * @param resource the resource
	 * @return a server resource
	 * @exception CoreException if this request fails. Reasons include:
	 * <ul>
	 * <li>???</li>
	 * </ul>
	 */	
	abstract public IRemoteResource getRemoteResource(IResource resource) throws TeamException;
	
	/**
	 * Returns synchronization info for the given resource, or
	 * <code>null</code> if there is no synchronization info
	 * because the subscriber does not apply to this resource.
	 * <p>
	 * Note that sync info may be returned for non-existing
	 * or for resources which have no corresponding remote resource.
	 * </p>
	 * <p> 
	 * This method may take some time; it depends on the comparison criteria
	 * that is used to calculate the synchronization state (e.g. using content
	 * or only timestamps).
	 * </p>
	 *
	 * @param resource the resource of interest
	 * @return sync info
	 */
	abstract public SyncInfo getSyncInfo(IResource resource, IProgressMonitor monitor) throws TeamException; 
	
	/** 
	 * Refreshes the resource hierarchy from the given resources and their 
	 * children (to the specified depth) from the corresponding resources in 
	 * the remote location. Resources are ignored in the following cases:
	 * <ul>
	 * <li>if they do not exist either in the workspace
	 * or in the corresponding remote location</li>
	 * <li>if the given resource is marked as derived (see IResource#isDerived())</li>
	 * <li>if the given resource is not managed by this subscriber</li>
	 * <li>if the given resource is a closed project (they are ineligible for synchronization)</li>
	 * <p>
	 * Typical synchronization operations use the statuses computed by this method 
	 * as the basis for determining what to do.  It is possible for the actual sync 
	 * status of the resource to have changed since the current local sync status 
	 * was refreshed.  Operations typically skip resources with stale sync information.  
	 * The chances of stale information being used can be reduced by running this
	 * method (where feasible) before doing other operations.  Note that this will
	 * of course affect performance.  
	 * </p>
	 * <p>
	 * The depth parameter controls whether refreshing is performed
	 * on just the given resource (depth=<code>DEPTH_ZERO</code>), 
	 * the resource and its children (depth=<code>DEPTH_ONE</code>),
	 * or recursively to the resource and all its descendents (depth=<code>DEPTH_INFINITE</code>).
	 * Use depth <code>DEPTH_ONE</code>, rather than depth 
	 * <code>DEPTH_ZERO</code>, to ensure that new members of a project
	 * or folder are detected.
	 * </p>
	 * <p>
	 * This method might change resources; any changes will be reported
	 * in a subsequent resource change event indicating changes to server sync 
	 * status.
	 * </p>
	 * <p>
	 * This method contacts the server and is therefore long-running; 
	 * progress and cancellation are provided by the given progress monitor.
	 * </p>
	 *
	 * @param resources the resources
	 * @param depth valid values are <code>DEPTH_ZERO</code>, 
	 *  <code>DEPTH_ONE</code>, or <code>DEPTH_INFINITE</code>
	 * @param monitor progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return status with code <code>OK</code> if there were no problems;
	 *     otherwise a description (possibly a multi-status) consisting of
	 *     low-severity warnings or informational messages. 
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> The server could not be contacted.</li>
	 * </ul>
	 */
	abstract public void refresh(IResource[] resources, int depth, IProgressMonitor monitor) throws TeamException;
	
	/**
	 * Returns the list of available comparison criteria supported by this subscriber.
	 */
	abstract public ComparisonCriteria[] getComparisonCriterias();
	
	/**
	 * Returns the comparison criteria that will be used by the sync info created by
	 * this subscriber.
	 */
	abstract public ComparisonCriteria getCurrentComparisonCriteria();
	
	/**
	 * Set the current comparison criteria to the one defined by the given id. An exception is
	 * thrown if the id is not suported by this subscriber.
	 */
	abstract public void setCurrentComparisonCriteria(String id) throws TeamException;

	/**
	 * Answers <code>true</code> if the base tree is maintained by this subscriber. If the base
	 * tree is not considered than the subscriber can be considered as not supported three-way
	 * comparisons. Instead comparisons are made between the local and remote only without
	 * consideration for the base.
	 */
	abstract public boolean isThreeWay();
	
	/**
	 * Returns if this subscription can be cancelled. This allows short-lived subscriptions to
	 * be terminated at the users request. For example, this could be used to finish a merge
	 * subscription once all changes have been merged. 
	 */
	abstract public boolean isCancellable();

	/**
	 * Cancels this subscription.
	 */	
	abstract public void cancel();
	
	/**
	 * Adds a listener to this team subscriber. 
	 * Has no effect if an identical listener is already registered.
	 * <p>
	 * Team resource change listeners are informed about state changes 
	 * that affect the resources supervised by this subscriber.</p>
	 * 
	 * @param listener a team resource change listener
	 */
	public void addListener(ITeamResourceChangeListener listener) {
		if(! listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	/**
	 * Removes a listener previously registered with this team subscriber.
	 * Has no affect if an identical listener is not registered.
	 * 
	 * @param listener a team resource change listener
	 */	
	public void removeListener(ITeamResourceChangeListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Fires a team resource change event to all registered listeners
	 * Only listeners registered at the time this method is called are notified.
	 */
	protected void fireTeamResourceChange(final TeamDelta[] deltas) {
		for (Iterator it = listeners.iterator(); it.hasNext();) {
			final ITeamResourceChangeListener l = (ITeamResourceChangeListener) it.next();
			l.teamResourceChanged(deltas);	
		}
	}

	/**
	 * Return an array of all out-of-sync resources (getKind() != 0) that occur 
	 * under the given resources to the specified depth. The purpose of this method is
	 * to provide subscribers a means of optimizing the determination of 
	 * all out-of-sync out-of-sync descendants of a set of resources. 
	 * <p>
	 * A return value of an empty array indicates that there are no out-of-sync resources
	 * supervised by the subscriber. A return of <code>null</code> indicates that the
	 * subscriber does not support this operation in an optimized fashion. In this case,
	 * the caller can determine the out-of-sync resources by traversing the resource
	 * structure form the roots of the subscriber (@see <code>getRoots()</code>).</p>
	 * 
	 * @param resources
	 * @param depth
	 * @param monitor
	 * @return
	 */
	public SyncInfo[] getAllOutOfSync(IResource[] resources, int depth, IProgressMonitor monitor) throws TeamException {
		return null;
	}
	
	/**
	 * Returns <code>true</code> if you can release changes to this subscriber and 
	 * <code>false</code> otherwise. 
	 */
	public boolean isReleaseSupported() {
		return true;
	}
	
	/**
	 * Return true if the receiver is equal to object.
	 * @return true if object is the same class as the receiver and has the same ID
	 */
	public boolean equals(Object object) {
		if (object instanceof TeamSubscriber) {
			TeamSubscriber subscriber = (TeamSubscriber) object;
			return getId().equals(subscriber.getId());
		}
		return super.equals(object);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getId().hashCode();
	}
}