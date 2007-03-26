/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.labelProviders;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

/**
 * TestColorAndFontLabelProvider is a simple label provider that uses fonts and
 * colors.
 * @since 3.3
 *
 */
public class TestColorAndFontLabelProvider extends LabelProvider implements
		IColorProvider, ILabelProvider {
	private final Display fDisplay;

	public TestColorAndFontLabelProvider(Display display) {
		fDisplay= display;
	}

	public Color getBackground(Object element) {
		return fDisplay.getSystemColor(SWT.COLOR_RED);
	}

	public Color getForeground(Object element) {
		return fDisplay.getSystemColor(SWT.COLOR_BLUE);
	}
	
	public Font getFont(Object element) {
		return JFaceResources.getFontRegistry().getItalic(JFaceResources.BANNER_FONT);
	}
}
