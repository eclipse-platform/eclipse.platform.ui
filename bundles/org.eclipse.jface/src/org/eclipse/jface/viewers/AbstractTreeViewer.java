/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.viewers;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.core.commands.util.ListenerList;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;

/**
 * Abstract base implementation for tree-structure-oriented viewers (trees and
 * table trees).
 * <p>
 * Nodes in the tree can be in either an expanded or a collapsed state,
 * depending on whether the children on a node are visible. This class
 * introduces public methods for controlling the expanding and collapsing of
 * nodes.
 * </p>
 * <p>
 * Content providers for abstract tree viewers must implement the <code>ITreeContentProvider</code>
 * interface.
 * </p>
 * 
 * @see TreeViewer
 * @see TableTreeViewer
 */
public abstract class AbstractTreeViewer extends StructuredViewer {

    /**
     * Constant indicating that all levels of the tree should be expanded or
     * collapsed.
     * 
     * @see #expandToLevel
     * @see #collapseToLevel
     */
    public static final int ALL_LEVELS = -1;

    /**
     * List of registered tree listeners (element type: <code>TreeListener</code>). */
    private ListenerList treeListeners = new ListenerList(1);

    /**
     * The level to which the tree is automatically expanded each time the
     * viewer's input is changed (that is, by <code>setInput</code>). A
     * value of 0 means that auto-expand is off.
     * 
     * @see #setAutoExpandLevel
     */
    private int expandToLevel = 0;

    /**
     * Safe runnable used to update an item.
     */
    class UpdateItemSafeRunnable extends SafeRunnable {
        private Object element;

        private Item item;

        UpdateItemSafeRunnable(Item item, Object element) {
            this.item = item;
            this.element = element;
        }

        public void run() {
            doUpdateItem(item, element);
        }
        
    }

    /**
     * Creates an abstract tree viewer. The viewer has no input, no content
     * provider, a default label provider, no sorter, no filters, and has
     * auto-expand turned off.
     */
    protected AbstractTreeViewer() {
        // do nothing
    }

    /**
     * Adds the given child elements to this viewer as children of the given
     * parent element. If this viewer does not have a sorter, the elements are
     * added at the end of the parent's list of children in the order given;
     * otherwise, the elements are inserted at the appropriate positions.
     * <p>
     * This method should be called (by the content provider) when elements
     * have been added to the model, in order to cause the viewer to accurately
     * reflect the model. This method only affects the viewer, not the model.
     * </p>
     * 
     * @param parentElement
     *           the parent element
     * @param childElements
     *           the child elements to add
     */
    public void add(Object parentElement, Object[] childElements) {
        Assert.isNotNull(parentElement);
        assertElementsNotNull(childElements);

        Widget widget = findItem(parentElement);
        // If parent hasn't been realized yet, just ignore the add.
        if (widget == null)
            return;

        internalAdd(widget, parentElement, childElements);
    }

	/**
     * Adds the given child elements to this viewer as children of the given
     * parent element.
     * <p>
     * EXPERIMENTAL.  Not to be used except by JDT.
     * This method was added to support JDT's explorations
     * into grouping by working sets, which requires viewers to support multiple 
     * equal elements.  See bug 76482 for more details.  This support will
     * likely be removed in Eclipse 3.2 in favour of proper support for
     * multiple equal elements. 
     * </p>
     *
     * @param widget 
     *           the widget for the parent element
     * @param parentElement
     *           the parent element
     * @param childElements
     *           the child elements to add
     * @since 3.1
     */
    protected void internalAdd(Widget widget, Object parentElement, Object[] childElements) {
		
		// optimization!
        // if the widget is not expanded we just invalidate the subtree
        if (widget instanceof Item) {
            Item ti = (Item) widget;
            if (!getExpanded(ti)) {
                boolean needDummy = isExpandable(parentElement);
                boolean haveDummy = false;
                // remove all children
                Item[] items = getItems(ti);
                for (int i = 0; i < items.length; i++) {
                    if (items[i].getData() != null) {
                        disassociate(items[i]);
                        items[i].dispose();
                    } else {
                        if (needDummy && !haveDummy) {
                            haveDummy = true;
                        } else {
                            items[i].dispose();
                        }
                    }
                }
                // append a dummy if necessary
                if (needDummy && !haveDummy) 
                    newItem(ti, SWT.NULL, -1);
                return;
            }
        }

        if (childElements.length > 0) {
            Object[] filtered = filter(childElements);
			if(getSorter() != null)
				getSorter().sort(this,filtered);
            createAddedElements(widget, filtered);
        }
	}

	/**
     * Create the new elements in the parent widget. If the
     * child already exists do nothing.
     * @param widget
     * @param elements Sorted list of elements to add.
     */
    private void createAddedElements(Widget widget, Object[] elements) {

		if(elements.length == 1){
			if (equals(elements[0], widget.getData()))
				return;
		}
		
		ViewerSorter sorter = getSorter ();
		Item[] items = getChildren(widget);
		
		//As the items are sorted already we optimize for a 
		//start position
		int lastInsertion = 0;		
		
		//Optimize for the empty case
		if(items.length == 0){
			for (int i = 0; i < elements.length; i++) {
				createTreeItem(widget, elements[i], -1);		
			}
			return;
		}
	
		for (int i = 0; i < elements.length; i++) {
			boolean newItem = true;
			Object element = elements[i];
			int index;
			if(sorter == null){
				if(itemExists(items,element)){
					refresh(element);
					newItem = false;
				}
				index = -1;
			}
			else{
				lastInsertion = insertionPosition(items,sorter,lastInsertion, element);
				//As we are only searching the original array we keep track of those positions only
				if(lastInsertion == items.length)
					index = -1;
				else{//See if we should just refresh
					while(lastInsertion < items.length && sorter.compare(this,element,items[lastInsertion].getData()) == 0){
						//As we cannot assume the sorter is consistent with equals() - therefore we can
						// just check against the item prior to this index (if any)
						if (items[lastInsertion].getData().equals(element)) {
							//refresh the element in case it has new children
							refresh(element);
							newItem = false;
						}
						lastInsertion ++;//We had an insertion so increment
					}
					//Did we get to the end?
					if(lastInsertion == items.length)
						index = -1;
					else
						index = lastInsertion + i; //Add the index as the array is growing					
				}
			}
			if(newItem)
				createTreeItem(widget, element, index);		
		}
    }
	

