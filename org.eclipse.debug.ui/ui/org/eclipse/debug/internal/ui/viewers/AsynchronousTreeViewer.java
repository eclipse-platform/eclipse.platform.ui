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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
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
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
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
public class AsynchronousTreeViewer extends AsynchronousViewer implements Listener {

    /**
     * The tree
     */
    private Tree fTree;

    /**
     * Collection of tree paths to be expanded. A path is removed from the
     * collection when it is expanded. The entire list is cleared when the input
     * to the viewer changes.
     */
    private List fPendingExpansion = new ArrayList();
    
    /**
     * Creates an asynchronous tree viewer on a newly-created tree control under
     * the given parent. The tree control is created using the SWT style bits
     * <code>MULTI, H_SCROLL, V_SCROLL,</code> and <code>BORDER</code>. The
     * viewer has no input, no content provider, a default label provider, no
     * sorter, and no filters.
     * 
     * @param parent
     *            the parent control
     */
    public AsynchronousTreeViewer(Composite parent) {
        this(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.VIRTUAL);
    }

    /**
     * Creates an asynchronous tree viewer on a newly-created tree control under
     * the given parent. The tree control is created using the given SWT style
     * bits. The viewer has no input.
     * 
     * @param parent
     *            the parent control
     * @param style
     *            the SWT style bits used to create the tree.
     */
    public AsynchronousTreeViewer(Composite parent, int style) {
        this(new Tree(parent, style));
    }

