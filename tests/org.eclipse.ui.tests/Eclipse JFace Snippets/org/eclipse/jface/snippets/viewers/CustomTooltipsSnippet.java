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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.jface.viewers.ViewerLabelProvider;
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
 * @since 3.3M2
 */
public class CustomTooltipsSnippet {
	private static class MyContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			return new String[] { "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten" };
		}

		public void dispose() {
			
		}

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
	    
	    ViewerLabelProvider labelProvider = new ViewerLabelProvider(null) {

			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ViewerLabelProvider#getTooltipText(java.lang.Object)
			 */
			public String getTooltipText(Object element) {
				return "Tooltip (" + element + ")";
			}

			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ViewerLabelProvider#getTooltipShift(java.lang.Object)
			 */
			public Point getTooltipShift(Object object) {
				return new Point(5,5);
			}

			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ViewerLabelProvider#getTooltipDisplayDelayTime(java.lang.Object)
			 */
			public int getTooltipDisplayDelayTime(Object object) {
				return 2000;
			}

			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ViewerLabelProvider#getTooltipTimeDisplayed(java.lang.Object)
			 */
			public int getTooltipTimeDisplayed(Object object) {
				return 5000;
			}
	    };
	    
	    labelProvider.setLabelProvider(new LabelProvider() {

			public String getText(Object element) {
				return element.toString();
			}
	    	
	    });
	    
	    TableColumn column = new TableColumn(v.getTable(),SWT.NONE);
	    new ViewerColumn(column,labelProvider);
	    column.setText("Column 1");
	    column.setWidth(100);
	    
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
