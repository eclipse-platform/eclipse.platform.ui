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
	final static String PACKAGE_PREFIX = "org.eclipse.ui"; //$NON-NLS-1$	
	final static String PACKAGE_FULL = PACKAGE_PREFIX + '.' + PACKAGE_BASE;
	final static String TAG_ACTIVITY = "activity"; //$NON-NLS-1$	
	final static String TAG_ACTIVITY_ID = "activityId"; //$NON-NLS-1$	
	final static String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	final static String TAG_ID = "id"; //$NON-NLS-1$
	final static String TAG_INCLUSIVE = "inclusive"; //$NON-NLS-1$
	final static String TAG_NAME = "name"; //$NON-NLS-1$	
	final static String TAG_PARENT_ID = "parentId"; //$NON-NLS-1$
	final static String TAG_PATTERN = "pattern"; //$NON-NLS-1$	
	final static String TAG_PATTERN_BINDING = "patternBinding"; //$NON-NLS-1$	
	final static String TAG_PLUGIN_ID = "pluginId"; //$NON-NLS-1$

	static IActivityDefinition readActivityDefinition(IMemento memento, String pluginIdOverride) {
		if (memento == null)
			throw new NullPointerException();			

		String description = memento.getString(TAG_DESCRIPTION);
		String id = memento.getString(TAG_ID);
		String name = memento.getString(TAG_NAME);
		String parentId = memento.getString(TAG_PARENT_ID);
		String pluginId = pluginIdOverride != null ? pluginIdOverride : memento.getString(TAG_PLUGIN_ID);
		return new ActivityDefinition(description, id, name, parentId, pluginId);
	}

	static List readActivityDefinitions(IMemento memento, String name, String pluginIdOverride) {
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
	
	static IPatternBindingDefinition readPatternBindingDefinition(IMemento memento, String pluginIdOverride) {
		if (memento == null)
			throw new NullPointerException();			

		String activityId = memento.getString(TAG_ACTIVITY_ID);
		boolean inclusive = Boolean.valueOf(memento.getString(TAG_INCLUSIVE)).booleanValue();
		String pattern = memento.getString(TAG_PATTERN);
		String pluginId = pluginIdOverride != null ? pluginIdOverride : memento.getString(TAG_PLUGIN_ID);
		return new PatternBindingDefinition(activityId, inclusive, pattern, pluginId);
	}

	static List readPatternBindingDefinitions(IMemento memento, String name, String pluginIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new NullPointerException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readPatternBindingDefinition(mementos[i], pluginIdOverride));
	
		return list;				
	}

	static void writeActivityDefinition(IMemento memento, IActivityDefinition activityDefinition) {
		if (memento == null || activityDefinition == null)
			throw new NullPointerException();

		memento.putString(TAG_DESCRIPTION, activityDefinition.getDescription());
		memento.putString(TAG_ID, activityDefinition.getId());
		memento.putString(TAG_NAME, activityDefinition.getName());
		memento.putString(TAG_PARENT_ID, activityDefinition.getParentId());
		memento.putString(TAG_PLUGIN_ID, activityDefinition.getPluginId());
	}

	static void writeActivityDefinitions(IMemento memento, String name, List activityDefinitions) {
		if (memento == null || name == null || activityDefinitions == null)
			throw new NullPointerException();
		
		activityDefinitions = new ArrayList(activityDefinitions);
		Iterator iterator = activityDefinitions.iterator();

		while (iterator.hasNext())
			Util.assertInstance(iterator.next(), IActivityDefinition.class);

		iterator = activityDefinitions.iterator();

		while (iterator.hasNext()) 
			writeActivityDefinition(memento.createChild(name), (IActivityDefinition) iterator.next());
	}	
	
	static void writePatternBindingDefinition(IMemento memento, IPatternBindingDefinition patternBindingDefinition) {
		if (memento == null || patternBindingDefinition == null)
			throw new NullPointerException();

		memento.putString(TAG_ACTIVITY_ID, patternBindingDefinition.getActivityId());
		memento.putString(TAG_INCLUSIVE, Boolean.toString(patternBindingDefinition.isInclusive()));
		memento.putString(TAG_PATTERN, patternBindingDefinition.getActivityId());
		memento.putString(TAG_PLUGIN_ID, patternBindingDefinition.getPluginId());
	}

	static void writePatternBindingDefinitions(IMemento memento, String name, List patternBindingDefinitions) {
		if (memento == null || name == null || patternBindingDefinitions == null)
			throw new NullPointerException();
		
		patternBindingDefinitions = new ArrayList(patternBindingDefinitions);
		Iterator iterator = patternBindingDefinitions.iterator();

		while (iterator.hasNext())
			Util.assertInstance(iterator.next(), IPatternBindingDefinition.class);

		iterator = patternBindingDefinitions.iterator();

		while (iterator.hasNext()) 
			writePatternBindingDefinition(memento.createChild(name), (IPatternBindingDefinition) iterator.next());
	}

	private Persistence() {
	}	
}
