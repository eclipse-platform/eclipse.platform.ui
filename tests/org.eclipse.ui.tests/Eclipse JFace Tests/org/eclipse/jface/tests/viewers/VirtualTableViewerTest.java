/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * The TableViewerTest is a test of the SWT#VIRTUAL support in TableViewers,
 */
public class VirtualTableViewerTest extends TableViewerTest {
	
	int currentVisibleIndex = -1;
	
	/**
	 * Create a new instance of the receiver.
	 * @param name
	 */
	public VirtualTableViewerTest(String name) {
		super(name);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.tests.viewers.TableViewerTest#createTableViewer(org.eclipse.swt.widgets.Composite)
	 */
	protected TableViewer createTableViewer(Composite parent) {
		TableViewer viewer =  new TableViewer(parent,SWT.VIRTUAL);
		final Table table = viewer.getTable();
		table.addListener(SWT.SetData,new Listener(){
			/* (non-Javadoc)
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				int index = table.indexOf(item);			
				if(index > currentVisibleIndex)
					currentVisibleIndex = index;

			}
		});
		return viewer;
	}
	
	/**
	 * Get the collection of currently visible table items.
	 * @return TableItem[]
	 */
	private TableItem[] getVisibleItems() {
		if(currentVisibleIndex < 0)//Anything shown yet?
			return new TableItem[0];
		Table table = ((TableViewer) fViewer).getTable();
		TableItem[] visible = new TableItem[currentVisibleIndex];
		for (int i = 0; i < visible.length; i++) {
			visible[i] = table.getItem(i);
		}
		return visible;
	}

	public void testElementsCreated() {
		
		TableItem[] items = getVisibleItems();
		
		for (int i = 0; i < items.length; i++) {
			TableItem item = items[i];
			assertTrue("Missing data in item " + String.valueOf(i), item
					.getData() instanceof TestElement);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.tests.viewers.TableViewerTest#getItemCount()
	 */
	protected int getItemCount() {
		return getVisibleItems().length;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.tests.viewers.StructuredViewerTest#testFilter()
	 */
	public void testFilter() {
		//The filter test is no use here as it is
		//based on the assumption that all items 
		//are created.
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.tests.viewers.StructuredViewerTest#testInsertSibling()
	 */
	public void testInsertSibling() {
		//This test is no use here as it is
		//based on the assumption that all items 
		//are created.
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.tests.viewers.StructuredViewerTest#testInsertSiblingReveal()
	 */
	public void testInsertSiblingReveal() {
//		This test is no use here as it is
		//based on the assumption that all items 
		//are created.
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.tests.viewers.StructuredViewerTest#testInsertSiblings()
	 */
	public void testInsertSiblings() {
//		This test is no use here as it is
		//based on the assumption that all items 
		//are created.
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.tests.viewers.StructuredViewerTest#testInsertSiblingWithFilterFiltered()
	 */
	public void testInsertSiblingWithFilterFiltered() {
		//This test is no use here as it is
		//based on the assumption that all items 
		//are created.
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.tests.viewers.StructuredViewerTest#testInsertSiblingWithFilterNotFiltered()
	 */
	public void testInsertSiblingWithFilterNotFiltered() {
		//This test is no use here as it is
		//based on the assumption that all items 
		//are created.
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.tests.viewers.StructuredViewerTest#testInsertSiblingWithSorter()
	 */
	public void testInsertSiblingWithSorter() {
		//This test is no use here as it is
		//based on the assumption that all items 
		//are created.
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.tests.viewers.StructuredViewerTest#testRenameWithFilter()
	 */
	public void testRenameWithFilter() {
		//This test is no use here as it is
		//based on the assumption that all items 
		//are created.
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.tests.viewers.StructuredViewerTest#testSorter()
	 */
	public void testSorter() {
		//This test is no use here as it is
		//based on the assumption that all items 
		//are created.
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.tests.viewers.StructuredViewerTest#testInsertSiblingSelectExpanded()
	 */
	public void testInsertSiblingSelectExpanded() {
		//This test is no use here as it is
		//based on the assumption that all items 
		//are created.
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.tests.viewers.StructuredViewerTest#testSomeChildrenChanged()
	 */
	public void testSomeChildrenChanged() {
		//This test is no use here as it is
		//based on the assumption that all items 
		//are created.
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.tests.viewers.StructuredViewerTest#testWorldChanged()
	 */
	public void testWorldChanged() {
		//This test is no use here as it is
		//based on the assumption that all items 
		//are created.
	}

}
