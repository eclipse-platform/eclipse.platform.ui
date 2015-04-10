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
package org.eclipse.e4.ui.css.swt.dom;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.internal.css.swt.dom.AbstractControlSelectionEraseListener;
import org.eclipse.e4.ui.internal.css.swt.dom.ControlSelectedColorCustomization;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;

public class TableElement extends ControlElement implements ISelectionBackgroundCustomizationElement {

	private final ControlSelectedColorCustomization fControlSelectedColorCustomization;

	private static class TableControlSelectionEraseListener extends AbstractControlSelectionEraseListener {

		@Override
		protected void fixEventDetail(Control control, Event event) {
			if ((event.detail & SWT.FOCUSED) != 0 || event.display.getFocusControl() == control) {
				// it has focus: remove the selected state as we
				// just painted it and it no longer needs to be
				// painted.
				// Note: it's not enough checking if the cell is focused because
				// if the table has the MULTI select style, only one will be
				// focused, but we still want to remove the selected state on
				// the other selected cells.
				event.detail &= ~SWT.SELECTED;
			} else {
				// it doesn't have focus: don't change the drawing
				// as the table selection won't appear properly if we
				// remove the selected state.
			}
		}

		@Override
		protected int getNumberOfColumns(Control control) {
			return ((Table) control).getColumnCount();
		}

	}

	public TableElement(Table table, CSSEngine engine) {
		super(table, engine);
		fControlSelectedColorCustomization = new ControlSelectedColorCustomization(table,
				new TableControlSelectionEraseListener());
	}

	@Override
	public void setSelectionBackgroundColor(Color color) {
		this.fControlSelectedColorCustomization.setSelectionBackgroundColor(color);
	}

	@Override
	public Color getSelectionBackgroundColor() {
		return this.fControlSelectedColorCustomization.getSelectionBackgroundColor();
	}

	@Override
	public void setSelectionBorderColor(Color color) {
		this.fControlSelectedColorCustomization.setSelectionBorderColor(color);

	}

	@Override
	public Color getSelectionBorderColor() {
		return this.fControlSelectedColorCustomization.getSelectionBorderColor();
	}

	@Override
	public void setHotBackgroundColor(Color color) {
		this.fControlSelectedColorCustomization.setHotBackgroundColor(color);

	}

	@Override
	public Color getHotBackgroundColor() {
		return this.fControlSelectedColorCustomization.getHotBackgroundColor();
	}

	@Override
	public void setHotBorderColor(Color color) {
		this.fControlSelectedColorCustomization.setHotBorderColor(color);
	}

	@Override
	public Color getHotBorderColor() {
		return this.fControlSelectedColorCustomization.getHotBorderColor();
	}

	@Override
	public Color getSelectionForegroundColor() {
		return this.fControlSelectedColorCustomization.getSelectionForegroundColor();
	}

	@Override
	public void setSelectionForegroundColor(Color color) {
		this.fControlSelectedColorCustomization.setSelectionForegroundColor(color);
	}

}
