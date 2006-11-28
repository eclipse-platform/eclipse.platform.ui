/*******************************************************************************
 * Copyright (c) 2006 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *     IBM - Improvement for Bug 159625 [Snippets] Update Snippet011CustomTooltips to reflect new API
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Explore New API: JFace custom tooltips drawing.
 * 
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 * @since 3.3
 */
public class Snippet011CustomTooltips {
	private static class MyContentProvider implements IStructuredContentProvider {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return new String[] { "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten" };
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
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final Display display = new Display ();
		Shell shell = new Shell (display);
	    shell.setLayout(new FillLayout());
	    
	    TableViewer v = new TableViewer(shell,SWT.FULL_SELECTION);
	    v.getTable().setLinesVisible(true);
	    v.getTable().setHeaderVisible(true);
	    v.activateCustomTooltips();
	    v.setContentProvider(new MyContentProvider());
	    
	    CellLabelProvider labelProvider = new CellLabelProvider() {

			
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.CellLabelProvider#getToolTipText(java.lang.Object)
			 */
			public String getToolTipText(Object element) {
				return "Tooltip (" + element + ")";
			}

		
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.CellLabelProvider#getToolTipShift(java.lang.Object)
			 */
			public Point getToolTipShift(Object object) {
				return new Point(5,5);
			}

			
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.CellLabelProvider#getToolTipDisplayDelayTime(java.lang.Object)
			 */
			public int getToolTipDisplayDelayTime(Object object) {
				return 2000;
			}

			
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.CellLabelProvider#getToolTipTimeDisplayed(java.lang.Object)
			 */
			public int getToolTipTimeDisplayed(Object object) {
				return 5000;
			}
			
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.CellLabelProvider#update(org.eclipse.jface.viewers.ViewerCell)
			 */
			public void update(ViewerCell cell) {
				cell.setText(cell.getElement().toString());
				
			}
	    };
	    
	    
	    TableViewerColumn column = new TableViewerColumn(v,SWT.NONE);
	    column.setLabelProvider(labelProvider);
	    column.getColumn().setText("Column 1");
	    column.getColumn().setWidth(100);
	    
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