    /**
     * See if element is the data of one of the elements in 
     * items.
     * @param items
     * @param element
     * @return <code>true</code> if the element matches.
     */
	private boolean itemExists(Item[] items, Object element) {
		if(usingElementMap())//if we can do a constant time lookup find it
			return findItem(element) != null;
		for (int i = 0; i < items.length; i++) {
			if(items[i].getData().equals(element))
				return true;
		}
		return false;
	}

	/**
     * Returns the index where the item should be inserted. It uses sorter to
     * determine the correct position, if sorter is not assigned, returns the
     * index of the element after the last.
     * 
     * @param items the items to search
     * @param sorter The sorter to use.
     * @param lastInsertion
     *            the start index to start search for position from this allows
     *            optimising search for multiple elements that are sorted
     *            themself.
     * @param element
     *            element to find position for.
     * @return the index to use when inserting the element.
     * 
     */
	
	private int insertionPosition(Item[] items,  ViewerSorter sorter, int lastInsertion, Object element) {
	    
		int size = items.length;
		if (sorter == null)
			return size;
	    int min = lastInsertion, max = size - 1;

		while (min <= max) {
            int mid = (min + max) / 2;
            Object data = items[mid].getData();
            int compare = sorter.compare(this, data, element);
            if (compare == 0) {
                return mid;//Return if we already match
            }
            if (compare < 0)
                min = mid + 1;
            else
                max = mid - 1;
	        }
	   return min;
	    
			
	}

	/**
     * Returns the index where the item should be inserted. It uses sorter to
     * determine the correct position, if sorter is not assigned, returns the
     * index of the element after the last.
     * 
     * @param parent The parent widget
     * @param sorter The sorter to use.
     * @param startIndex
     *            the start index to start search for position from this allows
     *            optimising search for multiple elements that are sorted
     *            themself.
     * @param element
     *            element to find position for.
     * @param currentSize
     * 			the current size of the collection
     * @return the index to use when inserting the element.
     * 
     */
	
    /**
     * Returns the index where the item should be inserted.
     * 
     * @param parent
     *           The parent widget the element will be inserted into.
     * @param element
     *           The element to insert.
     * @return int
     */
    protected int indexForElement(Widget parent, Object element) {
        ViewerSorter sorter = getSorter();
		
		
		Item[] items = getChildren(parent);
		int count = items.length;

        if (sorter == null)
            return count;
        int min = 0, max = count - 1;

        while (min <= max) {
            int mid = (min + max) / 2;
            Object data = items[mid].getData();
            int compare = sorter.compare(this, data, element);
            if (compare == 0) {
                // find first item > element
                while (compare == 0) {
                    ++mid;
                    if (mid >= count) {
                        break;
                    }
                    data = items[mid].getData();
                    compare = sorter.compare(this, data, element);
                }
                return mid;
            }
            if (compare < 0)
                min = mid + 1;
            else
                max = mid - 1;
        }
        return min;
    }

    /**
     * Adds the given child element to this viewer as a child of the given
     * parent element. If this viewer does not have a sorter, the element is
     * added at the end of the parent's list of children; otherwise, the
     * element is inserted at the appropriate position.
     * <p>
     * This method should be called (by the content provider) when a single
     * element has been added to the model, in order to cause the viewer to
     * accurately reflect the model. This method only affects the viewer, not
     * the model. Note that there is another method for efficiently processing
     * the simultaneous addition of multiple elements.
     * </p>
     * 
     * @param parentElement
     *           the parent element
     * @param childElement
     *           the child element
     */
    public void add(Object parentElement, Object childElement) {
        add(parentElement, new Object[] { childElement });
    }

    /**
     * Adds the given SWT selection listener to the given SWT control.
     * 
     * @param control
     *           the SWT control
     * @param listener
     *           the SWT selection listener
     * @deprecated
     */
    protected void addSelectionListener(Control control,
            SelectionListener listener) {
        // do nothing
    }

    /**
     * Adds a listener for expand and collapse events in this viewer. Has no
     * effect if an identical listener is already registered.
     * 
     * @param listener
     *           a tree viewer listener
     */
    public void addTreeListener(ITreeViewerListener listener) {
        treeListeners.add(listener);
    }

    /**
     * Adds the given SWT tree listener to the given SWT control.
     * 
     * @param control
     *           the SWT control
     * @param listener
     *           the SWT tree listener
     */
    protected abstract void addTreeListener(Control control,
            TreeListener listener);

    /* (non-Javadoc) @see StructuredViewer#associate(Object, Item) */
    protected void associate(Object element, Item item) {
        Object data = item.getData();
        if (data != null && data != element && equals(data, element)) {
            // workaround for PR 1FV62BT
            // assumption: elements are equal but not identical
            // -> remove from map but don't touch children
            unmapElement(data, item);
            item.setData(element);
            mapElement(element, item);
        } else {
            // recursively disassociate all
            super.associate(element, item);
        }
    }

    /**
     * Collapses all nodes of the viewer's tree, starting with the root. This
     * method is equivalent to <code>collapseToLevel(ALL_LEVELS)</code>.
     */
    public void collapseAll() {
        Object root = getRoot();
        if (root != null) {
            collapseToLevel(root, ALL_LEVELS);
        }
    }

    /**
     * Collapses the subtree rooted at the given element to the given level.
     * 
     * @param element
     *           the element
     * @param level
     *           non-negative level, or <code>ALL_LEVELS</code> to collapse
     *           all levels of the tree
     */
    public void collapseToLevel(Object element, int level) {
        Assert.isNotNull(element);
        Widget w = findItem(element);
        if (w != null)
            internalCollapseToLevel(w, level);
    }

    /**
     * Creates all children for the given widget.
     * <p>
     * The default implementation of this framework method assumes that <code>widget.getData()</code>
     * returns the element corresponding to the node. Note: the node is not
     * visually expanded! You may have to call <code>parent.setExpanded(true)</code>.
     * </p>
     * 
     * @param widget
     *           the widget
     */
    protected void createChildren(final Widget widget) {
        final Item[] tis = getChildren(widget);
        if (tis != null && tis.length > 0) {
            Object data = tis[0].getData();
            if (data != null)
                return; // children already there!
        }

        BusyIndicator.showWhile(widget.getDisplay(), new Runnable() {
            public void run() {
                // fix for PR 1FW89L7:
                // don't complain and remove all "dummies" ...
                if (tis != null) {
                    for (int i = 0; i < tis.length; i++) {
                        if (tis[i].getData() != null) {
                            disassociate(tis[i]);
                            Assert.isTrue(tis[i].getData() == null,
                                    "Second or later child is non -null");//$NON-NLS-1$

                        }
                        tis[i].dispose();
                    }
                }
                Object d = widget.getData();
                if (d != null) {
                    Object parentElement = d;
                    Object[] children = getSortedChildren(parentElement);
                    for (int i = 0; i < children.length; i++) {
                        createTreeItem(widget, children[i], -1);
                    }
                }
            }
        });
    }

