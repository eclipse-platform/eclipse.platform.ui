/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator.extension;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.eclipse.ui.navigator.IDescriptionProvider;

public class TestLabelProvider extends LabelProvider implements
		ICommonLabelProvider, IDescriptionProvider, IColorProvider,
		IFontProvider {

	private FontData boldFontData = new FontData();

	private Font boldFont;
	
	private Color backgroundColor;

	public void init(ICommonContentExtensionSite aSite) {

		boldFontData.setStyle(SWT.BOLD);

		boldFont = new Font(Display.getDefault(), boldFontData);

		backgroundColor = new Color(Display.getDefault(), 100, 149, 237);
	}

	public Image getImage(Object element) {
		if (element instanceof TestExtensionTreeData)
			return PlatformUI.getWorkbench().getSharedImages().getImage(
					ISharedImages.IMG_OBJ_ELEMENT);
		return null;
	}

	public String getText(Object element) {
		if (element instanceof TestExtensionTreeData) {
			TestExtensionTreeData data = (TestExtensionTreeData) element;
			return data.getName();
		}
		return null;
	}

	public String getDescription(Object anElement) {
		if (anElement instanceof TestExtensionTreeData) {
			TestExtensionTreeData data = (TestExtensionTreeData) anElement;
			return "TestItem: " + data.getName();
		}
		return null;
	}

	public void restoreState(IMemento aMemento) {

	}

	public void saveState(IMemento aMemento) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	 */
	public Color getForeground(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
	 */
	public Color getBackground(Object element) {
		return backgroundColor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
	 */
	public Font getFont(Object element) {
		if (element instanceof TestExtensionTreeData) {
			TestExtensionTreeData data = (TestExtensionTreeData) element;
			if (data.getParent() != null
					&& data.getParent().getParent() == null)
				return boldFont;
		}
		return null;
	}

	public void dispose() {
		final Font f = boldFont;
		final Color c = backgroundColor;
		boldFont = null;
		backgroundColor = null;
		Display.getCurrent().timerExec(20, new Runnable(){
			public void run() {
				f.dispose();
				c.dispose();
			}
		});
	}

}
