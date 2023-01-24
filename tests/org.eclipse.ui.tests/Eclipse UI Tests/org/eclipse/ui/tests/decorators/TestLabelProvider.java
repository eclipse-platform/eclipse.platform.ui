/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.tests.decorators;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.tests.TestPlugin;

/**
 * TestTreeLabelProvider is the lable provider for the tree
 * decorator test.
 */
public class TestLabelProvider implements ILabelProvider, IColorProvider, IFontProvider {

	Image image;

	@Override
	public Image getImage(Object element) {
		if (image == null) {
			TestPlugin plugin = TestPlugin.getDefault();
			image = plugin.getImageDescriptor("anything.gif").createImage();
		}
		return image;
	}

	@Override
	public String getText(Object element) {
		return ((TestElement) element).name;
	}

	@Override
	public Color getBackground(Object element) {

		int switchNumber = 0;
		if (element instanceof TreeElement treeElem) {
			switchNumber = treeElem.level;
		} else {
			switchNumber = ((TableElement) element).index%4;
		}

		switch (switchNumber) {
		case 0:
			return Display.getDefault().getSystemColor(SWT.COLOR_YELLOW);
		case 1:
			return Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
		case 2:
			return Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN);
		case 3:
			return Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
		default:
			break;
		}

		return null;

	}

	@Override
	public Color getForeground(Object element) {

		int switchNumber = 0;
		if (element instanceof TreeElement treeElem) {
			switchNumber = treeElem.level;
		} else {
			switchNumber = ((TableElement) element).index%4;
		}


		switch (switchNumber) {
		case 0:
			return Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED);
		case 1:
			return Display.getDefault().getSystemColor(SWT.COLOR_RED);
		case 2:
			return Display.getDefault().getSystemColor(SWT.COLOR_CYAN);
		case 3:
			return Display.getDefault().getSystemColor(SWT.COLOR_YELLOW);
		default:
			break;
		}

		return null;
	}

	@Override
	public Font getFont(Object element) {
		return JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT);
	}

	@Override
	public void addListener(ILabelProviderListener listener) {

	}

	@Override
	public void dispose() {
		if (image != null) {
			image.dispose();
		}

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {


	}
}