    /**
     * Creates a single item for the given parent and synchronizes it with the
     * given element.
     * 
     * @param parent
     *           the parent widget
     * @param element
     *           the element
     * @param index
     *           if non-negative, indicates the position to insert the item
     *           into its parent
     */
    protected void createTreeItem(Widget parent, Object element, int index) {
        Item item = newItem(parent, SWT.NULL, index);
        updateItem(item, element);
        updatePlus(item, element);
    }

    /**
     * The <code>AbstractTreeViewer</code> implementation of this method also
     * recurses over children of the corresponding element.
     */
    protected void disassociate(Item item) {
        super.disassociate(item);
        // recursively unmapping the items is only required when
        // the hash map is used. In the other case disposing
        // an item will recursively dispose its children.
        if (usingElementMap())
            disassociateChildren(item);
    }

    /**
     * Disassociates the children of the given SWT item from their
     * corresponding elements.
     * 
     * @param item
     *           the widget
     */
    private void disassociateChildren(Item item) {
        Item[] items = getChildren(item);
        for (int i = 0; i < items.length; i++) {
            if (items[i].getData() != null)
                disassociate(items[i]);
        }
    }

    /* (non-Javadoc) Method declared on StructuredViewer. */
    protected Widget doFindInputItem(Object element) {
        // compare with root
        Object root = getRoot();
        if (root == null)
            return null;

        if (equals(root, element))
            return getControl();
        return null;
    }

    /* (non-Javadoc) Method declared on StructuredViewer. */
    protected Widget doFindItem(Object element) {
        // compare with root
        Object root = getRoot();
        if (root == null)
            return null;

        Item[] items = getChildren(getControl());
        if (items != null) {
            for (int i = 0; i < items.length; i++) {
                Widget o = internalFindItem(items[i], element);
                if (o != null)
                    return o;
            }
        }
        return null;
    }

    /**
     * Copies the attributes of the given element into the given SWT item.
     * 
     * @param item
     *           the SWT item
     * @param element
     *           the element
     */
    protected abstract void doUpdateItem(Item item, Object element);

    /* (non-Javadoc) Method declared on StructuredViewer. */
    protected void doUpdateItem(Widget widget, Object element, boolean fullMap) {
        if (widget instanceof Item) {
            Item item = (Item) widget;

            // ensure that backpointer is correct
            if (fullMap) {
                associate(element, item);
            } else {
                item.setData(element);
                mapElement(element, item);
            }

            // update icon and label
            SafeRunnable.run(new UpdateItemSafeRunnable(item, element));
        }
    }

    /**
     * Expands all nodes of the viewer's tree, starting with the root. This
     * method is equivalent to <code>expandToLevel(ALL_LEVELS)</code>.
     */
    public void expandAll() {
        expandToLevel(ALL_LEVELS);
    }

    /**
     * Expands the root of the viewer's tree to the given level.
     * 
     * @param level
     *           non-negative level, or <code>ALL_LEVELS</code> to expand all
     *           levels of the tree
     */
    public void expandToLevel(int level) {
        expandToLevel(getRoot(), level);
    }

    /**
     * Expands all ancestors of the given element so that the given element
     * becomes visible in this viewer's tree control, and then expands the
     * subtree rooted at the given element to the given level.
     * 
     * @param element
     *           the element
     * @param level
     *           non-negative level, or <code>ALL_LEVELS</code> to expand all
     *           levels of the tree
     */
    public void expandToLevel(Object element, int level) {
        Widget w = internalExpand(element, true);
        if (w != null)
            internalExpandToLevel(w, level);
    }

    /**
     * Fires a tree collapsed event. Only listeners registered at the time this
     * method is called are notified.
     * 
     * @param event
     *           the tree expansion event
     * @see ITreeViewerListener#treeCollapsed
     */
    protected void fireTreeCollapsed(final TreeExpansionEvent event) {
        Object[] listeners = treeListeners.getListeners();
        for (int i = 0; i < listeners.length; ++i) {
            final ITreeViewerListener l = (ITreeViewerListener) listeners[i];
            SafeRunnable.run(new SafeRunnable() {
                public void run() {
                    l.treeCollapsed(event);
                }
            });
        }
    }

    /**
     * Fires a tree expanded event. Only listeners registered at the time this
     * method is called are notified.
     * 
     * @param event
     *           the tree expansion event
     * @see ITreeViewerListener#treeExpanded
     */
    protected void fireTreeExpanded(final TreeExpansionEvent event) {
        Object[] listeners = treeListeners.getListeners();
        for (int i = 0; i < listeners.length; ++i) {
            final ITreeViewerListener l = (ITreeViewerListener) listeners[i];
            SafeRunnable.run(new SafeRunnable() {
                public void run() {
                    l.treeExpanded(event);
                }
            });
        }

    }

    /**
     * Returns the auto-expand level.
     * 
     * @return non-negative level, or <code>ALL_LEVELS</code> if all levels
     *         of the tree are expanded automatically
     * @see #setAutoExpandLevel
     */
    public int getAutoExpandLevel() {
        return expandToLevel;
    }

    /**
     * Returns the SWT child items for the given SWT widget.
     * 
     * @param widget
     *           the widget
     * @return the child items
     */
    protected abstract Item[] getChildren(Widget widget);
	
	/**
	 * Get the child for the widget at index. Note that the default
	 * implementation is not very effecient and should be overridden
	 * if this class is implemented.
	 * @param widget the widget to check
	 * @param index the index of the widget
	 * @return Item or <code>null</code> if widget is not a type
	 * that can contain items.
     *
	 * @throws ArrayIndexOutOfBoundsException if the index is not valid.
     * @since 3.1
	 */
    protected Item getChild (Widget widget, int index) {
		return getChildren(widget)[index];
    }

    /**
     * Returns whether the given SWT item is expanded or collapsed.
     * 
     * @param item
     *           the item
     * @return <code>true</code> if the item is considered expanded and
     *         <code>false</code> if collapsed
     */
    protected abstract boolean getExpanded(Item item);

