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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.Match;


public class FileTreeContentProvider implements ITreeContentProvider, IFileSearchContentProvider {

	private final Object[] EMPTY_ARR= new Object[0];

	private AbstractTextSearchResult fResult;
	private final FileSearchPage fPage;
	private final AbstractTreeViewer fTreeViewer;
	private Map<Object, Set<Object>> fChildrenMap;
	/**
	 * Upper bound for the number of children of an element in
	 * {@link #fChildrenMap}. Only updated while inserting, so it may be bigger than
	 * the real maximum after elements have been removed. A too big value is not a
	 * problem: it only disables the shortcut in {@link #isTruncated(Object)}, which
	 * then traverses the shown elements to compute the correct answer.
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
				} else if (hasDisplayedMatches(result, element)) {
					// files whose matches are all filtered are not shown
					insert(element, false);
				}
			}
		}
	}

	private static boolean hasDisplayedMatches(AbstractTextSearchResult result, Object element) {
		if (!result.hasActiveMatchFilters()) {
			return true;
		}
		for (Match match : result.getMatches(element)) {
			if (!match.isFiltered()) {
				return true;
			}
		}
		return false;
	}

	private void insert(Object child, boolean refreshViewer) {
		Object parent= getParent(child);
		while (parent != null) {
			if (insertChild(parent, child)) {
				if (refreshViewer) {
					addToViewer(parent, child);
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
				addToViewer(fResult, child);
			}
		}
	}

	/**
	 * Adds a child that was just inserted into {@link #fChildrenMap} to the viewer,
	 * respecting the element limit: the viewer must show the same children as
	 * {@link #getChildren(Object)} returns.
	 *
	 * @param parent the parent of the child
	 * @param child  the child that was just inserted
	 */
	private void addToViewer(Object parent, Object child) {
		int elementLimit= getElementLimit();
		Set<Object> children= fChildrenMap.get(parent);
		if (elementLimit == -1 || children == null || children.size() <= elementLimit) {
			fTreeViewer.add(parent, child);
			return;
		}
		if (elementLimit <= 0) {
			return; // no child is shown at all
		}
		// The parent already had at least elementLimit children before the child was
		// inserted, so the first elementLimit of them were shown. If the new child is
		// among the first elementLimit children now, it displaces the last of the
		// previously shown children, otherwise it is hidden and nothing changes.
		List<Object> firstChildren= selectShownChildren(children, elementLimit + 1);
		int index= firstChildren.indexOf(child);
		if (index < 0 || index >= elementLimit) {
			return; // the new child is hidden by the element limit
		}
		fTreeViewer.remove(parent, new Object[] { firstChildren.get(elementLimit) });
		fTreeViewer.add(parent, child);
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
				removeWithoutMatches(element, refreshViewer);
			} else {
				if (refreshViewer) {
					fTreeViewer.refresh(element);
				}
			}
		}
	}

	/**
	 * Removes an element that has no children and no matches shown from the tree,
	 * and its parents as far as they don't have children or matches anymore.
	 *
	 * @param element       the element to remove, the caller has checked that it
	 *                      has no children and no matches shown
	 * @param refreshViewer whether the viewer is updated
	 */
	private void removeWithoutMatches(Object element, boolean refreshViewer) {
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
	}

	private boolean hasMatches(Object element) {
		if (element instanceof LineElement lineElement) {
			// a line without matches that are shown must not stay in the tree,
			// otherwise it is counted as shown but contributes no match
			return lineElement.hasDisplayedMatches(fResult);
		}
		return fPage.getDisplayedMatchCount(element) > 0;
	}

	private int getMatchCount(Object element) {
		if (fResult.hasActiveMatchFilters()) {
			return fPage.getDisplayedMatchCount(element);
		}
		// the matches of the element, not of the whole result: otherwise an element
		// whose matches were all removed stays in the tree as long as any other
		// element has matches
		return fResult.getMatchCount(element);
	}

	private void removeFromSiblings(Object element, Object parent) {
		Set<Object> siblings= fChildrenMap.get(parent);
		if (siblings != null) {
			siblings.remove(element);
		}
	}

	/**
	 * A child of an element in {@link #fChildrenMap} together with its precomputed
	 * sort key: comparing resources means comparing their paths, so the key of an
	 * element is computed once and not again for every comparison.
	 * <p>
	 * The order is deterministic and resembles the order of the viewer (containers
	 * before files, resources by path ignoring the case, the lines of a file by
	 * their offset), but it
	 * is not exactly the order of
	 * {@link FileSearchPage.DecoratorIgnoringViewerSorter}: that one compares the
	 * (locale dependent) texts of the label provider, which also depend on the sort
	 * order chosen by the user. Only the determinism matters here: the children are
	 * stored in a hash set, so without an explicit order the elements hidden by the
	 * element limit would be chosen arbitrarily and could differ for every search,
	 * even if the very same search is repeated.
	 * </p>
	 */
	private record SortedChild(Object element, int category, int offset, String sortKey)
			implements Comparable<SortedChild> {

		private static final int LINE_CATEGORY= 0;
		private static final int CONTAINER_CATEGORY= 1;
		private static final int FILE_CATEGORY= 2;

		static SortedChild of(Object element) {
			if (element instanceof LineElement line) {
				// a children set contains the lines of one single file, and the lines
				// of a file have distinct offsets: no tie breaker needed. Don't compute
				// the path of the file for every line.
				return new SortedChild(element, LINE_CATEGORY, line.getOffset(), ""); //$NON-NLS-1$
			}
			if (element instanceof IResource resource) {
				int category= element instanceof IContainer ? CONTAINER_CATEGORY : FILE_CATEGORY;
				return new SortedChild(element, category, 0, resource.getFullPath().toString());
			}
			// unknown element type: don't break the sort, even if the order of such
			// elements isn't stable
			return new SortedChild(element, FILE_CATEGORY, 0, String.valueOf(element));
		}

		@Override
		public int compareTo(SortedChild other) {
			if (category != other.category) {
				return Integer.compare(category, other.category);
			}
			if (offset != other.offset) {
				return Integer.compare(offset, other.offset);
			}
			// ignore the case like the collator used by the viewer, but fall back to a
			// case sensitive comparison: siblings that differ only in case (possible
			// on case sensitive file systems) must not be equal, otherwise the shown
			// children at the element limit would be chosen arbitrarily
			int result= sortKey.compareToIgnoreCase(other.sortKey);
			return result != 0 ? result : sortKey.compareTo(other.sortKey);
		}
	}

	/**
	 * Returns the children the viewer shows for an element: if the element has more
	 * children than the element limit allows, only the first children in the stable
	 * order defined by {@link SortedChild} are shown.
	 * <p>
	 * Note that the shown children are not necessarily the first ones in the order
	 * the viewer displays them: the viewer sorts the shown children by the texts of
	 * the label provider and the sort order chosen by the user, which is not known
	 * here. For the default sort orders both orders are close to each other.
	 * </p>
	 *
	 * @param children all children of an element, never <code>null</code>
	 * @return the children shown in the viewer
	 */
	private Collection<Object> getShownChildren(Set<Object> children) {
		int elementLimit= getElementLimit();
		if (elementLimit == -1 || elementLimit >= children.size()) {
			return children;
		}
		if (elementLimit <= 0) {
			return List.of();
		}
		return selectShownChildren(children, elementLimit);
	}

	/**
	 * Selects the first <code>elementLimit</code> children in the order defined by
	 * {@link SortedChild}. Only the selected children are kept while scanning, so
	 * that an element with a huge number of children isn't sorted completely just
	 * to show a few of them.
	 *
	 * @param children     all children of an element
	 * @param elementLimit maximum number of children to select, always greater
	 *                     than zero and not bigger than the number of children
	 * @return the selected children, sorted in the order defined by
	 *         {@link SortedChild}
	 */
	private static List<Object> selectShownChildren(Set<Object> children, int elementLimit) {
		// max-heap with the smallest children seen so far: the biggest of them is on
		// top and is dropped as soon as a smaller child shows up
		PriorityQueue<SortedChild> smallest= new PriorityQueue<>(elementLimit, Comparator.reverseOrder());
		for (Object child : children) {
			SortedChild candidate= SortedChild.of(child);
			if (smallest.size() < elementLimit) {
				smallest.add(candidate);
			} else if (candidate.compareTo(smallest.peek()) < 0) {
				smallest.poll();
				smallest.add(candidate);
			}
		}
		List<SortedChild> shown= new ArrayList<>(smallest);
		Collections.sort(shown);
		List<Object> elements= new ArrayList<>(shown.size());
		for (SortedChild child : shown) {
			elements.add(child.element());
		}
		return elements;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		Set<Object> children= fChildrenMap.get(parentElement);
		if (children == null) {
			return EMPTY_ARR;
		}
		return getShownChildren(children).toArray();
	}

	@Override
	public int getLeafCount(Object parentElement) {
		Set<Object> children = fChildrenMap.get(parentElement);
		if (children == null || children.isEmpty()) {
			return 0;
		}
		return countShownLeafs(parentElement);
	}

	/**
	 * Counts the leafs below the given element that are shown in the viewer. Like
	 * {@link #getChildren(Object)} only the children shown in the viewer are
	 * considered, subtrees hidden by the element limit are not traversed.
	 *
	 * @param element the element to count the shown leafs for
	 * @return number of leafs shown below the given element
	 */
	private int countShownLeafs(Object element) {
		Set<Object> children = fChildrenMap.get(element);
		if (children == null || children.isEmpty()) {
			return 1; // the element itself is a leaf
		}
		int count = 0;
		for (Object child : getShownChildren(children)) {
			count += countShownLeafs(child);
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
		collectShownLeafs(result, shownLines, shownFiles);

		int count = 0;
		boolean useFilterState = result.hasActiveMatchFilters();
		// count the matches of the shown lines with one pass over the matches of
		// each file instead of scanning them once per line
		for (Entry<Object, Set<LineElement>> entry : shownLines.entrySet()) {
			for (Match match : result.getMatches(entry.getKey())) {
				if (!(useFilterState && match.isFiltered())
						&& entry.getValue().contains(((FileMatch) match).getLineElement())) {
					count++;
				}
			}
		}
		for (Object file : shownFiles) {
			count += fPage.getDisplayedMatchCount(file);
		}
		return count;
	}

	private void collectShownLeafs(Object element, Map<Object, Set<LineElement>> shownLines,
			List<Object> shownFiles) {
		Set<Object> children = fChildrenMap.get(element);
		if (children == null || children.isEmpty()) {
			if (element instanceof LineElement line) {
				shownLines.computeIfAbsent(line.getParent(), _ -> new HashSet<>()).add(line);
			} else {
				shownFiles.add(element);
			}
			return;
		}
		for (Object child : getShownChildren(children)) {
			collectShownLeafs(child, shownLines, shownFiles);
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
						// Lines are leafs, and getUpdatedLinesWithMatches(..) has already
						// scanned all matches of the file and found none shown for this
						// line: don't scan them again for every removed line, which takes
						// O(lines * matches) if a filter hides all matches of a big file
						removeWithoutMatches(lineElement, singleElement);
					}
				}
			}
		} finally {
			if (updatedElements.length > 0 && !singleElement) {
				fTreeViewer.refresh();
			}
		}
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
		// If we have active match filters, we should only use non-filtered FileMatch
		// objects to collect LineElements to update. The filter state is evaluated
		// once per match by the search result (see AbstractTextSearchResult), it must
		// not be computed again here: match filters can be expensive and this code
		// runs in the UI thread for every batch of search results.
		boolean useFilterState = fResult.hasActiveMatchFilters();
		Set<LineElement> linesWithMatches = Collections.newSetFromMap(new IdentityHashMap<>());
		for (IResource file : files) {
			Enumeration<Match> matches = fResult.getMatchSet(file);
			while (matches.hasMoreElements()) {
				Match match = matches.nextElement();
				if (useFilterState && match.isFiltered()) {
					continue;
				}
				LineElement lineElement = ((FileMatch) match).getLineElement();
				if (!updatedLines.contains(lineElement)) {
					continue; // the line is not updated, no need to remember it
				}
				linesWithMatches.add(lineElement);
				if (linesWithMatches.size() == updatedLines.size()) {
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
