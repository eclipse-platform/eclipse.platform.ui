/*******************************************************************************
 * Copyright (c) 2007, 2014 Adam Neal and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Adam Neal - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 414565
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 414565
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * A simple TableViewer to demonstrate how custom tooltips could be created easily while preserving
 * the multiple selection.
 *
 * This is a modified example taken from Tom Schindl's Snippet023TreeViewerCustomTooltips.java
 *
 * This code is for users pre 3.3 others could use newly added tooltip support in {@link CellLabelProvider}

 * @author Adam Neal <Adam_Neal@ca.ibm.com>
 *
 */
public class Snippet031TableViewerCustomTooltipsMultiSelection {
	public class MyLableProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof MyModel) {
				switch (columnIndex) {
					case 0: return ((MyModel)element).col1;
					case 1: return ((MyModel)element).col2;
				}
			}
			return "";
		}

		@Override
		public void addListener(ILabelProviderListener listener) {
			/* Ignore */
		}

		@Override
		public void dispose() {
			/* Ignore */
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
			/* Ignore */
		}

	}

	public class MyModel {
		public String col1, col2;

		public MyModel(String c1, String c2) {
			col1 = c1;
			col2 = c2;
		}

		@Override
		public String toString() {
			return col1 + col2;
		}

	}

	public Snippet031TableViewerCustomTooltipsMultiSelection(Shell shell) {
		final Table table = new Table(shell, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
        table.setLinesVisible(true);

		final TableViewer v = new TableViewer(table);
		TableColumn tableColumn1 = new TableColumn(table, SWT.NONE);
		TableColumn tableColumn2 = new TableColumn(table, SWT.NONE);

		String column1 = "Column 1", column2 = "Column 2";
		/* Setup the table  columns */
        tableColumn1.setText(column1);
        tableColumn2.setText(column2);
        tableColumn1.pack();
        tableColumn2.pack();

        v.setColumnProperties(new String[] { column1, column2 });
		v.setLabelProvider(new MyLableProvider());
		v.setContentProvider(new ArrayContentProvider());
		v.setInput(createModel());

		/**
	     * The listener that gets added to the table.  This listener is responsible for creating the tooltips
	     * when hovering over a cell item. This listener will listen for the following events:
	     *  <li>SWT.KeyDown		- to remove the tooltip</li>
	     *  <li>SWT.Dispose		- to remove the tooltip</li>
	     *  <li>SWT.MouseMove	- to remove the tooltip</li>
	     *  <li>SWT.MouseHover	- to set the tooltip</li>
	     */
	    Listener tableListener = new Listener () {
	    	Shell tooltip = null;
	    	Label label = null;

	    	@Override
			public void handleEvent (Event event) {
			   switch (event.type) {
				   	case SWT.KeyDown:
				   	case SWT.Dispose:
				   	case SWT.MouseMove: {
				   		if (tooltip == null) break;
				   		tooltip.dispose ();
				   		tooltip = null;
				   		label = null;
				   		break;
				   	}
				   	case SWT.MouseHover: {
				   		Point coords = new Point(event.x, event.y);
						TableItem item = table.getItem(coords);
				   		if (item != null) {
				   			int columnCount = table.getColumnCount();
							for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
				   				if (item.getBounds(columnIndex).contains(coords)) {
				   					/* Dispose of the old tooltip (if one exists */
				   					if (tooltip != null  && !tooltip.isDisposed ()) tooltip.dispose ();

				   					/* Create a new Tooltip */
				   					tooltip = new Shell (table.getShell(), SWT.ON_TOP | SWT.NO_FOCUS | SWT.TOOL);
				   					tooltip.setBackground (table.getDisplay().getSystemColor (SWT.COLOR_INFO_BACKGROUND));
				   					FillLayout layout = new FillLayout ();
				   					layout.marginWidth = 2;
				   					tooltip.setLayout (layout);
				   					label = new Label (tooltip, SWT.NONE);
				   					label.setForeground (table.getDisplay().getSystemColor (SWT.COLOR_INFO_FOREGROUND));
				   					label.setBackground (table.getDisplay().getSystemColor (SWT.COLOR_INFO_BACKGROUND));

				   					/* Store the TableItem with the label so we can pass the mouse event later */
				   					label.setData ("_TableItem_", item);

				   					/* Set the tooltip text */
			   						label.setText("Tooltip: " + item.getData() + " : " + columnIndex);

			   						/* Setup Listeners to remove the tooltip and transfer the received mouse events */
				   					label.addListener (SWT.MouseExit, tooltipLabelListener);
				   					label.addListener (SWT.MouseDown, tooltipLabelListener);

				   					/* Set the size and position of the tooltip */
				   					Point size = tooltip.computeSize (SWT.DEFAULT, SWT.DEFAULT);
				   					Rectangle rect = item.getBounds (columnIndex);
				   					Point pt = table.toDisplay (rect.x, rect.y);
				   					tooltip.setBounds (pt.x, pt.y, size.x, size.y);

				   					/* Show it */
				   					tooltip.setVisible (true);
				   					break;
				   				}
				   			}
				   		}
				   	}
			   }
	    	}
		};

		table.addListener (SWT.Dispose, tableListener);
		table.addListener (SWT.KeyDown, tableListener);
		table.addListener (SWT.MouseMove, tableListener);
		table.addListener (SWT.MouseHover, tableListener);
	}

	   /**
	    * This listener is added to the tooltip so that it can either dispose itself if the mouse
	    * exits the tooltip or so it can pass the selection event through to the table.
	    */
	    final TooltipLabelListener tooltipLabelListener = new TooltipLabelListener();
	    final class TooltipLabelListener implements Listener {
	        private boolean isCTRLDown(Event e) {
	        	return (e.stateMask & SWT.CTRL) != 0;
	        }

		@Override
		public void handleEvent (Event event) {
			   Label label = (Label)event.widget;
			   Shell shell = label.getShell ();
			   switch (event.type) {
				   	case SWT.MouseDown:	/* Handle a user Click */
				   		/* Extract our Data */
				   		Event e = new Event ();
				   		e.item = (TableItem) label.getData ("_TableItem_");
				   		Table table = ((TableItem) e.item).getParent();

				   		/* Construct the new Selection[] to show */
				   		TableItem [] newSelection = null;
				   		if (isCTRLDown(event)) {
				   			/* We have 2 scenario's.
				   			 * 	1) We are selecting an already selected element - so remove it from the selected indices
				   			 *  2) We are selecting a non-selected element - so add it to the selected indices
				   			 */
				   			TableItem[] sel = table.getSelection();
				   			for (int i = 0; i < sel.length; ++i) {
				   				if (e.item.equals(sel[i])) {
				   					// We are de-selecting this element
				   					newSelection = new TableItem[sel.length - 1];
				   					System.arraycopy(sel, 0, newSelection, 0, i);
				   					System.arraycopy(sel, i+1, newSelection, i, sel.length - i - 1);
				   					break;
				   				}
		   					}

				   			/*
				   			 * If we haven't created the newSelection[] yet, than we are adding the newly selected element
				   			 * into the list of selected indicies
				   			 */
				   			if (newSelection == null) {
				   				newSelection = new TableItem[sel.length + 1];
				   				System.arraycopy(sel, 0, newSelection, 0, sel.length);
				   				newSelection[sel.length] = (TableItem) e.item;
				   			}

				   		} else {
				   			/* CTRL is not down, so we simply select the single element */
				   			newSelection = new TableItem[] { (TableItem) e.item };
				   		}
				   		/* Set the new selection of the table and notify the listeners */
				   		table.setSelection (newSelection);
				   		table.notifyListeners (SWT.Selection, e);

				   		/* Remove the Tooltip */
				   		shell.dispose ();
				   		table.setFocus();
				   		break;
				   	case SWT.MouseExit:
				   		shell.dispose ();
				   		break;
			   }
	    }};



	private List<MyModel> createModel() {
		List<MyModel> list = new ArrayList<MyModel>();
		list.add(new MyModel("A", "B"));
		list.add(new MyModel("C", "D"));
		list.add(new MyModel("E", "F"));
		return list;
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet031TableViewerCustomTooltipsMultiSelection(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();
	}
}
