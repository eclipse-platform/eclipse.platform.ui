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

import java.util.Objects;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.ToolItem;

import org.eclipse.ui.internal.findandreplace.IFindReplaceLogic;
import org.eclipse.ui.internal.findandreplace.SearchOptions;

/**
 * Builder for ToolItems for {@link AccessibleToolBar}.
 */
class AccessibleToolItemBuilder {
	private final AccessibleToolBar accessibleToolBar;
	private int styleBits = SWT.NONE;
	private Image image;
	private String toolTipText;
	private FindReplaceOverlayAction action;
	private SearchOptions searchOption;
	private IFindReplaceLogic findReplaceLogic;
	private boolean invertSearchOption;

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

	public AccessibleToolItemBuilder withAction(FindReplaceOverlayAction newAction) {
		this.action = newAction;
		return this;
	}

	/**
	 * Binds a {@link SearchOptions} value to this item. When built, the item's
	 * selection state is initialized from the logic's current activation state and
	 * kept in sync automatically. The item's enabled state is also initialized from
	 * and kept in sync with the option's availability.
	 */
	public AccessibleToolItemBuilder withSearchOption(SearchOptions option, IFindReplaceLogic logic) {
		this.searchOption = option;
		this.findReplaceLogic = logic;
		this.invertSearchOption = false;
		return this;
	}

	/**
	 * Like {@link #withSearchOption(SearchOptions, IFindReplaceLogic)} but inverts
	 * the selection mapping: the item is selected when the option is
	 * <em>inactive</em>. Useful for options like {@link SearchOptions#GLOBAL} where
	 * a "search in selection" button should be selected when searching globally is
	 * turned off.
	 */
	public AccessibleToolItemBuilder withInvertedSearchOption(SearchOptions option, IFindReplaceLogic logic) {
		this.searchOption = option;
		this.findReplaceLogic = logic;
		this.invertSearchOption = true;
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
		if (action != null) {
			accessibleToolItem.setAction(action);
		}
		ToolItem toolItem = accessibleToolItem.getToolItem();
		if (searchOption != null) {
			boolean initial = findReplaceLogic.isActive(searchOption);
			toolItem.setSelection(invertSearchOption ? !initial : initial);
			findReplaceLogic.addSearchOptionActivationChangedListener(searchOption, state -> {
				if (!toolItem.isDisposed()) {
					toolItem.setSelection(invertSearchOption ? !state : state);
				}
			});
			toolItem.setEnabled(findReplaceLogic.isAvailable(searchOption));
			findReplaceLogic.addSearchOptionAvailabilityChangedListener(searchOption, available -> {
				if (!toolItem.isDisposed()) {
					toolItem.setEnabled(available);
				}
			});
		}
		return toolItem;
	}
}
