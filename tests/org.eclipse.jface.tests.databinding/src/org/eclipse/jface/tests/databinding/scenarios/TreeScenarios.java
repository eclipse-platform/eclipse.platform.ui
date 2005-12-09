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

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IChangeListener;
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
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
 

public class TreeScenarios extends ScenariosTestCase {
	
	Tree tree=null;
	TreeViewer tviewer = null;
	TreeColumn firstCol = null;
	TreeColumn secondCol = null;
	
	// model
	Catalog catalog = null;	
	ITree   catalogModelTree = null;
	ITree   directoryModelTree = null;	
	
	

	protected void setUp() throws Exception {
		super.setUp();
		getComposite().setLayout(new FillLayout());

		tree = new Tree(getComposite(), SWT.NONE);
		firstCol = new TreeColumn(tree, SWT.NONE);
		firstCol.setWidth(100);
		secondCol = new TreeColumn(tree, SWT.NONE);
		secondCol.setWidth(100);
		tviewer = new TreeViewer(tree);
		
		catalog = SampleData.CATALOG_2005; // Lodging source
			
		catalogModelTree = SampleData.CATALOG_TREE;
		
		// Create a read only ITree wrapper for disk
		// File system.  A call to setChildren will just fire
		// a change event.
		directoryModelTree = new ITree() {					
			private ITree.ChangeSupport changeSupport = null;			
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
					ChangeEvent event = new ChangeEvent(this, ChangeEvent.REPLACE, old, children, parentElement, -1);								
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

			public void addTreeChangeListener(IChangeListener listener) {
				if (changeSupport==null)
					changeSupport = new ITree.ChangeSupport(this);
				changeSupport.addTreeChangeListener(listener);
			}

			public void removeTreeChangeListener(IChangeListener listener) {
				if (changeSupport!=null)
					changeSupport.removeTreeChangeListener(listener);
			}

			public void dispose() {
				changeSupport=null;
			}		
		};
				
		// Find a temp directory
		File temp = File.createTempFile("TreeScenario","jUnit");
		File root = new File (temp.getParent(),"TreeScenarioDir");
		temp.delete();
		
		if (root.exists()) 
			deleteFile(root);
		// Create a tempopary directory
		root.mkdirs();
		
		// prime the ITree 
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
	

	/*
	 * Recursively travere tree levels, and asserts that elements (objects, not col.) 
	 * in a given level are the equal.
	 * 
	 * viewer/model Nodes are the current traverse level.
	 * model, is the model tree.  The viewer is always assumed for the target.
	 * 
	 * Note: a call to this assertion will pull the complete tree to the viewer.
	 */
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
	 * Raw TreeViewer binding.  No TreeViewerDescription.  I in this case
	 * it is assumed that a user will provide label provider and cell editors/modifier.
	 * 
	 * Ensure that tree model is propagated virtualy. 
	 * to the target, and that changes in one, update the other 
	 */
	public void test_Trees_Scenario01() {
		
		getDbc().bind(new Property(tviewer, ViewersProperties.CONTENT), catalogModelTree, null);
						
		// Ensure that the catalog tree is fully propagated to the viewer
		assertEqualsTreeNode(null, null, catalogModelTree);
	
	}
	
	private void assertLabelProvider(TreeItem item) {
		
//		This is the way the SAMPLE tree is adding children to the catalog (first level)
//		Follow this order
//		
//		list.addAll(Arrays.asList(((Catalog)parentElement).getCategories()));
//		list.addAll(Arrays.asList(((Catalog)parentElement).getLodgings()));
//		list.addAll(Arrays.asList(((Catalog)parentElement).getAccounts()));
//
//      Also, make sure to maitained this order when using a  TreeModelDescription.
//		
//		modelDescription.addChildrenProperty(Catalog.class, "categories");
//		modelDescription.addChildrenProperty(Catalog.class, "lodgings");
//		modelDescription.addChildrenProperty(Catalog.class, "accounts");
//				
//		modelDescription.addChildrenProperty(Category.class, "adventures");		
		

		Account[] accounts = catalog.getAccounts();
		Category[] categories = catalog.getCategories();
		Lodging[] lodgings = catalog.getLodgings();
		
		tviewer.expandAll();
		spinEventLoop(0);
		// Ensure that the label provider follows the TableViewerDescription map
		TreeItem[] firstLevel = item.getItems();		
		int index = 0;		
		for (int i = 0; i < categories.length; i++, index++) { 
			// Categories
			assertEquals(categories[i].getName(), firstLevel[index].getText());
			
			// Adventures
			TreeItem[] secondLevel = firstLevel[index].getItems();
			Adventure[] adventures = categories[i].getAdventures();
			for (int j = 0; j < secondLevel.length; j++) {
				assertEquals(adventures[j].getName(), secondLevel[j].getText(0));
				assertEquals(Double.toString(adventures[j].getPrice()), secondLevel[j].getText(1));				
			}
		}
		// Lodging
		for (int i = 0; i < lodgings.length; i++, index++) {
			assertEquals(lodgings[i].getName(), firstLevel[index].getText(0));
			assertEquals(lodgings[i].getDescription(), firstLevel[index].getText(1));
		}
		// Account
		for (int i = 0; i < accounts.length; i++, index++) {
			assertEquals(accounts[i].getFirstName(), firstLevel[index].getText(0));
			assertEquals(accounts[i].getLastName(), firstLevel[index].getText(1));
		}		
	}
	
	/**
	 * Use a TreeViewerDescription to bind a TreeViewer to a ITree model.
	 */
	public void test_Trees_Scenario02() {
		
		//	Describe the Viewer
		TreeViewerDescription treeDescription = new TreeViewerDescription(tviewer);
		// catalog has no "name" property... so use getClass.getName()
		treeDescription.addColumn(Catalog.class, "class.name");
		// Lodging will have two colums
		treeDescription.addColumn(Lodging.class, "name");
		treeDescription.addColumn(Lodging.class, "description");		
		// Adventures will have two columns
		treeDescription.addColumn(Adventure.class, "name");
		treeDescription.addColumn(Adventure.class, "price");
//		treeDescription.getColumn(Adventure.class, 1).setPropertyType(Double.TYPE);					
		// Category will have one column
		treeDescription.addColumn(Category.class, "name");
		// Account will have two columns
		treeDescription.addColumn(Account.class, "firstName");
		treeDescription.addColumn(Account.class, "lastName");
		
		getDbc().bind(treeDescription, catalogModelTree, null);
		// Make sure that the catalog model has been propagated to the viewer
		assertEqualsTreeNode(null, null, catalogModelTree);				
		
		TreeItem item = tviewer.getTree().getItem(0);		
		assertEquals(item.getText(), catalog.getClass().getName());
		
		assertLabelProvider(item);

	}
	
	/**
	 * Simple binding of a TreeViewer to a Tree Model Description
	 */
	public void test_Trees_Scenario03() {
		
		// Describe the Viewer
		TreeViewerDescription treeDescription = new TreeViewerDescription(tviewer);
		treeDescription.addColumn(Catalog.class, "class.name");
		
		treeDescription.addColumn(Lodging.class, "name");
		treeDescription.addColumn(Lodging.class, "description");
		
		treeDescription.addColumn(Adventure.class, "name");
		treeDescription.addColumn(Adventure.class, "price");
					
		treeDescription.addColumn(Category.class, "name");
		
		treeDescription.addColumn(Account.class, "firstName");
		treeDescription.addColumn(Account.class, "lastName");
		
		// Describe the model
		TreeModelDescription modelDescription = new TreeModelDescription(new Object[] {catalog});
		// The order properties are added, determine the order that the tree will list
		// children that come from different properties
		modelDescription.addChildrenProperty(Catalog.class, "categories");
		modelDescription.addChildrenProperty(Catalog.class, "lodgings");
		modelDescription.addChildrenProperty(Catalog.class, "accounts");
				
		modelDescription.addChildrenProperty(Category.class, "adventures");
				
		
		getDbc().bind(treeDescription, modelDescription, null);
		//	Make sure that the catalog model has been propagated to the viewer
		assertEqualsTreeNode(null, null, catalogModelTree);
		
				
		TreeItem item = tviewer.getTree().getItem(0);		
		assertEquals(item.getText(), catalog.getClass().getName());
		
		assertLabelProvider(item);

		// Test the JavaBean event model of a TreeModelDescription based tree.
		//
		// Simple property changes ...  test that viewer got all of them,
		Account[] accounts = catalog.getAccounts();
		for (int i = 0; i < accounts.length; i++) 
			accounts[i].setFirstName("Changed: "+accounts[i].getFirstName());
		Category[] categories = catalog.getCategories();
		for (int i = 0; i < categories.length; i++) { 
			categories[i].setName("Changed: "+categories[i].getName());
			Adventure[] adventures = categories[i].getAdventures();
			for (int j = 0; j < adventures.length; j++) 
				adventures[j].setName("Changed: "+adventures[i].getName());
		}
		// compare TreeItems, and Model
		assertLabelProvider(item);
		
		//
		// Test for adding and removing elements from the Tree Model
		Account newAccount = new Account();
		newAccount.setFirstName("NewBee");
		newAccount.setLastName("Appended");
		
		//add to the end
		catalog.addAccount(newAccount);
		assertLabelProvider(item);
		
		// remove the first one
		catalog.removeAccount(catalog.getAccounts()[0]);
		assertLabelProvider(item);
		
		//	add to the end
		Adventure newAdventure = new Adventure();
		newAdventure.setName("Marriage");
		newAdventure.setPrice(9999999999999L);
		catalog.getCategories()[0].addAdventure(newAdventure);			
		assertLabelProvider(item);
	}

	/**
	 * Simple binding of a TreeViewer to a Tree Model Description, using a nested TreeUpdatable
	 */
	public void test_Trees_Scenario04() {
		
		// Describe the Viewer
		TreeViewerDescription treeDescription = new TreeViewerDescription(tviewer);	
		
		treeDescription.addColumn(Adventure.class, "name");
		treeDescription.addColumn(Adventure.class, "price");
					
		treeDescription.addColumn(Category.class, "name");
		
		// Describe the model with a Property Root object.  This will be translated to a Nested Updatable Tree
		TreeModelDescription modelDescription = new TreeModelDescription(new Property(catalog, "categories"));
		modelDescription.addChildrenProperty(Category.class, "adventures");
				
		
		getDbc().bind(treeDescription, modelDescription, null);
		//	Make sure that the catalog's categories have been propagated to the viewer
		assertEqualsTreeNode(null, null, SampleData.CATEGORY_TREE);
		
		
		// Make a change, make sure that it is propagated
		Adventure newAdventure = new Adventure();
		newAdventure.setName("new Adventure");
		catalog.getCategories()[0].addAdventure(newAdventure);
		
		assertEqualsTreeNode(null, null, SampleData.CATEGORY_TREE);
		
		// Adding a new category, will change the root of the tree
		// Ensure that the nested Tree drives this
		Category newCategory = new Category();				
		newCategory.setName("new Empty Category");
		catalog.addCategory(newCategory);
		
		assertEqualsTreeNode(null, null, SampleData.CATEGORY_TREE);		
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
	 * Test a simple file system, using an explicit ITree facade for the FS.
	 */
	public void test_Trees_Scenario05() {
		
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
	
	/**
	 * Test a simple file system, using a TreeModelDescription.
	 */
	public void test_Trees_Scenario06() {

		
		// Build  a file system.
		File root = (File) directoryModelTree.getChildren(null)[0];
		
		createFile(root, "rootfile1");
		createFile(root, "rootfile2");
		createFile(root, "rootfile3");
		
		File secondLevel = createDir(root, "secondLevel");
		createFile(secondLevel, "secondfile1");
		createFile(secondLevel, "secondfile2");
		createFile(secondLevel, "secondfile3");
		
		
		// Describe the Viewer
		TreeViewerDescription treeDescription = new TreeViewerDescription(tviewer);
		treeDescription.addColumn(File.class, "name");
		
		// Describe the Model
		TreeModelDescription modelDescription = new TreeModelDescription(new Object[] {root} );
		modelDescription.addChildrenProperty(File.class, "listFiles");
				
		getDbc().bind(treeDescription, modelDescription, null);
						
		// Test that all is there 
		assertEqualsTreeNode(null, null, directoryModelTree);
		
	}

}
