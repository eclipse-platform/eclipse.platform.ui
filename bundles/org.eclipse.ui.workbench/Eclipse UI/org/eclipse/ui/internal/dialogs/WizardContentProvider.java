/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.activities.ws.FilterableObject;
import org.eclipse.ui.model.AdaptableList;

/**
 * Provider used by the new format NewWizardNewPage.
 * 
 * @since 3.0
 */
public class WizardContentProvider
	extends FilterableObject
	implements ITreeContentProvider {

	/**
	 * @param filtering the initial filtering state.
	 */
	public WizardContentProvider(boolean filtering) {
		super(filtering);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof WizardCollectionElement) {
			ArrayList list = new ArrayList();
			WizardCollectionElement element =
				(WizardCollectionElement) parentElement;

			Object[] childCollections = element.getChildren();
			for (int i = 0; i < childCollections.length; i++) {
				handleChild(childCollections[i], list);
			}

			Object[] childWizards = element.getWizards();
			for (int i = 0; i < childWizards.length; i++) {
				handleChild(childWizards[i], list);
			}

			// flatten lists with only one category
			if (list.size() == 1
				&& list.get(0) instanceof WizardCollectionElement) {
				return getChildren(list.get(0));
			}

			return list.toArray();
		} else if (parentElement instanceof AdaptableList) {
			AdaptableList aList = (AdaptableList) parentElement;
			Object[] children = aList.getChildren();
			ArrayList list = new ArrayList(children.length);
			for (int i = 0; i < children.length; i++) {
				handleChild(children[i], list);
			}
			return list.toArray();
		} else
			return new Object[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		if (element instanceof WizardCollectionElement)
			return ((WizardCollectionElement) element).getParent(element);
		else
			return null;
	}

	/**
	 * Adds the supplied element to the list except if the provider is
	 * filtering and the element is an <code>IPluginContribution</code> that
	 * is currently being filtered.
	 * 
	 * @param element the element to test and add
	 * @param list the <code>Collection</code> to add to.
	 * @since 3.0
	 */
	private void handleChild(Object element, ArrayList list) {
		if (getFiltering() && WorkbenchActivityHelper.filterItem(element)) {
			return;
		}
		list.add(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		// do we need to check for unfiltered children?
		return (element instanceof WizardCollectionElement)
			&& !((WizardCollectionElement) element).isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
