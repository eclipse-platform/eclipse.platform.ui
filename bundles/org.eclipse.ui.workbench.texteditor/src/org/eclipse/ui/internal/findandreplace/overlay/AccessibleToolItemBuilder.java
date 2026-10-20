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

	public AccessibleToolItemForActionBuilder withAction(FindReplaceOverlayAction action) {
		return new AccessibleToolItemForActionBuilder(this, action);
	}

	public class AccessibleToolItemForActionBuilder {
		private final AccessibleToolItemBuilder parentBuilder;

		private final FindReplaceOverlayAction action;

		private AccessibleToolItemForActionBuilder(AccessibleToolItemBuilder parentBuilder,
				FindReplaceOverlayAction action) {
			this.parentBuilder = parentBuilder;
			this.action = action;
		}

		public ToolItem build() {
			AccessibleToolItem accessibleToolItem = parentBuilder.buildAccessibleToolItem();
			accessibleToolItem.setAction(action);
			return accessibleToolItem.getToolItem();
		}
	}

	public AccessibleToolItemForSearchOptionBuilder withAction(FindReplaceOverlaySearchOptionAction action) {
		return new AccessibleToolItemForSearchOptionBuilder(this, action);
	}

	public class AccessibleToolItemForSearchOptionBuilder extends AccessibleToolItemForActionBuilder {
		private final FindReplaceOverlaySearchOptionAction searchOptionAction;

		private boolean invertSearchOption;

		private AccessibleToolItemForSearchOptionBuilder(AccessibleToolItemBuilder parentBuilder,
				FindReplaceOverlaySearchOptionAction searchOptionAction) {
			super(parentBuilder, searchOptionAction);
			this.searchOptionAction = searchOptionAction;
		}

		public AccessibleToolItemForSearchOptionBuilder displayInverted() {
			this.invertSearchOption = true;
			return this;
		}

		@Override
		public ToolItem build() {
			ToolItem toolItem = super.build();
			IFindReplaceLogic findReplaceLogic = searchOptionAction.getFindReplaceLogic();
			SearchOptions searchOption = searchOptionAction.getSearchOption();
			boolean initial = findReplaceLogic.isActive(searchOption);
			toolItem.setSelection(invertSearchOption ? !initial : initial);
			findReplaceLogic.addSearchOptionActivationChangedListener(searchOption, state -> {
				if (!toolItem.isDisposed()) {
					toolItem.setSelection(invertSearchOption ? !state : state);
				}
			});
			searchOptionAction.setAvailable(findReplaceLogic.isAvailable(searchOption));
			findReplaceLogic.addSearchOptionAvailabilityChangedListener(searchOption, searchOptionAction::setAvailable);
			return toolItem;
		}
	}

	public ToolItem build() {
		return buildAccessibleToolItem().getToolItem();
	}

	private AccessibleToolItem buildAccessibleToolItem() {
		AccessibleToolItem accessibleToolItem = accessibleToolBar.createToolItem(styleBits);
		if (image != null) {
			accessibleToolItem.setImage(image);
		}
		if (toolTipText != null) {
			accessibleToolItem.setToolTipText(toolTipText);
		}
		return accessibleToolItem;
	}

}
