/*******************************************************************************
 * Copyright (c) 2007, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.labelProviders;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * TestColorAndFontLabelProvider is a simple label provider that uses fonts and
 * colors.
 * @since 3.3
 *
 */
public class TestColorAndFontLabelProvider extends LabelProvider implements
		IColorProvider {
	private final Display fDisplay;

	public TestColorAndFontLabelProvider(Display display) {
		fDisplay= display;
	}

	@Override
	public Color getBackground(Object element) {
		return fDisplay.getSystemColor(SWT.COLOR_RED);
	}

	@Override
	public Color getForeground(Object element) {
		return fDisplay.getSystemColor(SWT.COLOR_BLUE);
	}
}
