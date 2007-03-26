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

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.tests.viewers.TestElement;
import org.eclipse.jface.tests.viewers.ViewerTestCase;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Control;

/**
 * CompositeLabelProviderTest is the abstract superclass of the LabelProvider
 * tests that use multiple label provider suppliers.
 * 
 * @since 3.3
 * 
 */
public abstract class CompositeLabelProviderTest extends ViewerTestCase {

	class LabelTableContentProvider implements IStructuredContentProvider {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return fRootElement.getChildren();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}

	}

	Color background;
	Color foreground;
	Font font;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param name
	 */
	public CompositeLabelProviderTest(String name) {
		super(name);
	}

	/**
	 * Initialize the colors used by the receiver.
	 * 
	 * @param parent
	 */
	void initializeColors(Control parent) {
		background = parent.getDisplay().getSystemColor(SWT.COLOR_RED);
		foreground = parent.getDisplay().getSystemColor(SWT.COLOR_BLUE);
		font = JFaceResources.getBannerFont();
	}

	class TestTreeContentProvider implements ITreeContentProvider {

		public Object[] getChildren(Object parentElement) {
			return ((TestElement) parentElement).getChildren();
		}

		public Object getParent(Object element) {
			return ((TestElement) element).getContainer();
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		public Object[] getElements(Object inputElement) {
			return fRootElement.getChildren();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}
}
