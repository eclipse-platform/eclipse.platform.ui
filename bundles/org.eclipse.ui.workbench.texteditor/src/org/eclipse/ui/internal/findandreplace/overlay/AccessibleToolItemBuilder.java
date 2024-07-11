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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.ToolItem;

import org.eclipse.jface.bindings.keys.KeyStroke;

/**
 * Builder for ToolItems for {@link AccessibleToolBar}.
 */
class AccessibleToolItemBuilder {
	private final AccessibleToolBar accessibleToolBar;
	private int styleBits = SWT.NONE;
	private Image image;
	private String toolTipText;
	private List<KeyStroke> shortcuts = Collections.emptyList();
	private Runnable operation;

	public AccessibleToolItemBuilder(AccessibleToolBar accessibleToolBar) {
		this.accessibleToolBar = Objects.requireNonNull(accessibleToolBar);
	}

	public AccessibleToolItemBuilder withStyleBits(int newStyleBits) {
		this.styleBits = newStyleBits;
		return this;
	}

	public AccessibleToolItemBuilder withImage(Image newImage) {
		this.image = newImage;
		return this;
	}

	public AccessibleToolItemBuilder withToolTipText(String newToolTipText) {
		this.toolTipText = newToolTipText;
		return this;
	}

	public AccessibleToolItemBuilder withShortcuts(List<KeyStroke> newShortcuts) {
		this.shortcuts = newShortcuts;
		return this;
	}

	public AccessibleToolItemBuilder withOperation(Runnable newOperation) {
		this.operation = newOperation;
		return this;
	}

	public ToolItem build() {
		AccessibleToolItem accessibleToolItem = accessibleToolBar.createToolItem(styleBits);
		if (image != null) {
			accessibleToolItem.setImage(image);
		}
		if (toolTipText != null) {
			accessibleToolItem.setToolTipText(toolTipText);
		}
		if (operation != null) {
			accessibleToolItem.setOperation(operation, shortcuts);
		}

		return accessibleToolItem.getToolItem();
	}
}
