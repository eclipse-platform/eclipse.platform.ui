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

package org.eclipse.ui.tests.filteredtree;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.tests.viewers.TestElement;
import org.eclipse.jface.tests.viewers.TestModelContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.tests.harness.util.UITestCase;

public class FilteredTreeTests extends UITestCase {
	private FilteredTree fTreeViewer;
	private TestElement fRootElement;
	// create an 8000-item Tree
	private static int DEPTH = 3;
	private static int NUM_ITEMS = 20;
	
	private class MyFilteredTree extends FilteredTree{
		public MyFilteredTree(Composite comp, int style) {
			super(comp);
			doSomeStuffBeforeWidgetCreation();
			init(style, new PatternFilter());
		}
		
		private void doSomeStuffBeforeWidgetCreation(){
			// do nothing
		}
	}
	
	private abstract class FilteredTreeDialog extends Dialog {
		private int style;
		
		public FilteredTreeDialog(Shell shell, int treeStyle){
			super(shell);
			style = treeStyle;
		}
		protected Control createContents(Composite parent) {
			Composite c = new Composite(parent, SWT.NONE);
			c.setLayout(new GridLayout());

			c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			fTreeViewer = doCreateFilteredTree(c, style);
			fTreeViewer.getViewer().setUseHashlookup(true);
			setInput();
			return parent;
		}

		protected abstract FilteredTree doCreateFilteredTree(Composite comp, int style); 
		
	};	
	
	/**
	 * @param testName
	 */
	public FilteredTreeTests(String testName) {
		super(testName);
	}

	public void testCreateFilteredTree(){
		runFilteredTreeTest(SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.BORDER);
	}
	
	public void testCreateCheckboxFilteredTree(){
		runFilteredTreeTest(SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.BORDER | SWT.CHECK);
	}
	/*
	 * Tests creation of a subclass of filtered tree, using alternate constructor.
	 */
	public void testCreateMyFilteredTree(){
		fRootElement = TestElement.createModel(DEPTH, NUM_ITEMS);	
		final int treeStyle = SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL |SWT.FLAT;
		
		Dialog dialog = new FilteredTreeDialog((Shell)null, treeStyle){
			protected FilteredTree doCreateFilteredTree(Composite comp, int style) {
				return createMyFilteredTree(comp, treeStyle);
			}
		};

		dialog.create();
		Assert.isNotNull(fTreeViewer, "Filtered tree is null");
		int itemCount = fTreeViewer.getViewer().getTree().getItemCount();
		Assert.isTrue(itemCount == NUM_ITEMS, "tree item count " + itemCount
				+ " does not match expected: " + NUM_ITEMS);
		dialog.close();
	}
	
	private void runFilteredTreeTest(final int treeStyle){
		fRootElement = TestElement.createModel(DEPTH, NUM_ITEMS);	
		
		Dialog dialog = new FilteredTreeDialog((Shell)null, treeStyle){
			protected FilteredTree doCreateFilteredTree(Composite comp, int style) {
				return createFilteredTree(comp, treeStyle);
			}
		};

		dialog.create();
		Assert.isNotNull(fTreeViewer, "Filtered tree is null");
		int itemCount = fTreeViewer.getViewer().getTree().getItemCount();
		Assert.isTrue(itemCount == NUM_ITEMS, "tree item count " + itemCount
				+ " does not match expected: " + NUM_ITEMS);
		dialog.close();
	}
	
	private FilteredTree createFilteredTree(Composite parent, int style){
	      Composite c = new Composite(parent, SWT.NONE);
	      c.setLayout(new GridLayout());
	      FilteredTree fTree = new FilteredTree(c, style, new PatternFilter());

	      GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		  gd.widthHint = 400;
		  gd.heightHint = 500;
		  fTree.setLayoutData(gd);
		  fTree.getViewer().setContentProvider(new TestModelContentProvider());
		  fTree.getViewer().setLabelProvider(new LabelProvider());

	      return fTree;	
	}
	
	private FilteredTree createMyFilteredTree(Composite parent, int style){
		Composite c = new Composite(parent, SWT.NONE);
	    c.setLayout(new GridLayout());
	    FilteredTree fTree = new MyFilteredTree(c, style);
	    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = 400;
		gd.heightHint = 500;
		fTree.setLayoutData(gd);
		fTree.getViewer().setContentProvider(new TestModelContentProvider());
		fTree.getViewer().setLabelProvider(new LabelProvider());
		return fTree;
	}
	
	private void setInput() {
		fTreeViewer.getViewer().setInput(fRootElement);
	}

	protected void doTearDown() throws Exception {
		super.doTearDown();
		fTreeViewer = null;
		fRootElement = null;
	}	
	
	
}
