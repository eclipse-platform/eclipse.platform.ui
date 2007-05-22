/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - bug 170381
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Table;


/**
 * The VirtualLazyTableViewerTest is a test of table viewers
 * with lazy population.
 */
public class VirtualLazyTableViewerTest extends VirtualTableViewerTest {
	
	private List updatedElements;
	// by default, no failure is triggered when updateElement is called
	int updatedElementFailureTriggerIndex = -1;

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
	
	public void setUp() {
		updatedElements = new ArrayList();
		super.setUp();
		processEvents();
	}
	
	protected void setUpModel() {
		fRootElement = TestElement.createModel(2, 100);
        fModel = fRootElement.getModel();
	}

	public void tearDown() {
		super.tearDown();
		updatedElements = null;
	}
	
	// this method is called from TestLazyModelContentProvider
	public void updateElementCalled(int index) {
		updatedElements.add(new Integer(index));
		if(updatedElementFailureTriggerIndex!=-1 && updatedElements.size()>=updatedElementFailureTriggerIndex) {
			fail("unexpected call to updateElement, this is the " + updatedElements.size() + "th call");
		}
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

		// we are virtual, so not all indices we requested to select will be selected.
		indices = table.getSelectionIndices();
		selectionSize = indices.length;
		assertTrue("Expected at least one selected element", selectionSize > 0);
		
		table.showSelection();
		
		IStructuredSelection result = (IStructuredSelection) fViewer
				.getSelection();
		assertEquals(selectionSize, result.size());
		assertTrue("First elements do not match ",
				result.getFirstElement() == children[indices[0]]);
		int lastIndex = indices[indices.length - 1];
		assertTrue(
				"Last elements do not match ",
				result.toArray()[result.size() - 1] == children[lastIndex]);
	
	}
	
	public void testSetInputDoesNotMaterializeEverything() {
		fViewer.setInput(null);
		updatedElements.clear();
		// Assume something is wrong if all TableItems are materialized:
		updatedElementFailureTriggerIndex = fRootElement.getChildCount();
		fViewer.setInput(fRootElement);

		int materializedSize = updatedElements.size();
		assertTrue("Expected less than " + fRootElement.getChildCount()
				+ ", actual " + materializedSize,
				materializedSize < fRootElement.getChildCount());
		// create a new model and check if we get an equal number of calls to updateElement
		setUpModel();
		updatedElements.clear();
		fViewer.setInput(fRootElement);
		assertEquals(materializedSize, updatedElements.size());
	}
	
	public void testBug160153() {
		int childCount = fRootElement.getChildCount();
		TestElement lastChild = fRootElement.getChildAt(childCount-1);
		// materialize last child
		fViewer.setSelection(new StructuredSelection(lastChild));
		processEvents();
		assertNotNull("last Child should be in the map", fViewer.testFindItem(lastChild));
		((TableViewer)fViewer).setItemCount(childCount - 1);
		assertNull("last Child should no longer be in the map", fViewer.testFindItem(lastChild));
	}
	

	public void testSorter() {
		// This test is no use here as it is
		// based on the assumption that all items
		// are created.
	}
	
	public void testRenameWithSorter() {
		// This test is no use here as it is
		// based on the assumption that all items
		// are created.
	}
	
	public void testSetFilters() {
		// This test is no use here as it is
		// based on the assumption that all items
		// are created.
	}
	
	public void testFilter() {
		// This test is no use here as it is
		// based on the assumption that all items
		// are created.
	}
	
	public void testRenameWithFilter() {
		// This test is no use here as it is
		// based on the assumption that all items
		// are created.
	}
}
