package org.eclipse.ui.internal.dialogs;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/** 
 * Provides elements from a List.
 */
public class ListContentProvider implements IStructuredContentProvider {
	List contents;

	public ListContentProvider() {
	}
	/**
	 * Implements IStructuredContentProvider.
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object input) {
		if (contents != null && contents == input) {
			return contents.toArray();
		}
		return new Object[0];
	}
	/**
	 * Implements IContentProvider.
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof List) {
			contents = (List) newInput;
		}
		else {
			contents = null;
		}
	}
	/**
	 * Implements IContentProvider.
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}
}