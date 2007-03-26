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

	class IntListLabelProvider extends LabelProvider implements IColorProvider,
			IFontProvider {

		public IntListLabelProvider() {
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
		 * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
		 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.tests.viewers.ViewerTestCase#createViewer(org.eclipse.swt.widgets.Composite)
	 */
	protected StructuredViewer createViewer(Composite parent) {

		initializeColors(parent);
		StructuredViewer viewer = new TreeViewer(parent);
		viewer.setContentProvider(new TestTreeContentProvider());

		viewer.setLabelProvider(new DecoratingLabelProvider(
				new IntListLabelProvider(), null));
		return viewer;
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
