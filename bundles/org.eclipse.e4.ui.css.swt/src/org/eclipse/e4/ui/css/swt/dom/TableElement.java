/*******************************************************************************
 * Copyright (c) 2015 Fabio Zadrozny and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Fabio Zadrozny <fabiofz@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.dom;

import java.util.function.Supplier;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;

public class TableElement extends ControlElement implements IHeaderCustomizationElement {


	public TableElement(Control control, CSSEngine engine) {
		super(control, engine);
	}

	@Override
	public void reset() {
		setHeaderColor(null);
		setHeaderBackgroundColor(null);
		super.reset();
	}


	@Override
	public void setHeaderColor(Color color) {
		getTable().setHeaderForeground(color);
	}

	@Override
	public void setHeaderBackgroundColor(Color color) {
		getTable().setHeaderBackground(color);
	}

	public Table getTable() {
		return (Table) getNativeWidget();
	}

	@Override
	protected Supplier<String> internalGetAttribute(String attr) {
		if ("swt-lines-visible".equals(attr)) {
			Table table = getTable();
			return () -> String.valueOf(table.getLinesVisible());
		}
		return super.internalGetAttribute(attr);
	}
}
