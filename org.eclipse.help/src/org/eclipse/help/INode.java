/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help;

/**
 * <p>
 * An INode represents a node in a tree-structured user assistance
 * document. Each node may have zero or more children, and all nodes except
 * the root have a parent.
 * </p>
 * <p>
 * IMPORTANT: This API is still subject to change in 3.3. This interface may be
 * removed in favor of a concrete class.
 * </p>
 * 
 * @since 3.3
 */
public interface INode {

	/**
	 * Returns the node's children. If there are no children, returns an
	 * empty array.
	 * 
	 * @return the child nodes.
	 */
	public INode[] getChildren();
	
	/**
	 * Returns the node's parent. If this is the root of the tree, returns
	 * <code>null</code>.
	 * 
	 * @return the node's parent
	 */
	public INode getParent();
}
