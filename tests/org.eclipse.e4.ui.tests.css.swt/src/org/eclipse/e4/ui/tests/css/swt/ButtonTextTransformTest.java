/*******************************************************************************
 * Copyright (c) 2009 Remy Chi Jian Suen and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Remy Chi Jian Suen <remy.suen@gmail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
 
public class ButtonTextTransformTest extends TextTransformTest {
		
	protected Control createControl(Composite parent) {
		return new Button(parent, SWT.PUSH);
	}
	
	protected String getWidgetName() {
		return "Button";
	}

	protected String getText(Control control) {
		return ((Button) control).getText();
	}
	
	protected void setText(Control control, String string) {
		((Button) control).setText(string);
	}
}
