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

package org.eclipse.ui.activities;

import java.util.Set;

/**
 * <p>
 * TODO javadoc
 * </p>
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see IActivityServiceListener
 */
public interface IActivityService {

	/**
	 * Registers an instance of <code>IActivityServiceListener</code> to
	 * listen for changes to attributes of this instance.
	 * 
	 * @param activityServiceListener
	 *            the instance of <code>IActivityServiceListener</code> to
	 *            register. Must not be <code>null</code>. If an attempt is
	 *            made to register an instance of <code>IActivityServiceListener</code>
	 *            which is already registered with this instance, or if this
	 *            instance is disposed, no operation is performed.
	 */
	void addActivityServiceListener(IActivityServiceListener activityServiceListener);

	/**
	 * <p>
	 * Returns the set of identifiers to active activities.
	 * </p>
	 * <p>
	 * Notification is set to all registered listeners if this attribute
	 * changes.
	 * </p>
	 * 
	 * @return the set of identifiers to active activities. This set may be
	 *         empty, but is guaranteed not to be <code>null</code>. If this
	 *         set is not empty, it is guaranteed to only contain instances of
	 *         <code>String</code>.
	 */
	Set getActiveActivityIds();

	/**
	 * Unregisters an instance of <code>IActivityServiceListener</code>
	 * listening for changes to attributes of this instance.
	 * 
	 * @param activityServiceListener
	 *            the instance of <code>IActivityServiceListener</code> to
	 *            unregister. Must not be <code>null</code>. If an attempt
	 *            is made to unregister an instance of <code>IActivityServiceListener</code>
	 *            which is not already registered with this instance, or if
	 *            this instance is disposed, no operation is performed.
	 */
	void removeActivityServiceListener(IActivityServiceListener activityServiceListener);
}