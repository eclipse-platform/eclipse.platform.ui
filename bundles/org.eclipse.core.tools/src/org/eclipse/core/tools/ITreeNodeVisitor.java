/**********************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tools;

/** 
 * This interface is implemented by objects that visit trees made of
 * <code>TreeContentProviderNode</code> objects.
 *
 * @see org.eclipse.core.tools.TreeContentProviderNode#accept(ITreeNodeVisitor)
 */
public interface ITreeNodeVisitor {
	/** 
	 * Visits the given node.
	 *
	 * @param node the node to visit
	 * @return <code>true</code> if the node's child nodes  should  be visited;
	 * <code>false</code> if they should be skipped
	 */
	public boolean visit(TreeContentProviderNode node);
}