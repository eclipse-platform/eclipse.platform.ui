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
package org.eclipse.ui.internal.activities;

import java.util.Collection;

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
	 * defined in the receivers <code>IActivityManager</code> then no action is taken.
	 * 
	 * @param record
	 *            the contribution record to bind.
	 * @param activityId
	 *            the activity ID to bind to.
	 */
	//public void addActivityBinding(
	//	IObjectContributionRecord record,
	//	String activityId);

	/**
	 * Add a given id-&gt;object mapping. A given object should be added to the
	 * reciever only once.
	 * 
	 * @param pluginId
	 *            The plugin id.
	 * @param localId
	 *            The local id.
	 * @param object
	 *            The object being added.
	 * @return the <code>IObjectContributionRecord</code> that was used as a
	 *         key to store the provided object.
	 */
	public IObjectContributionRecord addObject(
		String pluginId,
		String localId,
		Object object);

	/**
	 * Apply default pattern bindings to all of the objects governed by the
	 * receiver.
	 */
	public void applyPatternBindings();

	/**
	 * Apply default pattern bindings based on the provided
	 * <code>IObjectContributionRecord</code> that is governed by the receiver.
	 * 
	 * @param record
	 *            <code>IObjectContributionRecord</code> the record to apply bindings on.
	 */
	public void applyPatternBindings(IObjectContributionRecord record);

	/**
	 * Find the <code>IObjectContributionRecords</code> that maps to the given object.
	 * 
	 * @param objectOfInterest
	 *            the object to key on.
	 * @return a <code>Collection</code> of <code>IObjectContributionRecord</code>s  that bind to
	 *         the supplied object. Typically this collection will have either
	 *         0 or 1 element in it.
	 */
	//public Collection findObjectContributionRecords(Object objectOfInterest);

	/**
	 * Return a <code>Set</code> of objects that are currently valid based on the enabled
	 * activities.  This <code>Set</code> is read only.
	 * 
	 * @return the <code>Collection</code> of active objects.
	 */
	public Collection getEnabledObjects();

	/**
	 * Get the <code>Set<code> of <code>IObjectContributionRecord</code> keys from the object store.
	 * This <code>Set</code> is read only.
	 * 
	 * @return the <code>Set</code> of keys.
	 */
	//public Set getObjectIds();

	/**
	 * Removes from the collection the object matching the given contribution
	 * record.
	 * 
	 * @param record
	 *            the <code>IObjectContributionRecord</code> of the object to remove.
	 */
	//public void removeObject(IObjectContributionRecord record);

	/**
	 * Set the enablement state for all activities bound to the given objects
	 * based on key-points in the UI (ie: the New wizard).
	 * 
	 * @param objectOfInterest
	 *            the object to enable or disable.
	 * @param enablement
	 *            the enablment state to grant to all matching activities.
	 */
	public void setEnablementFor(
		Object objectOfInterest,
		boolean enablement);
}