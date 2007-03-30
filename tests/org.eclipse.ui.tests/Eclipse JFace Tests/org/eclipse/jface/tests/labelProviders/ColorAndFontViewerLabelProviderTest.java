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

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.IViewerLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * ColorAndFontProviderTest is a test of a color and font provider that is an
 * IViewerLabelProvider.
 * 
 * @since 3.3
 * 
 */
public class ColorAndFontViewerLabelProviderTest extends CompositeLabelProviderTest {

	class ColorAndFontProvider extends LabelProvider implements IColorProvider,
			IFontProvider, IViewerLabelProvider {

		/**
		 * Create a new instance of the receiver.
		 */
		public ColorAndFontProvider() {
			super();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
		 */
		public Font getFont(Object element) {
			return font;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
		 */
		public Color getBackground(Object element) {
			return background;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
		 */
		public Color getForeground(Object element) {
			return foreground;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IViewerLabelProvider#updateLabel(org.eclipse.jface.viewers.ViewerLabel,
		 *      java.lang.Object)
		 */
		public void updateLabel(ViewerLabel label, Object element) {
			// Do nothing

		}

	}

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param name
	 */
	public ColorAndFontViewerLabelProviderTest(String name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.tests.viewers.ViewerTestCase#createViewer(org.eclipse.swt.widgets.Composite)
	 */
	protected StructuredViewer createViewer(Composite parent) {
		initializeColors(parent);
		final TableViewer v = new TableViewer(parent);
		v.setContentProvider(new LabelTableContentProvider());
		v.setLabelProvider(new ColorAndFontProvider());
		;
		v.getTable().setLinesVisible(true);
		return v;
	}

	/**
	 * Test that all of the colours and fonts from the label provider are
	 * applied.
	 */
	public void testColorsAndFonts() {
		Table table = (Table) fViewer.getControl();
		TableItem item = table.getItem(0);

		assertTrue("Background was not set", item.getBackground(0).equals(
				background));
		assertTrue("Foreground was not set", item.getForeground(0).equals(
				foreground));
		assertTrue("Font was not set", item.getFont(0).equals(font));

		Font oldFont = font;
		
		clearColors();
		fViewer.refresh(item.getData());

		Display display = table.getDisplay();
		assertTrue("Background was not cleared", item.getBackground(0).equals(
				display.getSystemColor(SWT.COLOR_LIST_BACKGROUND)));
		assertTrue("Foreground was not cleared", item.getForeground(0).equals(
				display.getSystemColor(SWT.COLOR_LIST_FOREGROUND)));
		assertFalse("Font was not cleared", item.getFont(0).getFontData()[0]
				.equals(oldFont.getFontData()[0]));

	}

	/**
	 * Clear the colors and fonts to null.
	 */
	private void clearColors() {
		background = null;
		foreground = null;
		font = null;

	}

}
