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
package org.eclipse.search2.internal.ui.basic.views;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.search.ui.ISearchResultChangedListener;
import org.eclipse.search.ui.text.IStructureProvider;
import org.eclipse.search.ui.text.ITextSearchResult;

/**
 * @author Thomas Mäder
 *
 */
public class SearchResultTreeModel extends SearchResultModel implements ISearchResultChangedListener {

	private Map fChildrenMap;
	public SearchResultTreeModel(DefaultSearchViewPage page, ITextSearchResult result) {
		super(result, page);
		fChildrenMap= new HashMap();
		initializeTree();
	}
	
	private void initializeTree() {
		if (fPage == null)
			return;
		Object[] elements= fResult.getElements();
		for (int i= 0; i < elements.length; i++) {
			insert(fResult.getStructureProvider(), elements[i], false);
		}
	}

	protected synchronized void clear() {
		fChildrenMap.clear();
		super.clear();
		fPage.refresh();
	}
	
	protected void remove(IStructureProvider structureProvider, Object child, boolean refreshViewer) {
		Object parent= structureProvider.getParent(child);
		if (fResult.getMatchCount(child) == 0) {
			fChildrenMap.remove(child);
			Set container= (Set) fChildrenMap.get(parent);
			if (container != null) {
				container.remove(child);
				if (container.size() == 0)
					remove(structureProvider, parent, refreshViewer);
			}
			if (refreshViewer) {
				fPage.handleRemove(child);
			}
		} else {
			if (refreshViewer) {
				fPage.handleUpdate(child);
			}
		}
	}

	protected void insert(IStructureProvider structureProvider, Object child, boolean refreshViewer) {
		Object parent= structureProvider.getParent(child);
		while(parent != null) {
			if (insertChild(parent, child)) {
				if (refreshViewer)
					fPage.handleInsert(parent, child);
			} else {
				return;
			}
			child= parent;
			parent= structureProvider.getParent(child);
		}
		if (insertChild(this, child)) {
			if (refreshViewer)
				fPage.handleInsert(this, child);
		}
	}	
	
	/**
	 * returns true if the child already was a child of parent.
	 * @param parent
	 * @param child
	 * @return
	 */
	private boolean insertChild(Object parent, Object child) {
		Set children= (Set) fChildrenMap.get(parent);
		if (children == null) {
			children= new HashSet();
			fChildrenMap.put(parent, children);
		}
		return children.add(child);
	}
	
	public Object[] getChildren(Object parent) {
		if (fChildrenMap == null)
			initializeTree();
		Set children= (Set) fChildrenMap.get(parent);
		if (children == null)
			return EMPTY_ARRAY;
		return children.toArray();
	}
	
	boolean hasChildren(Object parent) {
		return fChildrenMap.containsKey(parent);
	}
	
	Object getParent(Object object) {
		Object parent= getResult().getStructureProvider().getParent(object);
		if (parent == null)
			return this;
		return parent;
	}
	
	

}
