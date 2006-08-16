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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.help.IIndex;
import org.eclipse.help.IIndexEntry;
import org.eclipse.help.INode;
import org.eclipse.help.internal.Node;

/*
 * Help index implementation
 */
public class Index extends Node implements IIndex {
    
	private IIndexEntry[] entries;
	
	public IIndexEntry[] getEntries() {
		if (entries == null) {
			INode[] children = getChildren();
			if (children.length > 0) {
				List list = new ArrayList();
				for (int i=0;i<children.length;++i) {
					if (children[i] instanceof IIndexEntry) {
						list.add(children[i]);
					}
				}
				entries = (IIndexEntry[])list.toArray(new IIndexEntry[list.size()]);
			}
			else {
				entries = new IIndexEntry[0];
			}
		}
		return entries;
	}
}
