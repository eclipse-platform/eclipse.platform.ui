/*******************************************************************************
 * Copyright (c) 2024 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vector Informatik GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.findandreplace.overlay;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.layout.GridLayoutFactory;

/**
 * This class wraps the ToolBar to make it possible to use tabulator-keys to
 * navigate between the buttons of a ToolBar. For this, we simulate a singular
 * ToolBar by putting each ToolItem into it's own ToolBar and composing them
 * into a Composite. Since the "Enter" keypress could not previously trigger
 * activation behavior, we listen for it manually and send according events if
 * necessary.
 */
class AccessibleToolBar extends Composite {

	private final List<AccessibleToolItem> accessibleToolItems = new ArrayList<>();

	private final GridLayout layout;

	public AccessibleToolBar(Composite parent) {
		super(parent, SWT.NONE);
		this.layout = GridLayoutFactory.fillDefaults().numColumns(0).spacing(0, 0).margins(1, 1).create();
		this.setLayout(layout);
	}

	/**
	 * Creates a ToolItem handled by this ToolBar and returns it. Will add a
	 * KeyListener which will handle presses of "Enter".
	 *
	 * @param styleBits the StyleBits to apply to the created ToolItem
	 * @return a newly created ToolItem
	 */
	public AccessibleToolItem createToolItem(int styleBits) {
		AccessibleToolItem toolItem = new AccessibleToolItem(this, styleBits);
		accessibleToolItems.add(toolItem);
		layout.numColumns++;
		return toolItem;
	}

	@Override
	public void setBackground(Color color) {
		super.setBackground(color);
		// some ToolItems (like SWT.SEPARATOR) don't easily inherit the color from the
		// parent control
		for (AccessibleToolItem item : accessibleToolItems) {
			item.setBackground(color);
		}
	}

	void registerActionShortcutsAtControl(Control control) {
		for (AccessibleToolItem item : accessibleToolItems) {
			item.registerActionShortcutsAtControl(control);
		}
	}

	Control getFirstControl() {
		Control[] children = getChildren();
		return children.length == 0 ? null : getChildren()[0];
	}

}
