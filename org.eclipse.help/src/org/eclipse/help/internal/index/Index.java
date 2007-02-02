/*******************************************************************************
 * Copyright (c) 2005, 2007 Intel Corporation and others.
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
import org.eclipse.help.internal.UAElement;
import org.w3c.dom.Element;

public class Index extends UAElement implements IIndex {
    
	public static final String NAME = "index"; //$NON-NLS-1$

	public Index() {
		super(NAME);
	}
	
	public Index(IIndex src) {
		super(NAME, src);
		appendChildren(src.getChildren());
	}
	
	public Index(Element src) {
		super(src);
	}

	public IIndexEntry[] getEntries() {
		return (IIndexEntry[])getChildren(IIndexEntry.class);
	}
}
