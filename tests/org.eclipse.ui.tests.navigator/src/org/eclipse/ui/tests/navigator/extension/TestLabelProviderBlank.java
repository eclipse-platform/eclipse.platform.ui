/*******************************************************************************
 * Copyright (c) 2009 Oakland Software Incorporated and others.
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;

/**
 * @since 3.3
 *
 */
public class TestLabelProviderBlank extends TestLabelProvider {

	public void init(ICommonContentExtensionSite aSite) {
		super.init(aSite);
		_blank = true;
	}
	
	public static Color backgroundColor = Display.getCurrent().getSystemColor(
			SWT.COLOR_RED);
	
	public static Color getTestColor() {
		return backgroundColor;
	}
	public Color getBackground(Object element) {
		return backgroundColor;
	}

	public String getColorName() {
		return "Red";
	}

}
