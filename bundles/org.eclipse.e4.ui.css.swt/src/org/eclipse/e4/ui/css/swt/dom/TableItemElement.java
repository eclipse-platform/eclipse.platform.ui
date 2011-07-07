/*******************************************************************************
 * Copyright (c) 2009 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.dom;

import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.widgets.TableItem;


/**
 * {@link CSSStylableElement} implementation which wrap SWT {@link TableItem}.
 * 
 */
public class TableItemElement extends ItemElement {
	
	public TableItemElement(TableItem tableItem, CSSEngine engine) {
		super(tableItem, engine);
	}

	public boolean isPseudoInstanceOf(String s) {
		if ("odd".equals(s)) {
			TableItem tableItem = getTableItem();
			int index = tableItem.getParent().indexOf(tableItem);
			return ((index & 1) == 1);

		}
		if ("even".equals(s)) {
			TableItem tableItem = getTableItem();
			int index = tableItem.getParent().indexOf(tableItem);
			return ((index & 1) == 0);
		}
		return super.isPseudoInstanceOf(s);
	}

	protected TableItem getTableItem() {
		return (TableItem) getNativeWidget();
	}
}
