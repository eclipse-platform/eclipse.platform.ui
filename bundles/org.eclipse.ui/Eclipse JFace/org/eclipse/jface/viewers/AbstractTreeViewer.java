package org.eclipse.jface.viewers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import java.util.*;
import java.util.List; // Otherwise ambiguous

/**
 * Abstract base implementation for tree-structure-oriented viewers
 * (trees and table trees).
 * <p>
 * Nodes in the tree can be in either an expanded or a collapsed state,
 * depending on whether the children on a node are visible. This class
 * introduces public methods for controlling the expanding and collapsing
 * of nodes.
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
	 * Constant indicating that all levels of the tree should be expanded or collapsed.
	 *
	 * @see #expandToLevel
	 * @see #collapseToLevel
	 */
	public static final int ALL_LEVELS= -1;

	/**
	 * List of registered tree listeners (element type: <code>TreeListener</code>).
	 */
	private ListenerList treeListeners = new ListenerList(1);
		
	/**
	 * The level to which the tree is automatically expanded each time
	 * the viewer's input is changed (that is, by <code>setInput</code>).
	 * A value of 0 means that auto-expand is off.
	 *
	 * @see setAutoExpandLevel
	 */
	private int expandToLevel = 0;
/**
 * Creates an abstract tree viewer. The viewer has no input, no content provider, a
 * default label provider, no sorter, no filters, and has auto-expand turned off.
 */
protected AbstractTreeViewer() {
}
/**
 * Adds the given child elements to this viewer as children of the given parent element.
 * If this viewer does not have a sorter, the elements are added at the end of the 
 * parent's list of children in the order given; otherwise, the elements are inserted
 * at the appropriate positions.
 * <p>
 * This method should be called (by the content provider) when elements 
 * have been added to the model, in order to cause the viewer to accurately
 * reflect the model. This method only affects the viewer, not the model.
 * </p>
 *
 * @param parentElement the parent element
 * @param childElements the child elements to add
 */
public void add(Object parentElement, Object[] childElements) {
	Assert.isNotNull(parentElement);
	Assert.isNotNull(childElements);
	Widget widget = findItem(parentElement);
	// If parent hasn't been realized yet, just ignore the add.
	if (widget == null)
		return;

	Control tree = getControl();
	
	// optimization!
	// if the widget is not expanded we just invalidate the subtree
	if (widget instanceof Item) {
		Item ti= (Item) widget;
		if (!getExpanded(ti)) {
			boolean needDummy = isExpandable(parentElement);
			boolean haveDummy = false;
			// remove all children
			Item[] items= getItems(ti);
			for (int i = 0; i < items.length; i++) {
				if (items[i].getData() != null) {
					disassociate(items[i]);
					items[i].dispose();
				}
				else {
					if (needDummy && !haveDummy) {
						haveDummy = true;
					}
					else {
						items[i].dispose();
					}
				}
			}
			// append a dummy if necessary
			if (needDummy && !haveDummy) {
				newItem(ti, SWT.NULL, -1);
			} else {
				// XXX: Workaround (PR missing)
				tree.redraw();
			}	
			
			return;
		}
	}

	if (childElements.length > 0) {	
		List children = Arrays.asList(getSortedChildren(parentElement));
		for (int cc = 0; cc < childElements.length; cc++) {
			
			int ix = children.indexOf(childElements[cc]);
			if (ix < 0)	// child not found: ignore
				continue;

			Item[] ch = getChildren(widget);
			if (ch.length + 1 == children.size()) {
				createTreeItem(widget, childElements[cc], ix);	// insert child at position
				if (ch.length == 0) {
					//System.out.println("WORKAROUND setRedraw");
					tree.setRedraw(false);	// WORKAROUND
					tree.setRedraw(true);	// WORKAROUND
				}
				continue;
			}
			
			// couldn't handle this child:
			// skip other children and do general case
			// call refresh rather than internalRefresh to preserve selection
			refresh(parentElement);
			return;
		}
	}
}
/**
 * Adds the given child element to this viewer as a child of the given parent element.
 * If this viewer does not have a sorter, the element is added at the end of the 
 * parent's list of children; otherwise, the element is inserted at the appropriate position.
 * <p>
 * This method should be called (by the content provider) when a single element 
 * has been added to the model, in order to cause the viewer to accurately
 * reflect the model. This method only affects the viewer, not the model.
 * Note that there is another method for efficiently processing the simultaneous
 * addition of multiple elements.
 * </p>
 *
 * @param parentElement the parent element
 * @param childElement the child element
 */
