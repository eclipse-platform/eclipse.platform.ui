/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

/**
 * Tests TreeViewer's VIRTUAL support with a normal content provider.
 * @since 3.2
 */
public class VirtualTreeViewerTest extends TreeViewerTest {

	public VirtualTreeViewerTest(String name) {
		super(name);
	}

	protected StructuredViewer createViewer(Composite parent) {
		Tree tree = new Tree(parent, SWT.VIRTUAL);
		fTreeViewer = new TreeViewer(tree);
		fTreeViewer.setContentProvider(new TestModelContentProvider());
		return fTreeViewer;
	}
}
