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

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITreePathLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * DecoratingLabelProviderTreePathTest is the tree path version of the
 * DecoratingLabelProviderTreeTest.
 *
 * @since 3.3
 *
 */
public class DecoratingLabelProviderTreePathTest extends CompositeLabelProviderTest {

	class TreePathTestLabelProvider extends LabelProvider
			implements IColorProvider, IFontProvider, ITreePathLabelProvider {
		@Override
		public Color getForeground(Object element) {
			return foreground;
		}

		@Override
		public Color getBackground(Object element) {
			return background;
		}

		@Override
		public Font getFont(Object element) {
			return font;
		}

		@Override
		public void updateLabel(ViewerLabel label, TreePath elementPath) {
			label.setText(getText(elementPath.getLastSegment()));
		}
	}

	/**
	 * Create a new instance of the receiver.
	 *
	 * @param name
	 */
	public DecoratingLabelProviderTreePathTest(String name) {
		super(name);

	}

	@Override
	protected StructuredViewer createViewer(Composite parent) {

		initializeColors(parent);
		final TreeViewer v = new TreeViewer(parent);
		v.setLabelProvider(new TreePathTestLabelProvider());
		v.setContentProvider(new TestTreeContentProvider());

		v.setLabelProvider(new TreePathTestLabelProvider());

		v.getTree().setLinesVisible(true);
		return v;

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
