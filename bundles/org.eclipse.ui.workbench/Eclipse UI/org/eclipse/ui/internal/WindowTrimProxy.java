/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.swt.widgets.Control;

/**
 * A transition class for any control that wants to participate
 * in the workbench window trim.  It must supply a unique ID so
 * that it's position can be saved/restored.
 * 
 * @since 3.2
 */
public class WindowTrimProxy implements IWindowTrim {
	
	private Control fTrimControl;
	private String fId;
	private int fValidSides;
	
	/**
	 * Create the trim proxy for a control.
	 * @param c the trim control
	 * @param id an ID that it's known by
	 * @param validSides bitwise or of valid sides
	 * @see #getValidSides()
	 */
	public WindowTrimProxy(Control c, String id, int validSides) {
		fTrimControl = c;
		fId = id;
		fValidSides = validSides;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IWindowTrim#getControl()
	 */
	public Control getControl() {
		return fTrimControl;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IWindowTrim#getValidSides()
	 */
	public int getValidSides() {
		return fValidSides;
	}

	/**
	 * The default for a proxied window trim is to do nothing, as it
	 * can't be moved around.
	 * 
	 * @see org.eclipse.ui.internal.IWindowTrim#dock(int)
	 */
	public void dock(int dropSide) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IWindowTrim#getId()
	 */
	public String getId() {
		return fId;
	}
}
