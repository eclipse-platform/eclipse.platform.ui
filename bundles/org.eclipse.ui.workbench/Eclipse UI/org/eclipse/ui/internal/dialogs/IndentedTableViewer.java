/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;

/**
 * A concrete viewer based on a SWT <code>Table</code> control.  The viewer supports indentation
 * of the table elements.  
 * <p>
 * Label providers for this viewer must implement <code>IIndentedTableLabelProvider</code>
 * in order to get indentation.</p>
 */
public class IndentedTableViewer extends TableViewer {
    /**
     * Extends <code>ITableLabelProvider</code> with the methods
     * to provide the indentation.
     *
     * @see TableViewer
     */
    public interface IIndentedTableLabelProvider extends ITableLabelProvider {
        /**
         * Returns the indent level for the element.  This number will be used
         * as the parameter to the <code>TableItem.setImageIndent(int)<code> method.
         *
         * @param element the object representing the entire row, or 
         *    <code>null</code> indicating that no input object is set
         *    in the viewer
         */
        public int getIndent(Object element);
    }

    public IndentedTableViewer(Composite parent) {
        super(parent);
    }

    public IndentedTableViewer(Composite parent, int style) {
        super(parent, style);
    }

    public IndentedTableViewer(Table table) {
        super(table);
    }

    public void doUpdateItem(Widget widget, Object element, boolean fullMap) {
        if (widget instanceof TableItem) {
            TableItem item = (TableItem) widget;
            if (getLabelProvider() instanceof IIndentedTableLabelProvider) {
                IIndentedTableLabelProvider provider = (IIndentedTableLabelProvider) getLabelProvider();
                item.setImageIndent(provider.getIndent(element));
            }
        }
        super.doUpdateItem(widget, element, fullMap);
    }
}