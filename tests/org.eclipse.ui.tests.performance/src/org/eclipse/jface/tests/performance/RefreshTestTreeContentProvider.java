/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.performance;


import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * The RefreshTestTreeContentProvider is the content provider for test trees.
 */
public class RefreshTestTreeContentProvider implements ITreeContentProvider {

    static TestTreeElement[] elements;
    static {
        elements = new TestTreeElement[RefreshTestContentProvider.ELEMENT_COUNT];
        for (int i = 0; i < RefreshTestContentProvider.ELEMENT_COUNT; i++) {
           elements[i] = (new TestTreeElement(i,null));
            
        }
     
    }

    public RefreshTestTreeContentProvider() {
        super();
    }

    public Object[] getChildren(Object parentElement) {
       return ((TestTreeElement)parentElement).children;
    }

    public Object getParent(Object element) {
        return ((TestTreeElement)element).parent;
    }

    public boolean hasChildren(Object element) {
        return ((TestTreeElement)element).children.length > 0;
    }

    public Object[] getElements(Object inputElement) {
       return elements;
    }

    public void dispose() {
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

    }

}
