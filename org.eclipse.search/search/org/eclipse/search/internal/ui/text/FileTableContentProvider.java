/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui.text;

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

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof FileSearchResult) {
			int elementLimit= getElementLimit();
			Object[] elements= ((FileSearchResult)inputElement).getElements();
			if (elementLimit != -1 && elements.length > elementLimit) {
				Object[] shownElements= new Object[elementLimit];
				System.arraycopy(elements, 0, shownElements, 0, elementLimit);
				return shownElements;
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
			if (fResult.getMatchCount(updatedElement) > 0) {
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
