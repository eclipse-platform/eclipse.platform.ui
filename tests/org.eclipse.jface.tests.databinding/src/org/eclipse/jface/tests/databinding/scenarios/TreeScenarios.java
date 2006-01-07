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

import org.eclipse.jface.databinding.BindingException;
import org.eclipse.jface.tests.databinding.scenarios.model.Catalog;
import org.eclipse.jface.tests.databinding.scenarios.model.SampleData;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Tree;
 

public class TreeScenarios extends ScenariosTestCase {
	
	Tree tree=null;
	TreeViewer tviewer = null;
	Catalog catalog = null;

	protected void setUp() throws Exception {
		super.setUp();
		getComposite().setLayout(new FillLayout());

		tree = new Tree(getComposite(), SWT.NONE);
		tviewer = new TreeViewer(tree);
		
		catalog = SampleData.CATALOG_2005; // Lodging source

	}

	protected void tearDown() throws Exception {
		tree.dispose();
		tree = null;
		tviewer = null;
		super.tearDown();
	}
	
	public void test_Trees_Scenario01() throws BindingException {
		
	}

}
