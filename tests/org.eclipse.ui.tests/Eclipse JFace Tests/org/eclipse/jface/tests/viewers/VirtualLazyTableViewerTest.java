/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Table;


/**
 * The VirtualLazyTableViewerTest is a test of table viewers
 * with lazy population.
 */
public class VirtualLazyTableViewerTest extends VirtualTableViewerTest {
	
	/**
	 * Create a new instance of the receiver/
	 * @param name
	 */
	public VirtualLazyTableViewerTest(String name) {
		super(name);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.tests.viewers.TableViewerTest#getContentProvider()
	 */
	protected TestModelContentProvider getContentProvider() {
		return new TestLazyModelContentProvider(this);
	}
	
	/**
	 * Test selecting all elements.
	 */
	public void testSetIndexedSelection() {
		TestElement[] children = fRootElement.getChildren();
		int selectionSize = children.length / 2;
		int[] indices = new int[selectionSize];
		for (int i = 0; i < indices.length; i++) {
			indices[i]  = i * 2;			
		}
		
		Table table = ((TableViewer) fViewer).getTable();
		table.setSelection(indices);
		table.showSelection();
		
		IStructuredSelection result = (IStructuredSelection) fViewer
				.getSelection();
		assertTrue("Size was " + String.valueOf(result.size()) + " expected "
				+ String.valueOf(selectionSize),
				(result.size() == selectionSize));
		assertTrue("First elements do not match ",
				result.getFirstElement() == children[indices[0]]);
		int lastIndex = indices[indices.length - 1];
		assertTrue(
				"Last elements do not match ",
				result.toArray()[result.size() - 1] == children[lastIndex]);
	
	}
	

}
