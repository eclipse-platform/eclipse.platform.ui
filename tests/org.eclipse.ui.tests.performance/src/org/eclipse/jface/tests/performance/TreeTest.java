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

package org.eclipse.jface.tests.performance;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.3
 *
 */
public abstract class TreeTest extends ViewerTest {



	TreeViewer viewer;

	/**
	 * Create a new instance of the receiver.
	 * @param testName
	 * @param tagging
	 */
	public TreeTest(String testName, int tagging) {
		super(testName, tagging);
	}

	/**
	 * Create a new instance of the receiver.
	 * @param testName
	 */
	public TreeTest(String testName) {
		super(testName);
	}


	@Override
	protected StructuredViewer createViewer(Shell shell) {
		viewer = createTreeViewer(shell);
		viewer.setContentProvider(getContentProvider());
		viewer.setLabelProvider(getLabelProvider());
		viewer.setSorter(new ViewerSorter());
		viewer.setUseHashlookup(true);
		return viewer;
	}

	/**
	 * Create the tree viewer
	 * @return
	 */
	protected TreeViewer createTreeViewer(Shell shell) {
		return new TreeViewer(shell);
	}

	@Override
	protected Object getInitialInput() {
		return new TestTreeElement(0, null);
	}

	private IContentProvider getContentProvider() {
		return new ITreeContentProvider() {

			@Override
			public Object[] getChildren(Object parentElement) {
				TestTreeElement element = (TestTreeElement) parentElement;
				return element.children;
			}

			@Override
			public Object getParent(Object element) {
				return ((TestTreeElement) element).parent;
			}

			@Override
			public boolean hasChildren(Object element) {
				return ((TestTreeElement) element).children.length > 0;
			}

			@Override
			public Object[] getElements(Object inputElement) {
				return getChildren(inputElement);
			}

			@Override
			public void dispose() {
				// Do nothing here
			}

			@Override
			public void inputChanged(Viewer localViewer, Object oldInput,
					Object newInput) {
				// Do nothing here
			}

		};
	}

}
