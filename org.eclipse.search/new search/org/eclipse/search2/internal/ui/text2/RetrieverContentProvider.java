/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/

package org.eclipse.search2.internal.ui.text2;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.search.ui.text.AbstractTextSearchResult;

import org.eclipse.search.internal.ui.text.IFileSearchContentProvider;

public class RetrieverContentProvider implements ITreeContentProvider, IFileSearchContentProvider {
	private static final Object[] EMPTY_ARR= new Object[0];
	private RetrieverResult fResult;
	private RetrieverTreeViewer fTreeViewer;
	private Map fChildrenMap;
	private int[] fMatchCount;
	private HashSet fAutoExpand= new HashSet();
	private boolean fFlatLayout;

	public RetrieverContentProvider(RetrieverTreeViewer viewer, boolean flatLayout) {
		fTreeViewer= viewer;
		fFlatLayout= flatLayout;
	}

	public Object getParent(Object element) {
		if (element instanceof RetrieverLine) {
			return ((RetrieverLine) element).getParent();
		}
		if (element instanceof IProject) {
			return null;
		}
		if (element instanceof IResource) {
			IResource resource= (IResource) element;
			return fFlatLayout ? null : resource.getParent();
		}
		return null;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
		// nothing to do
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof AbstractTextSearchResult) {
			initialize((AbstractTextSearchResult) newInput);
		}
	}


	protected synchronized void initialize(AbstractTextSearchResult result) {
		fResult= (RetrieverResult) result;
		fChildrenMap= new HashMap();
		fAutoExpand.clear();
		fMatchCount= null;
		if (result != null) {
			Object[] elements= result.getElements();
			for (int i= 0; i < elements.length; i++) {
				Object element= elements[i];
				if (fResult.getDisplayedMatchCount(element) > 0) {
					insert(element, false, null, null);
				}
			}
			updateFilterItem(false, null, null);
		}
	}

	protected void insert(Object child, boolean refreshViewer, HashSet refreshElems, HashMap insertElems) {
		Object parent= getParent(child);
		Object dummyParent= parent;
		if (parent == null) {
			dummyParent= fResult;
		}

		// add child to our childmap
		if (insertInMap(dummyParent, child, fChildrenMap)) {
			if (refreshViewer) {
				insertInMap(dummyParent, child, insertElems);
			}
			if (parent != null) {
				insert(parent, refreshViewer, refreshElems, insertElems);
			}
		}
	}

	/**
	 * returns true if the child was added.
	 */
	private boolean insertInMap(Object parent, Object child, Map map) {
		Set children= (Set) map.get(parent);
		if (children == null) {
			children= new HashSet();
			map.put(parent, children);
		}
		return children.add(child);
	}

	protected void remove(Object element, boolean refreshViewer, HashSet refreshElems) {
		// precondition here:  fResult.getMatchCount(child) <= 0

		if (hasChildren(element)) {
			if (refreshViewer) {
				refreshElems.add(element);
			}
		} else {
			fChildrenMap.remove(element);
			Object parent= getParent(element);
			if (parent != null) {
				if (removeFromSiblings(element, parent)) {
					remove(parent, refreshViewer, refreshElems);
				}
			} else {
				if (removeFromSiblings(element, fResult)) {
					if (refreshViewer) {
						refreshElems.add(fResult);
					}
				}
			}
		}
	}

	private boolean removeFromSiblings(Object element, Object parent) {
		Set siblings= (Set) fChildrenMap.get(parent);
		if (siblings != null) {
			return siblings.remove(element);
		}
		return false;
	}

	public Object[] getChildren(Object parentElement) {
		Set children= (Set) fChildrenMap.get(parentElement);
		if (children == null)
			return EMPTY_ARR;
		return children.toArray();
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	public synchronized void elementsChanged(final Object[] updatedElements) {
		if (updatedElements.length == 0) {
			return;
		}
		Runnable r= new Runnable() {
			public void run() {
				handeElementsChanged(updatedElements);
			}
		};
		fTreeViewer.preservingSelection(r);
	}

	private void handeElementsChanged(final Object[] updatedElements) {
		final HashSet refreshElems= new HashSet();
		final HashMap insertElems= new HashMap();
		updateFilterItem(true, refreshElems, insertElems);
		for (int i= 0; i < updatedElements.length; i++) {
			Object updatedElem= updatedElements[i];
			if (fResult.getDisplayedMatchCount(updatedElem) > 0) {
				insert(updatedElem, true, refreshElems, insertElems);
			} else {
				remove(updatedElem, true, refreshElems);
			}
		}
		if (refreshElems.contains(fResult)) {
			fTreeViewer.refresh();
		} else {
			for (Iterator iter= refreshElems.iterator(); iter.hasNext();) {
				Object elem= iter.next();
				Object parent= getParent(elem);
				if (!containsElemOrAParent(refreshElems, parent)) {
					fTreeViewer.refresh(elem);
				}
			}
		}

		Object[] insertParents= insertElems.keySet().toArray();
		for (int i= 0; i < insertParents.length; i++) {
			Object element= insertParents[i];
			insertInView(element, refreshElems, insertElems);
		}
	}

	private void updateFilterItem(boolean updateViewer, final HashSet refreshElems, final HashMap insertElems) {
		if (fResult != null) {
			int[] matchCount= fResult.getDetailedMatchCount();
			if (matchCount[0] == matchCount[1]) {
				if (fMatchCount != null) {
					remove(fMatchCount, updateViewer, refreshElems);
					fMatchCount= null;
				}
			} else {
				if (fMatchCount != null) {
					fMatchCount[0]= matchCount[0];
					fMatchCount[1]= matchCount[1];
					refreshElems.add(fMatchCount);
				} else {
					fMatchCount= matchCount;
					insert(fMatchCount, updateViewer, refreshElems, insertElems);
				}
			}
		}
	}

	private boolean insertInView(Object element, HashSet refreshElems, HashMap insertElems) {
		Object parent= getParent(element);
		if (parent == null) {
			parent= fResult;
		}
		Collection children= (Collection) insertElems.remove(element);
		if (children == null) {
			return !containsElemOrAParent(refreshElems, element);
		}

		boolean needToInsert= true;
		if (element != fResult) {
			needToInsert= insertInView(parent, refreshElems, insertElems);
		}
		if (needToInsert) {
			needToInsert= !refreshElems.contains(element);
		}
		if (needToInsert) {
			fTreeViewer.add(element, children.toArray());
		}
		if (fAutoExpand.contains(element)) {
			fTreeViewer.setExpandedState(element, true);
		}
		return needToInsert;
	}

	private boolean containsElemOrAParent(final HashSet set, Object elem) {
		while (elem != null) {
			if (set.contains(elem)) {
				return true;
			}
			elem= getParent(elem);
		}
		return set.contains(fResult);
	}

	public void clear() {
		initialize(fResult);
		fTreeViewer.refresh();
	}

	public AbstractTextSearchResult getResult() {
		return fResult;
	}

	public void onExpansionStateChange(Object element, boolean expanded) {
		if (!expanded) {
			fAutoExpand.remove(element);
		} else {
			fAutoExpand.add(element);
		}
	}

	public void setLayout(boolean flat) {
		if (flat != fFlatLayout) {
			fFlatLayout= flat;
			initialize(fResult);
		}
	}

	// this works around the fact that expansion change is not reported when
	// the selection is set via next/previous.
	public void onSelectionChanged(SelectionChangedEvent event) {
		IStructuredSelection sel= (IStructuredSelection) event.getSelection();
		Object element= sel.getFirstElement();
		if (element != null) {
			element= getParent(element);
			while (element != null && fAutoExpand.add(element)) {
				element= getParent(element);
			}
		}
	}
}
