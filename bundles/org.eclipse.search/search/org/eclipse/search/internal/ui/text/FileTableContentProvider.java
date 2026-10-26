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
 *     Red Hat Inc. - add support to filter files from non-innermost nested projects
 *******************************************************************************/
package org.eclipse.search.internal.ui.text;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.search.ui.text.AbstractTextSearchResult;

public class FileTableContentProvider implements IStructuredContentProvider, IFileSearchContentProvider {

	private final Object[] EMPTY_ARR= new Object[0];

	private final FileSearchPage fPage;

	public FileTableContentProvider(FileSearchPage page) {
		fPage= page;
	}

	@Override
	public void dispose() {
		// nothing to do
	}

	@Override
	public int getLeafCount(Object parentElement) {
		// every element of the table is a leaf
		return getElements(parentElement).length;
	}

	@Override
	public boolean isTruncated(Object parentElement) {
		if (!(parentElement instanceof FileSearchResult fileSearchResult)) {
			return false; // see getElements(Object): nothing is shown
		}
		int elementLimit = getElementLimit();
		if (elementLimit == -1 || fileSearchResult.getElementsCount() <= elementLimit) {
			// no limit, or all elements fit into the limit. Match filters can only
			// reduce the number of shown elements, so nothing is hidden by the limit
			return false;
		}
		// here the result has more elements than the limit allows
		if (!fileSearchResult.hasActiveMatchFilters()) {
			// all elements would be shown, so the ones above the limit are hidden
			return true;
		}
		// Match filters are active: elements whose matches are all filtered out are
		// not shown and therefore not hidden by the element limit. Only count
		// displayed elements, and stop as soon as the element limit is exceeded
		return collectDisplayedElements(fileSearchResult, elementLimit + 1).size() > elementLimit;
	}

	@Override
	public int getShownMatchCount(AbstractTextSearchResult result) {
		if (result == null) {
			return 0;
		}
		int count = 0;
		for (Object element : getElements(result)) {
			count += fPage.getDisplayedMatchCount(element);
		}
		return count;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof FileSearchResult fileSearchResult) {
			int elementLimit= getElementLimit();
			if (!fileSearchResult.hasActiveMatchFilters()) {
				Object[] elements= fileSearchResult.getElements();
				if (elementLimit != -1 && elements.length > elementLimit) {
					Object[] shownElements= new Object[elementLimit];
					System.arraycopy(elements, 0, shownElements, 0, elementLimit);
					return shownElements;
				}
				return elements;
			}
			// apply the match filters before the element limit: elements whose
			// matches are all filtered must not occupy a place in the table
			int maxCount= elementLimit == -1 ? Integer.MAX_VALUE : elementLimit;
			return collectDisplayedElements(fileSearchResult, maxCount).toArray();
		}
		return EMPTY_ARR;
	}

	/**
	 * @param result   the search result
	 * @param maxCount maximum number of elements to collect
	 * @return up to <code>maxCount</code> elements of the result that have at least
	 *         one match that is not hidden by an active match filter
	 */
	private List<Object> collectDisplayedElements(AbstractTextSearchResult result, int maxCount) {
		List<Object> displayed= new ArrayList<>();
		if (maxCount <= 0) {
			return displayed;
		}
		for (Object element : result.getElements()) {
			if (fPage.getDisplayedMatchCount(element) > 0) {
				displayed.add(element);
				if (displayed.size() >= maxCount) {
					break;
				}
			}
		}
		return displayed;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// nothing to do
	}

	@Override
	public void elementsChanged(Object[] updatedElements) {
		TableViewer viewer= getViewer();
		int elementLimit= getElementLimit();
		boolean tableLimited= elementLimit != -1;
		boolean removedShownElement= false;
		for (Object updatedElement : updatedElements) {
			if (fPage.getDisplayedMatchCount(updatedElement) > 0) {
				if (viewer.contains(updatedElement)) {
					viewer.update(updatedElement, null);
				} else {
					if (!tableLimited || viewer.getTable().getItemCount() < elementLimit) {
						viewer.add(updatedElement);
					}
				}
			} else if (viewer.contains(updatedElement)) {
				viewer.remove(updatedElement);
				removedShownElement= true;
			}
		}
		if (removedShownElement && tableLimited && needsBackfill(viewer)) {
			// An element hidden by the element limit can take the place of the removed
			// one, but it is not necessarily part of the updated elements: a filter
			// update only reports the matches whose filter state changed. Let
			// getElements(Object) choose the shown elements, like for the initial
			// content, so that the table agrees with getLeafCount(Object) and
			// isTruncated(Object).
			viewer.refresh();
		}
	}

	/**
	 * @param viewer the table viewer
	 * @return <code>true</code> if the table has room for elements of the result
	 *         that are not shown yet
	 */
	private boolean needsBackfill(TableViewer viewer) {
		int itemCount= viewer.getTable().getItemCount();
		if (itemCount >= getElementLimit()) {
			return false;
		}
		AbstractTextSearchResult result= fPage.getInput();
		return result != null && result.getElementsCount() > itemCount;
	}

	private int getElementLimit() {
		return fPage.getElementLimit().intValue();
	}

	private TableViewer getViewer() {
		return (TableViewer) fPage.getViewer();
	}

	@Override
	public void clear() {
		getViewer().refresh();
	}
}
