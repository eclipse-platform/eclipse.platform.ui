/**********************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tools;

import org.eclipse.ui.part.ViewPart;

/** 
 * A common base class for all Spy Plug-in views
 */
public abstract class SpyView extends ViewPart {
	/**
	 * SpyView constructor comment.
	 */
	public SpyView() {
		super();
	}

	/**
	 * Asks this part to take focus within the workbench. Does nothing.
	 */
	public void setFocus() {
		// do nothing
	}
}