/*******************************************************************************
 * Copyright (c) 2008, 2010 Versant Corp. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Alexander Kuppe (Versant Corp.) - https://bugs.eclipse.org/248103
 ******************************************************************************/

package org.eclipse.ui.views.properties;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.views.properties.PropertiesMessages;

/**
 * Pins the properties view instance to the current selection.
 * 
 * @since 3.4
 */
public class PinPropertySheetAction extends Action {

	/**
	 * Creates a new <code>PinPropertySheetAction</code>.
	 */
	public PinPropertySheetAction() {
		super(PropertiesMessages.Pin_text, IAction.AS_CHECK_BOX);

		setId(PinPropertySheetAction.class.getName()
				+ "#" + Long.toString(System.currentTimeMillis())); //$NON-NLS-1$
		setToolTipText(PropertiesMessages.Pin_toolTip);
		setImageDescriptor(WorkbenchImages
				.getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_PIN_EDITOR));
		setDisabledImageDescriptor(WorkbenchImages
				.getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_PIN_EDITOR_DISABLED));

		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IPropertiesHelpContextIds.PIN_ACTION);
	}
}
