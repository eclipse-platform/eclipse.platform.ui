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

package org.eclipse.ui.internal.roles;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.ui.IMemento;

/**
 * The RoleParser is a class that reads the role memento and 
 * builds a list of roles from it.
 */
final class RoleParser {

	final static String TRUE_STRING = "true"; //$NON-NLS-1$
	final static String TAG_ACTIVITY_PATTERN_BINDING = "activityPatternBinding"; //$NON-NLS-1$
	final static String TAG_PATTERN = "pattern"; //$NON-NLS-1$\
	final static String TAG_ACTIVITY_BINDING = "activityBinding"; //$NON-NLS-1$
	final static String TAG_ID = "id"; //$NON-NLS-1$
	final static String TAG_NAME = "name"; //$NON-NLS-1$
	final static String TAG_VALUE = "value"; //$NON-NLS-1$	
	final static String TAG_ROLE = "role"; //$NON-NLS-1$	
	final static String TAG_ENABLED = "enabled"; //$NON-NLS-1$

	final static String TAG_ACTIVITY = "activity"; //$NON-NLS-1$
	final static String TAG_PARENT = "parent"; //$NON-NLS-1$

	/**
	 * Read an individual role definition from memento and
	 * enable the activites as appropriate.
	 * @param memento
	 * @return
	 */
	private static Role readRoleDefinition(IMemento memento,Hashtable activities) {
		if (memento == null)
			throw new IllegalArgumentException();

		String id = memento.getString(TAG_ID);
		String name = memento.getString(TAG_NAME);
		boolean enabled = TRUE_STRING.equals(memento.getString(TAG_ENABLED));

		String[] activityBindingValues = getValues(memento, TAG_ACTIVITY_BINDING, TAG_ID);

		Role newRole = new Role(name, id, activityBindingValues);
		
		for(int i = 0; i < activityBindingValues.length; i ++){
			Activity activity = (Activity) activities.get(activityBindingValues[i]);
			activity.setEnabled(enabled);
		}
		
		return newRole;
	}

	/**
	 * Get the values for main tag and collect all of the
	 * Strings for the entry called entry tag.
	 * @param memento IMemento
	 * @param mainTag The element tag
	 * @param entryTag The attribute tag
	 * @return
	 */
	private static String[] getValues(IMemento memento, String mainTag, String entryTag) {
		ArrayList activityBindings = new ArrayList();

		IMemento[] children = memento.getChildren(mainTag);
		for (int i = 0; i < children.length; i++) {
			activityBindings.add(children[i].getString(entryTag));
		}

		String[] activityBindingsArray = new String[activityBindings.size()];
		activityBindings.toArray(activityBindingsArray);
		return activityBindingsArray;
	}

	/**
	 * Read and return all of the role definitions in memento.
	 * Enable the activities in the Hastable based on role
	 * enablement.
	 * @param memento
	 * @return Role[]
	 */
	static Role[] readRoleDefinitions(IMemento memento,Hashtable activities) {
		if (memento == null)
			throw new IllegalArgumentException();

		IMemento[] mementos = memento.getChildren(TAG_ROLE);

		if (mementos == null)
			throw new IllegalArgumentException();

		List list = new ArrayList(mementos.length);

		for (int i = 0; i < mementos.length; i++) {
			list.add(readRoleDefinition(mementos[i],activities));
		}

		Role[] roles = new Role[list.size()];
		list.toArray(roles);
		return roles;
	}

	/** 
	* Read and return all fo the activity definitions in memento.
	* @param memento
	* @return Activity[]
	*/
	static Activity[] readActivityDefinitions(IMemento memento) {

		if (memento == null)
			throw new IllegalArgumentException();

		IMemento[] mementos = memento.getChildren(TAG_ACTIVITY);

		if (mementos == null)
			throw new IllegalArgumentException();

		List list = new ArrayList(mementos.length);

		for (int i = 0; i < mementos.length; i++) {
			list.add(readActivityDefinition(mementos[i]));
		}

		Activity[] roles = new Activity[list.size()];
		list.toArray(roles);
		return roles;
	}

	/**
	 * Read an activity definition from the supplied memento.
	 * @param memento
	 * @return
	 */
	private static Activity readActivityDefinition(IMemento memento) {

		String id = memento.getString(TAG_ID);
		String name = memento.getString(TAG_NAME);
		String parent = memento.getString(TAG_PARENT);

		return new Activity(id, name, parent);
	}

	/**
	* Read and return all of the pattern bindings in memento.
    * @param memento
    * @return Activity[]
	*/
	static Hashtable readPatternBindings(IMemento memento) {

		if (memento == null)
			throw new IllegalArgumentException();

		IMemento[] mementos = memento.getChildren(TAG_ACTIVITY_PATTERN_BINDING);

		if (mementos == null)
			throw new IllegalArgumentException();

		Hashtable patterns = new Hashtable();

		for (int i = 0; i < mementos.length; i++) {
			String id = mementos[i].getString(TAG_ID);
			String pattern = mementos[i].getString(TAG_PATTERN);
			patterns.put(pattern,id);
		}

		return patterns;
	}

}
