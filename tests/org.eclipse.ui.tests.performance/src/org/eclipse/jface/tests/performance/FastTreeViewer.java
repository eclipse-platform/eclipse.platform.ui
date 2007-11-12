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
public class FastTreeViewer extends TreeViewer {

	/**
	 * @param parent
	 */
	public FastTreeViewer(Composite parent) {
		super(parent);
	}

	/**
	 * @param parent
	 * @param style
	 */
	public FastTreeViewer(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * @param tree
	 */
	public FastTreeViewer(Tree tree) {
		super(tree);
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.TreeViewer#getChildren(org.eclipse.swt.widgets.Widget, java.lang.Object[])
	 */
	public Item[] getChildren(Widget widget, Object[] elementChildren) {
		Item[] items = super.getChildren(widget,elementChildren);
		if(elementChildren.length == 0 || items.length / elementChildren.length > 5){//Will there be a lot of disposal?
			getTree().removeAll();
			items =  getChildren(widget);
		}
		return items;
	}
}
