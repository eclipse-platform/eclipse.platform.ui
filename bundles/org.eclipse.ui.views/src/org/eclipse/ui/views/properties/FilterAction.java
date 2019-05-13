/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

package org.eclipse.ui.views.properties;

import org.eclipse.ui.PlatformUI;

/**
 * This action hides or shows expert properties in the <code>PropertySheetViewer</code>.
 */
/*package*/class FilterAction extends PropertySheetAction {
	/**
	 * Create the Filter action. This action is used to show
	 * or hide expert properties.
	 *
	 * @param viewer the viewer
	 * @param name the name
	 */
	public FilterAction(PropertySheetViewer viewer, String name) {
		super(viewer, name);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IPropertiesHelpContextIds.FILTER_ACTION);
	}

	/**
	 * Toggle the display of expert properties.
	 */

	@Override
	public void run() {
		PropertySheetViewer ps = getPropertySheet();
		ps.deactivateCellEditor();
		if (isChecked()) {
			ps.showExpert();
		} else {
			ps.hideExpert();
		}
	}
}
