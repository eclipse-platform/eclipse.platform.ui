/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.activities;

import java.util.Collection;

/**
 * IObjectActivityManager is the interface for using activities in the UI.
 * 
 * Example of populating the IObjectActivityManager for an extension point:
 * 
 * //add all contributions to the manager
 * 	ObjectActivityManager objectManager =
 * 		getWorkspace().getActivityManager(CONTRIBUTION_ID, true);
 * 	for (Iterator i = entries.iterator(); i.hasNext();) {
 * 		//IModel is only an example interface.
 * 		IModel next = (IModel) i.next();
 * 		objectManager.addObject(next.getPluginId(), next.getId(), next);
 * 		}
 * 	}
 * 	objectManager.applyPatternBindings();
 * 
 * Example of lookup of mappings:
 * 
 *  //get all contributions from the manager
 * 	ObjectActivityManager objectManager =
 * 		getWorkspace().getActivityManager(CONTRIBUTION_ID, false);//Do not create on lookup
 * 
 * if(objectManager == null)
 * 		return;
 * 
 * Collection entries = objectManager.getActiveObjects();
 * for (Iterator i = entries.iterator(); i.hasNext();) {
 * 		//IModel is only an example interface.
 * 		IModel next = (IModel) i.next();
 * 	}
 * 	objectManager.applyPatternBindings();
 * 
 * <b> NOTE: This is experimental API and subject to change </b>
 * @since 3.0
 */
public interface IObjectActivityManager {
	/**
	 * Adds a binding between object-&gt;activity. If the given activity is not
	 * defined in the RoleManager registry then no action is taken.
	 * 
	 * @param objectId
	 * @param activityId
	 * @since 3.0
	 */
	public abstract void addActivityBinding(IObjectContributionRecord record, String activityId);
	/**
	 * Add a given id-&gt;object mapping. A given object should be added to the
	 * reciever only once.
	 * 
	 * @param pluginId
	 *           The plugin id
	 * @param localId
	 *           The local id
	 * @param object
	 *           The object being added
	 * @return the ObjectContributionRecord that was used as a key to store the
	 *         provided object.
	 * @since 3.0
	 */
	public abstract IObjectContributionRecord addObject(String pluginId, String localId, Object object);
	/**
	 * Return a set of objects that are currently valid based on the active
	 * activities, or all objects if role filtering is currently disabled.
	 * 
	 * @return Collection
	 * @since 3.0
	 */
	public abstract Collection getActiveObjects();
	/**
	 * Set the enablement state for all activities bound to the given object.
	 * Useful for "turning on" activities based on key-points in the UI (ie:
	 * the New wizard).
	 * 
	 * @param objectOfInterest
	 *           The Object to enable or disable.
	 * @param enablement
	 * @since 3.0
	 */
	public abstract void setEnablementFor(Object objectOfInterest, boolean enablement);

	/**
	 * Apply default pattern bindings to all of the objects governed by the
	 * receiver.
	 * 
	 * @since 3.0
	 */
	public void applyPatternBindings();

	/**
	 * Apply default pattern bindings based on the provided
	 * ObjectContributionRecord that is governed by the receiver.
	 * 
	 * @param record
	 *           ObjectContributionRecord
	 * @since 3.0
	 */
	public void applyPatternBindings(IObjectContributionRecord record);
}