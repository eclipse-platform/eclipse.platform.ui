/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.events;

import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.internal.watson.IDeltaFilter;

public class DeltaFilter implements IDeltaFilter {
	protected int mask;
public DeltaFilter(int mask) {
	super();
	this.mask = mask;
}
public boolean includeElement(int flags) {
	/**
	 * There are two conditions for accepting a flag: 1) The flags and the mask
	 * have at least one bit in common, 2) The flags is zero, and the
	 * mask specifies to include changed children.  The only reason
	 * the flags can be zero is if there are changed children, in which
	 * case we want to treat it as a change.
	 */
	if (flags == 0)
		flags = IResourceDelta.CHANGED;
	return (flags & mask) != 0;
}
}
