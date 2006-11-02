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

import org.eclipse.help.IIndexEntry;
import org.eclipse.help.ITopic;
import org.eclipse.help.Node;
import org.eclipse.help.internal.NodeAdapter;
import org.eclipse.help.internal.Topic;

/*
 * Adapts a "entry" Node as an IIndexEntry. All methods operate on the
 * underlying adapted Node.
 */
public class IndexEntry extends NodeAdapter implements IIndexEntry {
	
	public static final String NAME = "entry"; //$NON-NLS-1$
	public static final String ATTRIBUTE_KEYWORD = "keyword"; //$NON-NLS-1$
	
	/*
	 * Constructs a new index entry adapter for an empty entry node.
	 */
	public IndexEntry() {
		super();
		setName(NAME);
	}

	/*
	 * Constructs a new index entry adapter for the given entry node.
	 */
	public IndexEntry(Node node) {
		super(node);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.IIndexEntry#getKeyword()
	 */
	public String getKeyword() {
		return node.getAttribute(ATTRIBUTE_KEYWORD);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.IIndexEntry#getSubentries()
	 */
	public IIndexEntry[] getSubentries() {
		return (IIndexEntry[])getChildren(NAME, IndexEntry.class);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.IIndexEntry#getTopics()
	 */
	public ITopic[] getTopics() {
		return (ITopic[])getChildren(Topic.NAME, Topic.class);
	}
	
	/*
	 * Sets the entry's keyword.
	 */
	public void setKeyword(String keyword) {
		node.setAttribute(ATTRIBUTE_KEYWORD, keyword);
	}
}
