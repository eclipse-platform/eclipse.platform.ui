/*******************************************************************************
 * Copyright (c) 2024 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.internal.findandreplace.overlay;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import org.eclipse.jface.layout.GridDataFactory;

class AccessibleToolItem {
	private final ToolItem toolItem;

	private FindReplaceOverlayAction action;

	AccessibleToolItem(Composite parent, int styleBits) {
		ToolBar toolbar = new ToolBar(parent, SWT.FLAT | SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.CENTER, SWT.CENTER).applyTo(toolbar);
		toolItem = new ToolItem(toolbar, styleBits);
	}

	ToolItem getToolItem() {
		return toolItem;
	}

	void setBackground(Color color) {
		toolItem.getParent().setBackground(color);
	}

	void setImage(Image image) {
		toolItem.setImage(image);
	}

	void setToolTipText(String text) {
		toolItem.setToolTipText(action != null ? action.addShortcutHintToTooltipText(text) : text);
	}

	void setAction(FindReplaceOverlayAction newAction) {
		this.action = newAction;
		setToolTipText(toolItem.getToolTipText());
		toolItem.addSelectionListener(SelectionListener.widgetSelectedAdapter(__ -> action.execute()));
	}

}
