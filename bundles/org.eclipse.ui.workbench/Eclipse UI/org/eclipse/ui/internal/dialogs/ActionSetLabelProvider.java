/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dialogs.DialogUtil;
import org.eclipse.ui.internal.registry.ActionSetCategory;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;

/**
 * Label provider for action sets in the ActionSetSelectionDialog.
 */
public class ActionSetLabelProvider extends LabelProvider {

	private String UNKNOWN = WorkbenchMessages.getString("ActionSetLabelProvider.Unknown"); //$NON-NLS-1$

	public ActionSetLabelProvider() {
		super();
	}
	
	public String getText(Object element) {
		String label = UNKNOWN;
		if (element instanceof ActionSetCategory)
			label = ((ActionSetCategory) element).getLabel();
		else if (element instanceof IActionSetDescriptor)
			label = ((IActionSetDescriptor) element).getLabel();
		return DialogUtil.removeAccel(label);
	}
}
