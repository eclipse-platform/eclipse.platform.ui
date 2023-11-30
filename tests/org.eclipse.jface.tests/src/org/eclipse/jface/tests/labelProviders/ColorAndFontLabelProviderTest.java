/*******************************************************************************
 * Copyright (c) 2007, 2023 IBM Corporation and others.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.junit.Test;

/**
 * ColorAndFontProviderTest is a test of a color and font provider but not an
 * IViewerLabelProvider.
 *
 * @since 3.3
 */
public class ColorAndFontLabelProviderTest extends CompositeLabelProviderTest {

	class ColorAndFontProvider extends LabelProvider implements IColorProvider, IFontProvider {

		/**
		 * Create a new instance of the receiver.
		 */
		public ColorAndFontProvider() {
			super();
		}

		@Override
		public Font getFont(Object element) {
			return font;
		}

		@Override
		public Color getBackground(Object element) {
			return background;
		}

		@Override
		public Color getForeground(Object element) {
			return foreground;
		}

	}

	@Override
	protected StructuredViewer createViewer(Composite parent) {
		initializeColors(parent);
		final TableViewer v = new TableViewer(parent);
		v.setContentProvider(new LabelTableContentProvider());
		v.setLabelProvider(new ColorAndFontProvider());
		v.getTable().setLinesVisible(true);
		return v;
	}

	/**
	 * Test that all of the colours and fonts from the label provider are applied.
	 */
	@Test
	public void testColorsAndFonts() {
		Table table = (Table) fViewer.getControl();
		TableItem item = table.getItem(0);

		assertEquals("Background was not set", item.getBackground(0), background);
		assertEquals("Foreground was not set", item.getForeground(0), foreground);
		assertEquals("Font was not set", item.getFont(0), font);

		Font oldFont = font;

		clearColors();
		fViewer.refresh(item.getData());

		Display display = table.getDisplay();
		assertEquals("Background was not cleared", item.getBackground(0),
				display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		assertEquals("Foreground was not cleared", item.getForeground(0),
				display.getSystemColor(SWT.COLOR_LIST_FOREGROUND));
		assertNotEquals("Font was not cleared", item.getFont(0).getFontData()[0], oldFont.getFontData()[0]);

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
