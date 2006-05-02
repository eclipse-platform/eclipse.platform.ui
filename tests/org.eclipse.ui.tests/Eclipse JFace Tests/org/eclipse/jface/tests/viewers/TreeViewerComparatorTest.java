/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.viewers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @since 3.2
 *
 */
public class TreeViewerComparatorTest extends ViewerComparatorTest{
	
	protected class TeamModelTreeContentProvider extends TeamModelContentProvider 
				implements ITreeContentProvider{

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
		 */
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof List){
				List children = new ArrayList();
				Iterator iter = ((List)parentElement).iterator();
				while(iter.hasNext()){
					Object next = iter.next();
					if (next instanceof Team){
						Team team = (Team)next;
						children.add(team);
					}
				}
				return children.toArray(new Team[children.size()]);
			}
			else if (parentElement instanceof Team){
				return ((Team)parentElement).members;
			}
			return null; 
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
		 */
		public Object getParent(Object element) {
			if (element instanceof TeamMember){
				return ((TeamMember)element).team;
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
		 */
		public boolean hasChildren(Object element) {
	        if (element instanceof Team) {
	            if (getChildren(element).length > 0) {
					return true;
				}
	        }
	        return false;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			List oldElement = (List) oldInput;
	        if (oldElement != null) {
	        	Iterator iter = oldElement.iterator();
	        	while (iter.hasNext()){
	        		((Team)iter.next()).removeListener(this);
	        	}
	        }
	        List newElement = (List) newInput;
	        if (newElement != null) {
	        	Iterator iter = newElement.iterator();
	        	while (iter.hasNext()){
	        		((Team)iter.next()).addListener(this);
	        	}
	        }			
		}
	}
	
	/**
	 * constructor
	 */
	public TreeViewerComparatorTest(String name) {
		super(name);
	}
	
	public void testViewerSorter(){
		fViewer.setSorter(new ViewerSorter());
		getTreeViewer().expandAll();
		String[][] expected = {TEAM3_SORTED, TEAM2_SORTED, TEAM1_SORTED};
		assertSortedResult(expected);
	}
	
	public void testViewerSorterInsertElement(){
		fViewer.setSorter(new ViewerSorter());
		getTreeViewer().expandAll();
		team1.addMember("Duong");
		String[][] expected = {TEAM3_SORTED, TEAM2_SORTED, TEAM1_SORTED_WITH_INSERT};
		assertSortedResult(expected);
	}
	
	public void testViewerComparator(){
		fViewer.setComparator(new ViewerComparator());
		getTreeViewer().expandAll();
		String[][] expected = {TEAM3_SORTED, TEAM2_SORTED, TEAM1_SORTED};
		assertSortedResult(expected);
	}
	
	public void testViewerComparatorInsertElement(){
		fViewer.setComparator(new ViewerComparator());
		getTreeViewer().expandAll();
		team1.addMember("Duong");
		String[][] expected = {TEAM3_SORTED, TEAM2_SORTED, TEAM1_SORTED_WITH_INSERT};
		assertSortedResult(expected);
	}
	
	private void assertSortedResult(String[][] resultArrays){
		TreeItem[] rootItems = getTreeViewer().getTree().getItems();
		assertEquals("Number of root items in tree not correct (actual=" + rootItems.length + ")", 3, rootItems.length);
		TreeItem item = rootItems[0];
		assertEquals("Item not expected.  actual=" + item.getText() + " expected=" + CORE, CORE, item.getText());	
		item = rootItems[1];
		assertEquals("Item not expected.  actual=" + item.getText() + " expected=" + RUNTIME, RUNTIME, item.getText());	
		item = rootItems[2];
		assertEquals("Item not expected.  actual=" + item.getText() + " expected=" + UI, UI, item.getText());	
		
		TreeItem[] childItems = rootItems[0].getItems();
		for (int i = 0; i < childItems.length; i++){
			TreeItem child = childItems[i];
			String result = child.getText();
			assertEquals("", resultArrays[0][i], result);		
		}
		childItems = rootItems[1].getItems();
		for (int i = 0; i < childItems.length; i++){
			TreeItem child = childItems[i];
			String result = child.getText();
			assertEquals("", resultArrays[1][i], result);		
		}
		childItems = rootItems[2].getItems();
		for (int i = 0; i < childItems.length; i++){
			TreeItem child = childItems[i];
			String result = child.getText();
			assertEquals("", resultArrays[2][i], result);		
		}
	}

	protected TreeViewer getTreeViewer(){
		return (TreeViewer)fViewer;
	}
	
	protected StructuredViewer createViewer(Composite parent) {
		Tree tree = new Tree(fShell, SWT.SINGLE | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.BORDER);
		TreeViewer viewer = new TreeViewer(tree);
		viewer.setContentProvider(new TeamModelTreeContentProvider());
		viewer.setLabelProvider(new TeamModelLabelProvider());
		return viewer;
	}

	protected void setInput() {
		List input = new ArrayList(3);
		input.add(team1);
		input.add(team2);
		input.add(team3);
		fViewer.setInput(input);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(TreeViewerComparatorTest.class);
	}

}
