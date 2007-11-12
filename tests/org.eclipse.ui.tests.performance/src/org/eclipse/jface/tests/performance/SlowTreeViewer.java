/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.performance;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;

/**
 * @since 3.3
 * 
 */
public class SlowTreeViewer extends TreeViewer {

	/**
	 * @param parent
	 */
	public SlowTreeViewer(Composite parent) {
		super(parent);
	}

	/**
	 * @param parent
	 * @param style
	 */
	public SlowTreeViewer(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * @param tree
	 */
	public SlowTreeViewer(Tree tree) {
		super(tree);
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.TreeViewer#getChildren(org.eclipse.swt.widgets.Widget, java.lang.Object[])
	 */
	public Item[] getChildren(Widget widget, Object[] elementChildren) {
		return getChildren(widget);
	}
}
