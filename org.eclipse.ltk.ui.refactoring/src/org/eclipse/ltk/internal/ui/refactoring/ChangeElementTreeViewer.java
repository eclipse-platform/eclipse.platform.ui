/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

class ChangeElementTreeViewer extends CheckboxTreeViewer {

	private static class GroupCategoryFilter extends ViewerFilter {
		private List fGroupCategories;
		public void setGroupCategory(List groupCategories) {
			fGroupCategories= groupCategories;
		}
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (fGroupCategories == null)
				return true;
			return ((PreviewNode)element).hasOneGroupCategory(fGroupCategories);
		}
	}

	private static class DerivedFilter extends ViewerFilter {
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			return ! ((PreviewNode) element).hasDerived();
		}
	}

	private static final DerivedFilter DERIVED_FILTER= new DerivedFilter();


	// Workaround for http://bugs.eclipse.org/bugs/show_bug.cgi?id=9390
	private List fDeferredTreeItemUpdates;

	public ChangeElementTreeViewer(Composite parentComposite) {
		super(parentComposite, SWT.NONE);
		addFilter(new GroupCategoryFilter());
		addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event){
				PreviewNode element= (PreviewNode)event.getElement();
				boolean checked= event.getChecked();

				element.setEnabled(checked);
				setSubtreeChecked(element, checked);
				setSubtreeGrayed(element, false);
				PreviewNode parent= element.getParent();
				while(parent != null) {
					int active= parent.getActive();
					parent.setEnabledShallow(active == PreviewNode.PARTLY_ACTIVE || active == PreviewNode.ACTIVE);
					boolean grayed= (active == PreviewNode.PARTLY_ACTIVE);
					setChecked(parent, checked ? true : grayed);
					setGrayed(parent, grayed);
					parent= parent.getParent();
				}
			}
		});
	}

	public void setGroupCategory(List/*<GroupCategory>*/ groupCategories) {
		((GroupCategoryFilter)(getFilters()[0])).setGroupCategory(groupCategories);
		refresh();
	}

	public void setHideDerived(boolean hide) {
		if (hide) {
			addFilter(DERIVED_FILTER);
		} else {
			removeFilter(DERIVED_FILTER);
		}
	}

	public void refresh() {
		try {
			fDeferredTreeItemUpdates= new ArrayList();
			super.refresh();
			processDeferredTreeItemUpdates();
		} finally  {
			fDeferredTreeItemUpdates= null;
		}
	}

	protected void handleInvalidSelection(ISelection invalidSelection, ISelection newSelection) {
		PreviewNode next= getLeaf((PreviewNode)getInput(), true);
		if (next != null) {
			newSelection= new StructuredSelection(next);
			setSelection(newSelection);
		}
		super.handleInvalidSelection(invalidSelection, newSelection);
	}

	protected void inputChanged(Object input, Object oldInput) {
		try {
			fDeferredTreeItemUpdates= new ArrayList();
			super.inputChanged(input, oldInput);
			processDeferredTreeItemUpdates();
		} finally {
			fDeferredTreeItemUpdates= null;
		}
	}

	protected void doUpdateItem(Item item, Object element) {
		super.doUpdateItem(item, element);
		if (fDeferredTreeItemUpdates == null) {
			applyCheckedState((TreeItem)item, (PreviewNode)element);
		} else {
			fDeferredTreeItemUpdates.add(item);
		}
	}

	private void processDeferredTreeItemUpdates() {
		for (Iterator iter= fDeferredTreeItemUpdates.iterator(); iter.hasNext();) {
			TreeItem item= (TreeItem)iter.next();
			applyCheckedState(item, (PreviewNode)item.getData());
		}
	}

	private void applyCheckedState(TreeItem item, PreviewNode ce) {
		int state= ce.getActive();
		boolean checked= state == PreviewNode.INACTIVE ? false : true;
		item.setChecked(checked);
		boolean grayed= state == PreviewNode.PARTLY_ACTIVE ? true : false;
		item.setGrayed(grayed);
	}

	protected void revealNext() {
		revealElement(true);
	}

	protected void revealPrevious() {
		revealElement(false);
	}

	private void setSubtreeGrayed(Object element, boolean grayed) {
		Widget widget= findItem(element);
		if (widget instanceof TreeItem) {
			TreeItem item= (TreeItem)widget;
			if (item.getGrayed() != grayed) {
				item.setGrayed(grayed);
				grayChildren(getChildren(item), grayed);
			}
		}
	}

	private void grayChildren(Item[] items, boolean grayed) {
		for (int i= 0; i < items.length; i++) {
			Item element= items[i];
			if (element instanceof TreeItem) {
				TreeItem item= (TreeItem)element;
				if (item.getGrayed() != grayed) {
					item.setGrayed(grayed);
					grayChildren(getChildren(item), grayed);
				}
			}
		}
	}

	private void revealElement(boolean next) {
		PreviewNode current= (PreviewNode)getInput();
		IStructuredSelection selection= (IStructuredSelection)getSelection();
		if (!selection.isEmpty())
			current= (PreviewNode)selection.iterator().next();

		PreviewNode candidate= getLeaf(current, next);
		if (candidate == null) {
			candidate= getElement(current, next);
			if (candidate != null) {
				PreviewNode leaf= getLeaf(candidate, next);
				if (leaf != null)
					candidate= leaf;
			}
		}
		if (candidate != null)
			setSelection(new StructuredSelection(candidate), true);
		else
			getControl().getDisplay().beep();
	}

	private PreviewNode getLeaf(PreviewNode element, boolean first) {
		PreviewNode result= null;
		PreviewNode[] children= getSortedChildrenAsPreviewNodes(element);
		while(children != null && children.length > 0) {
			result= children[first ? 0 : children.length - 1];
			children= getSortedChildrenAsPreviewNodes(result);
		}
		return result;
	}

	private PreviewNode getElement(PreviewNode element, boolean next) {
		while(true) {
			PreviewNode parent= element.getParent();
			if (parent == null)
				return null;
			PreviewNode candidate= getSibling(
				getSortedChildrenAsPreviewNodes(parent),
				element, next);
			if (candidate != null)
				return candidate;
			element= parent;
		}
	}

	private PreviewNode getSibling(PreviewNode[] children, PreviewNode element, boolean next) {
		for (int i= 0; i < children.length; i++) {
			if (children[i] == element) {
				if (next)
	 				if (i < children.length - 1)
	 					return children[i + 1];
	 				else
	 					return null;
	 			else
					if (i > 0)
	 					return children[i - 1];
	 				else
	 					return null;
			}
		}
		return null;
	}

	private PreviewNode[] getSortedChildrenAsPreviewNodes(PreviewNode parent) {
		Object[] sorted= getSortedChildren(parent);
		PreviewNode[] result= new PreviewNode[sorted.length];
		for (int i= 0; i < result.length; i++) {
			result[i]= (PreviewNode)sorted[i];
		}
		return result;
	}
}
