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
 * This action resets the <code>PropertySheetViewer</code> values back
 * to the default values.
 *
 * [Issue: should listen for selection changes in the viewer and set enablement]
 */
/*package*/class DefaultsAction extends PropertySheetAction {
	/**
	 * Create the Defaults action. This action is used to set
	 * the properties back to their default values.
	 *
	 * @param viewer the viewer
	 * @param name the name
	 */
	public DefaultsAction(PropertySheetViewer viewer, String name) {
		super(viewer, name);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IPropertiesHelpContextIds.DEFAULTS_ACTION);
	}

	/**
	 * Reset the properties to their default values.
	 */
	@Override
	public void run() {
		getPropertySheet().deactivateCellEditor();
		getPropertySheet().resetProperties();
	}
}
