/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the  accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.contexts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.util.Util;

final class Persistence {
	final static String PACKAGE_BASE = "contexts"; //$NON-NLS-1$
	final static String PACKAGE_FULL = "org.eclipse.ui.contexts"; //$NON-NLS-1$
	final static String PACKAGE_PREFIX = "org.eclipse.ui"; //$NON-NLS-1$
	final static String TAG_CHILD_CONTEXT_ID = "childContextId"; //$NON-NLS-1$		
	final static String TAG_CONTEXT = "context"; //$NON-NLS-1$	
	final static String TAG_CONTEXT_CONTEXT_BINDING = "contextContextBinding"; //$NON-NLS-1$		
	final static String TAG_ID = "id"; //$NON-NLS-1$
	final static String TAG_NAME = "name"; //$NON-NLS-1$	
	final static String TAG_PARENT_CONTEXT_ID = "parentContextId"; //$NON-NLS-1$		
	final static String TAG_PARENT_ID = "parentId"; //$NON-NLS-1$
	final static String TAG_PATTERN = "pattern"; //$NON-NLS-1$	
	final static String TAG_SOURCE_ID = "sourceId"; //$NON-NLS-1$

	static ContextContextBindingDefinition readContextContextBindingDefinition(
		IMemento memento,
		String sourceIdOverride) {
		if (memento == null)
			throw new NullPointerException();

		String childContextId = memento.getString(TAG_CHILD_CONTEXT_ID);
		String parentContextId = memento.getString(TAG_PARENT_CONTEXT_ID);
		String sourceId =
			sourceIdOverride != null
				? sourceIdOverride
				: memento.getString(TAG_SOURCE_ID);
		return new ContextContextBindingDefinition(
			childContextId,
			parentContextId,
			sourceId);
	}

	static List readContextContextBindingDefinitions(
		IMemento memento,
		String name,
		String sourceIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();

		IMemento[] mementos = memento.getChildren(name);

		if (mementos == null)
			throw new NullPointerException();

		List list = new ArrayList(mementos.length);

		for (int i = 0; i < mementos.length; i++)
			list.add(
				readContextContextBindingDefinition(
					mementos[i],
					sourceIdOverride));

		return list;
	}

	static ContextDefinition readContextDefinition(
		IMemento memento,
		String sourceIdOverride) {
		if (memento == null)
			throw new NullPointerException();

		String id = memento.getString(TAG_ID);
		String name = memento.getString(TAG_NAME);
		String parentId = memento.getString(TAG_PARENT_ID);
		String sourceId =
			sourceIdOverride != null
				? sourceIdOverride
				: memento.getString(TAG_SOURCE_ID);
		return new ContextDefinition(id, name, parentId, sourceId);
	}

	static List readContextDefinitions(
		IMemento memento,
		String name,
		String sourceIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();

		IMemento[] mementos = memento.getChildren(name);

		if (mementos == null)
			throw new NullPointerException();

		List list = new ArrayList(mementos.length);

		for (int i = 0; i < mementos.length; i++)
			list.add(readContextDefinition(mementos[i], sourceIdOverride));

		return list;
	}

	static void writeContextContextBindingDefinition(
		IMemento memento,
		ContextContextBindingDefinition contextContextBindingDefinition) {
		if (memento == null || contextContextBindingDefinition == null)
			throw new NullPointerException();

		memento.putString(
			TAG_CHILD_CONTEXT_ID,
			contextContextBindingDefinition.getChildContextId());
		memento.putString(
			TAG_PARENT_CONTEXT_ID,
			contextContextBindingDefinition.getParentContextId());
		memento.putString(
			TAG_SOURCE_ID,
			contextContextBindingDefinition.getSourceId());
	}

	static void writeContextContextBindingDefinitions(
		IMemento memento,
		String name,
		List contextContextBindingDefinitions) {
		if (memento == null
			|| name == null
			|| contextContextBindingDefinitions == null)
			throw new NullPointerException();

		contextContextBindingDefinitions =
			new ArrayList(contextContextBindingDefinitions);
		Iterator iterator = contextContextBindingDefinitions.iterator();

		while (iterator.hasNext())
			Util.assertInstance(
				iterator.next(),
				ContextContextBindingDefinition.class);

		iterator = contextContextBindingDefinitions.iterator();

		while (iterator.hasNext())
			writeContextContextBindingDefinition(
				memento.createChild(name),
				(ContextContextBindingDefinition) iterator.next());
	}

	static void writeContextDefinition(
		IMemento memento,
		ContextDefinition contextDefinition) {
		if (memento == null || contextDefinition == null)
			throw new NullPointerException();

		memento.putString(TAG_ID, contextDefinition.getId());
		memento.putString(TAG_NAME, contextDefinition.getName());
		memento.putString(TAG_PARENT_ID, contextDefinition.getParentId());
		memento.putString(TAG_SOURCE_ID, contextDefinition.getSourceId());
	}

	static void writeContextDefinitions(
		IMemento memento,
		String name,
		List contextDefinitions) {
		if (memento == null || name == null || contextDefinitions == null)
			throw new NullPointerException();

		contextDefinitions = new ArrayList(contextDefinitions);
		Iterator iterator = contextDefinitions.iterator();

		while (iterator.hasNext())
			Util.assertInstance(iterator.next(), ContextDefinition.class);

		iterator = contextDefinitions.iterator();

		while (iterator.hasNext())
			writeContextDefinition(
				memento.createChild(name),
				(ContextDefinition) iterator.next());
	}

	private Persistence() {
	}
}
