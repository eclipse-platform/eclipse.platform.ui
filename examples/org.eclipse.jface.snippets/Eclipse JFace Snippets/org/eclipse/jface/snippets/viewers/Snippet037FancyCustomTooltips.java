/*******************************************************************************
 * Copyright (c) 2006, 2014 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *     IBM - Improvement for Bug 159625 [Snippets] Update Snippet011CustomTooltips to reflect new API
 *     Lars Vogel (lars.vogel@gmail.com) - Bug 413427
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

/**
 * Explore New API: JFace custom tooltips drawing.
 *
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 * @since 3.3
 */
public class Snippet037FancyCustomTooltips {
	private static class MyContentProvider implements
			IStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			return new String[] { "one", "two", "three", "four", "five", "six",
					"seven", "eight", "nine", "ten" };
		}

		@Override
		public void dispose() {

		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}
	}

	private static class FancyToolTipSupport extends ColumnViewerToolTipSupport {

		protected FancyToolTipSupport(ColumnViewer viewer, int style,
				boolean manualActivation) {
			super(viewer, style, manualActivation);
		}


		@Override
		protected Composite createToolTipContentArea(Event event,
				Composite parent) {
			Composite comp = new Composite(parent,SWT.NONE);
			GridLayout l = new GridLayout(1,false);
			l.horizontalSpacing=0;
			l.marginWidth=0;
			l.marginHeight=0;
			l.verticalSpacing=0;

			comp.setLayout(l);
			Browser browser = new Browser(comp,SWT.BORDER);
			browser.setText(getText(event));
			browser.setLayoutData(new GridData(200,150));


			return comp;
		}

		@Override
		public boolean isHideOnMouseDown() {
			return false;
		}


		public static final void enableFor(ColumnViewer viewer, int style) {
			new FancyToolTipSupport(viewer,style,false);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());

		TableViewer v = new TableViewer(shell, SWT.FULL_SELECTION);
		v.getTable().setLinesVisible(true);
		v.getTable().setHeaderVisible(true);
		v.setContentProvider(new MyContentProvider());
		FancyToolTipSupport.enableFor(v,ToolTip.NO_RECREATE);

		CellLabelProvider labelProvider = new CellLabelProvider() {

			@Override
			public String getToolTipText(Object element) {
				return "<html><body>Tooltip (" + element + ")<br /><a href='http://www.bestsolution.at' target='_NEW'>www.bestsolution.at</a></body></html>";
			}

			@Override
			public Point getToolTipShift(Object object) {
				return new Point(5, 5);
			}

			@Override
			public int getToolTipDisplayDelayTime(Object object) {
				return 2000;
			}

			@Override
			public int getToolTipTimeDisplayed(Object object) {
				return 5000;
			}

			@Override
			public void update(ViewerCell cell) {
				cell.setText(cell.getElement().toString());
			}
		};

		TableViewerColumn column = new TableViewerColumn(v, SWT.NONE);
		column.setLabelProvider(labelProvider);
		column.getColumn().setText("Column 1");
		column.getColumn().setWidth(100);

		v.setInput("");

		shell.setSize(200, 200);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		display.dispose();
	}

}
