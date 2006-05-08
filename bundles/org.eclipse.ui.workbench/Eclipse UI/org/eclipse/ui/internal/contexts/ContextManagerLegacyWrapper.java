/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.ui.contexts.ContextManagerEvent;
import org.eclipse.ui.contexts.IContext;
import org.eclipse.ui.contexts.IContextManager;
import org.eclipse.ui.contexts.IContextManagerListener;

/**
 * A wrapper around the new API that supports the old API. This manager also
 * adds support for reading from the registry.
 * 
 * @since 3.1
 */
public final class ContextManagerLegacyWrapper implements
		org.eclipse.core.commands.contexts.IContextManagerListener,
		IContextManager {

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

	private final ContextManager contextManager;

	private List contextManagerListeners;

	/**
	 * Constructs a new instance of <code>MutableContextManager</code>. The
	 * registry is created on the platform's extension registry.
	 * 
	 * @param contextManager
	 *            The manager which will provided the real support; must not be
	 *            <code>null</code>.
	 */
	public ContextManagerLegacyWrapper(ContextManager contextManager) {

		if (contextManager == null) {
			throw new NullPointerException("The context manager cannot be null"); //$NON-NLS-1$
		}

		this.contextManager = contextManager;
		this.contextManager.addContextManagerListener(this);
	}

	public void addContextManagerListener(
			IContextManagerListener contextManagerListener) {
		if (contextManagerListener == null) {
			throw new NullPointerException();
		}

		if (contextManagerListeners == null) {
			contextManagerListeners = new ArrayList();
		}

		if (!contextManagerListeners.contains(contextManagerListener)) {
			contextManagerListeners.add(contextManagerListener);
		}
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
		if (contextManagerEvent == null) {
			throw new NullPointerException();
		}

		if (contextManagerListeners != null) {
			for (int i = 0; i < contextManagerListeners.size(); i++) {
				((IContextManagerListener) contextManagerListeners.get(i))
						.contextManagerChanged(contextManagerEvent);
			}
		}
	}

	public IContext getContext(String contextId) {
		return new ContextLegacyWrapper(contextManager.getContext(contextId),
				contextManager);
	}

	public SortedSet getDefinedContextIds() {
		return new DepthSortedContextIdSet(contextManager
				.getDefinedContextIds());
	}

	public SortedSet getEnabledContextIds() {
		return new DepthSortedContextIdSet(contextManager.getActiveContextIds());
	}

	public void removeContextManagerListener(
			IContextManagerListener contextManagerListener) {
		if (contextManagerListener == null) {
			throw new NullPointerException();
		}

		if (contextManagerListeners != null) {
			contextManagerListeners.remove(contextManagerListener);
		}
	}

	public void setEnabledContextIds(Set enabledContextIds) {
		contextManager.setActiveContextIds(enabledContextIds);
	}
}
