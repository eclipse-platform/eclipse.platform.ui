/*******************************************************************************
 * Copyright (c) 2019 Stefan Winkler and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stefan Winkler <stefan@winklerweb.net> - Initial contribution (bug 178019)
 *******************************************************************************/
package org.eclipse.ui.tests.navigator.extension;

import org.eclipse.jface.viewers.IToolTipProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

/**
 * @since 3.3
 */
public class TestLabelProviderTooltips extends TestLabelProvider implements IToolTipProvider {

	@Override
	protected void initSubclass() {
		backgroundColor = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
		backgroundColorName = "Black";
	}
	@Override
	public String getToolTipText(Object element) {
		return "ToolTip " + getText(element);
	}
}
