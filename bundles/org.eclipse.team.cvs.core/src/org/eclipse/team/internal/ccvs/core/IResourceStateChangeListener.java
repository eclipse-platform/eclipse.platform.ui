package org.eclipse.team.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.EventListener;

import org.eclipse.core.resources.IResource;

/**
 * A resource state change listener is notified of changes to resources
 * regarding their team state. 
 * <p>
 * Clients may implement this interface.
 * </p>
 * @see ITeamManager#addResourceStateChangeListener(IResourceStateChangeListener)
 */
public interface IResourceStateChangeListener extends EventListener{
	
	/**
	 * Notifies this listener that some resource state changes have already 
	 * happened. For example, a resource's team state has changed from checked-in
	 * to checked-out.
	 * <p>
	 * Note: This method is called by team core; it is not intended to be called 
	 * directly by clients.
	 * </p>
	 *
	 * @param resources that have changed state
	 * 
	 * [Note: The changed state event is purposely vague. For now it is only
	 * a hint to listeners that they should query the provider to determine the
	 * resources new team state.]
	 */
	public void resourceStateChanged(IResource[] changedResources);
}

