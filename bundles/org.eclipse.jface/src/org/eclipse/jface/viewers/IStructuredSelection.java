package org.eclipse.jface.viewers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.Iterator;
import java.util.List;

/**
 * A selection containing elements.
 */
public interface IStructuredSelection extends ISelection {
/**
 * Returns the first element in this selection, or <code>null</code>
 * if the selection is empty.
 *
 * @return an element, or <code>null</code> if none
 */
public Object getFirstElement();
/**
 * Returns an iterator over the elements of this selection.
 *
 * @return an iterator over the selected elements
 */
public Iterator iterator();
/**
 * Returns the number of elements selected in this selection.
 *
 * @return the number of elements selected
 */
public int size();
/**
 * Returns the elements in this selection as an array.
 *
 * @return the selected elements as an array
 */
public Object[] toArray();
/**
 * Returns the elements in this selection as a <code>List</code>.
 *
 * @return the selected elements as a list
 */
public List toList();
}
