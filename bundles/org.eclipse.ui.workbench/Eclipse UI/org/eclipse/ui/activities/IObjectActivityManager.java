/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.activities;

import java.util.Collection;
import java.util.Set;

/**
 * <p>
 * IObjectActivityManager is the interface for using activities in the UI.
 * 
 * Example of populating the IObjectActivityManager for an extension point:
 * </p>
 * 
 * <code>
 *  //add all contributions to the manager 
 *  ObjectActivityManager objectManager = getWorkspace().getObjectActivityManager(CONTRIBUTION_ID, true); 
 *  for (Iterator i = entries.iterator(); i.hasNext();) { 
 *      //IModel is only an example interface. 
 *      IModel next = (IModel) i.next(); 
 *      objectManager.addObject(next.getPluginId(), next.getId(), next); } 
 *  } 
 *  objectManager.applyPatternBindings();
 * </code>
 * 
 * <p>
 * Example of lookup of mappings:
 * </p>
 * 
 * <code>
 *  //get all contributions from the manager 
 *  ObjectActivityManager objectManager = getWorkspace().getObjectActivityManager(CONTRIBUTION_ID, false);
 * 
 * //Do not create on lookup
 *  if(objectManager == null) 
 *      return;
 * 
 * Collection entries = objectManager.getActiveObjects(); 
 *  for (Iterator i = entries.iterator(); i.hasNext();) { 
 *      //IModel is only an example interface. 
 *      IModel next = (IModel) i.next(); 
 *  } 
 *  objectManager.applyPatternBindings();
 * </code>
 * 
 * <p>
 * Please note that the default implementation of this interface assumes that a
 * user will not add an object without (either immediately, or after a batch
 * operation) applying pattern bindings.
 * </p>
 * 
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * <b>NOTE: This is experimental API and subject to change</b>
 * 
 * @since 3.0
 */
public interface IObjectActivityManager {

	//Return the pattern for enabled for all plugins.
	public static String ENABLED_ALL_PATTERN = "*"; //$NON-NLS-1$

	/**
	 * Adds a binding between object-&gt;activity. If the given activity is not
	 * defined in the RoleManager registry then no action is taken.
	 * 
	 * @param record
	 *            the contribution record to bind.
	 * @param activityId
	 *            the activity ID to bind to.
	 * @since 3.0
	 */
	public abstract void addActivityBinding(
		IObjectContributionRecord record,
		String activityId);

	/**
	 * Add a given id-&gt;object mapping. A given object should be added to the
	 * reciever only once.
	 * 
	 * @param pluginId
	 *            The plugin id
	 * @param localId
	 *            The local id
	 * @param object
	 *            The object being added
	 * @return the <code>IObjectContributionRecord</code> that was used as a
	 *         key to store the provided object.
	 * @since 3.0
	 */
	public abstract IObjectContributionRecord addObject(
		String pluginId,
		String localId,
		Object object);

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
	 *            IObjectContributionRecord
	 * @since 3.0
	 */
	public void applyPatternBindings(IObjectContributionRecord record);

	/**
	 * Find the IObjectContributionRecords that maps to the given object, or
	 * null.
	 * 
	 * @param objectOfInterest
	 *            the object to key on.
	 * @return a Collection of IObjectContributionRecord objects that bind to
	 *         the supplied object. Typically this collection will have either
	 *         0 or 1 element in it.
	 * @since 3.0
	 */
	public Collection findObjectContributionRecords(Object objectOfInterest);

	/**
	 * Return a set of objects that are currently valid based on the active
	 * activities, or all objects if role filtering is currently disabled.
	 * 
	 * @return the collection of active objects.
	 * @since 3.0
	 */
	public abstract Collection getActiveObjects();

	/**
	 * Get the Set of IObjectContributionRecord keys from the object store.
	 * This Set is read only.
	 * 
	 * @return the set of keys.
	 * @since 3.0
	 */
	public Set getObjectIds();

	/**
	 * Removes from the collection the object matching the given contribution
	 * record.
	 * 
	 * @param record
	 *            IObjectContributionRecord the record of the object to remove.
	 * @since 3.0
	 */
	public void removeObject(IObjectContributionRecord record);

	/**
	 * Set the enablement state for all activities bound to the given objects
	 * based on key-points in the UI (ie: the New wizard).
	 * 
	 * @param objectOfInterest
	 *            the Object to enable or disable.
	 * @param enablement
	 *            the enablment state to grant to all matching activities.
	 * @since 3.0
	 */
	public abstract void setEnablementFor(
		Object objectOfInterest,
		boolean enablement);
}