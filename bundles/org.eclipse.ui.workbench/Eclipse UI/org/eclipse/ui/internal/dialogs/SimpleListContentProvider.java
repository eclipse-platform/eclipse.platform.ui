package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
/**
 * The SimpleListContentProvider is a class designed to return a static list of items
 * when queried for use in simple list dialogs.
 */
public class SimpleListContentProvider implements IStructuredContentProvider{

	//The elements to display
	private Object[] elements;

/**
 * SimpleListContentProvider constructor comment.
 */
public SimpleListContentProvider() {
	super();
}
/**
 * Do nothing when disposing,
 */
public void dispose() {}
/**
 * Returns the elements to display in the viewer. The inputElement is ignored for this
 * provider.
 */
public Object[] getElements(Object inputElement) {
	return this.elements;
}
/**
 * Required method from IStructuredContentProvider. The input is assumed to not change 
 * for the SimpleListContentViewer so do nothing here.
 */
public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
/**
 * Set the elements to display.
 * @param items Object[]
 */
public void setElements(Object[] items) {

	this.elements = items;
}
}
