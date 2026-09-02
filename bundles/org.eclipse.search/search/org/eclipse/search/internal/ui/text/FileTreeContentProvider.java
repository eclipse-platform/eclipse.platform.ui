/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
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
 *     Juerg Billeter, juergbi@ethz.ch - 47136 Search view should show match objects
 *     Ulrich Etter, etteru@ethz.ch - 47136 Search view should show match objects
 *     Roman Fuchs, fuchsro@ethz.ch - 47136 Search view should show match objects
 *     Red Hat Inc. - add support for filtering out files not in innermost nested project
 *******************************************************************************/
package org.eclipse.search.internal.ui.text;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.Match;
import org.eclipse.search.ui.text.MatchFilter;


public class FileTreeContentProvider implements ITreeContentProvider, IFileSearchContentProvider {

	private final Object[] EMPTY_ARR= new Object[0];

	private AbstractTextSearchResult fResult;
	private final FileSearchPage fPage;
	private final AbstractTreeViewer fTreeViewer;
	private Map<Object, Set<Object>> fChildrenMap;

	FileTreeContentProvider(FileSearchPage page, AbstractTreeViewer viewer) {
		fPage= page;
		fTreeViewer= viewer;
		fChildrenMap = new HashMap<>();
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	private int getElementLimit() {
		return fPage.getElementLimit().intValue();
	}

	@Override
	public void dispose() {
		// nothing to do
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof FileSearchResult) {
			initialize((FileSearchResult) newInput);
		}
	}

	private synchronized void initialize(AbstractTextSearchResult result) {
		fResult= result;
		fChildrenMap= new HashMap<>();
		boolean showLineMatches= !((FileSearchQuery) fResult.getQuery()).isFileNameSearch();

		if (result != null) {
			Object[] elements= result.getElements();
			for (Object element : elements) {
				if (showLineMatches) {
					Match[] matches= result.getMatches(element);
					for (Match match : matches) {
						if (!match.isFiltered()) {
							insert(((FileMatch) match).getLineElement(), false);
						}
					}
				} else {
					insert(element, false);
				}
			}
		}
	}

	private void insert(Object child, boolean refreshViewer) {
		Object parent= getParent(child);
		while (parent != null) {
			if (insertChild(parent, child)) {
				if (refreshViewer) {
					fTreeViewer.add(parent, child);
				}
			} else {
				if (refreshViewer) {
					fTreeViewer.update(parent, null);
				}
				return;
			}
			child= parent;
			parent= getParent(child);
		}
		if (insertChild(fResult, child)) {
			if (refreshViewer) {
				fTreeViewer.add(fResult, child);
			}
		}
	}

	/**
	 * Adds the child to the parent.
	 *
	 * @param parent the parent
	 * @param child the child
	 * @return <code>true</code> if this set did not already contain the specified element

	 */
	private boolean insertChild(Object parent, Object child) {
		Set<Object> children= fChildrenMap.get(parent);
		if (children == null) {
			children= new HashSet<>();
			fChildrenMap.put(parent, children);
		}
		return children.add(child);
	}

	private boolean hasChild(Object parent, Object child) {
		Set<Object> children= fChildrenMap.get(parent);
		return children != null && children.contains(child);
	}


	private void remove(Object element, boolean refreshViewer) {
		// precondition here:  fResult.getMatchCount(child) <= 0

		if (hasChildren(element)) {
			if (refreshViewer) {
				fTreeViewer.refresh(element);
			}
		} else {
			if (!hasMatches(element)) {
				fChildrenMap.remove(element);
				Object parent= getParent(element);
				if (parent != null) {
					removeFromSiblings(element, parent);
					remove(parent, refreshViewer);
				} else {
					removeFromSiblings(element, fResult);
					if (refreshViewer) {
						fTreeViewer.refresh();
					}
				}
			} else {
				if (refreshViewer) {
					fTreeViewer.refresh(element);
				}
			}
		}
	}

	private boolean hasMatches(Object element) {
		if (element instanceof LineElement lineElement) {
			IResource resource = lineElement.getParent();
			if (hasMatches(resource)) {
				return lineElement.hasMatches(fResult);
			}
		}
		return fPage.getDisplayedMatchCount(element) > 0;
	}

	private boolean hasMatches(IResource element) {
		if (hasActiveMatchFilters()) {
			return fPage.getDisplayedMatchCount(element) > 0;
		} else {
			return fResult.hasMatches();
		}
	}

	private int getMatchCount(Object element) {
		if (hasActiveMatchFilters()) {
			return fPage.getDisplayedMatchCount(element);
		} else {
			return fResult.getMatchCount();
		}
	}

	private void removeFromSiblings(Object element, Object parent) {
		Set<Object> siblings= fChildrenMap.get(parent);
		if (siblings != null) {
			siblings.remove(element);
		}
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		Set<Object> children= fChildrenMap.get(parentElement);
		if (children == null) {
			return EMPTY_ARR;
		}

		int elementLimit = getElementLimit();
		if (elementLimit != -1 && elementLimit < children.size()) {
			Object[] limitedChildren = new Object[elementLimit];
			System.arraycopy(children.toArray(), 0, limitedChildren, 0, elementLimit);
			return limitedChildren;
		}

		return children.toArray();
	}

