/*******************************************************************************
 * Copyright (c) 2024 Enda O'Brien and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.views.markers.MarkerFieldFilterGroup;
import org.eclipse.ui.internal.views.markers.ScopeArea;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * CustomScopeArea is a custom implementation of ScopeArea that handles the
 * scope of the filter in FiltersConfigurationDialog.
 *
 * @since 3.5
 */

public class CustomScopeArea extends ScopeArea {

	/**
	 * Create a new instance of the receiver.
	 */
	public CustomScopeArea() {
		super();
	}

	@Override
	public void applyToGroup(MarkerFieldFilterGroup group) {
		group.setScope(scope);
	}

	@Override
	public void createContents(Composite parent) {

		buttons = new Button[5];

		buttons[MarkerFieldFilterGroup.ON_ANY] = createRadioButton(parent, MarkerMessages.filtersDialog_anyResource,
				MarkerFieldFilterGroup.ON_ANY);
		buttons[MarkerFieldFilterGroup.ON_SELECTED_ONLY] = createRadioButton(parent,
				MarkerMessages.filtersDialog_selectedResource, MarkerFieldFilterGroup.ON_SELECTED_ONLY);
	}

	@Override
	public String getTitle() {
		return MarkerMessages.filtersDialog_scopeTitle;
	}

	@Override
	public void initializeFromGroup(MarkerFieldFilterGroup group) {
		buttons[scope].setSelection(false);
		scope = group.getScope();
		buttons[scope].setSelection(true);
	}

}