    /**
     * Returns a list of elements corresponding to expanded nodes in this
     * viewer's tree, including currently hidden ones that are marked as
     * expanded but are under a collapsed ancestor.
     * <p>
     * This method is typically used when preserving the interesting state of a
     * viewer; <code>setExpandedElements</code> is used during the restore.
     * </p>
     * 
     * @return the array of expanded elements
     * @see #setExpandedElements
     */
    public Object[] getExpandedElements() {
        ArrayList v = new ArrayList();
        internalCollectExpanded(v, getControl());
        return v.toArray();
    }

    /**
     * Returns whether the node corresponding to the given element is expanded
     * or collapsed.
     * 
     * @param element
     *           the element
     * @return <code>true</code> if the node is expanded, and <code>false</code>
     *         if collapsed
     */
    public boolean getExpandedState(Object element) {
        Assert.isNotNull(element);
        Widget item = findItem(element);
        if (item instanceof Item)
            return getExpanded((Item) item);
        return false;
    }

    /**
     * Returns the number of child items of the given SWT control.
     * 
     * @param control
     *           the control
     * @return the number of children
     */
    protected abstract int getItemCount(Control control);

    /**
     * Returns the number of child items of the given SWT item.
     * 
     * @param item
     *           the item
     * @return the number of children
     */
    protected abstract int getItemCount(Item item);

    /**
     * Returns the child items of the given SWT item.
     * 
     * @param item
     *           the item
     * @return the child items
     */
    protected abstract Item[] getItems(Item item);

    /**
     * Returns the item after the given item in the tree, or <code>null</code>
     * if there is no next item.
     * 
     * @param item
     *           the item
     * @param includeChildren
     *           <code>true</code> if the children are considered in
     *           determining which item is next, and <code>false</code> if
     *           subtrees are ignored
     * @return the next item, or <code>null</code> if none
     */
    protected Item getNextItem(Item item, boolean includeChildren) {
        if (item == null) {
            return null;
        }
        if (includeChildren && getExpanded(item)) {
            Item[] children = getItems(item);
            if (children != null && children.length > 0) {
                return children[0];
            }
        }

        //next item is either next sibling or next sibling of first
        //parent that has a next sibling.
        Item parent = getParentItem(item);
        if (parent == null) {
            return null;
        }
        Item[] siblings = getItems(parent);
        if (siblings != null){
        	if(siblings.length <= 1) 
        		return getNextItem(parent, false);
        	
        	for (int i = 0; i < siblings.length; i++) {
                if (siblings[i] == item && i < (siblings.length - 1)) {
                    return siblings[i + 1];
                }
            }
        }
        return getNextItem(parent, false);
    }

    /**
     * Returns the parent item of the given item in the tree, or <code>null</code>
     * if there is no parent item.
     * 
     * @param item
     *           the item
     * @return the parent item, or <code>null</code> if none
     */
    protected abstract Item getParentItem(Item item);

    /**
     * Returns the item before the given item in the tree, or <code>null</code>
     * if there is no previous item.
     * 
     * @param item
     *           the item
     * @return the previous item, or <code>null</code> if none
     */
    protected Item getPreviousItem(Item item) {
        //previous item is either right-most visible descendent of previous
        //sibling or parent
        Item parent = getParentItem(item);
        if (parent == null) {
            return null;
        }
        Item[] siblings = getItems(parent);
        if (siblings.length == 0 || siblings[0] == item) {
            return parent;
        }
        Item previous = siblings[0];
        for (int i = 1; i < siblings.length; i++) {
            if (siblings[i] == item) {
                return rightMostVisibleDescendent(previous);
            }
            previous = siblings[i];
        }
        return null;
    }

    /* (non-Javadoc) Method declared on StructuredViewer. */
    protected Object[] getRawChildren(Object parent) {
        if (parent != null) {
            if (equals(parent, getRoot()))
                return super.getRawChildren(parent);
            ITreeContentProvider cp = (ITreeContentProvider) getContentProvider();
            if (cp != null) {
                Object[] result = cp.getChildren(parent);
                if (result != null)
                    return result;
            }
        }
        return new Object[0];
    }

    /**
     * Returns all selected items for the given SWT control.
     * 
     * @param control
     *           the control
     * @return the list of selected items
     */
    protected abstract Item[] getSelection(Control control);

    /* (non-Javadoc) Method declared on StructuredViewer. */
    protected List getSelectionFromWidget() {
        Widget[] items = getSelection(getControl());
        ArrayList list = new ArrayList(items.length);
        for (int i = 0; i < items.length; i++) {
            Widget item = items[i];
            Object e = item.getData();
            if (e != null)
                list.add(e);
        }
        return list;
    }

    /**
     * Handles a tree collapse event from the SWT widget.
     * 
     * @param event
     *           the SWT tree event
     */
    protected void handleTreeCollapse(TreeEvent event) {
        if (event.item.getData() != null) {
            fireTreeCollapsed(new TreeExpansionEvent(this, event.item.getData()));
        }
    }

    /**
     * Handles a tree expand event from the SWT widget.
     * 
     * @param event
     *           the SWT tree event
     */
    protected void handleTreeExpand(TreeEvent event) {
    	if (!(getContentProvider() instanceof ILazyTreeContentProvider)) {
			createChildren(event.item);
		}
        if (event.item.getData() != null) {
            fireTreeExpanded(new TreeExpansionEvent(this, event.item.getData()));
        }
    }

    /* (non-Javadoc) Method declared on Viewer. */
    protected void hookControl(Control control) {
        super.hookControl(control);
        addTreeListener(control, new TreeListener() {
            public void treeExpanded(TreeEvent event) {
                handleTreeExpand(event);
            }

            public void treeCollapsed(TreeEvent event) {
                handleTreeCollapse(event);
            }
        });
    }

    /*
     * (non-Javadoc) Method declared on StructuredViewer. Builds the initial
     * tree and handles the automatic expand feature.
     */
    protected void inputChanged(Object input, Object oldInput) {
        preservingSelection(new Runnable() {
            public void run() {
                Control tree = getControl();
                boolean useRedraw = true;
                // (size > REDRAW_THRESHOLD) || (table.getItemCount() >
                // REDRAW_THRESHOLD);
                if (useRedraw)
                    tree.setRedraw(false);
                removeAll(tree);
                tree.setData(getRoot());
                createChildren(tree);
                internalExpandToLevel(tree, expandToLevel);
                if (useRedraw)
                    tree.setRedraw(true);
            }
        });
    }

