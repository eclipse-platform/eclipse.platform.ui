package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.jface.viewers.*;
import java.util.*;

public class ViewContentProvider implements ITreeContentProvider {
/**
 * PerspContentProvider constructor comment.
 */
public ViewContentProvider() {
	super();
}
public void dispose() {
}
/**
 * Returns the child elements of the given parent element.
 */
public Object[] getChildren(Object element) {
	if (element instanceof IViewRegistry) {
		IViewRegistry reg = (IViewRegistry)element;
		return reg.getCategories();
	} else if (element instanceof ICategory) {
		ArrayList list = ((ICategory)element).getElements();
		if (list != null) {
			return list.toArray();
		} else {
			return new Object[0];
		}
	}
	return new Object[0];
}
/**
 * Return the children of an element.
 */
public Object[] getElements(Object element) {
	return getChildren(element);
}
/**
 * Returns the parent for the given element, or <code>null</code> 
 * indicating that the parent can't be computed. 
 */
public Object getParent(Object element) {
	return null;
}
/**
 * Returns whether the given element has children.
 */
public boolean hasChildren(java.lang.Object element) {
	if (element instanceof IViewRegistry)
		return true;
	else if (element instanceof ICategory)
		return true;
	return false;
}
public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
}
public boolean isDeleted(Object element) {
	return false;
}
}
