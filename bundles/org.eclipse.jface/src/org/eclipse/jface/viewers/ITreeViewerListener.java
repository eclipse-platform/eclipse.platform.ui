/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;

/**
 * A listener which is notified when a tree viewer expands or collapses
 * a node.
 */
public interface ITreeViewerListener {
	/**
	 * Notifies that a node in the tree has been collapsed.
	 *
	 * @param event event object describing details
	 */
	public void treeCollapsed(TreeExpansionEvent event);

	/**
	 * Notifies that a node in the tree has been expanded.
	 *
	 * @param event event object describing details
	 */
	public void treeExpanded(TreeExpansionEvent event);
}
