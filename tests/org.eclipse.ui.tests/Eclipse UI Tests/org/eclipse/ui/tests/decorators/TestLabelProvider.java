/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element) {
		if (image == null) {
			TestPlugin plugin = TestPlugin.getDefault();
			image = plugin.getImageDescriptor("anything.gif").createImage();
		}
		return image;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		return ((TestElement) element).name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
	 */
	@Override
	public Color getBackground(Object element) {
		
		int switchNumber = 0;
		if(element instanceof TreeElement)
			switchNumber = ((TreeElement) element).level;
		else
			switchNumber = ((TableElement) element).index%4;
		
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	 */
	@Override
	public Color getForeground(Object element) {
		
		int switchNumber = 0;
		if(element instanceof TreeElement)
			switchNumber = ((TreeElement) element).level;
		else
			switchNumber = ((TableElement) element).index%4;
		
		
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

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
	 */
	@Override
	public Font getFont(Object element) {
		return JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	@Override
	public void addListener(ILabelProviderListener listener) {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	@Override
	public void dispose() {
		if (image != null)
			image.dispose();

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	@Override
	public void removeListener(ILabelProviderListener listener) {
		

	}
}
