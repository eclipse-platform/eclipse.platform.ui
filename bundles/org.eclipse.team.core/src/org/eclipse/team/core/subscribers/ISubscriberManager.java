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

import org.eclipse.core.runtime.QualifiedName;

/**
 * The subscriber manager is responsible for managing the team subscribers.
 * It provides the ability to register and deregister subscribers and access
 * all currently registered subscribers. It also fires events when subscribers
 * are registered and deregistered.
 */
public interface ISubscriberManager {

	/**
	 * Register a subscriber with the subscriber manager. The id of the subscriber
	 * (returned by <code>TeamSubscriber#getId()</code>) must be unique.
	 * Registering a subscriber will result in all registered listeners being 
	 * notified of the addition in the same thread as the call to <code>registerSubscriber()</code>.
	 * 
	 * @param subscriber the subscriber being registered
	 */
	public void registerSubscriber(TeamSubscriber subscriber);
	
	/**
	 * Remove a subscriber from the subscriber manager. Deregistering a subscriber
	 * will result in all registered listeners being 
	 * notified of the addition in the same thread as the call to 
	 * <code>deregisterSubscriber()</code>.
	 * 
	 * @param subscriber the subscriber being deregistered
	 */
	public void deregisterSubscriber(TeamSubscriber subscriber);
	
	/**
	 * Get the subscriber with the given id. Return <code>null</code>
	 * if there is no registered subscriber with the given id.
	 * @param id the unique id of the subscriber
	 * @return the subscriber whose id matches the given one
	 */
	public TeamSubscriber getSubscriber(QualifiedName id);
	
	/**
	 * Return all registered subscribers.
	 * @return all registered subscribers
	 */
	public TeamSubscriber[] getSubscribers();
	
	/**
	 * Add a change listener that will be invoked when a subscriber is registered
	 * or deregistered.
	 * @param listener the team resource change listener to be added
	 */
	public void addTeamResourceChangeListener(ITeamResourceChangeListener listener);

	/**
	 * Remove a previously added change listener
	 * @param listener the team resource change listener to be removed
	 */
	public void removeTeamResourceChangeListener(ITeamResourceChangeListener listener);
}
