package org.eclipse.help.ui.internal.workingset;

/*
 * (c) Copyright IBM Corp. 2002. 
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.workingset.*;
import org.eclipse.jface.viewers.*;

public class HelpWorkingSetTreeContentProvider
	implements ITreeContentProvider {

	/**
	 * Constructor for HelpWorkingSetTreeContentProvider.
	 */
	public HelpWorkingSetTreeContentProvider() {
		super();
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof AdaptableTocsArray)
			return ((AdaptableTocsArray)parentElement).getChildren();
		else if (parentElement instanceof AdaptableToc)
			return ((AdaptableToc) parentElement).getChildren();
		else
			return new IAdaptable[0];
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		if (element instanceof AdaptableHelpResource)
			return ((AdaptableHelpResource) element).getParent();
		else
			return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		return (element instanceof AdaptableToc || element instanceof AdaptableTocsArray);
	}

	/**
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
