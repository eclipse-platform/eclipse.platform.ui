/*******************************************************************************
 * Copyright (c) 2009, 2014 Remy Chi Jian Suen and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Remy Chi Jian Suen <remy.suen@gmail.com> - initial API and implementation
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 443094
 ******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ButtonTextTransformTest extends TextTransformTest {

	@Override
	protected Control createControl(Composite parent) {
		return new Button(parent, SWT.PUSH);
	}

	@Override
	protected String getWidgetName() {
		return "Button";
	}

	@Override
	protected String getText(Control control) {
		return ((Button) control).getText();
	}

	@Override
	protected void setText(Control control, String string) {
		((Button) control).setText(string);
	}
}
