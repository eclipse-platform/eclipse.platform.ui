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

	private FileSearchPage fPage;
	private AbstractTextSearchResult fResult;

	public FileTableContentProvider(FileSearchPage page) {
		fPage= page;
	}

	@Override
	public void dispose() {
		// nothing to do
	}

	public Object[] getUnfilteredElements(AbstractTextSearchResult searchResult) {
		int elementLimit = getElementLimit();
		Object[] elements = searchResult.getElements();
		if (elementLimit != -1 && elements.length > elementLimit) {
			Object[] shownElements = new Object[elementLimit];
			System.arraycopy(elements, 0, shownElements, 0, elementLimit);
			return shownElements;
		}
		return elements;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof FileSearchResult) {
			FileSearchResult fileSearchResult = (FileSearchResult) inputElement;
			int elementLimit= getElementLimit();
			Object[] elements = fileSearchResult.getElements();
			if (elementLimit != -1 && elements.length > elementLimit) {
				Object[] shownElements= new Object[elementLimit];
				System.arraycopy(elements, 0, shownElements, 0, elementLimit);
				return shownElements;
			}
			if (fileSearchResult.getActiveMatchFilters().length > 0) {
				List<Object> elementList = new ArrayList<>();
				for (Object element : elements) {
					if (fPage.getDisplayedMatchCount(element) > 0) {
						elementList.add(element);
					}
				}
				elements = elementList.toArray();
			}
			return elements;
		}
		return EMPTY_ARR;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof FileSearchResult) {
			fResult= (FileSearchResult) newInput;
		}
	}

	@Override
	public void elementsChanged(Object[] updatedElements) {
		TableViewer viewer= getViewer();
		int elementLimit= getElementLimit();
		boolean tableLimited= elementLimit != -1;
		for (Object updatedElement : updatedElements) {
			if (fPage.getDisplayedMatchCount(updatedElement) > 0) {
				if (viewer.testFindItem(updatedElement) != null)
					viewer.update(updatedElement, null);
				else {
					if (!tableLimited || viewer.getTable().getItemCount() < elementLimit)
						viewer.add(updatedElement);
				}
			} else
				viewer.remove(updatedElement);
		}
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