public void add(Object parentElement, Object childElement) {
	add(parentElement, new Object[] { childElement });
}
/**
 * Adds the given SWT selection listener to the given SWT control.
 *
 * @param control the SWT control
 * @param listener the SWT selection listener
 */
protected abstract void addSelectionListener(Control control, SelectionListener listener);
/**
 * Adds a listener for expand and collapse events in this viewer.
 * Has no effect if an identical listener is already registered.
 *
 * @param listener a tree viewer listener
 */
public void addTreeListener(ITreeViewerListener listener) {
	treeListeners.add(listener);
}
/**
 * Adds the given SWT tree listener to the given SWT control.
 *
 * @param control the SWT control
 * @param listener the SWT tree listener
 */
protected abstract void addTreeListener(Control control, TreeListener listener);
/**
 * Collapses all nodes of the viewer's tree, starting with the root.
 * This method is equivalent to <code>collapseToLevel(ALL_LEVELS)</code>.
 */
public void collapseAll() {
	collapseToLevel(getRoot(), ALL_LEVELS);
}
/**
 * Collapses the subtree rooted at the given element to the given level.
 *
 * @param element the element
 * @param level non-negative level, or <code>ALL_LEVELS</code> to collapse
 *  all levels of the tree
 */
public void collapseToLevel(Object element, int level) {
	Widget w = findItem(element);
	if (w != null)
		internalCollapseToLevel(w, level);
}
/**
 * Creates all children for the given widget.
 * <p>
 * The default implementation of this framework method assumes 
 * that <code>widget.getData()</code> returns the element corresponding
 * to the node. Note: the node is not visually expanded! You may have to 
 * call <code>parent.setExpanded(true)</code>.
 * </p>
 *
 * @param widget the widget
 */
