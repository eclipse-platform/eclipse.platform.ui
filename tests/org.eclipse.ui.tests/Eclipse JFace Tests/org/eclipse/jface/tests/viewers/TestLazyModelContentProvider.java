/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * The TestLazyModelContentProvider is the lazy version
 * of the model content provider.
 */
public class TestLazyModelContentProvider extends TestModelContentProvider implements ILazyContentProvider {
	
	TableViewerTest test;
	TestElement input;
	
	TestLazyModelContentProvider(TableViewerTest testObject){
		test = testObject;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILazyContentProvider#invalidateElements(int, int)
	 */
	public void invalidateElements(int start, int finish) {
		updateElements(start, finish);

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILazyContentProvider#updateElements(int, int)
	 */
	public void updateElements(int start, int finish) {
		if(input == null)
			return; //Nothing to update yet
		int size = finish-start + 1;
		TestElement[] children = new TestElement[size];
        for (int i = 0; i < size; ++i)
            children[i] = input.getChildAt(i + start);
        ((TableViewer) test.fViewer).replace(children, start);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.tests.viewers.TestModelContentProvider#dispose()
	 */
	public void dispose() {
		super.dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.tests.viewers.TestModelContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		input = (TestElement) newInput;
		super.inputChanged(viewer, oldInput, newInput);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.tests.viewers.TestModelContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object element) {
		Assert.isTrue(false,"Should not ever call getElements if lazy");
		return super.getElements(element);
	}
}
