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
package org.eclipse.jface.tests.viewers;

import junit.framework.AssertionFailedError;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * The TestLazyModelContentProvider is the lazy version
 * of the model content provider.
 */
public class TestLazyModelContentProvider extends TestModelContentProvider implements ILazyContentProvider, IContentProvider {
	
	TableViewerTest test;
	TestElement input;
	
	TestLazyModelContentProvider(TableViewerTest testObject){
		test = testObject;
		if(!(testObject instanceof VirtualLazyTableViewerTest)) {
			throw new AssertionFailedError("TestLazyModelContentProvider only works with VirtualLazyTableViewerTest");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILazyContentProvider#updateElements(int, int)
	 */
	public void updateElement(int index) {
		
		((VirtualLazyTableViewerTest)test).updateElementCalled(index);

		if(input == null)
			return; //Nothing to update yet
		
        ((TableViewer) test.fViewer).replace(input.getChildAt(index), index);

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
		((TableViewer)viewer).setItemCount(input==null?0:input.getChildCount());
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
