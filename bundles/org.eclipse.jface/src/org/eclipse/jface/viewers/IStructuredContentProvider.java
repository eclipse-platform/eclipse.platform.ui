package org.eclipse.jface.viewers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * An interface to content providers for structured viewers.
 *
 * @see StructuredViewer
 */
public interface IStructuredContentProvider extends IContentProvider {
/**
 * Returns the elements to display in the viewer 
 * when its input is set to the given element. 
 * These elements can be presented as rows in a table, items in a list, etc.
 * The result is not modified by the viewer.
 *
 * @param inputElement the input element
 * @return the array of elements to display in the viewer
 */
public Object[] getElements(Object inputElement);
}
