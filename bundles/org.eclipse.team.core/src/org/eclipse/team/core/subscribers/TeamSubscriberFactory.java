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
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.SaveContext;

/**
 * A subscriber factory is responsible for saving and restoring subscribers. Implementations must 
 * provide a public no-arg constructor.
 * 
 * Example extension point for registering a subscriber factory:
 * 
 * <extension point="org.eclipse.team.core.subscriber">
 *     <subscriber class="org.eclipse.team.internal.webdav.DavSubscriberFactory"/>
 *</extension>
 *
 * @see org.eclipse.team.core.subscribers.TeamSubscriber 
 */
abstract public class TeamSubscriberFactory {
	
	/**
	 * A subscriber factory id identifies the factory type and the type of it's subscribers. Subscribers
	 * created via a specific factory should return a qualified name from TeamSubscriber#getID() that
	 * matches the id of their factory.
	 * <p>
	 * For example, a WebDav subscriber factory would have "org.eclipse.team.webdav.subscriber" as 
	 * its id. Subsequent WebDav subscribers must construct their id based on this qualifier.
	 * 
	 * @return the factory's id 
	 */
	abstract  public String getID();
	
	/** 
	 * Called to save the state of the given subscriber. The saved state should contain enough
	 * information so that a subcriber can be recreated from the returned <code>SaveContext</code>.
	 * A subscriber that doesn't have information to the saved should return <code>null</code>.
	 * <p>
	 * This may be called during workspace snapshot or at shutdown.
	 * </p>
	 * 
	 * @return a save context containing the state of this subscriber 
	 * @throws TeamException if there was a problem creating the save context. 
	 */
	abstract  public SaveContext saveSubscriber(TeamSubscriber subscriber) throws TeamException;
	
	/** 
	 * Called to restore a subscriber with <code>id</code> from a given <code>SaveContext</code>. This is
	 * used to restore subscribers between workbench sessions.
	 * 
	 * @return a subscriber instance 
	 * @throws TeamException if there was a problem restoring from the save context.
	 */
	abstract  public TeamSubscriber restoreSubscriber(QualifiedName id, SaveContext saveContext) throws TeamException;
}
