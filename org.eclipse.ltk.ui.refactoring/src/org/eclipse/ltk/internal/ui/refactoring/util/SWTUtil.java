/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.resource.JFaceResources;


/**
 * Utility class for swt-related functions.
 *
 * @since 3.2
 */
public final class SWTUtil {

	public static int getButtonWidthHint(Button button) {
		button.setFont(JFaceResources.getDialogFont());
		return Math.max(new PixelConverter(button).convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH), button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	}

	public static void setButtonDimensionHint(Button button) {
		Assert.isNotNull(button);
		Object data= button.getLayoutData();
		if (data instanceof GridData) {
			((GridData) data).widthHint= getButtonWidthHint(button);
			((GridData) data).horizontalAlignment= GridData.FILL;
		}
	}

	/**
	 * Adds an accessibility listener returning the given fixed name.
	 *
	 * @param control the control to add the accessibility support to
	 * @param text the name
	 * 
	 * @since 3.5.100
	 */
	public static void setAccessibilityText(Control control, final String text) {
		control.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			public void getName(AccessibleEvent e) {
				if (e.childID == ACC.CHILDID_SELF) {
					e.result= text;
				}
			}
		});
	}
	
	private SWTUtil() {
		// Not for instantiation
	}
}
