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

package org.eclipse.ui.internal.activities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.util.Util;

final class Persistence {
	final static String PACKAGE_BASE = "activities"; //$NON-NLS-1$
	final static String PACKAGE_FULL = "org.eclipse.ui.activities"; //$NON-NLS-1$
	final static String PACKAGE_PREFIX = "org.eclipse.ui"; //$NON-NLS-1$
	final static String TAG_ACTIVITY = "activity"; //$NON-NLS-1$	
	final static String TAG_ACTIVITY_ACTIVITY_BINDING = "activityActivityBinding"; //$NON-NLS-1$
	final static String TAG_DEFAULT_ENABLEMENT = "defaultEnablement"; //$NON-NLS-1$
	final static String TAG_ACTIVITY_ID = "activityId"; //$NON-NLS-1$	
	final static String TAG_ACTIVITY_PATTERN_BINDING = "activityPatternBinding"; //$NON-NLS-1$	
	final static String TAG_CATEGORY = "category"; //$NON-NLS-1$	
	final static String TAG_CATEGORY_ACTIVITY_BINDING = "categoryActivityBinding"; //$NON-NLS-1$	
	final static String TAG_CATEGORY_ID = "categoryId"; //$NON-NLS-1$
	final static String TAG_CHILD_ACTIVITY_ID = "childActivityId"; //$NON-NLS-1$		
	final static String TAG_ID = "id"; //$NON-NLS-1$
	final static String TAG_NAME = "name"; //$NON-NLS-1$	
	final static String TAG_PARENT_ACTIVITY_ID = "parentActivityId"; //$NON-NLS-1$
	final static String TAG_PATTERN = "pattern"; //$NON-NLS-1$	
	final static String TAG_PLUGIN_ID = "pluginId"; //$NON-NLS-1$
	final static String TAG_DESCRIPTION = "description"; //$NON-NLS-1$

	static ActivityActivityBindingDefinition readActivityActivityBindingDefinition(
		IMemento memento,
		String pluginIdOverride) {
		if (memento == null)
			throw new NullPointerException();

		String childActivityId = memento.getString(TAG_CHILD_ACTIVITY_ID);
		String parentActivityId = memento.getString(TAG_PARENT_ACTIVITY_ID);
		String pluginId =
			pluginIdOverride != null
				? pluginIdOverride
				: memento.getString(TAG_PLUGIN_ID);
		return new ActivityActivityBindingDefinition(
			childActivityId,
			parentActivityId,
			pluginId);
	}

	static List readActivityActivityBindingDefinitions(
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
				readActivityActivityBindingDefinition(
					mementos[i],
					pluginIdOverride));

