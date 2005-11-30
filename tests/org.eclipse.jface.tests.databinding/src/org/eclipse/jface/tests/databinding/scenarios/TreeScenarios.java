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
package org.eclipse.jface.tests.databinding.scenarios;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.eclipse.jface.databinding.IChangeEvent;
import org.eclipse.jface.databinding.ITree;
import org.eclipse.jface.databinding.Property;
import org.eclipse.jface.databinding.TreeModelDescription;
import org.eclipse.jface.databinding.viewers.TreeViewerDescription;
import org.eclipse.jface.databinding.viewers.ViewersProperties;
import org.eclipse.jface.tests.databinding.scenarios.model.Account;
import org.eclipse.jface.tests.databinding.scenarios.model.Adventure;
import org.eclipse.jface.tests.databinding.scenarios.model.Catalog;
import org.eclipse.jface.tests.databinding.scenarios.model.Category;
import org.eclipse.jface.tests.databinding.scenarios.model.Lodging;
import org.eclipse.jface.tests.databinding.scenarios.model.SampleData;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Tree;
 

public class TreeScenarios extends ScenariosTestCase {
	
	Tree tree=null;
	TreeViewer tviewer = null;
	Catalog catalog = null;
	ITree   catalogModelTree = null;
	ITree   directoryModelTree = null;	
	
	

	protected void setUp() throws Exception {
		super.setUp();
		getComposite().setLayout(new FillLayout());

		tree = new Tree(getComposite(), SWT.NONE);
		tviewer = new TreeViewer(tree);
		
		catalog = SampleData.CATALOG_2005; // Lodging source
		
		catalogModelTree = SampleData.CATEGORY_TREE;
		
		directoryModelTree = new ITree() {					
			private ITree.TreeChangeSupport changeSupport = null;			
			private Object[] rootObjects = Collections.EMPTY_LIST.toArray();

			public Class[] getTypes() {
				return new Class[] { File.class } ;
			}
		
			public boolean hasChildren(Object element) {
				return ((File)element).isDirectory();
			}
		
			public void setChildren(Object parentElement, Object[] children) {
				// Only allow to change change the root directory/ies
				Object old;
				if (parentElement==null) {
					old = rootObjects;
					rootObjects = children==null?Collections.EMPTY_LIST.toArray():children;
				}
				else {
					old = getChildren(parentElement);
					// this tree does not really create files
					// This "set" is just to ring the door bell
					// for the test
				}
				if (changeSupport!=null) {
					ITree.ChangeEvent event = new ITree.ChangeEvent(this, IChangeEvent.REPLACE, parentElement, old, children, -1);								
					changeSupport.fireTreeChange(event);
				}				
			}
					
			public Object[] getChildren(Object parentElement) {
				if (parentElement==null)
					return rootObjects;
				
				File[] children = ((File)parentElement).listFiles();
				if (children==null) return Collections.EMPTY_LIST.toArray();
				
				return children;
			}

			public void addTreeChangeListener(ITree.ChangeListener listener) {
				if (changeSupport==null)
					changeSupport = new ITree.TreeChangeSupport(this);
				changeSupport.addTreeChangeListener(listener);
			}

			public void removeTreeChangeListener(ITree.ChangeListener listener) {
				if (changeSupport!=null)
					changeSupport.removeTreeChangeListener(listener);
			}		
		};
				
		File temp = File.createTempFile("TreeScenario","jUnit");
		File root = new File (temp.getParent(),"TreeScenarioDir");
		temp.delete();
		
		if (root.exists()) 
			deleteFile(root);
		// Create a tempopary directory
		root.mkdirs();
		
		
		directoryModelTree.setChildren(null, new Object[] {root});

	}

	private void deleteFile (File f) {
		if (f.isDirectory()) {
			File[] list = f.listFiles();
			for (int i = 0; i < list.length; i++) 
				deleteFile(list[i]);							
		}
		f.delete();
	}
	
	protected void tearDown() throws Exception {
		tree.dispose();
		tree = null;
		tviewer = null;
		File root = (File) directoryModelTree.getChildren(null)[0];
		deleteFile(root);
		super.tearDown();
	}
	
	
	private void assertEqualsTreeNode (Object viewerNode, Object modelNode, ITree model) {
		assertEquals(viewerNode, modelNode);
		Object[] viewerChildren = ((ITreeContentProvider)tviewer.getContentProvider()).getChildren(viewerNode);
		if (viewerChildren.length==0)
			viewerChildren=null;
		Object[] modelChildren = model.getChildren(modelNode);
		if (modelChildren!=null && modelChildren.length==0)
			modelChildren=null;
		
		if (viewerChildren==null || modelChildren==null) {
			assertEquals(viewerChildren, modelChildren);
			return;
		}
		assertEquals(viewerChildren.length, modelChildren.length);
		
		for (int i = 0; i < modelChildren.length; i++) 
			assertEqualsTreeNode(viewerChildren[i], modelChildren[i], model);		
	}
	
