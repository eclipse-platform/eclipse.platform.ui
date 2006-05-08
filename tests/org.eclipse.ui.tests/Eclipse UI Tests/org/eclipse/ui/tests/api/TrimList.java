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
package org.eclipse.ui.tests.api;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.layout.IWindowTrim;

/**
 * TrimList is a control that we'd like to contribute to the
 * window trim, plus the supporting trim interface.  It generates a
 * static list and doesn't remember anything, but a real trim
 * contribution could be any control+model.
 * @since 3.2
 */
public class TrimList implements IWindowTrim {
	/**
	 * Our trim ID ... must be unique, as if we contributed this
	 * trim from an extension.
	 */
	public static final String TRIM_LIST_ID = "org.eclipse.ui.tests.api.TrimList";

	private static final String[] INIT_LIST = { "Offline", "Online", "Proxied" };

	private Combo fCombo;

	/**
	 * The trim objects must all have the same parent.  After 3.2M4 
	 * the IWindowTrim interface will be updated so that the workbench
	 * window can give each piece of trim the correct parent.
	 * @param shell the parent of this trim
	 */
	public TrimList(Shell shell) {
		fCombo = new Combo(shell, SWT.DROP_DOWN|SWT.READ_ONLY);
		for (int i = 0; i < INIT_LIST.length; i++) {
			String value = INIT_LIST[i];
			fCombo.add(value);
		}
		fCombo.select(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWindowTrim#getControl()
	 */
	public Control getControl() {
		return fCombo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWindowTrim#getValidSides()
	 */
	public int getValidSides() {
		return SWT.TOP | SWT.BOTTOM;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWindowTrim#dock(int)
	 */
	public void dock(int dropSide) {
		// nothing to do, we don't have to re-orient our control
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWindowTrim#getId()
	 */
	public String getId() {
		return TRIM_LIST_ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWindowTrim#getDisplayName()
	 */
	public String getDisplayName() {
		// Should be the NLS string name, but I'll cheat for now
		return "Trim List";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWindowTrim#isCloseable()
	 */
	public boolean isCloseable() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWindowTrim#handleClose()
	 */
	public void handleClose() {
		// nothing to do here.
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowTrim#getWidthHint()
	 */
	public int getWidthHint() {
		return SWT.DEFAULT;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowTrim#getHeightHint()
	 */
	public int getHeightHint() {
		return SWT.DEFAULT;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowTrim#isResizeable()
	 */
	public boolean isResizeable() {
		return false;
	}
}
