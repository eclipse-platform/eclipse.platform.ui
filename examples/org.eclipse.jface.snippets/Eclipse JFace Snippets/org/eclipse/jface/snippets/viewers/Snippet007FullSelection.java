/*******************************************************************************
 * Copyright (c) 2006 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * TableViewer: Highlight cell and support editing which requires SWT.FULL_SELECTION
 * 
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 *
 */
public class Snippet007FullSelection {
	private class MyContentProvider implements IStructuredContentProvider {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return (MyModel[])inputElement;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
			
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			
		}
		
	}
	
	public class MyModel {
		public int counter;
		
		public MyModel(int counter) {
			this.counter = counter;
		}
		
		public String toString() {
			return "Item " + this.counter;
		}
	}
	
	public Snippet007FullSelection(Shell shell) {
		final TableViewer v = new TableViewer(shell,SWT.BORDER|SWT.FULL_SELECTION);
		v.setLabelProvider(new LabelProvider());
		v.setContentProvider(new MyContentProvider());
		v.setCellModifier(new ICellModifier() {

			public boolean canModify(Object element, String property) {
				return ((MyModel)element).counter % 2 == 0;
			}

			public Object getValue(Object element, String property) {
				return ((MyModel)element).counter + "";
			}

			public void modify(Object element, String property, Object value) {
				TableItem item = (TableItem)element;
				((MyModel)item.getData()).counter = Integer.parseInt(value.toString());
				v.update(item.getData(), null);
			}
			
		});
		v.setColumnProperties(new String[] { "column1", "column2" });
		v.setCellEditors(new CellEditor[] { new TextCellEditor(v.getTable()),null });
		
		TableColumn column = new TableColumn(v.getTable(),SWT.NONE);
		column.setWidth(100);
		column.setText("Column 1");
		
		column = new TableColumn(v.getTable(),SWT.NONE);
		column.setWidth(100);
		column.setText("Column 2");
		
		MyModel[] model = createModel();
		v.setInput(model);
		v.getTable().setLinesVisible(true);
		v.getTable().setHeaderVisible(true);
		
		final Rectangle[] selectionBounds = new Rectangle[1];
		
		v.getTable().addListener(SWT.MouseDown, new Listener() {

			/* (non-Javadoc)
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			public void handleEvent(Event event) {
				TableItem item = v.getTable().getItem(new Point(event.x,event.y));
				if( item != null ) {
					int count = v.getTable().getColumnCount();
					
					if( count == 0 ) {
						selectionBounds[0] = item.getBounds();
					}
					
					for( int i = 0; i < count; i++ ) {
						if( item.getBounds(i).contains(event.x,event.y) ) {
							selectionBounds[0] = item.getBounds(i);
							return;
						}
					}
				}
			}
			
		});
		
		v.getTable().addListener(SWT.EraseItem, new Listener() {

			/* (non-Javadoc)
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			public void handleEvent(Event event) {
				if((event.detail & SWT.SELECTED) != 0) {
					if( selectionBounds[0] != null ) {
						GC gc = event.gc;

						Color background = gc.getBackground();
						gc.setBackground(v.getTable().getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
						gc.fillRectangle(selectionBounds[0].x,selectionBounds[0].y,selectionBounds[0].width,selectionBounds[0].height);
						gc.setBackground(background);
						
					}
					
					event.detail &= ~SWT.SELECTED;
				}
			}
		});
		
	}
	
	private MyModel[] createModel() {
		MyModel[] elements = new MyModel[10];
		
		for( int i = 0; i < 10; i++ ) {
			elements[i] = new MyModel(i);
		}
		
		return elements;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display ();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet007FullSelection(shell);
		shell.open ();
		
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		
		display.dispose ();

	}

}
