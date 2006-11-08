/*******************************************************************************
 * Copyright (c) 2005, 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     IBM Corporation - 122967 [Help] Remote help system
 *******************************************************************************/
package org.eclipse.help.internal.index;

import org.eclipse.help.IIndex;
import org.eclipse.help.IIndexEntry;
import org.eclipse.help.Node;
import org.eclipse.help.internal.NodeAdapter;

/*
 * Adapts a "index" Node as an IIndex. All methods operate on the
 * underlying adapted Node.
 */
public class Index extends NodeAdapter implements IIndex {
    
	public static final String NAME = "index"; //$NON-NLS-1$

	/*
	 * Constructs a new index adapter for an empty index node.
	 */
	public Index() {
		super();
		setNodeName(NAME);
	}
	
	/*
	 * Constructs a new index adapter for the given index node.
	 */
	public Index(Node node) {
		super(node);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.IIndex#getEntries()
	 */
	public IIndexEntry[] getEntries() {
		return (IndexEntry[])getChildNodes(IndexEntry.NAME, IndexEntry.class);
	}
}