	/**
	 * Simple TreeViewer binding.  No TreeDescription, in this case
	 * it is assumed that a user will provide label provider and cell editors/modifier.
	 * 
	 * Ensure that tree model is propagated virtualy. 
	 * to the target, and that changes in one, update the other 
	 */
	public void test_Trees_Scenario01() {
		
		getDbc().bind(new Property(tviewer, ViewersProperties.CONTENT), catalogModelTree, null);
				
		
		// null for a parent, represents root elements
		assertEqualsTreeNode(null, null, catalogModelTree);
	
	}
	
	/**
	 * Simple binding of a TreeViewer to a ITree model element
	 */
	public void test_Trees_Scenario02() {
		
		//	Describe the Viewer
		TreeViewerDescription treeDescription = new TreeViewerDescription(tviewer);
		treeDescription.addColumn(Catalog.class, "class.name");
		
		treeDescription.addColumn(Lodging.class, "name");
		treeDescription.addColumn(Lodging.class, "description");
		
		treeDescription.addColumn(Adventure.class, "name");
		treeDescription.addColumn(Adventure.class, "price");
		treeDescription.getColumn(Adventure.class, 1).setPropertyType(Double.TYPE);
					
		treeDescription.addColumn(Category.class, "name");
		
		treeDescription.addColumn(Account.class, "firstName");
		treeDescription.addColumn(Account.class, "lastName");
		
		getDbc().bind(treeDescription, catalogModelTree, null);
		
		assertEqualsTreeNode(null, null, catalogModelTree);
		
	}
	
	/**
	 * Simple binding of a TreeViewer to a Tree Description
	 */
	public void test_Trees_Scenario03() {
		
		// Describe the Viewer
		TreeViewerDescription treeDescription = new TreeViewerDescription(tviewer);
		treeDescription.addColumn(Catalog.class, "class.name");
		
		treeDescription.addColumn(Lodging.class, "name");
		treeDescription.addColumn(Lodging.class, "description");
		
		treeDescription.addColumn(Adventure.class, "name");
		treeDescription.addColumn(Adventure.class, "price");
		// TODO need to lazy set this
		treeDescription.getColumn(Adventure.class, 1).setPropertyType(Double.TYPE);
					
		treeDescription.addColumn(Category.class, "name");
		
		treeDescription.addColumn(Account.class, "firstName");
		treeDescription.addColumn(Account.class, "lastName");
		
		// Describe the model
		TreeModelDescription modelDescription = new TreeModelDescription(new Object[] {SampleData.CATALOG_2005});
		// The order properties are added, determine the order that the tree will list
		// children that come from different properties
		modelDescription.addChildrenProperty(Catalog.class, "categories");
		modelDescription.addChildrenProperty(Catalog.class, "lodgings");
		modelDescription.addChildrenProperty(Catalog.class, "accounts");
				
		modelDescription.addChildrenProperty(Category.class, "adventures");
		
		
		
		getDbc().bind(treeDescription, modelDescription, null);
						
		assertEqualsTreeNode(null, null, catalogModelTree);
		
	}

	
	private File createFile(File directory, String name) {
		File f = new File(directory, name);
		try {
			f.createNewFile();
		} catch (IOException e) {
			fail(e.getMessage());
		}
		return f;
	}
	
	private File createDir(File directory, String name) {
		File f = new File(directory, name);
		f.mkdir();
		return f;
	}
	
	/**
	 * Test a simple file system.
	 */
	public void test_Trees_Scenario04() {
		
		// Describe the Viewer
		TreeViewerDescription treeDescription = new TreeViewerDescription(tviewer);
		treeDescription.addColumn(File.class, "name");
		
		// Build  a file system.
		File root = (File) directoryModelTree.getChildren(null)[0];
		
		createFile(root, "rootfile1");
		createFile(root, "rootfile2");
		createFile(root, "rootfile3");
		
		root = createDir(root, "secondLevel");
		createFile(root, "secondfile1");
		File file2 = createFile(root, "secondfile2");
		createFile(root, "secondfile3");
		
		
		
		getDbc().bind(treeDescription, directoryModelTree, null);
						
		// Test that all is there 
		assertEqualsTreeNode(null, null, directoryModelTree);
		
		File file4 = createFile(root, "secondLevel4");
		directoryModelTree.setChildren(root, root.listFiles()); // This model does not listen to the file system.
																// this call will induce it to fires the change event.
		assertEqualsTreeNode(null, null, directoryModelTree);
		
		file2.delete();
		directoryModelTree.setChildren(root, root.listFiles());
		assertEqualsTreeNode(null, null, directoryModelTree);
		
		file4.delete();
		directoryModelTree.setChildren(root, root.listFiles());
		assertEqualsTreeNode(null, null, directoryModelTree);
	}

}
