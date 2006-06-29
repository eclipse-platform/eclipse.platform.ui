/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tools.search;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.*;
import org.eclipse.search.ui.ISearchResultPage;
import org.eclipse.search.ui.text.*;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IShowInTargetList;

/**
 * 
 */
public class FindUnusedSearchResultPage extends AbstractTextSearchViewPage implements ISearchResultPage {

	private static final String[] SHOW_IN_TARGETS= new String[] { JavaUI.ID_PACKAGES , IPageLayout.ID_RES_NAV };
	public static final IShowInTargetList SHOW_IN_TARGET_LIST= new IShowInTargetList() {
		public String[] getShowInTargetIds() {
			return SHOW_IN_TARGETS;
		}
	};
	
	public static class TableContentProvider implements IStructuredContentProvider {
		private AbstractTextSearchResult fSearchResult;
		private TableViewer fTableViewer;

		public Object[] getElements(Object inputElement) {
			if (fSearchResult != null)
				return fSearchResult.getElements();
			return new Object[0];
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			fTableViewer= (TableViewer) viewer;
			fSearchResult= (AbstractTextSearchResult) newInput;
		}

		public void elementsChanged(Object[] updatedElements) {
			int addCount= 0;
			int removeCount= 0;
			for (int i= 0; i < updatedElements.length; i++) {
				if (fSearchResult.getMatchCount(updatedElements[i]) > 0) {
					if (fTableViewer.testFindItem(updatedElements[i]) != null)
						fTableViewer.refresh(updatedElements[i]);
					else
						fTableViewer.add(updatedElements[i]);
					addCount++;
				} else {
					fTableViewer.remove(updatedElements[i]);
					removeCount++;
				}
			}
		}
		
		public void clear() {
			fTableViewer.refresh();
		}
	}
	
	
	private TableContentProvider fContentProvider;

	public FindUnusedSearchResultPage() {
		super(AbstractTextSearchViewPage.FLAG_LAYOUT_FLAT);
	}
	
	protected void showMatch(Match match, int currentOffset, int currentLength, boolean activate) throws PartInitException {
		try {
			Object element= match.getElement();
			if (element instanceof IJavaElement) {
				JavaUI.openInEditor((IJavaElement) element);
			}
		} catch (JavaModelException e1) {
			throw new PartInitException(e1.getStatus());
		}
	}
	
	/*
	 * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#elementsChanged(java.lang.Object[])
	 */
	protected void elementsChanged(Object[] objects) {
		if (fContentProvider != null)
			fContentProvider.elementsChanged(objects);
	}

	/*
	 * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#clear()
	 */
	protected void clear() {
		if (fContentProvider != null)
			fContentProvider.clear();
	}

	/*
	 * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#configureTreeViewer(org.eclipse.jface.viewers.TreeViewer)
	 */
	protected void configureTreeViewer(TreeViewer viewer) {
		throw new IllegalStateException("Doesn't support tree mode."); //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#configureTableViewer(org.eclipse.jface.viewers.TableViewer)
	 */
	protected void configureTableViewer(TableViewer viewer) {
		viewer.setLabelProvider(new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_POST_QUALIFIED));
		fContentProvider= new TableContentProvider();
		viewer.setContentProvider(fContentProvider);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (IShowInTargetList.class.equals(adapter)) {
			return SHOW_IN_TARGET_LIST;
		}
		return null;
	}

}
