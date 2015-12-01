/*******************************************************************************
 * Copyright (c) 2005, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     IBM Corporation - 122967 [Help] Remote help system
 *     IBM Corporation - Added support for see references
 *******************************************************************************/
package org.eclipse.help.internal.index;

import org.eclipse.help.IIndexEntry;
import org.eclipse.help.IIndexEntry2;
import org.eclipse.help.IIndexSee;
import org.eclipse.help.ITopic;
import org.eclipse.help.internal.UAElement;
import org.w3c.dom.Element;

public class IndexEntry extends UAElement implements IIndexEntry2 {
	
	public static final String NAME = "entry"; //$NON-NLS-1$
	public static final String ATTRIBUTE_KEYWORD = "keyword"; //$NON-NLS-1$
	
	public IndexEntry(IIndexEntry src) {
		super(NAME, src);
		setKeyword(src.getKeyword());
		appendChildren(src.getChildren());
	}
	
	public IndexEntry(Element src) {
		super(src);
	}

	@Override
	public String getKeyword() {
		return getAttribute(ATTRIBUTE_KEYWORD);
	}
	
	@Override
	public IIndexEntry[] getSubentries() {
		return (IIndexEntry[])getChildren(IIndexEntry.class);
	}
	
	@Override
	public ITopic[] getTopics() {
		return (ITopic[])getChildren(ITopic.class);
	}
	
	public void setKeyword(String keyword) {
		setAttribute(ATTRIBUTE_KEYWORD, keyword);
	}

	@Override
	public IIndexSee[] getSees() {
		return (IIndexSee[])getChildren(IIndexSee.class);
	}
}
