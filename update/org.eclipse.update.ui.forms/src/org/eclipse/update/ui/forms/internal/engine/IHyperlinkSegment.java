/*
 * Copyright (c) 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package org.eclipse.update.ui.forms.internal.engine;

import java.util.Hashtable;

import org.eclipse.swt.graphics.GC;
import org.eclipse.update.ui.forms.internal.IHyperlinkListener;

/**
 * @version 	1.0
 * @author
 */
public interface IHyperlinkSegment extends ITextSegment, IObjectReference {
	HyperlinkAction getAction(Hashtable objectTable);
	
	boolean contains(int x, int y);
	
	public void repaint(GC gc, boolean hover);
}