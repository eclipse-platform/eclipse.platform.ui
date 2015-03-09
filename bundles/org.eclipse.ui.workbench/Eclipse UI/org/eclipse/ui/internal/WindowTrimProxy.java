/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.layout.IWindowTrim;

/**
 * A transition class for any control that wants to participate in the workbench
 * window trim. It must supply a unique ID so that it's position can be
 * saved/restored.
 *
 * @since 3.2
 */
public class WindowTrimProxy implements IWindowTrim {

	private Control fTrimControl;

	private String fId;

	private String fDisplayName;

	private int fValidSides;

	private boolean fIsResizeable = false;

	private int fWidthHint = SWT.DEFAULT;

	private int fHeightHint = SWT.DEFAULT;

	/**
	 * Create the trim proxy for a control.
	 *
	 * @param c
	 *            the trim control
	 * @param id
	 *            an ID that it's known by
	 * @param displayName
	 *            the NLS name, for use in created menus
	 * @param validSides
	 *            bitwise or of valid sides
	 * @see #getValidSides()
	 */
	public WindowTrimProxy(Control c, String id, String displayName,
			int validSides) {
		fTrimControl = c;
		fId = id;
		fDisplayName = displayName;
		fValidSides = validSides;
	}

	/**
	 * Create a trim proxy for a control.
	 *
	 * @param c
	 * @param id
	 * @param displayName
	 * @param validSides
	 * @param resizeable
	 */
	public WindowTrimProxy(Control c, String id, String displayName,
			int validSides, boolean resizeable) {
		this(c, id, displayName, validSides);
		fIsResizeable = resizeable;
	}

	@Override
	public Control getControl() {
		return fTrimControl;
	}

	@Override
	public int getValidSides() {
		return fValidSides;
	}

	/**
	 * The default for a proxied window trim is to do nothing, as it can't be
	 * moved around.
	 *
	 * @see org.eclipse.ui.internal.layout.IWindowTrim#dock(int)
	 */
	@Override
	public void dock(int dropSide) {
	}

	@Override
	public String getId() {
		return fId;
	}

	@Override
	public String getDisplayName() {
		return fDisplayName;
	}

	@Override
	public boolean isCloseable() {
		return false;
	}

	@Override
	public void handleClose() {
		// nothing to do...
	}

	@Override
	public int getWidthHint() {
		return fWidthHint;
	}

	/**
	 * Update the width hint for this control.
	 * @param w pixels, or SWT.DEFAULT
	 */
	public void setWidthHint(int w) {
		fWidthHint = w;
	}

	@Override
	public int getHeightHint() {
		return fHeightHint;
	}

	/**
	 * Update the height hint for this control.
	 * @param h pixels, or SWT.DEFAULT
	 */
	public void setHeightHint(int h) {
		fHeightHint = h;
	}

	@Override
	public boolean isResizeable() {
		return fIsResizeable;
	}
}
