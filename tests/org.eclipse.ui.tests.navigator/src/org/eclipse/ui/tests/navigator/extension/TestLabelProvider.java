/*******************************************************************************
 * Copyright (c) 2003, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator.extension;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.eclipse.ui.navigator.IDescriptionProvider;

public abstract class TestLabelProvider extends LabelProvider implements
		ICommonLabelProvider, IDescriptionProvider, IColorProvider,
		IFontProvider {

	protected static FontData boldFontData = new FontData();

	public Color backgroundColor;
	public String backgroundColorName;

	public Image image;

	public Font font;

	private Font boldFont;

	public boolean _blank;
	public boolean _null;

	public static boolean _throw;
	
	public static void resetTest() {
		_throw = false;
	}

	static {
		boldFontData.setStyle(SWT.BOLD);
	}

	@Override
	public void init(ICommonContentExtensionSite aSite) {
		boldFont = new Font(Display.getDefault(), boldFontData);
		initSubclass();
	}

	protected void initSubclass() {

	}

	public static Color toForegroundColor(Color backColor) {
		RGB rgb = backColor.getRGB();
		RGB newRgb = new RGB(rgb.blue, rgb.red, rgb.green);
		return new Color(Display.getCurrent(), newRgb);
	}

	public Color getTestColor() {
		return backgroundColor;
	}

	public String getColorName() {
		return backgroundColorName;
	}

	@Override
	public Image getImage(Object element) {
		if (_throw)
			throw new RuntimeException("Throwing...");
		if (element instanceof TestExtensionTreeData)
			return PlatformUI.getWorkbench().getSharedImages().getImage(
					ISharedImages.IMG_OBJ_ELEMENT);
		return image;
	}

	@Override
	public String getText(Object element) {
		if (_throw)
			throw new RuntimeException("Throwing...");
		if (_blank)
			return "";
		if (_null)
			return null;

		if (element instanceof TestExtensionTreeData) {
			TestExtensionTreeData data = (TestExtensionTreeData) element;
			return getColorName() + data.getName();
		}
		if (element instanceof IResource) {
			return getColorName() + ((IResource) element).getName();
		}
		return element.toString();
	}

	@Override
	public String getDescription(Object anElement) {
		if (_throw)
			throw new RuntimeException("Throwing...");
		if (anElement instanceof TestExtensionTreeData) {
			TestExtensionTreeData data = (TestExtensionTreeData) anElement;
			return "TestItem: " + data.getName();
		}
		return null;
	}

	@Override
	public void restoreState(IMemento aMemento) {
		if (_throw)
			throw new RuntimeException("Throwing...");

	}

	@Override
	public void saveState(IMemento aMemento) {
		if (_throw)
			throw new RuntimeException("Throwing...");

	}

	@Override
	public Color getForeground(Object element) {
		if (_throw)
			throw new RuntimeException("Throwing...");
		return toForegroundColor(getTestColor());
	}

	@Override
	public Color getBackground(Object element) {
		if (_throw)
			throw new RuntimeException("Throwing...");
		return getTestColor();
	}

	@Override
	public Font getFont(Object element) {
		if (_throw)
			throw new RuntimeException("Throwing...");
		if (element instanceof TestExtensionTreeData) {
			TestExtensionTreeData data = (TestExtensionTreeData) element;
			if (data.getParent() != null
					&& data.getParent().getParent() == null)
				return boldFont;
		}
		return font;
	}

	@Override
	public void dispose() {
		if (_throw)
			throw new RuntimeException("Throwing...");
		boldFont = null;
	}

}
