/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.model.AdaptableList;

/**
 * Provider used by the NewWizardNewPage.
 * 
 * @since 3.0
 */
public class WizardContentProvider implements ITreeContentProvider {

    private AdaptableList input;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {
        input = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof WizardCollectionElement) {
            ArrayList list = new ArrayList();
            WizardCollectionElement element = (WizardCollectionElement) parentElement;

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
            // if there is only one category, return it's children directly (flatten list)
            if (list.size() == 1
                    && list.get(0) instanceof WizardCollectionElement) {
                return getChildren(list.get(0));
            }
                
            return list.toArray();
        } else {
			return new Object[0];
		}
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
        if (element instanceof WizardCollectionElement) {
            Object[] children = input.getChildren();
            for (int i = 0; i < children.length; i++) {
                if (children[i].equals(element)) {
					return input;
				}
            }
            return ((WizardCollectionElement) element).getParent(element);
        } 
        return null;
    }

    /**
     * Adds the item to the list, unless it's a collection element without any children.
     * 
     * @param element the element to test and add
     * @param list the <code>Collection</code> to add to.
     * @since 3.0
     */
    private void handleChild(Object element, ArrayList list) {
        if (element instanceof WizardCollectionElement) {
            if (hasChildren(element)) {
				list.add(element);
			}
        } else {
            list.add(element);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object element) {
        if (element instanceof WizardCollectionElement) {
            if (getChildren(element).length > 0) {
				return true;
			}
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        input = (AdaptableList) newInput;
    }
}