    /**
     * Creates an asynchronous tree viewer on the given tree control. The viewer
     * has no input, no content provider, a default label provider, no sorter,
     * and no filters.
     * 
     * @param tree
     *            the tree control
     */
    public AsynchronousTreeViewer(Tree tree) {
        super();
        Assert.isTrue((tree.getStyle() & SWT.VIRTUAL) != 0);
        fTree = tree;
        hookControl(fTree);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.StructuredViewer#hookControl(org.eclipse.swt.widgets.Control)
     */
    protected void hookControl(Control control) {
		super.hookControl(control);
		Tree tree = (Tree)control;
        tree.addTreeListener(new TreeListener() {
            public void treeExpanded(TreeEvent e) {
                ((TreeItem) e.item).setExpanded(true);
                ModelNode node = findNode(e.item);
                if (node != null) {
					internalRefresh(node);
                }
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
                        ModelNode node = findNode(item);
                        if (node != null) {
                        	internalRefresh(node);
                        }
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
     * Updates whether the given node has children.
     * 
     * @param node node to update
     */
    protected void updateHasChildren(ModelNode node) {
        ((AsynchronousTreeModel)getModel()).updateHasChildren(node);
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
                if (attemptExpansion(path)) {
                    i.remove();
                }
            }
        }
    }

    /**
     * Attempts to expand the given tree path and returns whether the expansion
     * was completed.
     * 
     * @param path path to expand
     * @return whether the expansion was completed
     */
    synchronized boolean attemptExpansion(TreePath path) {
        int segmentCount = path.getSegmentCount();
        for (int j = segmentCount - 1; j >= 0; j--) {
            Object element = path.getSegment(j);
            ModelNode[] nodes = getModel().getNodes(element);
            if (nodes != null) {
                for (int k = 0; k < nodes.length; k++) {
                    ModelNode node = nodes[k];
                    TreePath treePath = node.getTreePath();
                    if (path.startsWith(treePath, null)) {
                        if (!node.isDisposed()) {
                            Widget widget = findItem(node);
                            if (widget == null) {
                                // force the widgets to be mapped
                                ModelNode parent = node.getParentNode();
                                ModelNode child = node;
                                widget = findItem(parent);
                                while (widget == null && parent != null) {
                                    child = parent;
                                    parent = parent.getParentNode();
                                    if (parent != null) {
                                        widget = findItem(parent);
                                        treePath = parent.getTreePath();
                                    }
                                }
                                int childIndex = parent.getChildIndex(child);
                                if (childIndex < 0) {
                                    return false;
                                }
                                TreeItem[] items = getItems(widget);
                                if (childIndex < items.length) {
                                    widget = items[childIndex];
                                    mapElement(child, widget);
                                    widget.setData(child.getElement());
                                    treePath = child.getTreePath();
                                    node = child;
                                } else {
                                    return false;
                                }
                            }
                            if (widget instanceof TreeItem) {
                                TreeItem treeItem = (TreeItem) widget;
                                if (treeItem.getExpanded()) {
                                    return path.getSegmentCount() == treePath.getSegmentCount();
                                }
                                if (treeItem.getItemCount() > 0) {
                                    updateChildren(node);
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
     * @see org.eclipse.jface.viewers.Viewer#inputChanged(java.lang.Object,
     *      java.lang.Object)
     */
    synchronized protected void inputChanged(Object input, Object oldInput) {
        fPendingExpansion.clear();
        super.inputChanged(input, oldInput);
    }

    /**
     * Constructs and returns a tree path for the given item. Must be called
     * from the UI thread.
     * 
     * @param item
     *            item to constuct a path for
     * @return tree path for the item
     */
    protected synchronized TreePath getTreePath(TreeItem item) {
        TreeItem parent = item;
        List path = new ArrayList();
        while (parent != null && !parent.isDisposed()) {
            Object parentElement = parent.getData();
            if (parentElement == null) {
            	return new TreePath(new Object[0]);
            }
			path.add(0, parentElement);
            parent = parent.getParentItem();
        }
        path.add(0, fTree.getData());
        return new TreePath(path.toArray());
    }
    
    /**
     * Returns the tree paths to the given element in this viewer, possibly
     * empty.
     * 
     * @param element model element
     * @return the paths to the element in this viewer
     */
    public TreePath[] getTreePaths(Object element) {
        ModelNode[] nodes = getModel().getNodes(element);
        if (nodes == null) {
            return new TreePath[]{};
        }
        TreePath[] paths = new TreePath[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            paths[i] = nodes[i].getTreePath();
        }
        return paths;
    }
    
    /**
     * Container status of a node changed
     * 
     * @param node
     */
    protected void nodeContainerChanged(ModelNode node) {
         Widget widget = findItem(node);
        if (widget != null && !widget.isDisposed()) {
            if (isVisible(widget)) {
                boolean expanded = true;
                if (node.isContainer() && getItemCount(widget) == 0) {
                	setItemCount(widget, 1);
                }
                if (widget instanceof TreeItem) {
                    expanded = ((TreeItem)widget).getExpanded();
                }
                if (expanded) {
                    updateChildren(node);
                }
            }
        }
        attemptPendingUpdates();
    }
    
    protected int getItemCount(Widget widget) {
    	if (widget instanceof TreeItem) {
			return ((TreeItem) widget).getItemCount();
		}
    	return ((Tree) widget).getItemCount();
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.model.viewers.AsynchronousViewer#setItemCount(org.eclipse.swt.widgets.Widget, int)
     */
    protected void setItemCount(Widget widget, int itemCount) {
        if (widget == fTree) {
            fTree.setItemCount(itemCount);
        } else {
            ((TreeItem) widget).setItemCount(itemCount);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.model.viewers.AsynchronousViewer#getChildWidget(org.eclipse.swt.widgets.Widget, int)
     */
    protected Widget getChildWidget(Widget parent, int index) {
		if (parent instanceof Tree) {
			Tree tree = (Tree) parent;
			if (index < tree.getItemCount()) {
				return tree.getItem(index);
			}
		} else if (parent instanceof TreeItem){
			TreeItem item = (TreeItem) parent;
			if (index < item.getItemCount()) {
				return item.getItem(index);
			}
		}
		return null;
	}

	private TreeItem[] getItems(Widget widget) {
        if (widget instanceof TreeItem) {
            return ((TreeItem) widget).getItems();
        } else {
            return fTree.getItems();
        }
    }

    /**
     * Returns the parent widget for the given widget or <code>null</code>
     * 
     * @param widget
     * @return parent widget or <code>null</code>
     */
    protected Widget getParentWidget(Widget widget) {
    	if (widget instanceof TreeItem) {
    		TreeItem parentItem = ((TreeItem)widget).getParentItem();
    		if (parentItem == null) {
    			return getControl();
    		}
    		return parentItem;
    	}
    	return null;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.model.viewers.AsynchronousViewer#getChildIndex(org.eclipse.swt.widgets.Widget, org.eclipse.swt.widgets.Event)
     */
    protected int getChildIndex(Widget parent, Event event) {
        if (parent instanceof TreeItem) {
            return ((TreeItem) parent).indexOf((TreeItem)event.item);
        } else if (parent instanceof Tree) {
            return ((Tree)parent).indexOf((TreeItem)event.item);
        }
        return -1;
	}
    
    /**
     * Expands the given tree item and all of its parents. Does *not* update
     * elements or retrieve children.
     * 
     * @param child
     *            item to expand
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

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.model.viewers.AsynchronousViewer#clear(org.eclipse.swt.widgets.Widget)
     */
    protected void clear(Widget widget) {
    	if (widget instanceof TreeItem) {
    		TreeItem item = (TreeItem) widget;
    		item.clearAll(true);
    	} else {
    		fTree.clearAll(true);
    	}
    }

    protected boolean isVisible(Widget widget) {
        if (widget instanceof Tree) {
            return true;
        } else {
            TreeItem item = (TreeItem) widget;
            Rectangle itemBounds = item.getBounds();
            return !NOT_VISIBLE.equals(itemBounds);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.internal.ui.viewers.AsynchronousModelViewer#newSelectionFromWidget()
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

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.model.viewers.AsynchronousViewer#internalRefresh(org.eclipse.debug.internal.ui.model.viewers.ModelNode)
     */
    protected void internalRefresh(ModelNode node) {
        super.internalRefresh(node);
        updateHasChildren(node);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.StructuredViewer#reveal(java.lang.Object)
     */
    public void reveal(Object element) {
    	// TODO: in virtual case, we should attempt expansion
        ModelNode[] nodes = getModel().getNodes(element);
        if (nodes != null) {
        	for (int i = 0; i < nodes.length; i++) {
				ModelNode node = nodes[i];
				Widget widget = findItem(node);
				if (widget instanceof TreeItem) {
		            // TODO: only reveals the first occurrence - should we reveal all?
		            TreeItem item = (TreeItem) widget;
		            Tree tree = (Tree) getControl();
		            tree.showItem(item);			
		            return;
				}
			}
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
            List theNodes = new ArrayList();
            List theElements = new ArrayList();
            TreeSelection treeSelection = (TreeSelection) selection;
            TreePath[] paths = treeSelection.getPaths();
            for (int i = 0; i < paths.length; i++) {
                TreePath path = paths[i];
                if (path == null) {
                    continue;
                }
                ModelNode[] nodes = getModel().getNodes(path.getLastSegment());
                boolean selected = false;
                if (nodes != null) {
                    for (int j = 0; j < nodes.length; j++) {
                        ModelNode node = nodes[j];
                        if (node.correspondsTo(path)) {
                            Widget widget = findItem(node);
                            if (widget != null && !widget.isDisposed()) {
                                toSelect.add(widget);
                                theNodes.add(node);
                                theElements.add(path.getLastSegment());
                                selected = true;
                                break;
                            }
                        }
                        // attempt to map widget
                        ModelNode parent = node.getParentNode();
                        ModelNode child = node;
                        if (parent != null) {
                        	Widget widget = findItem(parent);
		                    if (widget != null && !widget.isDisposed()) {
		                        int childIndex = parent.getChildIndex(child);
		                        if (childIndex < 0) {
		                            break;
		                        }
		                        TreeItem[] items = getItems(widget);
		                        if (childIndex < items.length) {
		                            widget = items[childIndex];
		                            mapElement(child, widget);
		                            widget.setData(child.getElement());
		                            toSelect.add(widget);
		                            theNodes.add(child);
		                            theElements.add(child.getElement());
		                            selected = true;
		                        } else {
		                            break;
		                        }
		                    }
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
                // between set selection & refresh
                for (int i = 0; i < items.length; i++) {
                    TreeItem item = items[i];
                    Object element = theElements.get(i); 
                    if (!item.isDisposed() && item.getData() != element) {
                        ModelNode theNode = (ModelNode) theNodes.get(i);
						Widget mapped = findItem(theNode);
                        if (mapped == null) {
                        	// the node has been unmapped from the widget (pushed down perhaps)
                        	return selection;
                        }
                        theNode.remap(element);
                        item.setData(element);
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
     * @param item
     *            item to collapose recursively
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
    protected void setColors(Widget widget, RGB[] foregrounds, RGB[] backgrounds) {
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
    protected void setFonts(Widget widget, FontData[] fontData) {
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
     * Adds the item specified by the given tree path to this tree. Can be
     * called in a non-UI thread.
     * 
     * @param treePath
     */
    public void add(TreePath treePath) {
        ((AsynchronousTreeModel)getModel()).add(treePath);
    }

    /**
     * Removes the item specified in the given tree path from this tree. Can be
     * called in a non-UI thread.
     * 
     * @param treePath
     */
    public void remove(TreePath treePath) {
        synchronized (this) {
            for (Iterator i = fPendingExpansion.iterator(); i.hasNext();) {
                TreePath expansionPath = (TreePath) i.next();
                if (expansionPath.startsWith(treePath, null)) {
                    i.remove();
                }
            }
        }
        ((AsynchronousTreeModel)getModel()).remove(treePath);
    }

    protected void setLabels(Widget widget, String[] text, ImageDescriptor[] image) {
        if (widget instanceof TreeItem) {
            TreeItem item = (TreeItem) widget;
            if (!item.isDisposed()) {
                item.setText(text);
                item.setImage(getImages(image));
            }
        }
    }

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AsynchronousModelViewer#createModel()
	 */
	protected AsynchronousModel createModel() {
		return new AsynchronousTreeModel(this);
	}
    

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.AsynchronousModelViewer#nodeChanged(org.eclipse.debug.internal.ui.viewers.ModelNode)
     */
    public void nodeChanged(ModelNode node) {
        Widget widget = findItem(node);
        if (widget != null && !widget.isDisposed()) {
            if (widget instanceof TreeItem) {
                if (!isVisible(widget)) {
                    clear(widget);
                    return;
                }
            }
            widget.setData(node.getElement());
            mapElement(node, widget);
            internalRefresh(node);
            attemptPendingUpdates();
        }
    }
    
    /**
     * Attempt pending udpates. Subclasses may override but should call super.
     */
    protected void attemptPendingUpdates() {
    	attemptExpansion();
    	super.attemptPendingUpdates();
    }    

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.viewers.AsynchronousViewer#createUpdatePolicy()
	 */
	public IModelUpdatePolicy createUpdatePolicy() {
		return new TreeUpdatePolicy();
	}

	protected synchronized void unmapAllElements() {
		super.unmapAllElements();
		Tree tree = getTree();
		if (!tree.isDisposed()) {
			TreeItem[] items = tree.getItems();
			for (int i = 0; i < items.length; i++) {
				items[i].dispose();
			}
			clear(tree);
		}
	}

}
