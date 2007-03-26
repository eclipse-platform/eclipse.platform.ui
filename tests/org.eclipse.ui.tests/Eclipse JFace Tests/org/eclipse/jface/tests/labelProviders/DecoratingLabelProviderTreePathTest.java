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
public class DecoratingLabelProviderTreePathTest extends
		CompositeLabelProviderTest {

	class TreePathTestLabelProvider extends LabelProvider implements
			IColorProvider, IFontProvider, ITreePathLabelProvider {
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
		 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
		 */
		public Color getBackground(Object element) {
			return background;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
		 */
		public Font getFont(Object element) {
			return font;
		}

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.tests.labelProviders.DecoratingLabelProviderTreeTest#createViewer(org.eclipse.swt.widgets.Composite)
	 */
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
	 * Test that all of the colours and fonts from the label provider are
	 * applied.
	 */
	public void testColorsAndFonts() {
		Tree tree = (Tree) fViewer.getControl();
		TreeItem item = tree.getItem(0);

		assertTrue("Background was not set", item.getBackground(0).equals(
				background));
		assertTrue("Foreground was not set", item.getForeground(0).equals(
				foreground));
		assertTrue("Font was not set", item.getFont(0).equals(font));

	}
}
