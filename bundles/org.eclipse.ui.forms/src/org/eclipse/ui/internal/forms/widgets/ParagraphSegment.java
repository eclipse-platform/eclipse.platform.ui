/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.forms.widgets;

import java.util.Hashtable;

import org.eclipse.swt.graphics.GC;

/**
 * @version 	1.0
 * @author
 */
public abstract class ParagraphSegment {
	public abstract boolean advanceLocator(GC gc, int wHint, Locator loc, Hashtable objectTable, boolean computeHeightOnly);
	public abstract void paint(GC gc, int width, Locator loc, Hashtable resourceTable, boolean selected);
}
