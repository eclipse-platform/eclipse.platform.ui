/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
	final static String TAG_ACTIVITY_REQUIREMENT_BINDING = "activityRequirementBinding"; //$NON-NLS-1$
	final static String TAG_DEFAULT_ENABLEMENT = "defaultEnablement"; //$NON-NLS-1$
	final static String TAG_ACTIVITY_ID = "activityId"; //$NON-NLS-1$	
	final static String TAG_ACTIVITY_PATTERN_BINDING = "activityPatternBinding"; //$NON-NLS-1$	
	final static String TAG_CATEGORY = "category"; //$NON-NLS-1$	
	final static String TAG_CATEGORY_ACTIVITY_BINDING = "categoryActivityBinding"; //$NON-NLS-1$	
	final static String TAG_CATEGORY_ID = "categoryId"; //$NON-NLS-1$
	final static String TAG_REQUIRED_ACTIVITY_ID = "requiredActivityId"; //$NON-NLS-1$		
	final static String TAG_ID = "id"; //$NON-NLS-1$
	final static String TAG_NAME = "name"; //$NON-NLS-1$	
	final static String TAG_PATTERN = "pattern"; //$NON-NLS-1$	
	final static String TAG_SOURCE_ID = "sourceId"; //$NON-NLS-1$
	final static String TAG_DESCRIPTION = "description"; //$NON-NLS-1$

	static ActivityRequirementBindingDefinition readActivityRequirementBindingDefinition(
		IMemento memento,
		String sourceIdOverride) {
		if (memento == null)
			throw new NullPointerException();

		String childActivityId = memento.getString(TAG_REQUIRED_ACTIVITY_ID);
		String parentActivityId = memento.getString(TAG_ACTIVITY_ID);
        if (childActivityId == null || parentActivityId == null)
            return null;		
		String sourceId =
			sourceIdOverride != null
				? sourceIdOverride
				: memento.getString(TAG_SOURCE_ID);
		return new ActivityRequirementBindingDefinition(
			childActivityId,
			parentActivityId,
			sourceId);
	}

	static List readActivityRequirementBindingDefinitions(
		IMemento memento,
		String name,
		String sourceIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();

		IMemento[] mementos = memento.getChildren(name);

		if (mementos == null)
			throw new NullPointerException();

		List list = new ArrayList(mementos.length);

        for (int i = 0; i < mementos.length; i++) {
            ActivityRequirementBindingDefinition binding = readActivityRequirementBindingDefinition(mementos[i],
                    sourceIdOverride);
            if (binding != null)
                list.add(binding);
        }

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
		String sourceIdOverride) {
		if (memento == null)
			throw new NullPointerException();

		String id = memento.getString(TAG_ID);
        if (id == null)
            return null;
		String name = memento.getString(TAG_NAME);
        if (name == null)
            return null;		
		String description = memento.getString(TAG_DESCRIPTION);
        if (description == null)
            description = ""; //$NON-NLS-1$

		String sourceId =
			sourceIdOverride != null
				? sourceIdOverride
				: memento.getString(TAG_SOURCE_ID);
		return new ActivityDefinition(id, name, sourceId, description);
	}

	static List readActivityDefinitions(
		IMemento memento,
		String name,
		String sourceIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();

		IMemento[] mementos = memento.getChildren(name);

		if (mementos == null)
			throw new NullPointerException();

		List list = new ArrayList(mementos.length);

        for (int i = 0; i < mementos.length; i++) {
            ActivityDefinition definition = readActivityDefinition(mementos[i], sourceIdOverride);
            if (definition != null)
                list.add(definition);
        }

		return list;
	}

	static ActivityPatternBindingDefinition readActivityPatternBindingDefinition(
		IMemento memento,
		String sourceIdOverride) {
		if (memento == null)
			throw new NullPointerException();

		String activityId = memento.getString(TAG_ACTIVITY_ID);
        if (activityId == null)
            return null;		
		String pattern = memento.getString(TAG_PATTERN);
        if (pattern == null)
            return null;
		String sourceId =
			sourceIdOverride != null
				? sourceIdOverride
				: memento.getString(TAG_SOURCE_ID);
		return new ActivityPatternBindingDefinition(
			activityId,
			pattern,
			sourceId);
	}

	static List readActivityPatternBindingDefinitions(
		IMemento memento,
		String name,
		String sourceIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();

		IMemento[] mementos = memento.getChildren(name);

		if (mementos == null)
			throw new NullPointerException();

		List list = new ArrayList(mementos.length);

        for (int i = 0; i < mementos.length; i++) {
            ActivityPatternBindingDefinition definition = readActivityPatternBindingDefinition(mementos[i],
                    sourceIdOverride);
            if (definition != null)
                list.add(definition);
        }

		return list;
	}

	static CategoryActivityBindingDefinition readCategoryActivityBindingDefinition(
		IMemento memento,
		String sourceIdOverride) {
		if (memento == null)
			throw new NullPointerException();

		String activityId = memento.getString(TAG_ACTIVITY_ID);
        if (activityId == null)
            return null;
        String categoryId = memento.getString(TAG_CATEGORY_ID);
        if (categoryId == null)
            return null;

		String sourceId =
			sourceIdOverride != null
				? sourceIdOverride
				: memento.getString(TAG_SOURCE_ID);
		return new CategoryActivityBindingDefinition(
			activityId,
			categoryId,
			sourceId);
	}

	static List readCategoryActivityBindingDefinitions(
		IMemento memento,
		String name,
		String sourceIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();

		IMemento[] mementos = memento.getChildren(name);

		if (mementos == null)
			throw new NullPointerException();

		List list = new ArrayList(mementos.length);

        for (int i = 0; i < mementos.length; i++) {
            CategoryActivityBindingDefinition definition = readCategoryActivityBindingDefinition(mementos[i],
                    sourceIdOverride);
            if (definition != null)
                list.add(definition);
        }

		return list;
	}

	static CategoryDefinition readCategoryDefinition(
		IMemento memento,
		String sourceIdOverride) {
		if (memento == null)
			throw new NullPointerException();

		String id = memento.getString(TAG_ID);
        if (id == null)
            return null;
        String name = memento.getString(TAG_NAME);
        if (name == null)
            return null;
        String description = memento.getString(TAG_DESCRIPTION);
        if (description == null)
            description = ""; //$NON-NLS-1$
		String sourceId =
			sourceIdOverride != null
				? sourceIdOverride
				: memento.getString(TAG_SOURCE_ID);
		return new CategoryDefinition(id, name, sourceId, description);
	}

	static List readCategoryDefinitions(
		IMemento memento,
		String name,
		String sourceIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();

		IMemento[] mementos = memento.getChildren(name);

		if (mementos == null)
			throw new NullPointerException();

		List list = new ArrayList(mementos.length);

        for (int i = 0; i < mementos.length; i++) {
            CategoryDefinition definition = readCategoryDefinition(mementos[i], sourceIdOverride);
            if (definition != null)
                list.add(definition);
        }

		return list;
	}

	static void writeActivityRequirementBindingDefinition(
		IMemento memento,
		ActivityRequirementBindingDefinition activityRequirementBindingDefinition) {
		if (memento == null || activityRequirementBindingDefinition == null)
			throw new NullPointerException();

		memento.putString(
			TAG_REQUIRED_ACTIVITY_ID,
			activityRequirementBindingDefinition.getRequiredActivityId());
		memento.putString(
				TAG_ACTIVITY_ID,
			activityRequirementBindingDefinition.getActivityId());
		memento.putString(
			TAG_SOURCE_ID,
			activityRequirementBindingDefinition.getSourceId());
	}

	static void writeActivityRequirementBindingDefinitions(
		IMemento memento,
		String name,
		List activityRequirementBindingDefinitions) {
		if (memento == null
			|| name == null
			|| activityRequirementBindingDefinitions == null)
			throw new NullPointerException();

		activityRequirementBindingDefinitions =
			new ArrayList(activityRequirementBindingDefinitions);
		Iterator iterator = activityRequirementBindingDefinitions.iterator();

		while (iterator.hasNext())
			Util.assertInstance(
				iterator.next(),
				ActivityRequirementBindingDefinition.class);

		iterator = activityRequirementBindingDefinitions.iterator();

		while (iterator.hasNext())
			writeActivityRequirementBindingDefinition(
				memento.createChild(name),
				(ActivityRequirementBindingDefinition) iterator.next());
	}

	static void writeActivityDefinition(
		IMemento memento,
		ActivityDefinition activityDefinition) {
		if (memento == null || activityDefinition == null)
			throw new NullPointerException();

		memento.putString(TAG_ID, activityDefinition.getId());
		memento.putString(TAG_NAME, activityDefinition.getName());
		memento.putString(TAG_SOURCE_ID, activityDefinition.getSourceId());
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
			TAG_SOURCE_ID,
			activityPatternBindingDefinition.getSourceId());
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
			TAG_SOURCE_ID,
			categoryActivityBindingDefinition.getSourceId());
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
		memento.putString(TAG_SOURCE_ID, categoryDefinition.getSourceId());
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
