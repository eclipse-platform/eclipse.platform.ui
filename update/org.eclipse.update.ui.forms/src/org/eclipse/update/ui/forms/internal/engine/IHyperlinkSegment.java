/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.ui.forms.internal.engine;

import java.util.Hashtable;

import org.eclipse.swt.graphics.GC;

/**
 * @version 	1.0
 * @author
 */
public interface IHyperlinkSegment extends ITextSegment, IObjectReference {
	HyperlinkAction getAction(Hashtable objectTable);
	
	boolean contains(int x, int y);
	
	String getArg();
	
	public void repaint(GC gc, boolean hover);
}
