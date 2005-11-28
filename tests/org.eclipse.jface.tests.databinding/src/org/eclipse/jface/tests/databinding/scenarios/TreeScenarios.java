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
	ITree   treeModel = null;
	
	

	protected void setUp() throws Exception {
		super.setUp();
		getComposite().setLayout(new FillLayout());

		tree = new Tree(getComposite(), SWT.NONE);
		tviewer = new TreeViewer(tree);
		
		catalog = SampleData.CATALOG_2005; // Lodging source
		
		treeModel = SampleData.CATEGORY_TREE;

	}

	protected void tearDown() throws Exception {
		tree.dispose();
		tree = null;
		tviewer = null;
		super.tearDown();
	}
	
	
	private void assertEqualsTreeNode (Object viewerNode, Object modelNode) {
		assertEquals(viewerNode, modelNode);
		Object[] viewerChildren = ((ITreeContentProvider)tviewer.getContentProvider()).getChildren(viewerNode);
		if (viewerChildren.length==0)
			viewerChildren=null;
		Object[] modelChildren = treeModel.getChildren(modelNode);
		if (modelChildren!=null && modelChildren.length==0)
			modelChildren=null;
		
		if (viewerChildren==null || modelChildren==null) {
			assertEquals(viewerChildren, modelChildren);
			return;
		}
		assertEquals(viewerChildren.length, modelChildren.length);
		
		for (int i = 0; i < modelChildren.length; i++) 
			assertEqualsTreeNode(viewerChildren[i], modelChildren[i]);		
	}
	
	/**
	 * Simple TreeViewer binding.  No TreeDescription, in this case
	 * it is assumed that a user will provide label provider and cell editors/modifier.
	 * 
	 * Ensure that tree model is propagated virtualy. 
	 * to the target, and that changes in one, update the other 
	 */
	public void test_Trees_Scenario01() {
		
		getDbc().bind(new Property(tviewer, ViewersProperties.CONTENT), treeModel, null);
				
		
		// null for a parent, represents root elements
		assertEqualsTreeNode(null, null);
	
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
		
		getDbc().bind(treeDescription, treeModel, null);
		
		assertEqualsTreeNode(null, null);
		
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
						
		assertEqualsTreeNode(null, null);
		
	}


}
