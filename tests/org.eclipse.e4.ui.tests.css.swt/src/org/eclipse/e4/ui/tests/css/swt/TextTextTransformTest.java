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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class TextTextTransformTest extends TextTransformTest {

	@Override
	protected Control createControl(Composite parent) {
		return new Text(parent, SWT.SINGLE);
	}

	@Override
	protected String getWidgetName() {
		return "Text";
	}

	@Override
	protected String getText(Control control) {
		return ((Text) control).getText();
	}

	@Override
	protected void setText(Control control, String string) {
		((Text) control).setText(string);
	}
}
