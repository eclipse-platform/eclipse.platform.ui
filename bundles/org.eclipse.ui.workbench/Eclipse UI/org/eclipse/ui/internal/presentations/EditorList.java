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

package org.eclipse.ui.internal.presentations;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.presentations.IPresentablePart;

public class EditorList extends AbstractTableInformationControl {

    private class EditorListContentProvider implements IStructuredContentProvider {

        private EditorPresentation editorPresentation;

        public EditorListContentProvider() {
        }

        public void dispose() {
        }

        public Object[] getElements(Object inputElement) {
            if (editorPresentation == null) { return new CTabItem[0]; }                         
            final CTabFolder tabFolder = editorPresentation.getTabFolder();

            /* TODO
            ArrayList items = new ArrayList(Arrays.asList(tabFolder.getItems()));

            for (Iterator iterator = items.iterator(); iterator.hasNext();) {
                CTabItem tabItem = (CTabItem) iterator.next();

                if (tabItem.isShowing()) iterator.remove();
            }
            
            return items.toArray();
            */
            
            return tabFolder.getItems();
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            editorPresentation = (EditorPresentation) newInput;
        }
    }
    
    private class EditorListLabelProvider extends LabelProvider {

    	public EditorListLabelProvider() {
    	}

    	public String getText(Object element) {
    	    CTabItem tabItem = (CTabItem) element;
            EditorPresentation editorPresentation = (EditorPresentation) getTableViewer()
            .getInput();
            IPresentablePart presentablePart = editorPresentation.getPartForTab(tabItem);    	    
    	    return editorPresentation.getLabelText(presentablePart, false, true);
    	}

    	public Image getImage(Object element) {
    	    CTabItem tabItem = (CTabItem) element;
            EditorPresentation editorPresentation = (EditorPresentation) getTableViewer()
            .getInput();
            IPresentablePart presentablePart = editorPresentation.getPartForTab(tabItem);    	    
    	    return editorPresentation.getLabelImage(presentablePart);
    	}
    }    
    
    public EditorList(Shell parent, int shellStyle, int treeStyle) {
        super(parent, shellStyle, treeStyle);
        setBackgroundColor(new Color(parent.getDisplay(), 255, 255, 255));
    }

    protected TableViewer createTableViewer(Composite parent, int style) {
        Table table = new Table(parent, SWT.SINGLE | (style & ~SWT.MULTI));
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        TableViewer tableViewer = new TableViewer(table);
        tableViewer.addFilter(new NamePatternFilter());
        tableViewer.setContentProvider(new EditorListContentProvider());
        tableViewer.setSorter(new ViewerSorter());
        tableViewer.setLabelProvider(new EditorListLabelProvider());
        return tableViewer;
    }

    public void setInput(Object information) {
        EditorPresentation editorPresentation = (EditorPresentation) information;
        inputChanged(editorPresentation, editorPresentation.getTabFolder()
                .getSelection());
    }

    protected void gotoSelectedElement() {
        Object selection = getSelectedElement();

        if (selection != null) {
            EditorPresentation editorPresentation = (EditorPresentation) getTableViewer()
                    .getInput();
            editorPresentation.getTabFolder()
                    .setSelection((CTabItem) selection);
        }
        
        dispose();
    }
}