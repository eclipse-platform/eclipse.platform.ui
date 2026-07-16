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

	private String baseToolTipText;


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
		this.baseToolTipText = text;
		toolItem.setToolTipText(text);
	}

	void setAction(FindReplaceOverlayAction action) {
		setToolTipText(toolItem.getToolTipText());
		toolItem.addSelectionListener(SelectionListener.widgetSelectedAdapter(__ -> action.execute()));
		action.addShortcutHintListener(hint -> {
			if (!toolItem.isDisposed()) {
				String tooltipWithHint = baseToolTipText;
				if (!hint.isEmpty()) {
					tooltipWithHint += " (" + hint + ")"; //$NON-NLS-1$//$NON-NLS-2$
				}
				toolItem.setToolTipText(tooltipWithHint);
			}
		});
	}

}
