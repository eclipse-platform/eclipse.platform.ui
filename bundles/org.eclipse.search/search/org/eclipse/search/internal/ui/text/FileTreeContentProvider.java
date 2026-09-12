/*******************************************************************************
 * Copyright (c) 2000, 2026 IBM Corporation and others.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
	/**
	 * Upper bound for the number of children of an element in
	 * {@link #fChildrenMap}. Only updated while inserting, so it may be bigger than
	 * the real maximum after elements have been removed.
	 */
	private int fMaxChildrenCount;

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
		fMaxChildrenCount= 0;
		// the result may be provided by a client that doesn't use a FileSearchQuery
		boolean showLineMatches= fResult.getQuery() instanceof FileSearchQuery query && !query.isFileNameSearch();

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
		boolean added= children.add(child);
		if (added && children.size() > fMaxChildrenCount) {
			fMaxChildrenCount= children.size();
		}
		return added;
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
		Set<Object> children = fChildrenMap.get(parentElement);
		if (children == null || children.isEmpty()) {
			return 0;
		}
		return countShownLeafs(parentElement, getElementLimit());
	}

	/**
	 * Counts the leafs below the given element that are shown in the viewer. Like
	 * {@link #getChildren(Object)} only the first <code>elementLimit</code> children
	 * of an element are considered, subtrees hidden by the element limit are not
	 * traversed.
	 *
	 * @param element      the element to count the shown leafs for
	 * @param elementLimit the element limit, <code>-1</code> for no limit
	 * @return number of leafs shown below the given element
	 */
	private int countShownLeafs(Object element, int elementLimit) {
		Set<Object> children = fChildrenMap.get(element);
		if (children == null || children.isEmpty()) {
			return 1; // the element itself is a leaf
		}
		int count = 0;
		int index = 0;
		for (Object child : children) {
			if (elementLimit != -1 && index >= elementLimit) {
				break; // the remaining children are hidden
			}
			count += countShownLeafs(child, elementLimit);
			index++;
		}
		return count;
	}

	@Override
	public boolean isTruncated(Object parentElement) {
		int elementLimit = getElementLimit();
		if (elementLimit == -1 || fMaxChildrenCount <= elementLimit) {
			// no element can have more children than the element limit allows
			return false;
		}
		return isTruncated(parentElement, elementLimit);
	}

	/**
	 * Elements are hidden if and only if a shown element has more children than the
	 * element limit allows, so only shown subtrees have to be traversed, and the
	 * traversal can stop at the first truncated element.
	 *
	 * @param element      the element to check
	 * @param elementLimit the element limit, never <code>-1</code>
	 * @return <code>true</code> if elements below the given element are hidden
	 */
	private boolean isTruncated(Object element, int elementLimit) {
		Set<Object> children = fChildrenMap.get(element);
		if (children == null) {
			return false;
		}
		if (children.size() > elementLimit) {
			return true;
		}
		for (Object child : children) { // all children are shown
			if (isTruncated(child, elementLimit)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int getShownMatchCount(AbstractTextSearchResult result) {
		if (result == null || !fChildrenMap.containsKey(result)) {
			return 0;
		}
		Map<Object, Set<LineElement>> shownLines = new HashMap<>();
		List<Object> shownFiles = new ArrayList<>();
		collectShownLeafs(result, getElementLimit(), shownLines, shownFiles);

		int count = 0;
		// count the matches of the shown lines with one pass over the matches of
		// each file instead of scanning them once per line
		for (Entry<Object, Set<LineElement>> entry : shownLines.entrySet()) {
			for (Match match : result.getMatches(entry.getKey())) {
				if (isShown(result, match) && entry.getValue().contains(((FileMatch) match).getLineElement())) {
					count++;
				}
			}
		}
		for (Object file : shownFiles) {
			count += fPage.getDisplayedMatchCount(file);
		}
		return count;
	}

	private static boolean isShown(AbstractTextSearchResult result, Match match) {
		// see AbstractTextSearchViewPage#getDisplayedMatchCount(Object): if no filters
		// are set at all the filter state of a match is ignored
		return result.getActiveMatchFilters() == null || !match.isFiltered();
	}

	private void collectShownLeafs(Object element, int elementLimit, Map<Object, Set<LineElement>> shownLines,
			List<Object> shownFiles) {
		Set<Object> children = fChildrenMap.get(element);
		if (children == null || children.isEmpty()) {
			if (element instanceof LineElement line) {
				shownLines.computeIfAbsent(line.getParent(), k -> new HashSet<>()).add(line);
			} else {
				shownFiles.add(element);
			}
			return;
		}
		int index = 0;
		for (Object child : children) {
			if (elementLimit != -1 && index >= elementLimit) {
				break;
			}
			collectShownLeafs(child, elementLimit, shownLines, shownFiles);
			index++;
		}
	}

	@Override
	public boolean hasChildren(Object element) {
		Set<Object> children = fChildrenMap.get(element);
		if (children == null) {
			return false;
		}
		return !children.isEmpty();
	}

	static <T> Stream<T> toStream(Enumeration<T> e) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(e.asIterator(), Spliterator.ORDERED), false);
	}

	private boolean isUnfiltered(FileMatch m) {
		MatchFilter[] filters = fResult.getActiveMatchFilters();
		if (filters != null) {
			for (MatchFilter filter : filters) {
				if (filter.filters(m)) {
					return false;
				}
			}
		}
		return true;
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
		Set<LineElement> lineMatches = Collections.emptySet();
		// if we have active match filters, we should only use non-filtered FileMatch
		// objects to collect LineElements to update
		if (hasActiveMatchFilters()) {
			lineMatches = Arrays.stream(updatedElements).filter(LineElement.class::isInstance)
				// only for distinct files:
				.map(u -> ((LineElement) u).getParent()).distinct()
				// query matches:
				.map(fResult::getMatchSet).flatMap(FileTreeContentProvider::toStream)
				.map(m -> ((FileMatch) m)).filter(this::isUnfiltered).map(m -> m.getLineElement())
				.collect(Collectors.toSet());
		} else {
			lineMatches = Arrays.stream(updatedElements).filter(LineElement.class::isInstance)
					// only for distinct files:
					.map(u -> ((LineElement) u).getParent()).distinct()
					// query matches:
					.map(fResult::getMatchSet).flatMap(FileTreeContentProvider::toStream)
					.map(m -> ((FileMatch) m).getLineElement()).collect(Collectors.toSet());
		}
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
