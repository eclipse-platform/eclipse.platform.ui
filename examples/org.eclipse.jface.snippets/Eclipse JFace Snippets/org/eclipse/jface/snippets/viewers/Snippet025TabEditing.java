/*******************************************************************************
 * Copyright (c) 2006, 2010 Tom Schindl and others.
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
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * Edit cell values in a table
 * 
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 *
 */
public class Snippet025TabEditing {
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
	
	public Snippet025TabEditing(Shell shell) {
		final TableViewer v = new TableViewer(shell,SWT.BORDER|SWT.FULL_SELECTION);
		TableColumn tc = new TableColumn(v.getTable(),SWT.NONE);
		tc.setWidth(100);
		tc.setText("Column 1");
		
		tc = new TableColumn(v.getTable(),SWT.NONE);
		tc.setWidth(200);
		tc.setText("Column 2");
		
		v.setLabelProvider(new LabelProvider());
		v.setContentProvider(new MyContentProvider());
		v.setCellModifier(new ICellModifier() {

			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
			 */
			public boolean canModify(Object element, String property) {
				return ((MyModel)element).counter % 2 == 0;
			}

			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
			 */
			public Object getValue(Object element, String property) {
				return ((MyModel)element).counter + "";
			}

			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
			 */
			public void modify(Object element, String property, Object value) {
				TableItem item = (TableItem)element;
				((MyModel)item.getData()).counter = Integer.parseInt(value.toString());
				v.update(item.getData(), null);
			}
			
		});
		
		v.setColumnProperties(new String[] { "column1", "column2" });
		v.setCellEditors(new CellEditor[] { new TextCellEditor(v.getTable()), new TextCellEditor(v.getTable()) });
		TableViewerEditor.create(v,new ColumnViewerEditorActivationStrategy(v),ColumnViewerEditor.TABBING_HORIZONTAL|ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR|ColumnViewerEditor.TABBING_VERTICAL);
		
		MyModel[] model = createModel();
		v.setInput(model);
		v.getTable().setLinesVisible(true);
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
		new Snippet025TabEditing(shell);
		shell.open ();
		
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		
		display.dispose ();

	}

}
