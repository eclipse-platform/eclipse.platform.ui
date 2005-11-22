/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * A tree viewer that retrieves children and labels asynchronously via adapters
 * and supports duplicate elements in the tree with different parents.
 * Retrieving children and labels asynchrnously allows for arbitrary latency
 * without blocking the UI thread.
 * <p>
 * TODO: tree editor not implemented TODO: table tree - what implications does
 * it have on IPresentationAdapter?
 * 
 * TODO: Deprecate the public/abstract deferred workbench adapter in favor of
 * the presentation adapter.
 * </p>
 * <p>
 * Clients may instantiate and subclass this class.
 * </p>
 * 
 * @since 3.2
 */
public class AsynchronousTreeViewer extends AsynchronousViewer {

	/**
	 * A map of widget to parent widgets used to avoid requirement for parent
	 * access in UI thread. Currently used by update objects to detect/cancel
	 * updates on updates of children.
	 */
	private Map fItemToParentItem = new HashMap();

	/**
	 * The tree
	 */
	private Tree fTree;

	/**
	 * Array of tree paths to be expanded. As paths are expanded, those entries
	 * are set to <code>null</code>.
	 */
	private List fPendingExpansion = new ArrayList();

	/**
	 * Creates an asynchronous tree viewer on a newly-created tree control under
	 * the given parent. The tree control is created using the SWT style bits
	 * <code>MULTI, H_SCROLL, V_SCROLL,</code> and <code>BORDER</code>. The
	 * viewer has no input, no content provider, a default label provider, no
	 * sorter, and no filters.
	 * 
	 * @param parent the parent control
	 */
	public AsynchronousTreeViewer(Composite parent) {
		this(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
	}

	/**
	 * Creates an asynchronous tree viewer on a newly-created tree control under
	 * the given parent. The tree control is created using the given SWT style
	 * bits. The viewer has no input.
	 * 
	 * @param parent the parent control
	 * @param style the SWT style bits used to create the tree.
	 */
	public AsynchronousTreeViewer(Composite parent, int style) {
		this(new Tree(parent, style));
	}

	/**
	 * Creates an asynchronous tree viewer on the given tree control. The viewer
	 * has no input, no content provider, a default label provider, no sorter,
	 * and no filters.
	 * 
	 * @param tree the tree control
	 */
	public AsynchronousTreeViewer(Tree tree) {
		super();
		fTree = tree;
		hookControl(fTree);
		setUseHashlookup(false);
		tree.addTreeListener(new TreeListener() {
			public void treeExpanded(TreeEvent e) {
				((TreeItem) e.item).setExpanded(true);
				internalRefresh(e.item.getData(), e.item);
			}

			public void treeCollapsed(TreeEvent e) {
			}
		});
		tree.addMouseListener(new MouseListener() {

			public void mouseUp(MouseEvent e) {
			}

			public void mouseDown(MouseEvent e) {
			}

			public void mouseDoubleClick(MouseEvent e) {
				TreeItem item = ((Tree) e.widget).getItem(new Point(e.x, e.y));
				if (item != null) {
					if (item.getExpanded()) {
						item.setExpanded(false);
					} else {
						item.setExpanded(true);
						internalRefresh(item.getData(), item);
					}
				}
			}
		});
	}

	/**
	 * Returns the tree control for this viewer.
	 * 
	 * @return the tree control for this viewer
	 */
	public Tree getTree() {
		return fTree;
	}

	/**
	 * Updates whether the given element has children.
	 * 
	 * @param element element to update
	 * @param widget widget associated with the element in this viewer's tree
	 */
	protected void updateHasChildren(Object element, Widget widget) {
		IAsynchronousTreeContentAdapter adapter = getTreeContentAdapter(element);
		if (adapter != null) {
			IContainerRequestMonitor update = new ContainerRequestMonitor(widget, this);
			schedule(update);
			adapter.isContainer(element, getPresentationContext(), update);
		}
	}

	/**
	 * Updates the children of the given element.
	 * 
	 * @param parent element of which to update children
	 * @param widget widget associated with the element in this viewer's tree
	 */
	protected void updateChildren(Object parent, Widget widget) {
		IAsynchronousTreeContentAdapter adapter = getTreeContentAdapter(parent);
		if (adapter != null) {
			IChildrenRequestMonitor update = new ChildrenRequestMonitor(widget, this);
			schedule(update);
			adapter.retrieveChildren(parent, getPresentationContext(), update);
		}
	}

	/**
	 * Returns the tree element adapter for the given element or
	 * <code>null</code> if none.
	 * 
	 * @param element element to retrieve adapter for
	 * @return presentation adapter or <code>null</code>
	 */
	protected IAsynchronousTreeContentAdapter getTreeContentAdapter(Object element) {
		IAsynchronousTreeContentAdapter adapter = null;
		if (element instanceof IAsynchronousTreeContentAdapter) {
			adapter = (IAsynchronousTreeContentAdapter) element;
		} else if (element instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) element;
			adapter = (IAsynchronousTreeContentAdapter) adaptable.getAdapter(IAsynchronousTreeContentAdapter.class);
		}
		return adapter;
	}