    /**
     * Recursively collapses the subtree rooted at the given widget to the
     * given level.
     * <p>
     * </p>
     * Note that the default implementation of this method does not call <code>setRedraw</code>.
     * 
     * @param widget
     *           the widget
     * @param level
     *           non-negative level, or <code>ALL_LEVELS</code> to collapse
     *           all levels of the tree
     */
    protected void internalCollapseToLevel(Widget widget, int level) {
        if (level == ALL_LEVELS || level > 0) {

            if (widget instanceof Item)
                setExpanded((Item) widget, false);

            if (level == ALL_LEVELS || level > 1) {
                Item[] children = getChildren(widget);
                if (children != null) {
                    int nextLevel = (level == ALL_LEVELS ? ALL_LEVELS
                            : level - 1);
                    for (int i = 0; i < children.length; i++)
                        internalCollapseToLevel(children[i], nextLevel);
                }
            }
        }
    }

    /**
     * Recursively collects all expanded elements from the given widget.
     * 
     * @param result
     *           a list (element type: <code>Object</code>) into which to
     *           collect the elements
     * @param widget
     *           the widget
     */
    private void internalCollectExpanded(List result, Widget widget) {
        Item[] items = getChildren(widget);
        for (int i = 0; i < items.length; i++) {
            Item item = items[i];
            if (getExpanded(item)) {
                Object data = item.getData();
                if (data != null)
                    result.add(data);
            }
            internalCollectExpanded(result, item);
        }
    }

    /**
     * Tries to create a path of tree items for the given element. This method
     * recursively walks up towards the root of the tree and assumes that
     * <code>getParent</code> returns the correct parent of an element.
     * 
     * @param element
     *           the element
     * @param expand
     *           <code>true</code> if all nodes on the path should be
     *           expanded, and <code>false</code> otherwise
     * @return Widget
     */
    protected Widget internalExpand(Object element, boolean expand) {

        if (element == null)
            return null;

        Widget w = internalGetWidgetToSelect(element);
        if (w == null) {
            if (equals(element, getRoot())) { // stop at root
                return null;
            }
            // my parent has to create me
            ITreeContentProvider cp = (ITreeContentProvider) getContentProvider();
            if (cp == null) {
                return null;
            }
            Object parent = cp.getParent(element);
            if (parent != null) {
                Widget pw = internalExpand(parent, false);
                if (pw != null) {
                    // let my parent create me
                    createChildren(pw);
                    // expand parent and find me
                    if (pw instanceof Item) {
                        Item item = (Item) pw;
                        w = internalFindChild(item, element);
                        if (expand) {
                        	// expand parent items bottom-up after they have all been materialized, to reduce flicker
                        	while (item != null && !getExpanded(item)) {
                        		setExpanded(item, true);
                        		item = getParentItem(item);
                        	}
                        }
                    }
                }
            }
        }
        return w;
    }

    /**
     * Returns the widget to be selected for the given element.
     * 
     * @param element the element to select
     * @return the widget to be selected, or <code>null</code> if not found
     * 
     * @since 3.1
     */
	protected Widget internalGetWidgetToSelect(Object element) {
		return findItem(element);
	}

    /**
     * Recursively expands the subtree rooted at the given widget to the given
     * level.
     * <p>
     * </p>
     * Note that the default implementation of this method does not call <code>setRedraw</code>.
     * 
     * @param widget
     *           the widget
     * @param level
     *           non-negative level, or <code>ALL_LEVELS</code> to collapse
     *           all levels of the tree
     */
    protected void internalExpandToLevel(Widget widget, int level) {
        if (level == ALL_LEVELS || level > 0) {
        	if(!(getContentProvider() instanceof ILazyTreeContentProvider)) {
        		createChildren(widget);
        	}
            if (widget instanceof Item)
                setExpanded((Item) widget, true);
            if (level == ALL_LEVELS || level > 1) {
                Item[] children = getChildren(widget);
                if (children != null) {
                    int newLevel = (level == ALL_LEVELS ? ALL_LEVELS
                            : level - 1);
                    for (int i = 0; i < children.length; i++)
                        internalExpandToLevel(children[i], newLevel);
                }
            }
        }
    }

    /**
     * Non-recursively tries to find the given element as a child of the given
     * parent item.
     * 
     * @param parent
     *           the parent item
     * @param element
     *           the element
     * @return Widget
     */
    private Widget internalFindChild(Item parent, Object element) {
        Item[] items = getChildren(parent);
        for (int i = 0; i < items.length; i++) {
            Item item = items[i];
            Object data = item.getData();
            if (data != null && equals(data, element))
                return item;
        }
        return null;
    }

    /**
     * Recursively tries to find the given element.
     * 
     * @param parent
     *           the parent item
     * @param element
     *           the element
     * @return Widget
     */
    private Widget internalFindItem(Item parent, Object element) {

        // compare with node
        Object data = parent.getData();
        if (data != null) {
            if (equals(data, element))
                return parent;
        }
        // recurse over children
        Item[] items = getChildren(parent);
        for (int i = 0; i < items.length; i++) {
            Item item = items[i];
            Widget o = internalFindItem(item, element);
            if (o != null)
                return o;
        }
        return null;
    }

    /* (non-Javadoc) Method declared on StructuredViewer. */
    protected void internalRefresh(Object element) {
        internalRefresh(element, true);
    }

    /* (non-Javadoc) Method declared on StructuredViewer. */
    protected void internalRefresh(Object element, boolean updateLabels) {
        // If element is null, do a full refresh.
        if (element == null) {
            internalRefresh(getControl(), getRoot(), true, updateLabels);
            return;
        }
        Widget item = findItem(element);
        if (item != null) {
            // pick up structure changes too
            internalRefresh(item, element, true, updateLabels);
        }
    }

