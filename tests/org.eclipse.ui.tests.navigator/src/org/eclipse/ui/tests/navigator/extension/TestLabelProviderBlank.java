/*******************************************************************************
 * Copyright (c) 2009, 2010 Oakland Software Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Oakland Software Incorporated - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.navigator.extension;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;

/**
 * @since 3.3
 *
 */
public class TestLabelProviderBlank extends TestStyledLabelProvider {

	public static TestLabelProviderBlank instance;

	@Override
	protected void initSubclass() {
		backgroundColor = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
		backgroundColorName = "Red";
		font = new Font(Display.getDefault(), boldFontData);
		image = PlatformUI.getWorkbench().getSharedImages().getImage(
				ISharedImages.IMG_OBJ_ADD);
		instance = this;
	}

	@Override
	public void init(ICommonContentExtensionSite aSite) {
		super.init(aSite);
		_blank = true;
	}

}
