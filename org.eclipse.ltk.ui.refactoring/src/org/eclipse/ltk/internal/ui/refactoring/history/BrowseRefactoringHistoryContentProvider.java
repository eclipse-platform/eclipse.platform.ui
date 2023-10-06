/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.history;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.viewers.Viewer;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryImplementation;
import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryContentProvider;
import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryControlConfiguration;

/**
 * Tree content provider for the browse refactoring history control.
 *
 * @since 3.2
 */
public final class BrowseRefactoringHistoryContentProvider extends RefactoringHistoryContentProvider {

	/** The no elements constant */
	private static final Object[] NO_ELEMENTS= {};

	/** The workspace project constant */
	private static final String WORKSPACE_PROJECT= ".workspace"; //$NON-NLS-1$

	/** The refactoring history control configuration to use */
	private final RefactoringHistoryControlConfiguration fControlConfiguration;

	/** The project content providers */
	private Map<String, RefactoringHistoryContentProvider> fProjectContentProviders= null;

	/** The project refactoring histories map */
	private Map<String, RefactoringHistory> fProjectRefactoringHistories= null;

	/** The refactoring history, or <code>null</code> */
	private RefactoringHistory fRefactoringHistory= null;

	/** Should the refactoring history be sorted by projects? */
	private boolean fSortProjects= true;

	/**
	 * Creates a new browse refactoring history content provider.
	 *
	 * @param configuration
	 *            the refactoring history control configuration
	 */
	public BrowseRefactoringHistoryContentProvider(final RefactoringHistoryControlConfiguration configuration) {
		super(configuration);
		Assert.isNotNull(configuration);
		fControlConfiguration= configuration;
	}

	@Override
	public Object[] getChildren(final Object element) {
		if (fSortProjects && element instanceof RefactoringHistoryNode) {
			final RefactoringHistoryNode node= (RefactoringHistoryNode) element;
			if (node instanceof RefactoringHistoryProject)
				return getRefactoringHistoryEntries((RefactoringHistoryProject) node);
			else {
				final RefactoringHistoryContentProvider provider= getRefactoringHistoryContentProvider(node);
				if (provider != null)
					return provider.getChildren(element);
			}
			return NO_ELEMENTS;
		}
		return super.getChildren(element);
	}

	@Override
	public Object[] getElements(final Object element) {
		if (fSortProjects && element instanceof RefactoringHistory)
			return getRootElements();
		return super.getElements(element);
	}

	@Override
	public Object getParent(final Object element) {
		if (fSortProjects && element instanceof RefactoringHistoryNode) {
			final RefactoringHistoryContentProvider provider= getRefactoringHistoryContentProvider((RefactoringHistoryNode) element);
			if (provider != null)
				return provider.getParent(element);
			return null;
		}
		return super.getParent(element);
	}

	/**
	 * Returns the refactoring histories.
	 *
	 * @return the map of projects to refactoring histories
	 */
	private Map<String, RefactoringHistory> getRefactoringHistories() {
		if (fProjectRefactoringHistories == null) {
			fProjectRefactoringHistories= new HashMap<>();
			Map<String, Collection<RefactoringDescriptorProxy>> projectRefactoringHistories= new HashMap<>();
			if (fRefactoringHistory != null && !fRefactoringHistory.isEmpty()) {
				for (RefactoringDescriptorProxy proxy : fRefactoringHistory.getDescriptors()) {
					String current= proxy.getProject();
					if (current == null || current.length() == 0)
						current= WORKSPACE_PROJECT;
					Collection<RefactoringDescriptorProxy> collection= projectRefactoringHistories.get(current);
					if (collection == null) {
						collection= new HashSet<>();
						projectRefactoringHistories.put(current, collection);
					}
					collection.add(proxy);
				}
				for (String current : new ArrayList<>(projectRefactoringHistories.keySet())) {
					final Collection<RefactoringDescriptorProxy> collection= projectRefactoringHistories.get(current);
					if (collection != null)
						fProjectRefactoringHistories.put(current, new RefactoringHistoryImplementation(collection.toArray(new RefactoringDescriptorProxy[collection.size()])));
				}
			}
		}
		return fProjectRefactoringHistories;
	}

	/**
	 * Returns the refactoring descriptor proxies for the specified project.
	 *
	 * @param project
	 *            the project
	 * @return the refactoring history, or <code>null</code> if no history is
	 *         available for the project
	 */
	private RefactoringHistory getRefactoringHistory(final String project) {
		getRefactoringHistories();
		return fProjectRefactoringHistories.get(project);
	}

