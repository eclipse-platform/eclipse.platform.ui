/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui.text;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.search.internal.ui.SearchPreferencePage;

public class FileTableContentProvider extends FileContentProvider implements IStructuredContentProvider {
	private FileSearchPage fPage;

	public FileTableContentProvider(FileSearchPage page) {
		fPage= page;
	}
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof FileSearchResult) {
			Object[] elements= ((FileSearchResult)inputElement).getElements();
			int tableLimit= SearchPreferencePage.getTableLimit();
			if (SearchPreferencePage.isTableLimited() && elements.length > tableLimit) {
				Object[] shownElements= new Object[tableLimit];
				System.arraycopy(elements, 0, shownElements, 0, tableLimit);
				return shownElements;
			}
			return elements;
		}
		return EMPTY_ARR;
	}
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof FileSearchResult) {
			fResult= (FileSearchResult) newInput;
		}
	}
	
	public void elementsChanged(Object[] updatedElements) {
		TableViewer viewer= getViewer();
		boolean tableLimited= SearchPreferencePage.isTableLimited();
		for (int i= 0; i < updatedElements.length; i++) {
			if (fResult.getMatchCount(updatedElements[i]) > 0) {
				if (viewer.testFindItem(updatedElements[i]) != null)
					viewer.update(updatedElements[i], null);
				else {
					if (!tableLimited || viewer.getTable().getItemCount() < SearchPreferencePage.getTableLimit())
						viewer.add(updatedElements[i]);
				}
			} else
				viewer.remove(updatedElements[i]);
		}
	}

	private TableViewer getViewer() {
		return (TableViewer) fPage.getViewer();
	}
	public void clear() {
		getViewer().refresh();
	}
}