	@Override
	public int getLeafCount(Object parentElement) {
		Object[] children = getChildren(parentElement);
		if (children.length == 0) {
			return 0;
		}
		int count = 0;
		for (Object object : children) {
			boolean leaf = !hasChildren(object);
			if (leaf) {
				count++;
			} else {
				count += getLeafCount(object);
			}
		}
		return count;
	}

	@Override
	public boolean hasChildren(Object element) {
		Set<Object> children = fChildrenMap.get(element);
		if (children == null) {
			return false;
		}
		return !children.isEmpty();
	}

	/**
	 *
	 * Update the search contents. Screen out any results that are filtered via
	 * active match filters (e.g. selected filters in the pull-down menu of the
	 * FileSearchPage). For example, one match filter exists to screen matches that
	 * are duplicates reported when one or more projects are nested within another.
	 * Each outer project will own a separate IResource for files found in inner
	 * projects and searches will report for both inner and outer resources. The
	 * filter mentioned only eliminates duplicates that are not from the innermost
	 * nested project owning the file.
	 */
	@Override
	public synchronized void elementsChanged(Object[] updatedElements) {
		boolean singleElement = updatedElements.length == 1;
		Set<LineElement> lineMatches = getUpdatedLinesWithMatches(updatedElements);
		try {
			for (Object updatedElement : updatedElements) {
				if (!(updatedElement instanceof LineElement lineElement)) {
					// change events to elements are reported in file search.
					// ask the page to determine if element is filtered.
					if (getMatchCount(updatedElement) > 0) {
						insert(updatedElement, singleElement);
					} else {
						remove(updatedElement, singleElement);
					}
				} else {
					// change events to line elements are reported in text
					// search
					boolean hasMatches = lineMatches.contains(lineElement);
					if (hasMatches) {
						if (singleElement && hasChild(lineElement.getParent(), lineElement)) {
							fTreeViewer.update(new Object[] { lineElement, lineElement.getParent() }, null);
						} else {
							insert(lineElement, singleElement);
						}
					} else {
						remove(lineElement, singleElement);
					}
				}
			}
		} finally {
			if (updatedElements.length > 0 && !singleElement) {
				fTreeViewer.refresh();
			}
		}
	}

	private boolean hasActiveMatchFilters() {
		MatchFilter[] activeMatchFilters = fResult.getActiveMatchFilters();
		return activeMatchFilters != null && activeMatchFilters.length > 0;
	}

	/**
	 * Collects the updated line elements that still have matches. The matches of a
	 * file are enumerated only once, no matter how many lines of that file have been
	 * updated, and only the given lines are remembered instead of all lines of the
	 * touched files.
	 *
	 * @param updatedElements the updated elements, may contain elements that are no
	 *                        line elements
	 * @return the line elements of <code>updatedElements</code> that have at least
	 *         one match that is not hidden by an active match filter
	 */
	private Set<LineElement> getUpdatedLinesWithMatches(Object[] updatedElements) {
		// LineElement doesn't implement equals(..)/hashCode(), matches refer to the
		// very same instance the update is reported for
		Set<LineElement> updatedLines = Collections.newSetFromMap(new IdentityHashMap<>());
		Set<IResource> files = new HashSet<>();
		for (Object updatedElement : updatedElements) {
			if (updatedElement instanceof LineElement lineElement) {
				updatedLines.add(lineElement);
				files.add(lineElement.getParent());
			}
		}
		if (updatedLines.isEmpty()) {
			return Collections.emptySet();
		}
		// if we have active match filters, we should only use non-filtered FileMatch
		// objects to collect LineElements to update. The filter state is evaluated
		// once per match by the search result (see AbstractTextSearchResult), it must
		// not be computed again here: match filters can be expensive and this code
		// runs in the UI thread for every batch of search results.
		boolean useFilterState = hasActiveMatchFilters();
		Set<LineElement> linesWithMatches = Collections.newSetFromMap(new IdentityHashMap<>());
		for (IResource file : files) {
			Enumeration<Match> matches = fResult.getMatchSet(file);
			while (matches.hasMoreElements()) {
				Match match = matches.nextElement();
				if (useFilterState && match.isFiltered()) {
					continue;
				}
				LineElement lineElement = ((FileMatch) match).getLineElement();
				if (updatedLines.contains(lineElement) && linesWithMatches.add(lineElement)
						&& linesWithMatches.size() == updatedLines.size()) {
					return linesWithMatches; // all updated lines have matches
				}
			}
		}
		return linesWithMatches;
	}

	@Override
	public void clear() {
		initialize(fResult);
		fTreeViewer.refresh();
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof IProject) {
			return null;
		}
		if (element instanceof IResource resource) {
			return resource.getParent();
		}
		if (element instanceof LineElement) {
			return ((LineElement) element).getParent();
		}

		if (element instanceof FileMatch match) {
			return match.getLineElement();
		}
		return null;
	}
}