	/**
	 * Expands all elements in the given tree selection.
	 * 
	 * @param selection
	 */
	public synchronized void expand(ISelection selection) {
		if (selection instanceof TreeSelection) {
            TreePath[] paths = ((TreeSelection) selection).getPaths();
            for (int i = 0; i < paths.length; i++) {
                fPendingExpansion.add(paths[i]);
            }
			if (getControl().getDisplay().getThread() == Thread.currentThread()) {
				attemptExpansion();
			} else {
				WorkbenchJob job = new WorkbenchJob("attemptExpansion") { //$NON-NLS-1$
					public IStatus runInUIThread(IProgressMonitor monitor) {
						attemptExpansion();
						return Status.OK_STATUS;
					}

				};
				job.setSystem(true);
				job.schedule();
			}
		}
	}

	/**
	 * Attempts to expand all pending expansions.
	 */
	synchronized void attemptExpansion() {
		if (fPendingExpansion != null) {
            for (Iterator i = fPendingExpansion.iterator(); i.hasNext();) {
                TreePath path = (TreePath) i.next();
                if (path != null && attemptExpansion(path)) {
                    i.remove();
                }
            }
		}
	}

	/**
	 * Attempts to expand the given tree path and returns whether the expansion
	 * was completed.
	 * 
	 * @param path path to exapand
	 * @return whether the expansion was completed
	 */
	synchronized boolean attemptExpansion(TreePath path) {
		int segmentCount = path.getSegmentCount();
		for (int j = segmentCount - 1; j >= 0; j--) {
			Object element = path.getSegment(j);
			Widget[] treeItems = getWidgets(element);
			if (treeItems != null) {
				for (int k = 0; k < treeItems.length; k++) {
					if (treeItems[k] instanceof TreeItem) {
						TreeItem treeItem = (TreeItem) treeItems[k];
						TreePath treePath = getTreePath(treeItem);
						if (path.startsWith(treePath)) {
							if (!treeItem.getExpanded() && treeItem.getItemCount() > 0) {
								update(element);
								updateChildren(element, treeItem);
								expand(treeItem);
								if (path.getSegmentCount() == treePath.getSegmentCount()) {
									return true;
								}
								return false;
							}
						}
					}
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.Viewer#getControl()
	 */
	public Control getControl() {
		return fTree;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#unmapAllElements()
	 */
	protected synchronized void unmapAllElements() {
		super.unmapAllElements();
		fItemToParentItem.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.Viewer#inputChanged(java.lang.Object,
	 *      java.lang.Object)
	 */
	synchronized protected void inputChanged(Object input, Object oldInput) {
        fPendingExpansion.clear();
		super.inputChanged(input, oldInput);
		map(input, fTree);
		refresh();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.viewers.AsynchronousViewer#map(java.lang.Object,
	 *      org.eclipse.swt.widgets.Widget)
	 */
	protected void map(Object element, Widget item) {
		super.map(element, item);
		if (item instanceof TreeItem) {
			TreeItem treeItem = (TreeItem) item;
			TreeItem parentItem = treeItem.getParentItem();
			if (parentItem != null) {
				fItemToParentItem.put(treeItem, parentItem);
			}
		}
	}

	/**
	 * Returns all paths to the given element or <code>null</code> if none.
	 * 
	 * @param element
	 * @return paths to the given element or <code>null</code>
	 */
	public synchronized TreePath[] getTreePaths(Object element) {
		Widget[] widgets = getWidgets(element);
		if (widgets == null) {
			return null;
		}
		TreePath[] paths = new TreePath[widgets.length];
		for (int i = 0; i < widgets.length; i++) {
			List path = new ArrayList();
			path.add(element);
			Widget widget = widgets[i];
			TreeItem parent = null;
			if (widget instanceof TreeItem) {
				TreeItem treeItem = (TreeItem) widget;
				parent = getParentItem(treeItem);
			}
			while (parent != null) {
				Object data = getElement(parent);
				if (data == null) {
					// if the parent is unmapped while attempting selection
					return null;
				}
				path.add(0, data);
				parent = getParentItem(parent);
			}
			if (!path.get(0).equals(getInput())) { 
				path.add(0, getInput());
			}
			paths[i] = new TreePath(path.toArray());
			if (widget instanceof TreeItem) {
				paths[i].setTreeItem((TreeItem) widget);
			}
		}
		return paths;
	}

	/**
	 * Constructs and returns a tree path for the given item. Must be called
	 * from the UI thread.
	 * 
	 * @param item item to constuct a path for
	 * @return tree path for the item
	 */
	protected synchronized TreePath getTreePath(TreeItem item) {
		TreeItem parent = item;
		List path = new ArrayList();
		while (parent != null) {
			path.add(0, parent.getData());
			parent = parent.getParentItem();
		}
		path.add(0, fTree.getData());
		return new TreePath(path.toArray());
	}

	/**
	 * Called by <code>ContainerRequestMonitor</code> after it is determined
	 * if the widget contains children.
	 * 
	 * @param widget
	 * @param containsChildren
	 */
	synchronized void setIsContainer(Widget widget, boolean containsChildren) {
		TreeItem[] prevChildren = null;
		if (widget instanceof Tree) {
			Tree tree = (Tree) widget;
			prevChildren = tree.getItems();
		} else {
			prevChildren = ((TreeItem) widget).getItems();
		}
		if (containsChildren) {
			if (prevChildren.length == 0) {
				if (widget instanceof Tree) {
					// update root elements in the tree
					updateChildren(widget.getData(), widget);
				} else {
					// insert new dummy node to add +
					newTreeItem(widget, 0);
					if (((TreeItem) widget).getExpanded()) {
						updateChildren(widget.getData(), widget);
					}
				}
			} else {
				if (widget instanceof Tree || ((TreeItem) widget).getExpanded()) {
					// if expanded, update the children
					updateChildren(widget.getData(), widget);
				}
			}
		} else if (prevChildren.length > 0) {
			// dispose previous children
			for (int i = 0; i < prevChildren.length; i++) {
				TreeItem prevChild = prevChildren[i];
				unmap(prevChild.getData(), prevChild);
				prevChild.dispose();
			}
		}
		attemptExpansion();
	}
	
	/**
	 * Adds a child at end of parent.
	 * 
	 * @param parent
	 * @param child
	 */
	synchronized void add(Widget parent, Object child) {
		// refresh vs. add if not expanded
		boolean expanded = true;
		if (parent instanceof TreeItem) {
			expanded = ((TreeItem)parent).getExpanded();
		}
		if (expanded) {
			Object[] objects = filter(new Object[]{child});
			if (objects.length == 1) {
				child = objects[0];
				TreeItem item = newTreeItem(parent);
				map(child, item);
				internalRefresh(child, item);
			}
		} else {
			internalRefresh(parent.getData(), parent);
		}		
	}

	/**
	 * Called by <code>ChildrenRequestMonitor</code> after children have been
	 * retrieved.
	 * 
	 * @param widget
	 * @param newChildren
	 */
	synchronized void setChildren(final Widget widget, final List newChildren) {
		preservingSelection(new Runnable() {

			public void run() {
				// apply filters
				Object[] children = filter(newChildren.toArray());

				// sort filtered children
				ViewerSorter viewerSorter = getSorter();
				if (viewerSorter != null) {
					viewerSorter.sort(AsynchronousTreeViewer.this, children);
				}

				// update tree
				TreeItem[] prevItems = null;
				if (widget instanceof Tree) {
					Tree tree = (Tree) widget;
					prevItems = tree.getItems();
				} else {
					prevItems = ((TreeItem) widget).getItems();
				}

				int index = 0;
				for (; index < children.length; index++) {
					Object kid = children[index];
					TreeItem item = null;
					if (index < prevItems.length) {
						item = prevItems[index];
						Object oldData = item.getData();
						if (kid.equals(oldData)) {
							if (kid != oldData) {
								// if equal but not identical, remap the element
								remap(kid, item);
							}
						} else {
							unmap(oldData, item);
							map(kid, item);
						}
					} else {
						item = newTreeItem(widget, index);
						map(kid, item);
					}
					internalRefresh(kid, item);
				}
				// remove left over old items
				while (index < prevItems.length) {
					TreeItem oldItem = prevItems[index];
					unmap(oldItem.getData(), oldItem);
					oldItem.dispose();
					index++;
				}
			}

		});

		attemptExpansion();
		attemptSelection(true);
	}

	/**
	 * Expands the given tree item and all of its parents. Does *not* update
	 * elements or retrieve children.
	 * 
	 * @param child item to expand
	 */
	private void expand(TreeItem child) {
		if (!child.getExpanded()) {
			child.setExpanded(true);

			TreeItem parent = child.getParentItem();
			if (parent != null) {
				expand(parent);
			}
		}
	}

	/**
	 * Creates a new tree item as a child of the given widget at the specified
	 * index.
	 * 
	 * @param parent parent widget - a Tree or TreeItem
	 * @param index index at which to create new child
	 * @return tree item
	 */
	protected TreeItem newTreeItem(Widget parent, int index) {
		if (parent instanceof Tree) {
			return new TreeItem((Tree) parent, SWT.NONE, index);
		}
		return new TreeItem((TreeItem) parent, SWT.NONE, index);
	}
	
	/**
	 * Creates a new tree item as a child of the given widget as last.
	 * 
	 * @param parent parent widget - a Tree or TreeItem
	 * @return tree item
	 */
	protected TreeItem newTreeItem(Widget parent) {
		if (parent instanceof Tree) {
			return new TreeItem((Tree) parent, SWT.NONE);
		}
		return new TreeItem((TreeItem) parent, SWT.NONE);
	}	

	/**
	 * Unmaps the given item, and unmaps and disposes of all children of that
	 * item. Does not dispose of the given item.
	 * 
	 * @param kid
	 * @param oldItem
	 */
	protected synchronized void unmap(Object kid, Widget widget) {
		if (kid == null) {
			// when unmapping a dummy item
			return;
		}
		
		TreeItem[] selection = fTree.getSelection();
		if (selection.length == 1 && isChild(widget, selection[0])) {
			setSelection(null);
		}
		
		super.unmap(kid, widget);
		fItemToParentItem.remove(widget);
		if (widget instanceof TreeItem) {
			TreeItem item = (TreeItem) widget;
			TreeItem[] children = item.getItems();
			for (int i = 0; i < children.length; i++) {
				TreeItem child = children[i];
				unmap(child.getData(), child);
				child.dispose();
			}
		}
	}

	private boolean isChild(Widget parent, TreeItem child) {
		if (child==null) {
			return false;
		} else if (parent == child) {
			return true;
		}
		return isChild(parent, child.getParentItem());
	}

	/**
	 * Returns the parent item for an item or <code>null</code> if none.
	 * 
	 * @param item item for which parent is requested
	 * @return parent item or <code>null</code>
	 */
	protected synchronized TreeItem getParentItem(TreeItem item) {
		return (TreeItem) fItemToParentItem.get(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#doFindInputItem(java.lang.Object)
	 */
	protected Widget doFindInputItem(Object element) {
		if (element.equals(getInput())) {
			return fTree;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#doFindItem(java.lang.Object)
	 */
	protected Widget doFindItem(Object element) {
		Widget[] widgets = getWidgets(element);
		if (widgets != null && widgets.length > 0) {
			return widgets[0];
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AsynchronousViewer#newSelectionFromWidget()
	 */
	protected ISelection newSelectionFromWidget() {
		Control control = getControl();
		if (control == null || control.isDisposed()) {
			return StructuredSelection.EMPTY;
		}
		List list = getSelectionFromWidget();
		return new TreeSelection((TreePath[]) list.toArray());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#getSelectionFromWidget()
	 */
	protected synchronized List getSelectionFromWidget() {
		TreeItem[] selection = fTree.getSelection();
		TreePath[] paths = new TreePath[selection.length];
		for (int i = 0; i < selection.length; i++) {
			paths[i] = getTreePath(selection[i]);
		}
		return Arrays.asList(paths);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.viewers.AsynchronousViewer#internalRefresh(java.lang.Object,
	 *      org.eclipse.swt.widgets.Widget)
	 */
	protected synchronized void internalRefresh(Object element, Widget item) {
		super.internalRefresh(element, item);
		updateHasChildren(element, item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#reveal(java.lang.Object)
	 */
	public void reveal(Object element) {
		Widget[] widgets = getWidgets(element);
		if (widgets != null && widgets.length > 0) {
			// TODO: only reveals the first occurrence - should we reveal all?
			TreeItem item = (TreeItem) widgets[0];
			Tree tree = (Tree) getControl();
			tree.showItem(item);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.viewers.AsynchronousViewer#doAttemptSelectionToWidget(org.eclipse.jface.viewers.ISelection,
	 *      boolean)
	 */
	protected synchronized ISelection doAttemptSelectionToWidget(ISelection selection, boolean reveal) {
		List remaining = new ArrayList();
		if (!selection.isEmpty()) {
			List toSelect = new ArrayList();
			List theElements = new ArrayList();
			TreeSelection treeSelection = (TreeSelection) selection;
			TreePath[] paths = treeSelection.getPaths();
			for (int i = 0; i < paths.length; i++) {
				TreePath path = paths[i];
				if (path == null) {
					continue;
				}
				TreePath[] treePaths = getTreePaths(path.getLastSegment());
				boolean selected = false;
				if (treePaths != null) {
					for (int j = 0; j < treePaths.length; j++) {
						TreePath existingPath = treePaths[j];
						if (existingPath.equals(path)) {
							TreeItem treeItem = existingPath.getTreeItem();
							toSelect.add(treeItem);
							theElements.add(path.getLastSegment());
							selected = true;
							break;
						}
					}
				}
				if (!selected) {
					remaining.add(path);
				}
			}
			if (!toSelect.isEmpty()) {
				final TreeItem[] items = (TreeItem[]) toSelect.toArray(new TreeItem[toSelect.size()]);
				// TODO: hack to ensure selection contains 'selected' element
				// instead of 'equivalent' element. Handles synch problems
				// between
				// set selection & refresh
				for (int i = 0; i < items.length; i++) {
					TreeItem item = items[i];
					Object element = theElements.get(i);
					if (item.getData() != element) {
						remap(element, item);
					}
				}
				fTree.setSelection(items);
				if (reveal) {
					fTree.showItem(items[0]);
				}
			}
		} else {
			fTree.setSelection(new TreeItem[0]);
		}
		return new TreeSelection((TreePath[]) remaining.toArray(new TreePath[remaining.size()]));
	}

	/**
	 * Collapses all items in the tree.
	 */
	public void collapseAll() {
		TreeItem[] items = fTree.getItems();
		for (int i = 0; i < items.length; i++) {
			TreeItem item = items[i];
			if (item.getExpanded())
				collapse(item);
		}
	}

	/**
	 * Collaspes the given item and all of its children items.
	 * 
	 * @param item item to collapose recursively
	 */
	protected void collapse(TreeItem item) {
		TreeItem[] items = item.getItems();
		for (int i = 0; i < items.length; i++) {
			TreeItem child = items[i];
			if (child.getExpanded()) {
				collapse(child);
			}
		}
		item.setExpanded(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.viewers.AsynchronousViewer#setColor(org.eclipse.swt.widgets.Widget,
	 *      org.eclipse.swt.graphics.RGB, org.eclipse.swt.graphics.RGB)
	 */
	void setColors(Widget widget, RGB[] foregrounds, RGB[] backgrounds) {
		if (widget instanceof TreeItem) {
			TreeItem item = (TreeItem) widget;
			Color[] fgs = getColor(foregrounds);
			Color[] bgs = getColor(backgrounds);
			for (int i = 0; i < bgs.length; i++) {
				item.setForeground(i, fgs[i]);
				item.setBackground(i, bgs[i]);	
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.viewers.AsynchronousViewer#setFont(org.eclipse.swt.widgets.Widget,
	 *      org.eclipse.swt.graphics.FontData)
	 */
	void setFonts(Widget widget, FontData[] fontData) {
		if (widget instanceof TreeItem) {
			TreeItem item = (TreeItem) widget;
			Font[] fonts = getFonts(fontData);
			for (int i = 0; i < fonts.length; i++) {
				item.setFont(i, fonts[i]);	
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.viewers.AsynchronousViewer#getParent(org.eclipse.swt.widgets.Widget)
	 */
	protected Widget getParent(Widget widget) {
		return (Widget) fItemToParentItem.get(widget);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.viewers.AsynchronousViewer#acceptsSelection(org.eclipse.jface.viewers.ISelection)
	 */
	protected boolean acceptsSelection(ISelection selection) {
		return selection instanceof TreeSelection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.viewers.AsynchronousViewer#getEmptySelection()
	 */
	protected ISelection getEmptySelection() {
		return new TreeSelection(new TreePath[0]);
	}
	
	/**
	 * Adds the item specified by the given tree path to this tree.
	 * Can be called in a non-UI thread.
	 * 
	 * @param treePath
	 */
	public synchronized void add(TreePath treePath) {
		int parentIndex = treePath.getSegmentCount() - 2; 
		if (parentIndex >= 0) {
			// find the paths to the parent, if it's present
			Object parent = treePath.getSegment(parentIndex);
			TreePath[] paths = getTreePaths(parent);
			if (paths != null) {
				// find the right path
				for (int i = 0; i < paths.length; i++) {
					TreePath path = paths[i];
					if (treePath.startsWith(path)) {
						Widget widget = path.getTreeItem();
						if (widget == null) {
							widget = getTree();
						}
						AddRequestMonitor addRequest = new AddRequestMonitor(widget, treePath, this);
						schedule(addRequest);
						addRequest.done();
						return;
					}
				}
			}
			// refresh the leaf parent, if any
			for (int i = parentIndex -1; i >= 0; i++) {
				parent = treePath.getSegment(i);
				paths = getTreePaths(parent);
				if (paths != null) {
					for (int j = 0; j < paths.length; j++) {
						TreePath path = paths[j];
						if (treePath.startsWith(path)) {
							Widget widget = path.getTreeItem();
							if (widget == null) {
								widget = getTree();
							}
							internalRefresh(parent, widget);
							return;
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * Removes the item specified in the given tree path from this tree.
	 * Can be called in a non-UI thread.
	 * 
	 * @param treePath
	 */	
	public synchronized void remove(TreePath treePath) {
        for (Iterator i = fPendingExpansion.iterator(); i.hasNext();) {
            TreePath expansionPath = (TreePath) i.next();
            if (expansionPath.startsWith(treePath)) {
                i.remove();
            }
        }
		if (treePath.getSegmentCount() > 1) {
			// find the paths to the element, if it's present
			Object element = treePath.getLastSegment();
			TreePath[] paths = getTreePaths(element);
			if (paths != null) {
				// find the right path
				for (int i = 0; i < paths.length; i++) {
					TreePath path = paths[i];
					if (treePath.equals(path)) {
						TreeItem item = path.getTreeItem();
						RemoveRequestMonitor request = new RemoveRequestMonitor(item, treePath, this);
						schedule(request);
						request.done();
						return;
					}
				}
			}
			// refresh the parent, if present
			if (treePath.getSegmentCount() >= 2) {
				element = treePath.getSegment(treePath.getSegmentCount() - 2);
				paths = getTreePaths(element);
				if (paths != null) {
					// find the right path
					for (int i = 0; i < paths.length; i++) {
						TreePath path = paths[i];
						if (treePath.startsWith(path)) {
							Widget widget = path.getTreeItem();
							if (widget == null) {
								widget = getTree();
							}
							internalRefresh(element, widget);
							return;
						}
					}
				}
			}
		}
	}	

	synchronized void remove(final Widget widget) {
		preservingSelection(new Runnable() {
			public void run() {
				Object element = widget.getData(); 
				unmap(element, widget);
				widget.dispose();		
			}
		});
	}
	
	void setLabels(Widget widget, String[] text, ImageDescriptor[] image) {
		if (widget instanceof TreeItem) {
			TreeItem item = (TreeItem) widget;
			item.setText(text);
			item.setImage(getImages(image));
		}
	}

}
