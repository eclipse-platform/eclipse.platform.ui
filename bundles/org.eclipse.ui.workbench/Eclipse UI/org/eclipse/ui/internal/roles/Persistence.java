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
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.util.Util;

final class Persistence {

	final static String PACKAGE_BASE = "roles"; //$NON-NLS-1$
	final static String PACKAGE_PREFIX = "org.eclipse.ui"; //$NON-NLS-1$	
	final static String PACKAGE_FULL = PACKAGE_PREFIX + '.' + PACKAGE_BASE;
	final static String TAG_ACTIVITY_BINDING = "activityBinding"; //$NON-NLS-1$	
	final static String TAG_ACTIVITY_ID = "activityId"; //$NON-NLS-1$	
	final static String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	final static String TAG_ID = "id"; //$NON-NLS-1$
	final static String TAG_NAME = "name"; //$NON-NLS-1$	
	final static String TAG_PLUGIN_ID = "pluginId"; //$NON-NLS-1$
	final static String TAG_ROLE = "role"; //$NON-NLS-1$	
	final static String TAG_ROLE_ID = "roleId"; //$NON-NLS-1$	

	static IActivityBindingDefinition readActivityBindingDefinition(
		IMemento memento,
		String pluginIdOverride) {
		if (memento == null)
			throw new NullPointerException();

		String activityId = memento.getString(TAG_ACTIVITY_ID);
		String pluginId =
			pluginIdOverride != null
				? pluginIdOverride
				: memento.getString(TAG_PLUGIN_ID);
		String roleId = memento.getString(TAG_ROLE_ID);
		return new ActivityBindingDefinition(activityId, pluginId, roleId);
	}

	static List readActivityBindingDefinitions(
		IMemento memento,
		String name,
		String pluginIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();

		IMemento[] mementos = memento.getChildren(name);

		if (mementos == null)
			throw new NullPointerException();

		List list = new ArrayList(mementos.length);

		for (int i = 0; i < mementos.length; i++)
			list.add(
				readActivityBindingDefinition(mementos[i], pluginIdOverride));

		return list;
	}

	static ICategoryDefinition readRoleDefinition(
		IMemento memento,
		String pluginIdOverride) {
		if (memento == null)
			throw new NullPointerException();

		String description = memento.getString(TAG_DESCRIPTION);
		String id = memento.getString(TAG_ID);
		String name = memento.getString(TAG_NAME);
		String pluginId =
			pluginIdOverride != null
				? pluginIdOverride
				: memento.getString(TAG_PLUGIN_ID);
		return new CategoryDefinition(description, id, name, pluginId);
	}

	static List readRoleDefinitions(
		IMemento memento,
		String name,
		String pluginIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();

		IMemento[] mementos = memento.getChildren(name);

		if (mementos == null)
			throw new NullPointerException();

		List list = new ArrayList(mementos.length);

		for (int i = 0; i < mementos.length; i++)
			list.add(readRoleDefinition(mementos[i], pluginIdOverride));

		return list;
	}

	static void writeActivityBindingDefinition(
		IMemento memento,
		IActivityBindingDefinition activityBindingDefinition) {
		if (memento == null || activityBindingDefinition == null)
			throw new NullPointerException();

		memento.putString(
			TAG_ACTIVITY_ID,
			activityBindingDefinition.getActivityId());
		memento.putString(
			TAG_PLUGIN_ID,
			activityBindingDefinition.getPluginId());
		memento.putString(TAG_ROLE_ID, activityBindingDefinition.getRoleId());
	}

	static void writeActivityBindingDefinitions(
		IMemento memento,
		String name,
		List activityBindingDefinitions) {
		if (memento == null
			|| name == null
			|| activityBindingDefinitions == null)
			throw new NullPointerException();

		activityBindingDefinitions = new ArrayList(activityBindingDefinitions);
		Iterator iterator = activityBindingDefinitions.iterator();

		while (iterator.hasNext())
			Util.assertInstance(
				iterator.next(),
				IActivityBindingDefinition.class);

		iterator = activityBindingDefinitions.iterator();

		while (iterator.hasNext())
			writeActivityBindingDefinition(
				memento.createChild(name),
				(IActivityBindingDefinition) iterator.next());
	}

	static void writeRoleDefinition(
		IMemento memento,
		ICategoryDefinition roleDefinition) {
		if (memento == null || roleDefinition == null)
			throw new NullPointerException();

		memento.putString(TAG_DESCRIPTION, roleDefinition.getDescription());
		memento.putString(TAG_ID, roleDefinition.getId());
		memento.putString(TAG_NAME, roleDefinition.getName());
		memento.putString(TAG_PLUGIN_ID, roleDefinition.getPluginId());
	}

	static void writeRoleDefinitions(
		IMemento memento,
		String name,
		List roleDefinitions) {
		if (memento == null || name == null || roleDefinitions == null)
			throw new NullPointerException();

		roleDefinitions = new ArrayList(roleDefinitions);
		Iterator iterator = roleDefinitions.iterator();

		while (iterator.hasNext())
			Util.assertInstance(iterator.next(), ICategoryDefinition.class);

		iterator = roleDefinitions.iterator();

		while (iterator.hasNext())
			writeRoleDefinition(
				memento.createChild(name),
				(ICategoryDefinition) iterator.next());
	}

	private Persistence() {
	}
}
