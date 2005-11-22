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
import org.eclipse.jface.databinding.PropertyDesc;
import org.eclipse.jface.databinding.viewers.ViewersProperties;
import org.eclipse.jface.tests.databinding.scenarios.model.Catalog;
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
	 * it is assumed that a user will provide label provider and cell editors/modifyer.
	 * 
	 * Ensure that tree model is propagated virtualy. 
	 * to the target, and that changes in one, update the other 
	 */
	public void test_Trees_Scenario01() {
		
		getDbc().bind(new PropertyDesc(tviewer, ViewersProperties.CONTENT), treeModel, null);
				
		
		// null for a parent, represents root elements
		assertEqualsTreeNode(null, null);
		
		
		
		
	}

}
