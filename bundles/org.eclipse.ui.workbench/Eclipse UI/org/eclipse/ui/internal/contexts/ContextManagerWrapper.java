/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.contexts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.contexts.ContextManagerEvent;
import org.eclipse.ui.contexts.IContext;
import org.eclipse.ui.contexts.IContextManagerListener;
import org.eclipse.ui.internal.util.Util;

/**
 * A wrapper around the new API that supports the old API. This manager also
 * adds support for reading from the registry.
 * 
 * @since 3.1
 */
public final class ContextManagerWrapper implements
		org.eclipse.core.commands.contexts.IContextManagerListener,
		IMutableContextManager {

	/**
	 * A comparator between context identifiers, that sorts them based on depth
	 * within the tree. Context identifiers representing deeper items (i.e.,
	 * items with more ancestors), have lesser values (i.e., would appear
	 * earlier in a set).
	 * 
	 * @since 3.0
	 */
	private class ContextIdDepthComparator implements Comparator {

		/**
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public final int compare(final Object object1, final Object object2) {
			final String contextId1 = (String) object1;
			final String contextId2 = (String) object2;
			Context context;
			String parentId;

			// Get the depth of the first context.
			int depth1 = 0;
			context = contextManager.getContext(contextId1);
			try {
				parentId = context.getParentId();
				while (parentId != null) {
					depth1++;
					context = contextManager.getContext(parentId);
					parentId = context.getParentId();
				}
			} catch (final NotDefinedException e) {
				// Do nothing. Stop ascending the ancestry.
			}

			// Get the depth of the second context.
			int depth2 = 0;
			context = contextManager.getContext(contextId2);
			try {
				parentId = context.getParentId();
				while (parentId != null) {
					depth2++;
					context = contextManager.getContext(parentId);
					parentId = context.getParentId();
				}
			} catch (final NotDefinedException e) {
				// Do nothing. Stop ascending the ancestry.
			}

			// If the contexts are equal depth, then use their identifier.
			int compare = depth2 - depth1;
			if (compare == 0) {
				compare = contextId1.compareTo(contextId2);
			}

			return compare;
		}
	}

	/**
	 * A set that contains context identifiers (strings). The set is sorted
	 * based on how many ancestors the corresponding contexts have. Contexts
	 * with no parents appear last, while contexts with the most ancestors
	 * appear first.
	 * 
	 * @since 3.0
	 */
	private class DepthSortedContextIdSet extends TreeSet {

		/**
		 * Generated serial version UID for this class.
		 * 
		 * @since 3.1
		 */
		private static final long serialVersionUID = 3257291326872892465L;

		/**
		 * Constructs a new instance of <code>DepthSortedContextIdSet</code>
		 * with the set to be sorted.
		 * 
		 * @param contextIds
		 *            A set of context identifiers (strings); this may contain
		 *            <code>null</code> values. The set may not be
		 *            <code>null</code>, but may be empty.
		 */
		private DepthSortedContextIdSet(final Set contextIds) {
			super(new ContextIdDepthComparator());
			addAll(contextIds);
		}
	}

	static boolean isContextDefinitionChildOf(String ancestor, String id,
			Map contextDefinitionsById) {
		Collection visited = new HashSet();

		while (id != null && !visited.contains(id)) {
			ContextDefinition contextDefinition = (ContextDefinition) contextDefinitionsById
					.get(id);
			visited.add(id);

			if (contextDefinition != null
					&& Util.equals(id = contextDefinition.getParentId(),
							ancestor))
				return true;
		}

		return false;
	}

	private final ContextManager contextManager;

	private List contextManagerListeners;

	private IContextRegistry contextRegistry;

	/**
	 * Constructs a new instance of <code>MutableContextManager</code>. The
	 * registry is created on the platform's extension registry.
	 * 
	 * @param contextManager
	 *            The manager which will provided the real support; must not be
	 *            <code>null</code>.
	 */
	public ContextManagerWrapper(ContextManager contextManager) {
		this(new ExtensionContextRegistry(Platform.getExtensionRegistry()),
				contextManager);
	}

	/**
	 * Constructs a new instance of <code>MutableContextManager</code>.
	 * 
	 * @param contextRegistry
	 *            The registry from which the contexts should be read; must not
	 *            be <code>null</code>.
	 * @param contextManager
	 *            The manager which will provided the real support; must not be
	 *            <code>null</code>.
	 */
	private ContextManagerWrapper(IContextRegistry contextRegistry,
			ContextManager contextManager) {
		if (contextRegistry == null)
			throw new NullPointerException();

		if (contextManager == null) {
			throw new NullPointerException("The context manager cannot be null"); //$NON-NLS-1$
		}

		this.contextRegistry = contextRegistry;
		this.contextManager = contextManager;

		this.contextRegistry
				.addContextRegistryListener(new IContextRegistryListener() {

					public void contextRegistryChanged(
							ContextRegistryEvent contextRegistryEvent) {
						readRegistry();
					}
				});
		this.contextManager.addContextManagerListener(this);

		readRegistry();
	}

	public void addContextManagerListener(
			IContextManagerListener contextManagerListener) {
		if (contextManagerListener == null)
			throw new NullPointerException();

		if (contextManagerListeners == null)
			contextManagerListeners = new ArrayList();

		if (!contextManagerListeners.contains(contextManagerListener))
			contextManagerListeners.add(contextManagerListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.contexts.IContextManagerListener#contextManagerChanged(org.eclipse.core.commands.contexts.ContextManagerEvent)
	 */
	public void contextManagerChanged(
			org.eclipse.core.commands.contexts.ContextManagerEvent contextManagerEvent) {
		final String contextId = contextManagerEvent.getContextId();
		final boolean definedContextsChanged;
		final Set previouslyDefinedContextIds;
		if (contextId == null) {
			definedContextsChanged = false;
			previouslyDefinedContextIds = null;
		} else {
			definedContextsChanged = true;
			previouslyDefinedContextIds = new HashSet();
			previouslyDefinedContextIds.addAll(contextManager
					.getDefinedContextIds());
			if (contextManagerEvent.isContextDefined()) {
				previouslyDefinedContextIds.remove(contextId);
			} else {
				previouslyDefinedContextIds.add(contextId);
			}
		}

		fireContextManagerChanged(new ContextManagerEvent(this,
				definedContextsChanged, contextManagerEvent
						.isActiveContextsChanged(),
				previouslyDefinedContextIds, contextManagerEvent
						.getPreviouslyActiveContextIds()));

	}

	protected void fireContextManagerChanged(
			ContextManagerEvent contextManagerEvent) {
		if (contextManagerEvent == null)
			throw new NullPointerException();

		if (contextManagerListeners != null)
			for (int i = 0; i < contextManagerListeners.size(); i++)
				((IContextManagerListener) contextManagerListeners.get(i))
						.contextManagerChanged(contextManagerEvent);
	}

	public IContext getContext(String contextId) {
		return new ContextWrapper(contextManager.getContext(contextId),
				contextManager);
	}

	public SortedSet getDefinedContextIds() {
		return new DepthSortedContextIdSet(contextManager
				.getDefinedContextIds());
	}

	public SortedSet getEnabledContextIds() {
		return new DepthSortedContextIdSet(contextManager.getActiveContextIds());
	}

	private void readRegistry() {
		Collection contextDefinitions = new ArrayList();
		contextDefinitions.addAll(contextRegistry.getContextDefinitions());
		Map contextDefinitionsById = new HashMap(ContextDefinition
				.contextDefinitionsById(contextDefinitions, false));

		// Remove invalid definitions.
		for (Iterator iterator = contextDefinitionsById.values().iterator(); iterator
				.hasNext();) {
			ContextDefinition contextDefinition = (ContextDefinition) iterator
					.next();
			String name = contextDefinition.getName();

			if (name == null || name.length() == 0)
				iterator.remove();
		}
		for (Iterator iterator = contextDefinitionsById.keySet().iterator(); iterator
				.hasNext();)
			if (!isContextDefinitionChildOf(null, (String) iterator.next(),
					contextDefinitionsById))
				iterator.remove();

		// Copy the definitions into the context manager.
		final Iterator contextDefinitionItr = contextDefinitionsById.values()
				.iterator();
		while (contextDefinitionItr.hasNext()) {
			final ContextDefinition contextDefinition = (ContextDefinition) contextDefinitionItr
					.next();
			final String contextId = contextDefinition.getId();
			final Context context = contextManager.getContext(contextId);
			context.define(contextDefinition.getName(), contextDefinition
					.getDescription(), contextDefinition.getParentId());
		}

		// Determine whether the set of defined contexts changed.
		final boolean definedContextIdsChanged;
		Set definedContextIds = new HashSet(contextDefinitionsById.keySet());
		Set previouslyDefinedContextIds = contextManager.getDefinedContextIds();
		if (definedContextIds.equals(previouslyDefinedContextIds)) {
			previouslyDefinedContextIds = null;
			definedContextIdsChanged = false;
		} else {
			definedContextIdsChanged = true;
		}

		// If changes did occur, then notify listeners.
		if (definedContextIdsChanged)
			fireContextManagerChanged(new ContextManagerEvent(this,
					definedContextIdsChanged, false,
					previouslyDefinedContextIds, null));
	}

	public void removeContextManagerListener(
			IContextManagerListener contextManagerListener) {
		if (contextManagerListener == null)
			throw new NullPointerException();

		if (contextManagerListeners != null)
			contextManagerListeners.remove(contextManagerListener);
	}

	public void setEnabledContextIds(Set enabledContextIds) {
		contextManager.setActiveContextIds(enabledContextIds);
	}
}