    /**
     * Refreshes the tree starting at the given widget.
     * <p>
     * EXPERIMENTAL.  Not to be used except by JDT.
     * This method was added to support JDT's explorations
     * into grouping by working sets, which requires viewers to support multiple 
     * equal elements.  See bug 76482 for more details.  This support will
     * likely be removed in Eclipse 3.2 in favour of proper support for
     * multiple equal elements. 
     * </p>
     * 
     * @param widget
     *           the widget
     * @param element
     *           the element
     * @param doStruct
     *           <code>true</code> if structural changes are to be picked up,
     *           and <code>false</code> if only label provider changes are of
     *           interest
     * @param updateLabels
     *           <code>true</code> to update labels for existing elements,
     *           <code>false</code> to only update labels as needed, assuming
     *           that labels for existing elements are unchanged.
     * @since 3.1
     */
    protected void internalRefresh(Widget widget, Object element,
            boolean doStruct, boolean updateLabels) {

        if (widget instanceof Item) {
            if (doStruct) {
                updatePlus((Item) widget, element);
            }
            if (updateLabels || !equals(element, widget.getData())) {
                doUpdateItem(widget, element, true);
            } else {
                associate(element, (Item) widget);
            }
        }

        if (doStruct) {
            internalRefreshStruct(widget, element, updateLabels);
        } else {
            Item[] children = getChildren(widget);
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    Widget item = children[i];
                    Object data = item.getData();
                    if (data != null)
                        internalRefresh(item, data, doStruct, updateLabels);
                }
            }
        }
    }

    /**
     * Update the structure and recurse. Items are updated in updateChildren,
     * as needed.
     * @param widget
     * @param element
     * @param updateLabels
     */
    private void internalRefreshStruct(Widget widget, Object element,
            boolean updateLabels) {
        updateChildren(widget, element, null, updateLabels);
        Item[] children = getChildren(widget);
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                Widget item = children[i];
                Object data = item.getData();
                if (data != null)
                    internalRefreshStruct(item, data, updateLabels);
            }
        }
    }

    /**
     * Removes the given elements from this viewer.
     * <p>
     * EXPERIMENTAL.  Not to be used except by JDT.
     * This method was added to support JDT's explorations
     * into grouping by working sets, which requires viewers to support multiple 
     * equal elements.  See bug 76482 for more details.  This support will
     * likely be removed in Eclipse 3.2 in favour of proper support for
     * multiple equal elements. 
     * </p>
     * 
     * @param elements
     *           the elements to remove
     * @since 3.1
     */
    protected void internalRemove(Object[] elements) {
        Object input = getInput();
        // Note: do not use the comparer here since the hashtable
        // contains SWT Items, not model elements.
        CustomHashtable parentItems = new CustomHashtable(5);
        for (int i = 0; i < elements.length; ++i) {
            if (equals(elements[i], input)) {
                setInput(null);
                return;
            }
            Widget childItem = findItem(elements[i]);
            if (childItem instanceof Item) {
                Item parentItem = getParentItem((Item) childItem);
                if (parentItem != null) {
                    parentItems.put(parentItem, parentItem);
                }
                disassociate((Item) childItem);
                childItem.dispose();
            }
        }
        Control tree = getControl();
        for (Enumeration e = parentItems.keys(); e.hasMoreElements();) {
            Item parentItem = (Item) e.nextElement();
            if(parentItem.isDisposed())
            	continue;
            if (!getExpanded(parentItem) && getItemCount(parentItem) == 0) {
                // append a dummy if necessary
                if (isExpandable(parentItem.getData())) {
                    newItem(parentItem, SWT.NULL, -1);
                } else {
                    // XXX: Workaround (PR missing)
                    tree.redraw();
                }
            }
        }
    }

    /**
     * Sets the expanded state of all items to correspond to the given set of
     * expanded elements.
     * 
     * @param expandedElements
     *           the set (element type: <code>Object</code>) of elements
     *           which are expanded
     * @param widget
     *           the widget
     */
    private void internalSetExpanded(CustomHashtable expandedElements,
            Widget widget) {
        Item[] items = getChildren(widget);
        for (int i = 0; i < items.length; i++) {
            Item item = items[i];
            Object data = item.getData();
            if (data != null) {
                // remove the element to avoid an infinite loop
                // if the same element appears on a child item
                boolean expanded = expandedElements.remove(data) != null;
                if (expanded != getExpanded(item)) {
                    if (expanded) {
                        createChildren(item);
                    }
                    setExpanded(item, expanded);
                }
            }
            internalSetExpanded(expandedElements, item);
        }
    }

    /**
     * Return whether the tree node representing the given element can be
     * expanded.
     * <p>
     * The default implementation of this framework method calls <code>hasChildren</code>
     * on this viewer's content provider. It may be overridden if necessary.
     * </p>
     * 
     * @param element
     *           the element
     * @return <code>true</code> if the tree node representing the given
     *         element can be expanded, or <code>false</code> if not
     */
    public boolean isExpandable(Object element) {
        ITreeContentProvider cp = (ITreeContentProvider) getContentProvider();
        return cp != null && cp.hasChildren(element);
    }

    /* (non-Javadoc) Method declared on Viewer. */
    protected void labelProviderChanged() {
        // we have to walk the (visible) tree and update every item
        Control tree = getControl();
        tree.setRedraw(false);
        // don't pick up structure changes, but do force label updates
        internalRefresh(tree, getRoot(), false, true);
        tree.setRedraw(true);
    }

    /**
     * Creates a new item.
     * 
     * @param parent
     *           the parent widget
     * @param style
     *           SWT style bits
     * @param index
     *           if non-negative, indicates the position to insert the item
     *           into its parent
     * @return the newly-created item
     */
    protected abstract Item newItem(Widget parent, int style, int index);

    /**
     * Removes the given elements from this viewer. The selection is updated if
     * required.
     * <p>
     * This method should be called (by the content provider) when elements
     * have been removed from the model, in order to cause the viewer to
     * accurately reflect the model. This method only affects the viewer, not
     * the model.
     * </p>
     * 
     * @param elements
     *           the elements to remove
     */
    public void remove(final Object[] elements) {
        assertElementsNotNull(elements);
        preservingSelection(new Runnable() {
            public void run() {
                internalRemove(elements);
            }
        });
    }

    /**
     * Removes the given element from the viewer. The selection is updated if
     * necessary.
     * <p>
     * This method should be called (by the content provider) when a single
     * element has been removed from the model, in order to cause the viewer to
     * accurately reflect the model. This method only affects the viewer, not
     * the model. Note that there is another method for efficiently processing
     * the simultaneous removal of multiple elements.
     * </p>
     * 
     * @param element
     *           the element
     */
    public void remove(Object element) {
        remove(new Object[] { element });
    }

    /**
     * Removes all items from the given control.
     * 
     * @param control
     *           the control
     */
    protected abstract void removeAll(Control control);

    /**
     * Removes a listener for expand and collapse events in this viewer. Has no
     * affect if an identical listener is not registered.
     * 
     * @param listener
     *           a tree viewer listener
     */
    public void removeTreeListener(ITreeViewerListener listener) {
        treeListeners.remove(listener);
    }

    /* Non-Javadoc. Method defined on StructuredViewer. */
    public void reveal(Object element) {
        Assert.isNotNull(element);
        Widget w = internalExpand(element, true);
        if (w instanceof Item)
            showItem((Item) w);
    }

    /**
     * Returns the rightmost visible descendent of the given item. Returns the
     * item itself if it has no children.
     * 
     * @param item
     *           the item to compute the descendent of
     * @return the rightmost visible descendent or the item iself if it has no
     *         children
     */
    private Item rightMostVisibleDescendent(Item item) {
        Item[] children = getItems(item);
        if (getExpanded(item) && children != null && children.length > 0) {
            return rightMostVisibleDescendent(children[children.length - 1]);
        }
        return item;
    }

    /* (non-Javadoc) Method declared on Viewer. */
    public Item scrollDown(int x, int y) {
        Item current = getItem(x, y);
        if (current != null) {
            Item next = getNextItem(current, true);
            showItem(next == null ? current : next);
            return next;
        }
        return null;
    }

    /* (non-Javadoc) Method declared on Viewer. */
    public Item scrollUp(int x, int y) {
        Item current = getItem(x, y);
        if (current != null) {
            Item previous = getPreviousItem(current);
            showItem(previous == null ? current : previous);
            return previous;
        }
        return null;
    }

    /**
     * Sets the auto-expand level. The value 0 means that there is no
     * auto-expand; 1 means that top-level elements are expanded, but not their
     * children; 2 means that top-level elements are expanded, and their
     * children, but not grandchildren; and so on.
     * <p>
     * The value <code>ALL_LEVELS</code> means that all subtrees should be
     * expanded.
     * </p>
     * 
     * @param level
     *           non-negative level, or <code>ALL_LEVELS</code> to expand all
     *           levels of the tree
     */
    public void setAutoExpandLevel(int level) {
        expandToLevel = level;
    }

    /**
     * The <code>AbstractTreeViewer</code> implementation of this method
     * checks to ensure that the content provider is an <code>ITreeContentProvider</code>.
     */
    public void setContentProvider(IContentProvider provider) {
    	// the actual check is in assertContentProviderType
        super.setContentProvider(provider);
    }
    
	protected void assertContentProviderType(IContentProvider provider) {
		Assert.isTrue(provider instanceof ITreeContentProvider);
	}

    /**
     * Sets the expand state of the given item.
     * 
     * @param item
     *           the item
     * @param expand
     *           the expand state of the item
     */
    protected abstract void setExpanded(Item item, boolean expand);

    /**
     * Sets which nodes are expanded in this viewer's tree. The given list
     * contains the elements that are to be expanded; all other nodes are to be
     * collapsed.
     * <p>
     * This method is typically used when restoring the interesting state of a
     * viewer captured by an earlier call to <code>getExpandedElements</code>.
     * </p>
     * 
     * @param elements
     *           the array of expanded elements
     * @see #getExpandedElements
     */
    public void setExpandedElements(Object[] elements) {
        assertElementsNotNull(elements);
        CustomHashtable expandedElements = newHashtable(elements.length * 2 + 1);
        for (int i = 0; i < elements.length; ++i) {
            Object element = elements[i];
            // Ensure item exists for element
            internalExpand(element, false);
            expandedElements.put(element, element);
        }
        internalSetExpanded(expandedElements, getControl());
    }

    /**
     * Sets whether the node corresponding to the given element is expanded or
     * collapsed.
     * 
     * @param element
     *           the element
     * @param expanded
     *           <code>true</code> if the node is expanded, and <code>false</code>
     *           if collapsed
     */
    public void setExpandedState(Object element, boolean expanded) {
        Assert.isNotNull(element);
        Widget item = internalExpand(element, false);
        if (item instanceof Item) {
            if (expanded) {
                createChildren(item);
            }
            setExpanded((Item) item, expanded);
        }
    }

    /**
     * Sets the selection to the given list of items.
     * 
     * @param items
     *           list of items (element type: <code>org.eclipse.swt.widgets.Item</code>)
     */
    protected abstract void setSelection(List items);

    /* (non-Javadoc) Method declared on StructuredViewer. */
    protected void setSelectionToWidget(List v, boolean reveal) {
        if (v == null) {
            setSelection(new ArrayList(0));
            return;
        }
        int size = v.size();
        List newSelection = new ArrayList(size);
        for (int i = 0; i < size; ++i) {
            // Use internalExpand since item may not yet be created. See
            // 1G6B1AR.
            Widget w = internalExpand(v.get(i), false);
            if (w instanceof Item) {
                newSelection.add(w);
            }
        }
        setSelection(newSelection);
        
        // Although setting the selection in the control should reveal it,
        // setSelection may be a no-op if the selection is unchanged,
        // so explicitly reveal the first item in the selection here.
        // See bug 100565 for more details.
        if (reveal && newSelection.size() > 0) {
            showItem((Item) newSelection.get(0));
        }
    }

    /**
     * Shows the given item.
     * 
     * @param item
     *           the item
     */
    protected abstract void showItem(Item item);

    /**
     * Updates the tree items to correspond to the child elements of the given
     * parent element. If null is passed for the children, this method obtains
     * them (only if needed).
     * 
     * @param widget
     *           the widget
     * @param parent
     *           the parent element
     * @param elementChildren
     *           the child elements, or null
     * @deprecated this is no longer called by the framework
     */
    protected void updateChildren(Widget widget, Object parent,
            Object[] elementChildren) {
        updateChildren(widget, parent, elementChildren, true);
    }

    /**
     * Updates the tree items to correspond to the child elements of the given
     * parent element. If null is passed for the children, this method obtains
     * them (only if needed).
     * 
     * @param widget
     *           the widget
     * @param parent
     *           the parent element
     * @param elementChildren
     *           the child elements, or null
     * @param updateLabels
     *           <code>true</code> to update labels for existing elements,
     *           <code>false</code> to only update labels as needed, assuming
     *           that labels for existing elements are unchanged.
     * @since 2.1
     */
    private void updateChildren(Widget widget, Object parent,
            Object[] elementChildren, boolean updateLabels) {
        // optimization! prune collapsed subtrees
        if (widget instanceof Item) {
            Item ti = (Item) widget;
            if (!getExpanded(ti)) {
                // need a dummy node if element is expandable;
                // but try to avoid recreating the dummy node
                boolean needDummy = isExpandable(parent);
                boolean haveDummy = false;
                // remove all children
                Item[] items = getItems(ti);
                for (int i = 0; i < items.length; i++) {
                    if (items[i].getData() != null) {
                        disassociate(items[i]);
                        items[i].dispose();
                    } else {
                        if (needDummy && !haveDummy) {
                            haveDummy = true;
                        } else {
                            items[i].dispose();
                        }
                    }
                }
                if (needDummy && !haveDummy) {
                    newItem(ti, SWT.NULL, -1);
                }

                return;
            }
        }

        // If the children weren't passed in, get them now since they're needed
        // below.
        if (elementChildren == null) {
            elementChildren = getSortedChildren(parent);
        }

        Control tree = getControl();

        // WORKAROUND
        int oldCnt = -1;
        if (widget == tree)
            oldCnt = getItemCount(tree);

        Item[] items = getChildren(widget);

        // save the expanded elements
        CustomHashtable expanded = newHashtable(CustomHashtable.DEFAULT_CAPACITY); // assume num expanded is small
        for (int i = 0; i < items.length; ++i) {
            if (getExpanded(items[i])) {
                Object element = items[i].getData();
                if (element != null) {
                    expanded.put(element, element);
                }
            }
        }

        int min = Math.min(elementChildren.length, items.length);

        // Note: In the code below, doing disassociate calls before associate
        // calls is important,
        // since a later disassociate can undo an earlier associate,
        // if items are changing position.

        // dispose of all items beyond the end of the current elements
        for (int i = items.length; --i >= min;) {
            if (items[i].getData() != null) {
                disassociate(items[i]);
            }
            items[i].dispose();
        }

        // compare first min items, and update item if necessary
        // need to do it in two passes:
        // 1: disassociate old items
        // 2: associate new items
        // because otherwise a later disassociate can remove a mapping made for
        // a previous associate,
        // making the map inconsistent
        for (int i = 0; i < min; ++i) {
            Item item = items[i];
            Object oldElement = item.getData();
            if (oldElement != null) {
                Object newElement = elementChildren[i];
                if (newElement != oldElement) {
                    if (equals(newElement, oldElement)) {
                        // update the data to be the new element, since
                        // although the elements
                        // may be equal, they may still have different labels
                        // or children
                        item.setData(newElement);
                        mapElement(newElement, item);
                    } else {
                        disassociate(item);
                        //Clear the text and image to force a label update
                        item.setImage(null);
                        item.setText("");//$NON-NLS-1$

                    }
                }
            }
        }
        for (int i = 0; i < min; ++i) {
            Item item = items[i];
            Object newElement = elementChildren[i];
            if (item.getData() == null) {
                // old and new elements are not equal
                associate(newElement, item);
                updatePlus(item, newElement);
                updateItem(item, newElement);
                // Restore expanded state for items that changed position.
                // Make sure setExpanded is called after updatePlus, since
                // setExpanded(false) fails if item has no children.
                // Need to call setExpanded for both expanded and unexpanded
                // cases
                // since the expanded state can change either way.
                setExpanded(item, expanded.containsKey(newElement));
            } else {
                // old and new elements are equal
                updatePlus(item, newElement);
                if (updateLabels) {
                    updateItem(item, newElement);
                }
            }
        }

        // add any remaining elements
        if (min < elementChildren.length) {
            for (int i = min; i < elementChildren.length; ++i) {
                createTreeItem(widget, elementChildren[i], i);
            }

            // Need to restore expanded state in a separate pass
            // because createTreeItem does not return the new item.
            // Avoid doing this unless needed.
            if (expanded.size() > 0) {
                // get the items again, to include the new items
                items = getChildren(widget);
                for (int i = min; i < elementChildren.length; ++i) {
                    // Restore expanded state for items that changed position.
                    // Make sure setExpanded is called after updatePlus (called
                    // in createTreeItem), since
                    // setExpanded(false) fails if item has no children.
                    // Only need to call setExpanded if element was expanded
                    // since new items are initially unexpanded.
                    if (expanded.containsKey(elementChildren[i])) {
                        setExpanded(items[i], true);
                    }
                }
            }
        }

        // WORKAROUND
        if (widget == tree && oldCnt == 0 && getItemCount(tree) != 0) {
            //System.out.println("WORKAROUND setRedraw");
            tree.setRedraw(false);
            tree.setRedraw(true);
        }
    }

    /**
     * Updates the "+"/"-" icon of the tree node from the given element. It
     * calls <code>isExpandable</code> to determine whether an element is
     * expandable.
     * 
     * @param item
     *           the item
     * @param element
     *           the element
     */
    protected void updatePlus(Item item, Object element) {
        boolean hasPlus = getItemCount(item) > 0;
        boolean needsPlus = isExpandable(element);
        boolean removeAll = false;
        boolean addDummy = false;
        Object data = item.getData();
        if (data != null && equals(element, data)) {
            // item shows same element
            if (hasPlus != needsPlus) {
                if (needsPlus)
                    addDummy = true;
                else
                    removeAll = true;
            }
        } else {
            // item shows different element
            removeAll = true;
            addDummy = needsPlus;

            // we cannot maintain expand state so collapse it
            setExpanded(item, false);
        }
        if (removeAll) {
            // remove all children
            Item[] items = getItems(item);
            for (int i = 0; i < items.length; i++) {
                if (items[i].getData() != null)
                    disassociate(items[i]);
                items[i].dispose();
            }
        }
        if (addDummy)
            newItem(item, SWT.NULL, -1); // append a dummy
    }

    /**
     * Gets the expanded elements that are visible to the user. An expanded
     * element is only visible if the parent is expanded.
     * 
     * @return the visible expanded elements
     * @since 2.0
     */
    public Object[] getVisibleExpandedElements() {
        ArrayList v = new ArrayList();
        internalCollectVisibleExpanded(v, getControl());
        return v.toArray();
    }

    private void internalCollectVisibleExpanded(ArrayList result, Widget widget) {
        Item[] items = getChildren(widget);
        for (int i = 0; i < items.length; i++) {
            Item item = items[i];
            if (getExpanded(item)) {
                Object data = item.getData();
                if (data != null)
                    result.add(data);
                //Only recurse if it is expanded - if
                //not then the children aren't visible
                internalCollectVisibleExpanded(result, item);
            }
        }
    }
}