		return list;
	}
	
    static String readDefaultEnablement(
        IMemento memento) {
		if (memento == null)
			throw new NullPointerException();

		return memento.getString(TAG_ID);
    }
	

	static ActivityDefinition readActivityDefinition(
		IMemento memento,
		String pluginIdOverride) {
		if (memento == null)
			throw new NullPointerException();

		String id = memento.getString(TAG_ID);
		String name = memento.getString(TAG_NAME);
		String description = memento.getString(TAG_DESCRIPTION);
		String pluginId =
			pluginIdOverride != null
				? pluginIdOverride
				: memento.getString(TAG_PLUGIN_ID);
		return new ActivityDefinition(id, name, pluginId, description);
	}

	static List readActivityDefinitions(
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
			list.add(readActivityDefinition(mementos[i], pluginIdOverride));

		return list;
	}

	static ActivityPatternBindingDefinition readActivityPatternBindingDefinition(
		IMemento memento,
		String pluginIdOverride) {
		if (memento == null)
			throw new NullPointerException();

		String activityId = memento.getString(TAG_ACTIVITY_ID);
		String pattern = memento.getString(TAG_PATTERN);
		String pluginId =
			pluginIdOverride != null
				? pluginIdOverride
				: memento.getString(TAG_PLUGIN_ID);
		return new ActivityPatternBindingDefinition(
			activityId,
			pattern,
			pluginId);
	}

	static List readActivityPatternBindingDefinitions(
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
				readActivityPatternBindingDefinition(
					mementos[i],
					pluginIdOverride));

		return list;
	}

	static CategoryActivityBindingDefinition readCategoryActivityBindingDefinition(
		IMemento memento,
		String pluginIdOverride) {
		if (memento == null)
			throw new NullPointerException();

		String activityId = memento.getString(TAG_ACTIVITY_ID);
		String categoryId = memento.getString(TAG_CATEGORY_ID);
		String pluginId =
			pluginIdOverride != null
				? pluginIdOverride
				: memento.getString(TAG_PLUGIN_ID);
		return new CategoryActivityBindingDefinition(
			activityId,
			categoryId,
			pluginId);
	}

	static List readCategoryActivityBindingDefinitions(
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
				readCategoryActivityBindingDefinition(
					mementos[i],
					pluginIdOverride));

		return list;
	}

	static CategoryDefinition readCategoryDefinition(
		IMemento memento,
		String pluginIdOverride) {
		if (memento == null)
			throw new NullPointerException();

		String id = memento.getString(TAG_ID);
		String name = memento.getString(TAG_NAME);
		String description = memento.getString(TAG_DESCRIPTION);
		String pluginId =
			pluginIdOverride != null
				? pluginIdOverride
				: memento.getString(TAG_PLUGIN_ID);
		return new CategoryDefinition(id, name, pluginId, description);
	}

	static List readCategoryDefinitions(
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
			list.add(readCategoryDefinition(mementos[i], pluginIdOverride));

		return list;
	}

	static void writeActivityActivityBindingDefinition(
		IMemento memento,
		ActivityActivityBindingDefinition activityActivityBindingDefinition) {
		if (memento == null || activityActivityBindingDefinition == null)
			throw new NullPointerException();

		memento.putString(
			TAG_CHILD_ACTIVITY_ID,
			activityActivityBindingDefinition.getChildActivityId());
		memento.putString(
			TAG_PARENT_ACTIVITY_ID,
			activityActivityBindingDefinition.getParentActivityId());
		memento.putString(
			TAG_PLUGIN_ID,
			activityActivityBindingDefinition.getPluginId());
	}

	static void writeActivityActivityBindingDefinitions(
		IMemento memento,
		String name,
		List activityActivityBindingDefinitions) {
		if (memento == null
			|| name == null
			|| activityActivityBindingDefinitions == null)
			throw new NullPointerException();

		activityActivityBindingDefinitions =
			new ArrayList(activityActivityBindingDefinitions);
		Iterator iterator = activityActivityBindingDefinitions.iterator();

		while (iterator.hasNext())
			Util.assertInstance(
				iterator.next(),
				ActivityActivityBindingDefinition.class);

		iterator = activityActivityBindingDefinitions.iterator();

		while (iterator.hasNext())
			writeActivityActivityBindingDefinition(
				memento.createChild(name),
				(ActivityActivityBindingDefinition) iterator.next());
	}

	static void writeActivityDefinition(
		IMemento memento,
		ActivityDefinition activityDefinition) {
		if (memento == null || activityDefinition == null)
			throw new NullPointerException();

		memento.putString(TAG_ID, activityDefinition.getId());
		memento.putString(TAG_NAME, activityDefinition.getName());
		memento.putString(TAG_PLUGIN_ID, activityDefinition.getPluginId());
	}

	static void writeActivityDefinitions(
		IMemento memento,
		String name,
		List activityDefinitions) {
		if (memento == null || name == null || activityDefinitions == null)
			throw new NullPointerException();

		activityDefinitions = new ArrayList(activityDefinitions);
		Iterator iterator = activityDefinitions.iterator();

		while (iterator.hasNext())
			Util.assertInstance(iterator.next(), ActivityDefinition.class);

		iterator = activityDefinitions.iterator();

		while (iterator.hasNext())
			writeActivityDefinition(
				memento.createChild(name),
				(ActivityDefinition) iterator.next());
	}

	static void writeActivityPatternBindingDefinition(
		IMemento memento,
		ActivityPatternBindingDefinition activityPatternBindingDefinition) {
		if (memento == null || activityPatternBindingDefinition == null)
			throw new NullPointerException();

		memento.putString(
			TAG_ACTIVITY_ID,
			activityPatternBindingDefinition.getActivityId());
		memento.putString(
			TAG_PATTERN,
			activityPatternBindingDefinition.getPattern());
		memento.putString(
			TAG_PLUGIN_ID,
			activityPatternBindingDefinition.getPluginId());
	}

	static void writeActivityPatternBindingDefinitions(
		IMemento memento,
		String name,
		List activityPatternBindingDefinitions) {
		if (memento == null
			|| name == null
			|| activityPatternBindingDefinitions == null)
			throw new NullPointerException();

		activityPatternBindingDefinitions =
			new ArrayList(activityPatternBindingDefinitions);
		Iterator iterator = activityPatternBindingDefinitions.iterator();

		while (iterator.hasNext())
			Util.assertInstance(
				iterator.next(),
				ActivityPatternBindingDefinition.class);

		iterator = activityPatternBindingDefinitions.iterator();

		while (iterator.hasNext())
			writeActivityPatternBindingDefinition(
				memento.createChild(name),
				(ActivityPatternBindingDefinition) iterator.next());
	}

	static void writeCategoryActivityBindingDefinition(
		IMemento memento,
		CategoryActivityBindingDefinition categoryActivityBindingDefinition) {
		if (memento == null || categoryActivityBindingDefinition == null)
			throw new NullPointerException();

		memento.putString(
			TAG_ACTIVITY_ID,
			categoryActivityBindingDefinition.getActivityId());
		memento.putString(
			TAG_CATEGORY_ID,
			categoryActivityBindingDefinition.getCategoryId());
		memento.putString(
			TAG_PLUGIN_ID,
			categoryActivityBindingDefinition.getPluginId());
	}

	static void writeCategoryActivityBindingDefinitions(
		IMemento memento,
		String name,
		List categoryActivityBindingDefinitions) {
		if (memento == null
			|| name == null
			|| categoryActivityBindingDefinitions == null)
			throw new NullPointerException();

		categoryActivityBindingDefinitions =
			new ArrayList(categoryActivityBindingDefinitions);
		Iterator iterator = categoryActivityBindingDefinitions.iterator();

		while (iterator.hasNext())
			Util.assertInstance(
				iterator.next(),
				CategoryActivityBindingDefinition.class);

		iterator = categoryActivityBindingDefinitions.iterator();

		while (iterator.hasNext())
			writeCategoryActivityBindingDefinition(
				memento.createChild(name),
				(CategoryActivityBindingDefinition) iterator.next());
	}

	static void writeCategoryDefinition(
		IMemento memento,
		CategoryDefinition categoryDefinition) {
		if (memento == null || categoryDefinition == null)
			throw new NullPointerException();

		memento.putString(TAG_ID, categoryDefinition.getId());
		memento.putString(TAG_NAME, categoryDefinition.getName());
		memento.putString(TAG_PLUGIN_ID, categoryDefinition.getPluginId());
	}

	static void writeCategoryDefinitions(
		IMemento memento,
		String name,
		List categoryDefinitions) {
		if (memento == null || name == null || categoryDefinitions == null)
			throw new NullPointerException();

		categoryDefinitions = new ArrayList(categoryDefinitions);
		Iterator iterator = categoryDefinitions.iterator();

		while (iterator.hasNext())
			Util.assertInstance(iterator.next(), CategoryDefinition.class);

		iterator = categoryDefinitions.iterator();

		while (iterator.hasNext())
			writeCategoryDefinition(
				memento.createChild(name),
				(CategoryDefinition) iterator.next());
	}

	private Persistence() {
	    //no-op
	}
}
