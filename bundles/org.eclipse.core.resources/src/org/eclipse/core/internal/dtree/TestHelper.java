/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.dtree;

/**
 * Helper class for the test suite.
 */
public class TestHelper {
	/**
	 * Returns the root node of a tree.
	 */
	public static AbstractDataTreeNode getRootNode(AbstractDataTree tree) {
		return tree.getRootNode();
	}
}
