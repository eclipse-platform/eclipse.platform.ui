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
package org.eclipse.debug.internal.ui.model.viewers;

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
import org.eclipse.debug.internal.ui.viewers.TreePath;
import org.eclipse.debug.internal.ui.viewers.TreeSelection;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousContentAdapter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
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
public class AsynchronousTreeModelViewer extends AsynchronousModelViewer implements Listener {

    private static final Rectangle NOT_VISIBLE = new Rectangle(0, 0, 0, 0);

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
     * Map of parent nodes for which chidlren were needed to "set data"
     * in the virtual tree. A parent is added to this map when we try go
     * get children but they aren't there yet. The children are retrieved
     * asynchronously, and later put back into the tree widgetry.
     * The value is an array of intsfor the indicies of the children that 
     * were requested. 
     */
    private Map fParentsPendingChildren = new HashMap();

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
    public AsynchronousTreeModelViewer(Composite parent) {
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
    public AsynchronousTreeModelViewer(Composite parent, int style) {
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
    public AsynchronousTreeModelViewer(Tree tree) {
        super();
        Assert.isTrue((tree.getStyle() & SWT.VIRTUAL) != 0);

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
        tree.addListener(SWT.SetData, this);
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
     * @param element
     *            element to update
     * @param widget
     *            widget associated with the element in this viewer's tree
     */
    protected void updateHasChildren(Object element, Widget widget) {
        ModelNode node = getModel().getNode(widget);
        if (node != null) {
            ((AsynchronousTreeModel)getModel()).updateHasChildren(node);
        }
    }

    /**
     * Updates the children of the given element.
     * 
     * @param parent
     *            element of which to update children
     * @param widget
     *            widget associated with the element in this viewer's tree
     */
    protected void updateChildren(Object parent, Widget widget) {
        ModelNode node = getModel().getNode(widget);
        if (node != null) {
            ((AsynchronousTreeModel)getModel()).updateChildren(node);
        }
    }

    /**
     * Returns the tree element adapter for the given element or
     * <code>null</code> if none.
     * 
     * @param element
     *            element to retrieve adapter for
     * @return presentation adapter or <code>null</code>
     */
    protected IAsynchronousContentAdapter getContentAdapter(Object element) {        
        IAsynchronousContentAdapter adapter = null;
        if (element instanceof IAsynchronousContentAdapter) {
            adapter = (IAsynchronousContentAdapter) element;
        } else if (element instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) element;
            adapter = (IAsynchronousContentAdapter) adaptable.getAdapter(IAsynchronousContentAdapter.class);
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
                    if (path.startsWith(treePath)) {
                        if (!node.isDisposed()) {
                            Widget widget = node.getWidget();
                            if (widget == null) {
                                // force the widgets to be mapped
                                ModelNode parent = node.getParentNode();
                                ModelNode child = node;
                                widget = parent.getWidget();
                                while (widget == null && parent != null) {
                                    child = parent;
                                    parent = parent.getParentNode();
                                    if (parent != null) {
                                        widget = parent.getWidget();
                                        treePath = parent.getTreePath();
                                    }
                                }
                                int childIndex = parent.getChildIndex(child);
                                if (childIndex < 0) {
                                    return false;
                                }
                                TreeItem[] items = getItems(widget);
                                if (childIndex < items.length) {
                                    getModel().mapWidget(items[childIndex], child);
                                    widget = child.getWidget();
                                    treePath = child.getTreePath();
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
        fParentsPendingChildren.clear();
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
     * The children of a node have changed.
     * 
     * @param parent
     */
    protected void nodeChildrenChanged(ModelNode parentNode) {
    	ModelNode[] children = parentNode.getChildrenNodes();
    	if(children == null) {
    		children = new ModelNode[0];
    	}
    	nodeChildrenSet(parentNode, children);
    }
    
    /**
     * Container status of a node changed
     * 
     * @param node
     */
    protected void nodeContainerChanged(ModelNode node) {
         Widget widget = node.getWidget();
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
                    updateChildren(node.getElement(), widget);
                }
            }
        }
        attemptExpansion();
        attemptSelection(false);
    }
    
    protected int getItemCount(Widget widget) {
    	if (widget instanceof TreeItem) {
			return ((TreeItem) widget).getItemCount();
		}
    	return ((Tree) widget).getItemCount();
    }

    protected void doUpdateItem(Widget item, Object element, boolean fullMap) {
        super.doUpdateItem(item, element, fullMap);
        updateHasChildren(element, item);
    }

    private void setItemCount(Widget widget, int itemCount) {
        if (widget == fTree) {
            fTree.setItemCount(itemCount);
        } else {
            ((TreeItem) widget).setItemCount(itemCount);
        }
    }

    private TreeItem[] getItems(Widget widget) {
        if (widget instanceof TreeItem) {
            return ((TreeItem) widget).getItems();
        } else {
            return fTree.getItems();
        }
    }

    public void handleEvent(final Event event) {
    	// don't preserve selection when item is revealed: see bug 125499
        TreeItem item = (TreeItem) event.item;
        Widget parentItem = item.getParentItem();
        int index = 0;
        if (parentItem != null) {
            index = ((TreeItem) parentItem).indexOf(item);
        } else {
            parentItem = fTree;
            index = fTree.indexOf(item);
        }

        ModelNode parentNode = getModel().getModelNode(parentItem);
        if (parentNode != null) {
        	ModelNode[] childrenNodes = parentNode.getChildrenNodes();
        	if (childrenNodes != null && index < childrenNodes.length) {
        		ModelNode child = childrenNodes[index];
        		getModel().mapWidget(item, child);
        		internalRefresh(child.getElement(), item);
        	} else {
        		addPendingChildIndex(parentNode, index);
            }
        }
    }
    
    /**
     * Note that the child at the specified index of the given parent node needs
     * data mapped to its widget when the data becomes available.
     * 
     * @param parent
     * @param index
     */
    private synchronized void addPendingChildIndex(ModelNode parent, int index) {
        int[] indicies = (int[]) fParentsPendingChildren.get(parent);
	    if (indicies == null) {
	        indicies = new int[]{index};
        } else {
            int[] next = new int[indicies.length + 1];
            System.arraycopy(indicies, 0, next, 0, indicies.length);
            next[indicies.length] = index;
            indicies = next;
        }
        fParentsPendingChildren.put(parent, indicies);    	
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

    private void clear(Widget widget) {
    	if (widget instanceof TreeItem) {
    		TreeItem item = (TreeItem) widget;
    		item.clearAll(true);
    	} else {
    		fTree.clearAll(true);
    	}
    }

    private boolean isVisible(Widget widget) {
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
                ModelNode[] nodes = getModel().getNodes(path.getLastSegment());
                boolean selected = false;
                if (nodes != null) {
                    for (int j = 0; j < nodes.length; j++) {
                        ModelNode node = nodes[j];
                        if (node.correspondsTo(path)) {
                            Widget widget = node.getWidget();
                            if (widget != null && !widget.isDisposed()) {
                                toSelect.add(widget);
                                theElements.add(path.getLastSegment());
                                selected = true;
                                break;
                            }
                        }
                        // attempt to map widget
                        ModelNode parent = node.getParentNode();
                        ModelNode child = node;
                        Widget widget = parent.getWidget();
                        if (parent != null && widget != null && !widget.isDisposed()) {
                            int childIndex = parent.getChildIndex(child);
                            if (childIndex < 0) {
                                break;
                            }
                            TreeItem[] items = getItems(widget);
                            if (childIndex < items.length) {
                                getModel().mapWidget(items[childIndex], child);
                                widget = child.getWidget();
                                toSelect.add(widget);
                                theElements.add(child.getElement());
                                selected = true;
                            } else {
                                break;
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
                        ModelNode node = getModel().getNode(item);
                        if (node == null) {
                        	// the node has been unmapped from the widget (pushed down perhaps)
                        	return selection;
                        }
                        node.remap(element);
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
                if (expansionPath.startsWith(treePath)) {
                    i.remove();
                }
            }
        }
        ((AsynchronousTreeModel)getModel()).remove(treePath);
    }

    void setLabels(Widget widget, String[] text, ImageDescriptor[] image) {
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
    protected void nodeChanged(ModelNode node) {
        Widget widget = node.getWidget();
        if (widget != null && !widget.isDisposed()) {
            if (widget instanceof TreeItem) {
                if (!isVisible(widget)) {
                    clear(widget);
                    return;
                }
            }
            widget.setData(node.getElement());
            getModel().mapWidget(widget, node);
            internalRefresh(node.getElement(), widget);
            attemptExpansion();
            attemptSelection(false);
        }
    }
    
    /**
     * Called when nodes are set in the model. The children may not have been retrieved
     * yet when the tree got the call to "set data".
     * 
     * @param parent
     * @param children
     */
    protected void nodeChildrenSet(ModelNode parent, ModelNode[] children) {
        int[] indicies = (int[]) fParentsPendingChildren.remove(parent);
        Widget widget = parent.getWidget();
        if (widget != null && !widget.isDisposed()) {
            if (indicies != null) {
                for (int i = 0; i < indicies.length; i++) {
                    int index = indicies[i];
                    TreeItem item = null;
                    if (widget instanceof TreeItem) {
                        TreeItem treeItem = (TreeItem)widget;
                        if (index < treeItem.getItemCount()) {
                            item = treeItem.getItem(index);
                        }
                    } else {
                        Tree tree = (Tree)widget;
                        if (index < tree.getItemCount()) {
                            item = ((Tree)widget).getItem(index);
                        }
                    }
                    if (item != null) {
                        if (index < children.length) {
                            getModel().mapWidget(item, children[index]);
                            internalRefresh(children[index].getElement(), item);
                        }
                    }
                }
                setItemCount(widget, children.length);
            }
            else {
                setItemCount(widget, children.length);
            }   
        }
        attemptExpansion();
        attemptSelection(false);        
    }

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.viewers.AsynchronousModelViewer#createUpdatePolicy()
	 */
	public IModelUpdatePolicy createUpdatePolicy() {
		return new TreeUpdatePolicy();
	}
	
    /**
     * A node has been disposed from the model.
     * 
     * @param node
     */
    protected synchronized void nodeDisposed(ModelNode node) {
    	super.nodeDisposed(node);
    	Widget widget = node.getWidget();
    	if (widget instanceof Tree && !widget.isDisposed()) {
    		clear(widget);
    	}
    }	

}