	/**
	 * Returns the refactoring history content provider for the specified node.
	 *
	 * @param node
	 *            the refactoring history node
	 * @return the refactoring history content provider, or <code>null</code>
	 */
	private RefactoringHistoryContentProvider getRefactoringHistoryContentProvider(final RefactoringHistoryNode node) {
		Assert.isNotNull(node);
		final RefactoringHistoryNode root= getRootNode(node);

		String projectName= null;
		if (root instanceof RefactoringHistoryProject) {
			projectName= ((RefactoringHistoryProject) root).getProject();
		} else if (!(node instanceof RefactoringHistoryEntry)) {
			projectName= WORKSPACE_PROJECT;
		}
		if (projectName != null) {
			final RefactoringHistory history= getRefactoringHistory(projectName);
			if (history != null) {
				final RefactoringHistoryContentProvider provider= getRefactoringHistoryContentProvider(projectName);
				if (provider != null) {
					provider.inputChanged(null, null, history);
					return provider;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the refactoring history content provider for the specified
	 * project.
	 *
	 * @param project
	 *            the project
	 * @return the refactoring history content provider
	 */
	private RefactoringHistoryContentProvider getRefactoringHistoryContentProvider(final String project) {
		if (fProjectContentProviders == null)
			fProjectContentProviders= new HashMap<>();
		RefactoringHistoryContentProvider provider= fProjectContentProviders.get(project);
		if (provider == null) {
			provider= fControlConfiguration.getContentProvider();
			fProjectContentProviders.put(project, provider);
		}
		return provider;
	}

	/**
	 * Returns the refactoring history entries for the specified project.
	 *
	 * @param project
	 *            the project
	 * @return the refactoring history entries
	 */
	private Object[] getRefactoringHistoryEntries(final RefactoringHistoryProject project) {
		final String name= project.getProject();
		final RefactoringHistory history= getRefactoringHistory(name);
		if (history != null) {
			if (fControlConfiguration.isTimeDisplayed()) {
				final RefactoringHistoryContentProvider provider= getRefactoringHistoryContentProvider(project);
				if (provider != null) {
					provider.inputChanged(null, null, history);
					final Object[] elements= provider.getRootElements();
					if (!WORKSPACE_PROJECT.equals(name)) {
						for (int index= 0; index < elements.length; index++) {
							if (elements[index] instanceof RefactoringHistoryDate) {
								final RefactoringHistoryDate date= (RefactoringHistoryDate) elements[index];
								elements[index]= new RefactoringHistoryDate(project, date.getTimeStamp(), date.getKind());
							}
						}
					}
					return elements;
				}
			} else {
				final RefactoringDescriptorProxy[] proxies= history.getDescriptors();
				final RefactoringHistoryEntry[] entries= new RefactoringHistoryEntry[proxies.length];
				for (int index= 0; index < proxies.length; index++)
					entries[index]= new RefactoringHistoryEntry(project, proxies[index]);
				return entries;
			}
		}
		return NO_ELEMENTS;
	}

	@Override
	public Object[] getRootElements() {
		if (fSortProjects) {
			final List<Object> list= new ArrayList<>(32);
			for (String project : getRefactoringHistories().keySet()) {
				if (WORKSPACE_PROJECT.equals(project)) {
					final RefactoringHistory history= getRefactoringHistory(project);
					if (history != null) {
						if (fControlConfiguration.isTimeDisplayed()) {
							final RefactoringHistoryContentProvider provider= getRefactoringHistoryContentProvider(project);
							if (provider != null) {
								provider.inputChanged(null, null, history);
								list.addAll(Arrays.asList(provider.getRootElements()));
							}
						} else {
							final RefactoringDescriptorProxy[] proxies= history.getDescriptors();
							final RefactoringHistoryEntry[] entries= new RefactoringHistoryEntry[proxies.length];
							for (int index= 0; index < proxies.length; index++)
								entries[index]= new RefactoringHistoryEntry(null, proxies[index]);
							list.addAll(Arrays.asList(entries));
						}
					}
				} else
					list.add(new RefactoringHistoryProject(project));
			}
			return list.toArray();
		} else if (fControlConfiguration.isTimeDisplayed())
			return super.getRootElements();
		else
			return new Object[] { new RefactoringHistoryCollection()};
	}

	/**
	 * Returns the root node of the specified node.
	 *
	 * @param node
	 *            the refactoring history node
	 * @return the root node, or the node itself
	 */
	private RefactoringHistoryNode getRootNode(final RefactoringHistoryNode node) {
		RefactoringHistoryNode current= node;
		RefactoringHistoryNode parent= current.getParent();
		while (parent != null) {
			current= parent;
			parent= current.getParent();
		}
		return current;
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object predecessor, final Object successor) {
		super.inputChanged(viewer, predecessor, successor);
		if (predecessor == successor)
			return;
		if (successor instanceof RefactoringHistory)
			fRefactoringHistory= (RefactoringHistory) successor;
		else
			fRefactoringHistory= null;
		fProjectRefactoringHistories= null;
		fProjectContentProviders= null;
	}

	/**
	 * Should the refactoring history be sorted by projects?
	 *
	 * @return <code>true</code> if it should be sorted by projects,
	 *         <code>false</code> otherwise
	 */
	public boolean isSortProjects() {
		return fSortProjects;
	}

	/**
	 * Determines whether the refactoring history should be sorted by projects.
	 *
	 * @param sort
	 *            <code>true</code> to sort by projects, <code>false</code>
	 *            otherwise
	 */
	public void setSortProjects(final boolean sort) {
		if (sort != fSortProjects) {
			fProjectRefactoringHistories= null;
			fProjectContentProviders= null;
		}
		fSortProjects= sort;
	}
}
