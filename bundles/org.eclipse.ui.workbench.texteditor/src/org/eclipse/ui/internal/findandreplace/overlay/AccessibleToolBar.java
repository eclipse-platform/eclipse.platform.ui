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
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

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

	private List<ToolBar> toolBars = new ArrayList<>();

	public AccessibleToolBar(Composite parent) {
		super(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(0).spacing(0, 0).margins(0, 0).applyTo(this);
	}

	/**
	 * Creates a ToolItem handled by this ToolBar and returns it. Will add a
	 * KeyListener which will handle presses of "Enter".
	 *
	 * @param styleBits the StyleBits to apply to the created ToolItem
	 * @return a newly created ToolItem
	 */
	public ToolItem createToolItem(int styleBits) {
		ToolBar parent = new ToolBar(this, SWT.FLAT | SWT.HORIZONTAL);
		ToolItem toolItem = new ToolItem(parent, styleBits);

		addToolItemTraverseListener(parent, toolItem);

		((GridLayout) getLayout()).numColumns++;

		toolBars.add(parent);
		return toolItem;
	}

	private void addToolItemTraverseListener(ToolBar parent, ToolItem result) {
		parent.addTraverseListener(e -> {
			if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
				result.setSelection(!result.getSelection());
				e.doit = false;
			}
		});
	}

	@Override
	public void setBackground(Color color) {
		super.setBackground(color);
		for (ToolBar bar : toolBars) { // some ToolItems (like SWT.SEPARATOR) don't easily inherit the color from the
										// parent control.
			bar.setBackground(color);
		}
	}

}
