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

import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @since 3.3
 *
 */
public class DecoratingLabelProviderTreeTest extends CompositeLabelProviderTest {

	class IntListLabelProvider extends LabelProvider implements IColorProvider, IFontProvider {

		public IntListLabelProvider() {
		}

		@Override
		public Color getBackground(Object element) {
			return background;
		}

		@Override
		public Color getForeground(Object element) {
			return foreground;
		}

		@Override
		public Font getFont(Object element) {
			return font;
		}
	}

	/**
	 * @param name
	 */
	public DecoratingLabelProviderTreeTest(String name) {
		super(name);
	}

	@Override
	protected StructuredViewer createViewer(Composite parent) {

		initializeColors(parent);
		StructuredViewer viewer = new TreeViewer(parent);
		viewer.setContentProvider(new TestTreeContentProvider());

		viewer.setLabelProvider(new DecoratingLabelProvider(new IntListLabelProvider(), null));
		return viewer;
	}

	/**
	 * Test that all of the colours and fonts from the label provider are applied.
	 */
	public void testColorsAndFonts() {
		Tree tree = (Tree) fViewer.getControl();
		TreeItem item = tree.getItem(0);

		assertEquals("Background was not set", item.getBackground(0), background);
		assertEquals("Foreground was not set", item.getForeground(0), foreground);
		assertEquals("Font was not set", item.getFont(0), font);

	}

}
