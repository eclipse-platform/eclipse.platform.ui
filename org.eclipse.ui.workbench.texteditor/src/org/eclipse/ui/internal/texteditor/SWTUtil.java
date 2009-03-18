/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.resource.JFaceResources;


/**
 * Utility class to simplify access to some SWT resources.
 *
 * @since 3.3
 */
public class SWTUtil {

	/**
	 * The default visible item count for {@link Combo}s.
	 * Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=245569 .
	 *
	 * @see Combo#setVisibleItemCount(int)
	 *
	 * @since 3.5
	 */
	public static final int COMBO_VISIBLE_ITEM_COUNT= 30;
	
	/**
	 * Returns a width hint for the given button.
	 *
	 * @param button the button
	 * @return the width hint for the button
	 */
	public static int getButtonWidthHint(Button button) {
		button.setFont(JFaceResources.getDialogFont());
		PixelConverter converter= new PixelConverter(button);
		int widthHint= converter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		return Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	}

	/**
	 * Sets width and height hint for the button control.
	 * <b>Note:</b> This is a NOP if the button's layout data is not
	 * an instance of <code>GridData</code>.
	 *
	 * @param button	the button for which to set the dimension hint
	 */
	public static void setButtonDimensionHint(Button button) {
		Assert.isNotNull(button);
		Object gd= button.getLayoutData();
		if (gd instanceof GridData) {
			((GridData)gd).widthHint= getButtonWidthHint(button);
			((GridData)gd).horizontalAlignment = GridData.FILL;
		}
	}

	/**
	 * Sets the default visible item count for {@link Combo}s.
	 * Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=7845 .
	 *
	 * @param combo the combo
	 *
	 * @see Combo#setVisibleItemCount(int)
	 * @see #COMBO_VISIBLE_ITEM_COUNT
	 *
	 * @since 3.5
	 */
	public static void setDefaultVisibleItemCount(Combo combo) {
		combo.setVisibleItemCount(COMBO_VISIBLE_ITEM_COUNT);
	}
}
