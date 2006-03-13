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

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreePathViewerSorter;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 3.2
 * 
 */
public class SimpleTreeViewerTest extends ViewerTestCase {

	private TreeViewer treeViewer;

	/**
	 * @param name
	 */
	public SimpleTreeViewerTest(String name) {
		super(name);
	}

	protected StructuredViewer createViewer(Composite parent) {
		treeViewer = new TreeViewer(parent);
		treeViewer.setContentProvider(new TestModelContentProvider());
		return treeViewer;
	}

	public void testSetTreePathViewerSorterOnNullInput() {
		treeViewer.setInput(null);
		treeViewer.setSorter(new TreePathViewerSorter());
	}

}
