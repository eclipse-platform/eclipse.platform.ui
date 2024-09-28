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

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.layout.GridDataFactory;

class AccessibleToolItem {
	private final ToolItem toolItem;

	private FindReplaceOverlayAction action = new FindReplaceOverlayAction(null);

	AccessibleToolItem(Composite parent, int styleBits) {
		ToolBar toolbar = new ToolBar(parent, SWT.FLAT | SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.CENTER, SWT.CENTER).applyTo(toolbar);
		toolItem = new ToolItem(toolbar, styleBits);
		addToolItemTraverseListener(toolbar);
	}

	private void addToolItemTraverseListener(ToolBar parent) {
		parent.addTraverseListener(e -> {
			if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
				action.execute();
				e.doit = false;
			}
		});
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
		toolItem.setToolTipText(action.addShortcutHintToTooltipText(text));
	}

	void setOperation(Runnable operation, List<KeyStroke> shortcuts) {
		boolean isCheckbox = (toolItem.getStyle() & SWT.CHECK) != 0;
		if (isCheckbox) {
			action = new FindReplaceOverlayAction(() -> {
				toolItem.setSelection(!toolItem.getSelection());
				operation.run();
			});
		} else {
			action = new FindReplaceOverlayAction(operation);
		}
		action.addShortcuts(shortcuts);
		setToolTipText(toolItem.getToolTipText());
		toolItem.addSelectionListener(SelectionListener.widgetSelectedAdapter(__ -> operation.run()));
	}

	void registerActionShortcutsAtControl(Control control) {
		FindReplaceShortcutUtil.registerActionShortcutsAtControl(action, control);
	}

}
