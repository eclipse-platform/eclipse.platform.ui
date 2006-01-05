/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/*
 * Utility methods for working with Trees.
 */
public class TreeUtil {

	/*
	 * Fully expands the given tree.
	 */
	public static void expandTree(Tree tree) {
		tree.setFocus();
		TreeItem[] items = tree.getItems();
		for (int i=0;i<items.length;++i) {
			tree.setSelection(new TreeItem[] { items[i] });
			Keyboard.press(SWT.ARROW_RIGHT);
		}
	}
}
