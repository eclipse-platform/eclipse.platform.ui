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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
/**
 * @author Thomas Mäder
 *  
 */
public class FileTreeContentProvider extends FileContentProvider implements ITreeContentProvider {
	private AbstractTreeViewer fTreeViewer;
	private Map fChildrenMap;
	
	FileTreeContentProvider(AbstractTreeViewer viewer) {
		fTreeViewer= viewer;
	}
	
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}
	

	protected synchronized void initialize(FileSearchResult result) {
		super.initialize(result);
		fChildrenMap= new HashMap();
		Object[] elements= result.getElements();
		for (int i = 0; i < elements.length; i++) {
			insert(elements[i], false);
		}
	}

	private void insert(Object child, boolean refreshViewer) {
		Object parent= getParent(child);
		while(parent != null) {
			if (insertChild(parent, child)) {
				if (refreshViewer)
					fTreeViewer.add(parent, child);
			} else {
				return;
			}
			child= parent;
			parent= getParent(child);
		}
		if (insertChild(this, child)) {
			if (refreshViewer)
				fTreeViewer.add(fResult, child);
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
	
	private void remove(Object child, boolean refreshViewer) {
		Object parent= getParent(child);
		if (fResult.getMatchCount(child) == 0) {
			fChildrenMap.remove(child);
			Set container= (Set) fChildrenMap.get(parent);
			if (container != null) {
				container.remove(child);
				if (container.size() == 0)
					remove(parent, refreshViewer);
			}
			if (refreshViewer) {
				fTreeViewer.remove(child);
			}
		} else {
			if (refreshViewer) {
				fTreeViewer.refresh(child);
			}
		}
	}

	
	public Object[] getChildren(Object parentElement) {
		Set children= (Set) fChildrenMap.get(parentElement);
		if (children == null)
			return EMPTY_ARR;
		return children.toArray();
	}
	
	public Object getParent(Object element) {
		if (element instanceof IProject)
			return fResult;
		if (element instanceof IResource) {
			IResource resource = (IResource) element;
			return resource.getParent();
		}
		return null;
	}
	
	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	public synchronized void elementsChanged(Object[] updatedElements) {
		for (int i= 0; i < updatedElements.length; i++) {
			if (fResult.getMatchCount(updatedElements[i]) > 0)
				insert(updatedElements[i], true);
			else
				remove(updatedElements[i], true);
			
		}
	}
	
	public void clear() {
		initialize(fResult);
		fTreeViewer.refresh();
	}
}
