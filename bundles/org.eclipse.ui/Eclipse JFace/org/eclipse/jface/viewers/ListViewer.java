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
 * A concrete viewer based on an SWT <code>List</code> control.
 * <p>
 * This class is not intended to be subclassed. It is designed to be
 * instantiated with a pre-existing SWT list control and configured
 * with a domain-specific content provider, label provider, element filter (optional),
 * and element sorter (optional).
 * </p>
 */
public class ListViewer extends StructuredViewer {

	/**
	 * This viewer's list control.
	 */
	private org.eclipse.swt.widgets.List list;

	/**
	 * A list of viewer elements (element type: <code>Object</code>).
	 */
	private java.util.List listMap = new ArrayList();
/**
 * Creates a list viewer on a newly-created list control under the given parent.
 * The list control is created using the SWT style bits <code>MULTI, H_SCROLL, V_SCROLL,</code> and <code>BORDER</code>.
 * The viewer has no input, no content provider, a default label provider, 
 * no sorter, and no filters.
 *
 * @param parent the parent control
 */
public ListViewer(Composite parent) {
	this(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
}
/**
 * Creates a list viewer on a newly-created list control under the given parent.
 * The list control is created using the given SWT style bits.
 * The viewer has no input, no content provider, a default label provider, 
 * no sorter, and no filters.
 *
 * @param parent the parent control
 * @param style the SWT style bits
 */
public ListViewer(Composite parent, int style) {
	this(new org.eclipse.swt.widgets.List(parent, style));
}
/**
 * Creates a list viewer on the given list control.
 * The viewer has no input, no content provider, a default label provider, 
 * no sorter, and no filters.
 *
 * @param list the list control
 */
public ListViewer(org.eclipse.swt.widgets.List list) {
	this.list = list;
	hookControl(list);
}
/**
 * Adds the given elements to this list viewer.
 * If this viewer does not have a sorter, the elements are added at the end
 * in the order given; otherwise the elements are inserted at appropriate positions.
 * <p>
 * This method should be called (by the content provider) when elements 
 * have been added to the model, in order to cause the viewer to accurately
 * reflect the model. This method only affects the viewer, not the model.
 * </p>
 *
 * @param elements the elements to add
 */
public void add(Object[] elements) {
	Object[] filtered = filter(elements);
	ILabelProvider labelProvider = (ILabelProvider) getLabelProvider();
	for (int i = 0; i < filtered.length; i++){
		Object element = filtered[i];
		int ix = indexForElement(element);
		list.add(labelProvider.getText(element), ix);
		listMap.add(ix, element);
		mapElement(element, list); // must map it, since findItem only looks in map, if enabled
	}
}
/**
 * Adds the given element to this list viewer.
 * If this viewer does not have a sorter, the element is added at the end;
 * otherwise the element is inserted at the appropriate position.
 * <p>
 * This method should be called (by the content provider) when a single element 
 * has been added to the model, in order to cause the viewer to accurately
 * reflect the model. This method only affects the viewer, not the model.
 * Note that there is another method for efficiently processing the simultaneous
 * addition of multiple elements.
 * </p>
 *
 * @param element the element
 */
public void add(Object element) {
	add(new Object[] { element });
}
/* (non-Javadoc)
 * Method declared on StructuredViewer.
 * Since SWT.List doesn't use items we always return the List itself.
 */
protected Widget doFindInputItem(Object element) {
	if (element != null && element.equals(getRoot()))
		return getList();
	return null;
}
/* (non-Javadoc)
 * Method declared on StructuredViewer.
 * Since SWT.List doesn't use items we always return the List itself.
 */
protected Widget doFindItem(Object element) {
	if (element != null) {
		if (listMap.contains(element))
			return getList();
	}
	return null;
}
/* (non-Javadoc)
 * Method declared on StructuredViewer.
 */
protected void doUpdateItem(Widget data, Object element, boolean fullMap) {
	if (element != null) {
		int ix = listMap.indexOf(element);
		if (ix >= 0) {
			ILabelProvider labelProvider = (ILabelProvider) getLabelProvider();
			list.setItem(ix, labelProvider.getText(element));
		}
	}
}
/* (non-Javadoc)
 * Method declared on Viewer.
 */
public Control getControl() {
	return list;
}
/**
 * Returns the element with the given index from this list viewer.
 * Returns <code>null</code> if the index is out of range.
 *
 * @param index the zero-based index
 * @return the element at the given index, or <code>null</code> if the
 *   index is out of range
 */
public Object getElementAt(int index) {
	if (index >= 0 && index < listMap.size())
		return listMap.get(index);
	return null;
}
/**
 * The list viewer implementation of this <code>Viewer</code> framework
 * method returns the label provider, which in the case of list
 * viewers will be an instance of <code>ILabelProvider</code>.
 */
public IBaseLabelProvider getLabelProvider() {
	return super.getLabelProvider();
}
/**
 * Returns this list viewer's list control.
 *
 * @return the list control
 */
public org.eclipse.swt.widgets.List getList() {
	return list;
}
/* (non-Javadoc)
 * Method declared on Viewer.
 */
/* (non-Javadoc)
 * Method declared on StructuredViewer.
 */
protected List getSelectionFromWidget() {
	int[] ixs = getList().getSelectionIndices();
	ArrayList list = new ArrayList(ixs.length);
	for (int i = 0; i < ixs.length; i++) {
		Object e = getElementAt(ixs[i]);
		if (e != null)
			list.add(e);
	}
	return list;
}
/*
 * Returns the index where the item should be inserted.
*/
protected int indexForElement(Object element) {
	ViewerSorter sorter = getSorter();
	if(sorter == null)
		return list.getItemCount();
	int count = list.getItemCount();
	int min = 0, max = count - 1;
	while (min <= max) {
		int mid = (min + max) / 2;
		Object data = listMap.get(mid);
		int compare = sorter.compare(this, data, element);
		if (compare == 0) {
			// find first item > element
			while (compare == 0) {
				++mid;
				if (mid >= count) {
					break;
				}
				data = listMap.get(mid);
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
/* (non-Javadoc)
 * Method declared on Viewer.
 */
protected void inputChanged(Object input, Object oldInput) {
	listMap.clear();
	Object[] children = getSortedChildren(getRoot());
	int size = children.length;
	org.eclipse.swt.widgets.List list = getList();
	list.removeAll();
	String[] labels = new String[size];
	for (int i = 0; i < size; i++) {
		Object el = children[i];
		labels[i] = ((ILabelProvider) getLabelProvider()).getText(el);
		listMap.add(el);
		mapElement(el, list); // must map it, since findItem only looks in map, if enabled
	}
	list.setItems(labels);
}
/* (non-Javadoc)
 * Method declared on StructuredViewer.
 */
protected void internalRefresh(Object element) {

	if (element == null || element.equals(getRoot())) {
		// the parent
		if (listMap != null)
			listMap.clear();
		unmapAllElements();
		List selection = getSelectionFromWidget();
		list.setRedraw(false);
		list.removeAll();
		Object[] children = getSortedChildren(getRoot());
		ILabelProvider labelProvider= (ILabelProvider) getLabelProvider();
		for (int i= 0; i < children.length; i++) {
			Object el = children[i];
			list.add(labelProvider.getText(el), i);
			listMap.add(el);
			mapElement(el, list); // must map it, since findItem only looks in map, if enabled
		}
		list.setRedraw(true);
		setSelectionToWidget(selection, false);
	} else {
		doUpdateItem(list, element, true);
	}
}
/**
 * Removes the given elements from this list viewer.
 *
 * @param elements the elements to remove
 */
private void internalRemove(final Object[] elements) {
	Object input = getInput();
	for (int i = 0; i < elements.length; ++i) {
	    if (elements[i].equals(input)) {
		    setInput(null);
		    return;
	    }
		int ix = listMap.indexOf(elements[i]);
		if (ix >= 0) {
			list.remove(ix);
			listMap.remove(ix);
			unmapElement(elements[i], list);
		}
	}
}
/**
 * Removes the given elements from this list viewer.
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
 * Removes the given element from this list viewer.
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
/*
 * Non-Javadoc.
 * Method defined on StructuredViewer.
 */
public void reveal(Object element) {
	int index = listMap.indexOf(element);
	if (index == -1)
		return;
	// algorithm patterned after List.showSelection()
	int count = list.getItemCount();
	if (count == 0)
		return;
	int height = list.getItemHeight();
	Rectangle rect = list.getClientArea();
	int topIndex = list.getTopIndex();
	int visibleCount = Math.max ((rect.x + rect.height) / height, 1);
	int bottomIndex = Math.min (topIndex + visibleCount + 1, count - 1);
	if ((topIndex <= index) && (index <= bottomIndex)) return;
	int newTop = Math.min (Math.max (index - (visibleCount / 2), 0), count - 1);
	list.setTopIndex(newTop);
}
/**
 * The list viewer implementation of this <code>Viewer</code> framework
 * method ensures that the given label provider is an instance
 * of <code>ILabelProvider</code>.
 */
public void setLabelProvider(IBaseLabelProvider labelProvider) {
	Assert.isTrue(labelProvider instanceof ILabelProvider);
	super.setLabelProvider(labelProvider);
}
/* (non-Javadoc)
 * Method declared on StructuredViewer.
 */
protected void setSelectionToWidget(List in, boolean reveal) {
	org.eclipse.swt.widgets.List list = getList();
	if (in == null || in.size() == 0) { // clear selection
		list.deselectAll();
	} else {
		int n = in.size();
		int[] ixs = new int[n];
		int count = 0;
		for (int i = 0; i < n; ++i) {
			Object el = in.get(i);
			int ix = listMap.indexOf(el);
			if (ix >= 0)
				ixs[count++] = ix;
		}
		if (count < n) {
			System.arraycopy(ixs, 0, ixs = new int[count], 0, count);
		}
		list.setSelection(ixs);
		if (reveal) {
			list.showSelection();
		}
	}
}
}
