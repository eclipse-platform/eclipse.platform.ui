/*******************************************************************************
 * Copyright (c) 2006, 2014 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 414565
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Explore New API: JFace custom tooltips drawing.
 *
 * @since 3.3M2
 */
public class Snippet015CustomTooltipsForTree {
	private static class MyContentProvider implements ITreeContentProvider {

		private static final String ROOT = "Root";

		@Override
		public Object[] getElements(Object inputElement) {
			return new Object[]{ROOT};
		}

		@Override
		public void dispose() {

		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {


		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if(parentElement.equals(ROOT))
				return new String[] { "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten" };
			return new Object[0];
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return element.equals(ROOT);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final Display display = new Display ();
		Shell shell = new Shell (display);
	    shell.setLayout(new FillLayout());

	    TreeViewer v = new TreeViewer(shell,SWT.FULL_SELECTION);
	    v.getTree().setLinesVisible(true);
	    v.getTree().setHeaderVisible(true);
	    ColumnViewerToolTipSupport.enableFor(v);

	    v.setContentProvider(new MyContentProvider());

	    CellLabelProvider labelProvider = new CellLabelProvider() {

			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ViewerLabelProvider#getTooltipText(java.lang.Object)
			 */
			@Override
			public String getToolTipText(Object element) {
				return "Tooltip (" + element + ")";
			}

			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ViewerLabelProvider#getTooltipShift(java.lang.Object)
			 */
			@Override
			public Point getToolTipShift(Object object) {
				return new Point(5,5);
			}

			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ViewerLabelProvider#getTooltipDisplayDelayTime(java.lang.Object)
			 */
			@Override
			public int getToolTipDisplayDelayTime(Object object) {
				return 2000;
			}

			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ViewerLabelProvider#getTooltipTimeDisplayed(java.lang.Object)
			 */
			@Override
			public int getToolTipTimeDisplayed(Object object) {
				return 5000;
			}

			@Override
			public void update(ViewerCell cell) {
				cell.setText(cell.getElement().toString());

			}
	    };


	    v.setLabelProvider(labelProvider);
	    v.setInput("");

	    shell.setSize(200,200);
	    shell.open ();

	    while (!shell.isDisposed()) {
	        if (!display.readAndDispatch ()) {
	        	display.sleep ();
	        }
	    }

	    display.dispose ();
	}

}
