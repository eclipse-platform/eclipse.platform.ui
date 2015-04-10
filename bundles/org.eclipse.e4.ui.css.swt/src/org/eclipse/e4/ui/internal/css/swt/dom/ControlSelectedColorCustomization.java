/*******************************************************************************
 * Copyright (c) 2015 Fabio Zadrozny and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fabio Zadrozny <fabiofz@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.css.swt.dom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;

/**
 * Customization for selection/hot color for Trees/Tables.
 */
public class ControlSelectedColorCustomization {

	public final static String SELECTION_FOREGROUND_COLOR = "org.eclipse.e4.ui.css.swt.selectionForegroundColor"; //$NON-NLS-1$
	public final static String SELECTION_BACKGROUND_COLOR = "org.eclipse.e4.ui.css.swt.selectionBackgroundColor"; //$NON-NLS-1$
	public final static String SELECTION_BORDER_COLOR = "org.eclipse.e4.ui.css.swt.selectionBorderColor"; //$NON-NLS-1$
	public final static String HOT_BACKGROUND_COLOR = "org.eclipse.e4.ui.css.swt.hotBackgroundColor"; //$NON-NLS-1$
	public final static String HOT_BORDER_COLOR = "org.eclipse.e4.ui.css.swt.hotBorderColor"; //$NON-NLS-1$

	private final Control fControl;

	private final Listener fEraseListener;

	public ControlSelectedColorCustomization(Control control, Listener eraseListener) {
		this.fControl = control;
		this.fEraseListener = eraseListener;
	}


	private void setEraseListener(Control control) {
		control.removeListener(SWT.EraseItem, fEraseListener);
		control.addListener(SWT.EraseItem, fEraseListener);
	}

	// Selection foreground
	public void setSelectionForegroundColor(Color color) {
		fControl.setData(SELECTION_FOREGROUND_COLOR, color);
		setEraseListener(fControl);
	}

	public Color getSelectionForegroundColor() {
		return getSelectionForegroundColor(fControl);
	}

	public static Color getSelectionForegroundColor(Control control) {
		Object data = control.getData(SELECTION_FOREGROUND_COLOR);
		if (data instanceof Color) {
			return (Color) data;
		}
		return null;
	}

	// Selection background
	public void setSelectionBackgroundColor(Color color) {
		fControl.setData(SELECTION_BACKGROUND_COLOR, color);
		setEraseListener(fControl);
	}


	public Color getSelectionBackgroundColor() {
		return getSelectionBackgroundColor(fControl);
	}

	public static Color getSelectionBackgroundColor(Control control) {
		Object data = control.getData(SELECTION_BACKGROUND_COLOR);
		if (data instanceof Color) {
			return (Color) data;
		}
		return null;
	}

	// Selection border
	public void setSelectionBorderColor(Color color) {
		fControl.setData(SELECTION_BORDER_COLOR, color);
		setEraseListener(fControl);
	}

	public Color getSelectionBorderColor() {
		return getSelectionBorderColor(fControl);
	}

	public static Color getSelectionBorderColor(Control control) {
		Object data = control.getData(SELECTION_BORDER_COLOR);
		if (data instanceof Color) {
			return (Color) data;
		}
		return null;
	}

	// --- Hot background
	public void setHotBackgroundColor(Color color) {
		fControl.setData(HOT_BACKGROUND_COLOR, color);
		setEraseListener(fControl);
	}

	public Color getHotBackgroundColor() {
		return getHotBackgroundColor(fControl);
	}

	public static Color getHotBackgroundColor(Control fControl) {
		Object data = fControl.getData(HOT_BACKGROUND_COLOR);
		if (data instanceof Color) {
			return (Color) data;
		}
		return null;
	}

	// --- Hot border
	public void setHotBorderColor(Color color) {
		fControl.setData(HOT_BORDER_COLOR, color);
		setEraseListener(fControl);
	}

	public Color getHotBorderColor() {
		return getHotBorderColor(fControl);
	}

	public static Color getHotBorderColor(Control control) {
		Object data = control.getData(HOT_BORDER_COLOR);
		if (data instanceof Color) {
			return (Color) data;
		}
		return null;
	}


}
