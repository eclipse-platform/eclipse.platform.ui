/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations.newapi;

import org.eclipse.ui.presentations.IPresentablePart;

/**
 * @since 3.0
 */
public abstract class TabList {
	public abstract void add(IPresentablePart newPart);
	public abstract void remove(IPresentablePart removed);
	public abstract void select(IPresentablePart selected);
	public abstract void insert(IPresentablePart added, int index);
	public abstract IPresentablePart[] getParts();
}
