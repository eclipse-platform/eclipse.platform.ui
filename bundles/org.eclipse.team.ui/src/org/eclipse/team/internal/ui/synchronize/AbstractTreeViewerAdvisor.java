/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize;

import org.eclipse.compare.internal.INavigatable;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Abstract superclass for tree viewer advisors
 */
public class AbstractTreeViewerAdvisor extends StructuredViewerAdvisor implements IAdaptable {

	private INavigatable nav;
	
	/**
	 * Interface used to implement navigation for tree viewers. This interface is used by
	 * {@link TreeViewerAdvisor#navigate(TreeViewer, boolean, boolean, boolean) to open 
	 * selections and navigate.
	 */
	public interface ITreeViewerAccessor {
		public void createChildren(TreeItem item);
		public void openSelection();
	}
	
	private static TreeItem findNextPrev(TreeViewer viewer, TreeItem item, boolean next) {
		if (item == null || !(viewer instanceof ITreeViewerAccessor))
			return null;
		TreeItem children[] = null;
		ITreeViewerAccessor treeAccessor = (ITreeViewerAccessor) viewer;
		if (!next) {
			TreeItem parent = item.getParentItem();
			if (parent != null)
				children = parent.getItems();
			else
				children = item.getParent().getItems();
			if (children != null && children.length > 0) {
				// goto previous child
				int index = 0;
				for (; index < children.length; index++)
					if (children[index] == item)
						break;
				if (index > 0) {
					item = children[index - 1];
					while (true) {
						treeAccessor.createChildren(item);
						int n = item.getItemCount();
						if (n <= 0)
							break;
						item.setExpanded(true);
						item = item.getItems()[n - 1];
					}
					// previous
					return item;
				}
			}
			// go up
			return parent;
		} else {
			item.setExpanded(true);
			treeAccessor.createChildren(item);
			if (item.getItemCount() > 0) {
				// has children: go down
				children = item.getItems();
				return children[0];
			}
			while (item != null) {
				children = null;
				TreeItem parent = item.getParentItem();
				if (parent != null)
					children = parent.getItems();
				else
					children = item.getParent().getItems();
				if (children != null && children.length > 0) {
					// goto next child
					int index = 0;
					for (; index < children.length; index++)
						if (children[index] == item)
							break;
					if (index < children.length - 1) {
						// next
						return children[index + 1];
					}
				}
				// go up
				item = parent;
			}
		}
		return item;
	}

	private static void setSelection(TreeViewer viewer, TreeItem ti, boolean fireOpen, boolean expandOnly) {
		if (ti != null) {
			Object data= ti.getData();
			if (data != null) {
				// Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=20106
				ISelection selection = new StructuredSelection(data);
				if (expandOnly) {
					viewer.expandToLevel(data, 0);
				} else {
					viewer.setSelection(selection, true);
					ISelection currentSelection = viewer.getSelection();
					if (fireOpen && currentSelection != null && selection.equals(currentSelection)) {
						if (viewer instanceof ITreeViewerAccessor) {
							((ITreeViewerAccessor) viewer).openSelection();
						}
					}
				}
			}
		}
	}

	/**
	 * Selects the next (or previous) node of the current selection.
	 * If there is no current selection the first (last) node in the tree is selected.
	 * Wraps around at end or beginning.
	 * Clients may not override. 
	 *
	 * @param next if <code>true</code> the next node is selected, otherwise the previous node
	 * @return <code>true</code> if at end (or beginning)
	 */
	public static boolean navigate(TreeViewer viewer, boolean next, boolean fireOpen, boolean expandOnly) {
		Tree tree = viewer.getTree();
		if (tree == null)
			return false;
		TreeItem item = null;
		TreeItem children[] = tree.getSelection();
		if (children != null && children.length > 0)
			item = children[0];
		if (item == null) {
			children = tree.getItems();
			if (children != null && children.length > 0) {
				item = children[0];
				if (item != null && item.getItemCount() <= 0) {
					setSelection(viewer, item, fireOpen, expandOnly); // Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=20106
					return false;
				}
			}
		}
		while (true) {
			item = findNextPrev(viewer, item, next);
			if (item == null)
				break;
			if (item.getItemCount() <= 0)
				break;
		}
		if (item != null) {
			setSelection(viewer, item, fireOpen, expandOnly); // Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=20106
			return false;
		}
		return true;
	}

	public AbstractTreeViewerAdvisor(ISynchronizePageConfiguration configuration) {
		super(configuration);
		INavigatable nav = (INavigatable)configuration.getProperty(SynchronizePageConfiguration.P_NAVIGATOR);
		if (nav == null) {
			configuration.setProperty(SynchronizePageConfiguration.P_NAVIGATOR, getAdapter(INavigatable.class));
		}
		configuration.addActionContribution(new NavigationActionGroup());
	}

	/**
	 * Allow navigation in tree viewers.
	 * 
	 * @param next if <code>true</code> then navigate forwards, otherwise navigate
	 * backwards.
	 * @return <code>true</code> if the end is reached, and <code>false</code> otherwise.
	 */
	public boolean navigate(boolean next) {
		return navigate((TreeViewer)getViewer(), next, false, false);
	}

	/* (non-Javadoc)
	 * Allow adding an advisor to the PartNavigator and support coordinated
 	 * navigation between several objects.
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if(adapter == INavigatable.class) {
			if(nav == null) {
				nav = new INavigatable() {
					public boolean gotoDifference(boolean next) {
						return AbstractTreeViewerAdvisor.this.navigate(next);
					}
				};
			}
			return nav;
		}
		return null;
	}
	
	/**
	 * Handles a double-click event from the viewer. Expands or collapses a folder when double-clicked.
	 * 
	 * @param viewer the viewer
	 * @param event the double-click event
	 */
	protected boolean handleDoubleClick(StructuredViewer viewer, DoubleClickEvent event) {
		if (super.handleDoubleClick(viewer, event)) return true;
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		Object element = selection.getFirstElement();
		AbstractTreeViewer treeViewer = (AbstractTreeViewer) getViewer();
		if(element != null) {
			if (treeViewer.getExpandedState(element)) {
				treeViewer.collapseToLevel(element, AbstractTreeViewer.ALL_LEVELS);
			} else {
				AbstractTreeViewerAdvisor.navigate((TreeViewer)getViewer(), true /* next */, false /* no-open */, true /* only-expand */);
			}
		}
		return true;
	}
}
