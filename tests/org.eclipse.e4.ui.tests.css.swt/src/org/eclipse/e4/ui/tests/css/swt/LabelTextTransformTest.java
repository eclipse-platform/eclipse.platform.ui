/*******************************************************************************
 * Copyright (c) 2009, 2014 Remy Chi Jian Suen and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Remy Chi Jian Suen <remy.suen@gmail.com> - initial API and implementation
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 443094
 ******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class LabelTextTransformTest extends TextTransformTest {

	@Override
	protected Control createControl(Composite parent) {
		return new Label(parent, SWT.LEAD);
	}

	@Override
	protected String getWidgetName() {
		return "Label";
	}

	@Override
	protected String getText(Control control) {
		return ((Label) control).getText();
	}

	@Override
	protected void setText(Control control, String string) {
		((Label) control).setText(string);
	}
}