protected void createChildren(Widget widget) {
	Item[] tis = getChildren(widget);
	if (tis != null && tis.length > 0) {
		Object data = tis[0].getData();
		if (data != null)
			return; // children already there!

		// dummies are identified by not having client data
		// assert that there is only a single child!
		//Assert.isTrue(tis.length == 1);
		//
		//tis[0].dispose(); // remove dummy

		// fix for PR 1FW89L7:
		// don't complain and remove all "dummies" ...
		for (int i = 0; i < tis.length; i++)
			tis[i].dispose();
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
/**
 * Creates a single item for the given parent and synchronizes it with
 * the given element.
 *
 * @param parent the parent widget
 * @param element the element
 * @param index if non-negative, indicates the position to insert the item 
 *    into its parent
 */
protected void createTreeItem(Widget parent, Object element, int index) {
	Item item = newItem(parent, SWT.NULL, index);
	updateItem(item, element);
	updatePlus(item, element);
}
/**
 * The <code>AbstractTreeViewer</code> implementation of this method
 * also recurses over children of the corresponding element.
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
 * @param item the widget
 */
private void disassociateChildren(Item item) {
	Item[] items = getChildren(item);
	for (int i = 0; i < items.length; i++) {
		if (items[i].getData() != null)
			disassociate(items[i]);
	}
}
/* (non-Javadoc)
 * Method declared on StructuredViewer.
 */
protected Widget doFindInputItem(Object element) {
	// compare with root
	Object root= getRoot();
	if (root == null)
		return null;
		
	if (root.equals(element))
		return getControl();
	return null;
}
/* (non-Javadoc)
 * Method declared on StructuredViewer.
 */
protected Widget doFindItem(Object element) {
	// compare with root
	Object root= getRoot();
	if (root == null)
		return null;
						
	Item[] items= getChildren(getControl());
	if (items != null) {
		for (int i= 0; i < items.length; i++) {
			Widget o= internalFindItem(items[i], element);
			if (o != null) 
				return o;
		}
	}
	return null;
}
/**
 * Copies the attributes of the given element into the given SWT item.
 *
 * @param item the SWT item
 * @param element the element
 */
protected abstract void doUpdateItem(Item item, Object element);
/* (non-Javadoc)
 * Method declared on StructuredViewer.
 */
protected void doUpdateItem(Widget widget, Object element, boolean fullMap) {
	if (widget instanceof Item) {
		Item item = (Item) widget;

		// ensure that backpointer is correct
		if (fullMap) {

			Object data = widget.getData();
			if (data != null && data != element && data.equals(element)) {
				// workaround for PR 1FV62BT
				// assumption: elements are equal but not identical
				// -> remove from map but don't touch children
				unmapElement(data, item);
				item.setData(element);
				mapElement(element, item);
			} else {
				// recursively disassociate all
				associate(element, item);
			}

		} else {
			item.setData(element);
			mapElement(element, item);
		}

		// update icon and label
		doUpdateItem(item,element);
	}
}
/**
 * Expands all nodes of the viewer's tree, starting with the root.
 * This method is equivalent to <code>expandToLevel(ALL_LEVELS)</code>.
 */
public void expandAll() {
	expandToLevel(ALL_LEVELS);
}
/**
 * Expands the root of the viewer's tree to the given level.
 *
 * @param level non-negative level, or <code>ALL_LEVELS</code> to expand
 *  all levels of the tree
 */
public void expandToLevel(int level) {
	expandToLevel(getRoot(), level);
}
/**
 * Expands all ancestors of the given element so that the given element
 * becomes visible in this viewer's tree control, and then expands the
 * subtree rooted at the given element to the given level.
 *
 * @param element the element
 * @param level non-negative level, or <code>ALL_LEVELS</code> to expand
 *  all levels of the tree
 */
public void expandToLevel(Object element, int level) {
	Widget w = internalExpand(element, true);
	if (w != null)
		internalExpandToLevel(w, level);
}
/**
 * Fires a tree collapsed event.
 * Only listeners registered at the time this method is called are notified.
 *
 * @param event the tree expansion event
 *
 * @see ITreeViewerListener#treeCollapsed
 */
protected void fireTreeCollapsed(TreeExpansionEvent event) {
	Object[] listeners = treeListeners.getListeners();
	for (int i = 0; i < listeners.length; ++i) {
		((ITreeViewerListener) listeners[i]).treeCollapsed(event);
	}
}
/**
 * Fires a tree expanded event.
 * Only listeners registered at the time this method is called are notified.
 *
 * @param event the tree expansion event
 *
 * @see ITreeViewerListener#treeExpanded
 */
protected void fireTreeExpanded(TreeExpansionEvent event) {
	Object[] listeners = treeListeners.getListeners();
	for (int i = 0; i < listeners.length; ++i) {
		((ITreeViewerListener) listeners[i]).treeExpanded(event);
	}
}
/**
 * Returns the auto-expand level.
 *
 * @return non-negative level, or <code>EXPAND_ALL</code> if
 *  all levels of the tree are expanded automatically
 * @see #setAutoExpandLevel
 */
public int getAutoExpandLevel() {
	return expandToLevel;
}
/**
 * Returns the SWT child items for the given SWT widget.
 *
 * @param widget the widget
 * @return the child items
 */
protected abstract Item[] getChildren(Widget widget);
/**
 * Returns whether the given SWT item is expanded or collapsed.
 *
 * @param item the item
 * @return <code>true</code> if the item is considered expanded
 * and <code>false</code> if collapsed
 */
protected abstract boolean getExpanded(Item item);
/**
 * Returns a list of elements corresponding to expanded nodes in this
 * viewer's tree, including currently hidden ones that are marked as
 * expanded but are under a collapsed ancestor.
 * <p>
 * This method is typically used when preserving the interesting
 * state of a viewer; <code>setExpandedElements</code> is used during the restore.
 * </p>
 *
 * @return the array of expanded elements
 *
 * @see #setExpandedElements
 */
public Object[] getExpandedElements() {
	ArrayList v= new ArrayList();
	internalCollectExpanded(v, getControl());
	return v.toArray();
}
/**
 * Returns whether the node corresponding to the given element is expanded or collapsed.
 *
 * @param element the element
 * @return <code>true</code> if the node is expanded, and <code>false</code> if collapsed
 */
public boolean getExpandedState(Object element) {
	Widget item = findItem(element);
	if (item instanceof Item)
		return getExpanded((Item) item);
	return false;
}
/**
 * Returns the number of child items of the given SWT control.
 *
 * @param control the control
 * @return the number of children
 */
protected abstract int getItemCount(Control control);
/**
 * Returns the number of child items of the given SWT item.
 *
 * @param item the item
 * @return the number of children
 */
protected abstract int getItemCount(Item item);
/**
 * Returns the child items of the given SWT item.
 *
 * @param item the item
 * @return the child items
 */
protected abstract Item[] getItems(Item item);
/**
 * Returns the item after the given item in the tree, or
 * <code>null</code> if there is no next item.
 *
 * @param item the item
 * @param includeChildren <code>true</code> if the children are 
 *  considered in determining which item is next, and <code>false</code>
 *  if subtrees are ignored
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
	if (siblings != null && siblings.length <= 1) {
		return getNextItem(parent, false);
	}
	for (int i = 0; i < siblings.length; i++) {
		if (siblings[i] == item && i < (siblings.length - 1)) {
			return siblings[i+1];
		}
	}
	return getNextItem(parent, false);
}
/**
 * Returns the parent item of the given item in the tree, or
 * <code>null</code> if there is parent item.
 *
 * @param item the item
 * @return the parent item, or <code>null</code> if none
 */
protected abstract Item getParentItem(Item item);
/**
 * Returns the item before the given item in the tree, or
 * <code>null</code> if there is no previous item.
 *
 * @param item the item
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
/* (non-Javadoc)
 * Method declared on StructuredViewer.
 */
protected Object[] getRawChildren(Object parent) {
	if (parent != null) {
		if (parent.equals(getRoot()))
			return super.getRawChildren(parent);
		Object[] result = ((ITreeContentProvider) getContentProvider()).getChildren(parent);
		if (result != null)
			return result;
	}
	return new Object[0];
}
/**
 * Returns all selected items for the given SWT control.
 *
 * @param control the control
 * @return the list of selected items
 */
protected abstract Item[] getSelection(Control control);
/* (non-Javadoc)
 * Method declared on StructuredViewer.
 */
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
 * @param event the SWT tree event
 */
protected void handleTreeCollapse(TreeEvent event) {
	if (event.item.getData() != null) {
		fireTreeCollapsed(new TreeExpansionEvent(this, event.item.getData()));
	}
}
/**
 * Handles a tree expand event from the SWT widget.
 *
 * @param event the SWT tree event
 */
protected void handleTreeExpand(TreeEvent event) {
	createChildren(event.item);
	if (event.item.getData() != null) {
		fireTreeExpanded(new TreeExpansionEvent(this, event.item.getData()));
	}
}
/* (non-Javadoc)
 * Method declared on Viewer.
 */
protected void hookControl(Control control) {
	super.hookControl(control);
	addSelectionListener(control,new SelectionListener() {
		public void widgetSelected(SelectionEvent e) {
			handleSelect(e);
		}
		public void widgetDefaultSelected(SelectionEvent e) {
			handleDoubleSelect(e);
		}
	});
	addTreeListener(control,new TreeListener() {
		public void treeExpanded(TreeEvent event) {
			handleTreeExpand(event);
		}
		public void treeCollapsed(TreeEvent event) {
			handleTreeCollapse(event);
		}
	});
}
/* (non-Javadoc)
 * Method declared on StructuredViewer.
 * Builds the initial tree and handles the automatic expand feature.
 */
protected void inputChanged(Object input, Object oldInput) {
	preservingSelection(new Runnable() {
		public void run() {
			Control tree = getControl();
		    boolean useRedraw = true; // (size > REDRAW_THRESHOLD) || (table.getItemCount() > REDRAW_THRESHOLD);
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
 * Note that the default implementation of this method does not call
 * <code>setRedraw</code>.
 *
 * @param widget the widget
 * @param level non-negative level, or <code>ALL_LEVELS</code> to collapse
 *  all levels of the tree
 */
protected void internalCollapseToLevel(Widget widget, int level) {
	if (level == ALL_LEVELS || level > 0) {

		if (widget instanceof Item)
			setExpanded((Item) widget, false);

		if (level == ALL_LEVELS || level > 1) {
			Item[] children = getChildren(widget);
			if (children != null) {
				int nextLevel = (level == ALL_LEVELS ? ALL_LEVELS : level - 1);
				for (int i = 0; i < children.length; i++)
					internalCollapseToLevel(children[i], nextLevel);
			}
		}
	}
}
/**
 * Recursively collects all expanded elements from the given widget.
 *
 * @param result a list (element type: <code>Object</code>) into which
 *    to collect the elements
 * @param widget the widget
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
 * Tries to create a path of tree items for the given element.
 * This method recursively walks up towards the root of the tree
 * and assumes that <code>getParent</code> returns the correct
 * parent of an element.
 *
 * @param element the element
 * @param expand <code>true</code> if all nodes on the path should be expanded, 
 *    and <code>false</code> otherwise
 */
protected Widget internalExpand(Object element, boolean expand) {

	if (element == null)
		return null;

	Widget w= findItem(element);
	if (w == null) {	
		if (element.equals(getRoot()))	// stop at root
			return null;
		// my parent has to create me
		Object parent= ((ITreeContentProvider) getContentProvider()).getParent(element);
		if (parent != null) {
			Widget pw= internalExpand(parent, expand);
			if (pw != null) {
				// let my parent create me
				createChildren(pw);
				// expand parent and find me
				if (pw instanceof Item) {
					Item item= (Item)pw;
					if (expand)
						setExpanded(item, true);
					w= internalFindChild(item, element);
				}
			}
		}
	}
	return w;
}
/**
 * Recursively expands the subtree rooted at the given widget to the
 * given level.
 * <p>
 * </p>
 * Note that the default implementation of this method does not call
 * <code>setRedraw</code>.
 *
 * @param widget the widget
 * @param level non-negative level, or <code>ALL_LEVELS</code> to collapse
 *  all levels of the tree
 */
protected void internalExpandToLevel(Widget widget, int level) {
	if (level == ALL_LEVELS || level > 0) {
		createChildren(widget);
		if (widget instanceof Item)
			setExpanded((Item) widget, true);
		if (level == ALL_LEVELS || level > 1) {
			Item[] children = getChildren(widget);
			if (children != null) {
				int newLevel = (level == ALL_LEVELS ? ALL_LEVELS : level - 1);
				for (int i = 0; i < children.length; i++)
					internalExpandToLevel(children[i], newLevel);
			}
		}
	}
}
/**
 * Non-recursively tries to find the given element as a child of the given parent item.
 *
 * @param parent the parent item
 * @param element the element
 */
private Widget internalFindChild(Item parent, Object element) {
	Item[] items = getChildren(parent);
	for (int i = 0; i < items.length; i++) {
		Item item = items[i];
		Object data = item.getData();
		if (data != null && data.equals(element))
			return item;
	}
	return null;
}
/**
 * Recursively tries to find the given element.
 *
 * @param parent the parent item
 * @param element the element
 */
private Widget internalFindItem(Item parent, Object element) {

	// compare with node
	Object data= parent.getData();
	if (data != null) {
		if (data.equals(element))
			return parent;
	}
	// recurse over children
	Item[] items= getChildren(parent);
	for (int i= 0; i < items.length; i++) {
		Item item= items[i];
		Widget o= internalFindItem(item, element);
		if (o != null)
			return o;
	}
	return null;
}
/* (non-Javadoc)
 * Method declared on StructuredViewer.
 */
public void internalRefresh(Object element) {
	// If element is null, do a full refresh.
	if (element == null) {
		internalRefresh(getControl(), getRoot(), true);
		return;
	}
	Widget item = findItem(element);
	if (item != null) {
		// pick up structure changes too
		internalRefresh(item, element, true);
	}
}
/**
 * Refreshes the tree starting at the given widget.
 *
 * @param widget the widget
 * @param element the element
 * @param doStruct <code>true</code> if structural changes are to be picked up,
 *   and <code>false</code> if only label provider changes are of interest
 */
private void internalRefresh(Widget widget, Object element, boolean doStruct) {
	
	if (widget instanceof Item) {
		if (doStruct) {
			updatePlus((Item)widget, element);
		}	
		updateItem((Item)widget, element);
	}

	if (doStruct) {
		// pass null for children, to allow updateChildren to get them only if needed
		updateChildren(widget, element, null);
	}
	// recurse
	Item[] children= getChildren(widget);
	if (children != null) {
		for (int i= 0; i < children.length; i++) {
			Widget item= children[i];
			Object data= item.getData();
			if (data != null)
				internalRefresh(item, data, doStruct);
		}
	}
}
/**
 * Removes the given elements from this viewer.
 *
 * @param elements the elements to remove
 */
private void internalRemove(Object[] elements) {
	Object input = getInput();
	Set parentItems = new HashSet(5);
	for (int i = 0; i < elements.length; ++i) {
		if (elements[i].equals(input)) {
			setInput(null);
			return;
		}
		Widget childItem = findItem(elements[i]);
		if (childItem instanceof Item) {
			Item parentItem = getParentItem((Item) childItem);
			if (parentItem != null) {
				parentItems.add(parentItem);
			}
			disassociate((Item) childItem);
			childItem.dispose();
		}
	}
	Control tree = getControl();
	for (Iterator i = parentItems.iterator(); i.hasNext();) {
		Item parentItem = (Item) i.next();
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
 * Sets the expanded state of all items to correspond to the given set of expanded elements.
 *
 * @param expandedElements the set (element type: <code>Object</code>) of elements which are expanded
 * @param widget the widget
 */
private void internalSetExpanded(Set expandedElements, Widget widget) {
	Item[] items = getChildren(widget);
	for (int i = 0; i < items.length; i++) {
		Item item = items[i];
		Object data = item.getData();
		if (data != null) {
			boolean expanded = expandedElements.contains(data);
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
 * Return whether the tree node representing the given element
 * can be expanded.
 * <p>
 * The default implementation of this framework method calls 
 * <code>hasChildren</code> on this viewer's content provider.
 * It may be overridden if necessary.
 * </p>
 *
 * @param element the element
 * @return <code>true</code> if the tree node representing
 * the given element can be expanded, or <code>false</code> if not
 */
public boolean isExpandable(Object element) {
	return ((ITreeContentProvider) getContentProvider()).hasChildren(element);
}
/* (non-Javadoc)
 * Method declared on Viewer.
 */
protected void labelProviderChanged() {
	// we have to walk the (visible) tree and update every item
	Control tree= getControl();
	tree.setRedraw(false);
	// don't pick up structure changes
	internalRefresh(tree, getRoot(), false);
	tree.setRedraw(true);
}
/**
 * Creates a new item.
 *
 * @param parent the parent widget
 * @param style SWT style bits
 * @param index if non-negative, indicates the position to insert the item 
 *    into its parent
 * @return the newly-created item 
 */
protected abstract Item newItem(Widget parent, int style, int index);
/**
 * Removes the given elements from this viewer.
 * The selection is updated if required.
 * <p>
 * This method should be called (by the content provider) when elements 
 * have been removed from the model, in order to cause the viewer to accurately
 * reflect the model. This method only affects the viewer, not the model.
 * </p>
 *
 * @param elements the elements to remove
 */
public void remove(final Object[] elements) {
	preservingSelection(new Runnable() {
		public void run() {
			internalRemove(elements);
		}
	});
}
/**
 * Removes the given element from the viewer.
 * The selection is updated if necessary.
 * <p>
 * This method should be called (by the content provider) when a single element 
 * has been removed from the model, in order to cause the viewer to accurately
 * reflect the model. This method only affects the viewer, not the model.
 * Note that there is another method for efficiently processing the simultaneous
 * removal of multiple elements.
 * </p>
 *
 * @param element the element
 */
public void remove(Object element) {
	remove(new Object[] { element });
}
/**
 * Removes all items from the given control.
 *
 * @param control the control
 */
protected abstract void removeAll(Control control);
/**
 * Removes a listener for expand and collapse events in this viewer.
 * Has no affect if an identical listener is not registered.
 *
 * @param listener a tree viewer listener
 */
public void removeTreeListener(ITreeViewerListener listener) {
	treeListeners.remove(listener);
}
/*
 * Non-Javadoc.
 * Method defined on StructuredViewer.
 */
public void reveal(Object element) {
	Widget w = internalExpand(element, true);
	if (w instanceof Item)
		showItem((Item) w);
}
/**
 * Returns the rightmost visible descendent of the given item.
 * Returns the item itself if it has no children.
 *
 * @param item the item to compute the descendent of
 * @return the rightmost visible descendent or the item iself
 *  if it has no children
 */
private Item rightMostVisibleDescendent(Item item) {
	Item[] children = getItems(item);
	if (getExpanded(item) && children != null && children.length > 0) {
		return rightMostVisibleDescendent(children[children.length-1]);
	} else {
		return item;
	}
}
/* (non-Javadoc)
 * Method declared on Viewer.
 */
public Item scrollDown(int x, int y) {
	Item current = (Item)getItem(x, y);
	if (current != null) {
		Item next = getNextItem(current, true);
		showItem(next == null ? current : next);
		return next;
	}
	return null;
}
/* (non-Javadoc)
 * Method declared on Viewer.
 */
public Item scrollUp(int x, int y) {
	Item current = (Item)getItem(x, y);
	if (current != null) {
		Item previous = getPreviousItem(current);
		showItem(previous == null ? current : previous);
		return previous;
	}
	return null;
}
/**
 * Sets the auto-expand level.
 * The value 0 means that the root's children are not visible; 
 * 1 means that the root is expanded so that its children are visible,
 * but the root's grandchildren are not; and so on.
 * The value <code>EXPAND_ALL</code> means that all subtrees should be
 * expanded.
 *
 * @param level non-negative level, or <code>EXPAND_ALL</code> to expand
 *  all levels of the tree
 */
public void setAutoExpandLevel(int level) {
	expandToLevel = level;
}
/**
 * The <code>AbstractTreeViewer</code> implementation of this method
 * checks to ensure that the content provider is an <code>ITreeContentProvider</code>.
 */
public void setContentProvider(IContentProvider provider) {
	Assert.isTrue(provider instanceof ITreeContentProvider);
	super.setContentProvider(provider);
}
/**
 * Sets the expand state of the given item.
 *
 * @param item the item
 * @param expand the expand state of the item
 */
protected abstract void setExpanded(Item item, boolean expand);
/**
 * Sets which nodes are expanded in this viewer's tree.
 * The given list contains the elements that are to be expanded;
 * all other nodes are to be collapsed.
 * <p>
 * This method is typically used when restoring the interesting
 * state of a viewer captured by an earlier call to <code>getExpandedElements</code>.
 * </p>
 *
 * @param elements the array of expanded elements
 * @see #getExpandedElements
 */
public void setExpandedElements(Object[] elements) {
	Set expandedElements = new HashSet(elements.length*2+1);
	for (int i = 0; i < elements.length; ++i) {
		// Ensure item exists for element
		internalExpand(elements[i], false);
		expandedElements.add(elements[i]);
	}
	internalSetExpanded(expandedElements, getControl());
}
/**
 * Sets whether the node corresponding to the given element is expanded or collapsed.
 *
 * @param element the element
 * @param expanded <code>true</code> if the node is expanded, and <code>false</code> if collapsed
 */
public void setExpandedState(Object element, boolean expanded) {
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
 * @param items list of items (element type: <code>org.eclipse.swt.widgets.Item</code>)
 */
protected abstract void setSelection(List items);
/* (non-Javadoc)
 * Method declared on StructuredViewer.
 */
protected void setSelectionToWidget(List v, boolean reveal) {
	if (v == null) {
		setSelection(new ArrayList(0));
		return;
	}
	int size = v.size();
	List newSelection = new ArrayList(size);
	for (int i = 0; i < size; ++i) {
		// Use internalExpand since item may not yet be created.  See 1G6B1AR.
		Widget w = internalExpand(v.get(i), true);
		if (w instanceof Item) {
			newSelection.add(w);
		}
	}
	setSelection(newSelection);
	if (reveal && newSelection.size() > 0) {
		showItem((Item) newSelection.get(0));
	}
}
/**
 * Shows the given item.
 *
 * @param item the item
 */
protected abstract void showItem(Item item);
/**
 * Updates the tree items to correspond to the child elements of the given parent element.
 * If null is passed for the children, this method obtains them (only if needed).
 *
 * @param widget the widget
 * @param parent the parent element
 * @param elementChildren the child elements, or null
 */
protected void updateChildren(Widget widget, Object parent, Object[] elementChildren) {
	// optimization! prune collapsed subtrees
	if (widget instanceof Item) {
		Item ti= (Item) widget;
		if (!getExpanded(ti)) {
			// need a dummy node if element is expandable;
			// but try to avoid recreating the dummy node
			boolean needDummy = isExpandable(parent);
			boolean haveDummy = false;
			// remove all children
			Item[] items= getItems(ti);
			for (int i= 0; i < items.length; i++) {
				if (items[i].getData() != null) {
					disassociate(items[i]);
					items[i].dispose();
				}
				else {
					if (needDummy && !haveDummy) {
						haveDummy = true;
					}
					else {
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

	// If the children weren't passed in, get them now since they're needed below.
	if (elementChildren == null) {
		elementChildren = getSortedChildren(parent);
	}
	
	Control tree = getControl();
	
	// WORKAROUND
	int oldCnt= -1;
	if (widget == tree)
		oldCnt= getItemCount(tree);

	Item[] items = getChildren(widget);
	int min = Math.min(elementChildren.length, items.length);

	// Note: In the code below, doing disassociate calls before associate calls is important,
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
	// 2: associate and update new items
	// because otherwise a later disassociate can remove a mapping made for a previous associate,
	// making the map inconsistent
	for (int i = 0; i < min; ++i) {
		Item item = items[i];
		Object oldElement = item.getData();
		if (oldElement != null) {
			Object newElement = elementChildren[i];
			if (newElement != oldElement) {
				if (newElement.equals(oldElement)) {
					// update the data to be the new element, since although the elements 
					// may be equal, they may still have different labels or children
					item.setData(newElement);
					mapElement(newElement, item);
				} else {
					disassociate(item);
				}
			}
		}
	}
	for (int i = 0; i < min; ++i) {
		Item item = items[i];
		if (item.getData() == null) {
			Object newElement = elementChildren[i];
			associate(newElement, item);
			updatePlus(item, newElement);
			updateItem(item, newElement);
		}
	}
	
	// add any remaining elements
	for (int i = min; i < elementChildren.length; ++i) {
		createTreeItem(widget, elementChildren[i], i);
	}
	
	// WORKAROUND
	if (widget == tree && oldCnt == 0 && getItemCount(tree) != 0) {
		//System.out.println("WORKAROUND setRedraw");
		tree.setRedraw(false);
		tree.setRedraw(true);
	}
}
/**
 * Updates the "+"/"-" icon of the tree node from the given element.
 * It calls <code>isExpandable</code> to determine whether 
 * an element is expandable.
 *
 * @param item the item
 * @param element the element
 */
protected void updatePlus(Item item, Object element) {
	boolean hasPlus = getItemCount(item) > 0;
	boolean needsPlus = isExpandable(element);
	boolean removeAll = false;
	boolean addDummy = false;
	Object data = item.getData();
	if (data != null && element.equals(data)) {
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
 * Get the expanded elements that are visible
 * to the user as an expanded element is only
 * isible if the parent is expanded.
 * @return Collection of Object
 */
public Object[] getVisibleExpandedElements() {
	ArrayList v= new ArrayList();
	internalCollectVisibleExpanded(v, getControl());
	return v.toArray();
}
	
private void internalCollectVisibleExpanded(Collection result, Widget widget){
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
