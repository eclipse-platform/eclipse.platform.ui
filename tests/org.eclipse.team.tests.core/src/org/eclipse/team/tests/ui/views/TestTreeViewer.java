/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ui.views;

import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.*;

public class TestTreeViewer extends TreeViewer {

	public TestTreeViewer(Composite parent) {
		super(parent);
	}

	public TestTreeViewer(Composite parent, int style) {
		super(parent, style);
	}

	public TestTreeViewer(Tree tree) {
		super(tree);
	}
	
	public Item[] getRootItems() {
		expandAll();
		return getChildren(getControl());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#getChildren(org.eclipse.swt.widgets.Widget)
	 */
	public Item[] getChildren(Widget o) {
		return super.getChildren(o);
	}
	
	public boolean hasItemFor(DiffNode node) {
		return findItem(node) != null;
	}
}
