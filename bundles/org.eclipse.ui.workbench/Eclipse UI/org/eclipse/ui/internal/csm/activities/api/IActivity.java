/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.csm.activities.api;

import java.util.Set;

/**
 * <p>
 * JAVADOC
 * </p>
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public interface IActivity extends Comparable {

	/**
	 * Registers an IActivityListener instance with this activity.
	 *
	 * @param activityListener the IActivityListener instance to register.
	 */	
	void addActivityListener(IActivityListener activityListener);

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	String getDescription()
		throws ActivityNotDefinedException;
	
	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	String getId();
	
	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	String getName()
		throws ActivityNotDefinedException;

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	Set getPatternBindings();

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	boolean isActive();

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	boolean isDefined();
	
	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	boolean isEnabled();	
	
	/**
	 * Unregisters an IActivityListener instance with this activity.
	 *
	 * @param activityListener the IActivityListener instance to unregister.
	 */
	void removeActivityListener(IActivityListener activityListener);
}
