/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.dom;

import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTImageHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.w3c.dom.Node;

/**
 * {@link CSSStylableElement} implementation which wrap SWT {@link TableItem}.
 *
 */
public class ToolItemElement extends ItemElement {

	private boolean isSelected = false;

	boolean dynamicEnabled = Boolean.getBoolean("org.eclipse.e4.ui.css.dynamic");

	private SelectionListener selectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (!e.widget.isDisposed()) {
				ToolItemElement.this.isSelected = getToolItem().getSelection();
				doApplyStyles();
			}
		}
	};

	public ToolItemElement(ToolItem toolItem, CSSEngine engine) {
		super(toolItem, engine);

		//		if (!dynamicEnabled) {
		//			return;
		//		}

		//		toolItem.addSelectionListener(selectionListener);
	}

	public ToolItem getToolItem() {
		return (ToolItem) getNativeWidget();
	}

	@Override
	public Node getParentNode() {
		ToolItem item = getToolItem();
		ToolBar parent = item.getParent();
		if (parent != null) {
			return getElement(parent);
		}
		return null;
	}

	@Override
	public Node item(int index) {
		ToolItem item = getToolItem();
		if ((item.getStyle() & SWT.SEPARATOR) == SWT.SEPARATOR) {
			Object control = item.getControl();
			if (control != null) {
				return getElement(control);
			}
		}
		return null;
	}

	@Override
	public int getLength() {
		ToolItem item = getToolItem();
		return (item.getStyle() & SWT.SEPARATOR) == SWT.SEPARATOR
				&& item.getControl() != null ? 1 : 0;
	}
	//
	//	@Override
	//	public boolean isPseudoInstanceOf(String s) {
	//		if ("checked".equalsIgnoreCase(s)) {
	//			return this.isSelected;
	//		}
	//		return super.isPseudoInstanceOf(s);
	//	}


	@Override
	public void reset() {
		CSSSWTImageHelper.restoreDefaultImage(getToolItem());
		getToolItem().setBackground(null);
		getToolItem().setForeground(null);
		super.reset();
	}
}
