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

import java.text.Collator;
import java.util.Iterator;

import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
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
            //no-op
        }

        public void dispose() {
            //no-op
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
    
    private class EditorListLabelProvider extends LabelProvider implements IFontProvider {

    	private Font boldFont = null;

		public EditorListLabelProvider() {
		    //no-op
    	}

    	public String getText(Object element) {
    	    CTabItem tabItem = (CTabItem) element;
            EditorPresentation editorPresentation = (EditorPresentation) getTableViewer()
            .getInput();
            IPresentablePart presentablePart = editorPresentation.getPartForTab(tabItem);    	    
    	    return editorPresentation.getLabelText(presentablePart, true); 
    	}

    	public Image getImage(Object element) {
    	    CTabItem tabItem = (CTabItem) element;
            EditorPresentation editorPresentation = (EditorPresentation) getTableViewer()
            .getInput();
            IPresentablePart presentablePart = editorPresentation.getPartForTab(tabItem);    	    
    	    return editorPresentation.getLabelImage(presentablePart);
    	}
    	
		public Font getFont(Object element) {
			CTabItem tabItem = (CTabItem) element;
			if (tabItem.isShowing()) // visible
				return null;
			
			if (boldFont == null) {
				Font originalFont = tabItem.getFont();
				FontData fontData[] = originalFont.getFontData();
				// Adding the bold attribute
				for (int i = 0; i < fontData.length; i++) 
					fontData[i].setStyle(fontData[i].getStyle()|SWT.BOLD);
				boldFont = new Font(tabItem.getDisplay(), fontData);
			}
			return boldFont;
		}
		
		public void dispose() {
			super.dispose();
			if (boldFont != null)
				boldFont.dispose();
		}
    }    
    
    private class EditorListViewerSorter extends ViewerSorter {
    	
    	public EditorListViewerSorter(){
    	    //no-op
    	}
    	
    	public EditorListViewerSorter(Collator collator) {
    		super(collator);
    	}
    	
    	/* (non-Javadoc)
    	 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
    	 */
    	public int compare(Viewer viewer, Object e1, Object e2) {
    		int cat1 = category(e1);
    		int cat2 = category(e2);

    		if (cat1 != cat2)
    			return cat1 - cat2;

    		// cat1 == cat2

    		String name1;
    		String name2;

    		if (viewer == null || !(viewer instanceof ContentViewer)) {
    			name1 = e1.toString();
    			name2 = e2.toString();
    		} else {
    			IBaseLabelProvider prov = ((ContentViewer) viewer).getLabelProvider();
    			if (prov instanceof ILabelProvider) {
    				ILabelProvider lprov = (ILabelProvider) prov;
    				name1 = lprov.getText(e1);
    				name2 = lprov.getText(e2);
    				// ILabelProvider's implementation in EditorList calls 
    				// EditorPresentation.getLabelText which returns the name of dirty 
    				// files begining with "* ", sorting should not take "* " in consideration
    				if (name1.startsWith("* ")) //$NON-NLS-1$
    					name1 = name1.substring(2);
    				if (name2.startsWith("* ")) //$NON-NLS-1$
    					name2 = name2.substring(2);
    			} else {
    				name1 = e1.toString();
    				name2 = e2.toString();
    			}
    		}
    		if (name1 == null)
    			name1 = "";//$NON-NLS-1$
    		if (name2 == null)
    			name2 = "";//$NON-NLS-1$
    		return collator.compare(name1, name2);
    	}
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ViewerSorter#category(java.lang.Object)
		 */
		public int category(Object element) {
			
			CTabItem tabItem = (CTabItem) element;
			if (tabItem.isShowing())
				return 1; // visible
			return 0; // not visible
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
        tableViewer.setSorter(new EditorListViewerSorter());
        tableViewer.setLabelProvider(new EditorListLabelProvider());
        return tableViewer;
    }

    public void setInput(Object information) {
        EditorPresentation editorPresentation = (EditorPresentation) information;
        inputChanged(editorPresentation, editorPresentation.getTabFolder()
                .getSelection());
    }

    protected void gotoSelectedElement() {
        Object selectedElement = getSelectedElement();

        if (selectedElement != null) {
            EditorPresentation editorPresentation = (EditorPresentation) getTableViewer()
                    .getInput();
            editorPresentation.setSelection((CTabItem) selectedElement);
        }
        
        dispose();
    }
    
    protected void deleteSelectedElements() {
        IStructuredSelection structuredSelection = getSelectedElements();
        
        if (structuredSelection != null) {
            EditorPresentation editorPresentation = (EditorPresentation) getTableViewer()
            .getInput();
            
            for (Iterator iterator = structuredSelection.iterator(); iterator.hasNext();) {
    			IPresentablePart presentablePart = editorPresentation.getPartForTab((CTabItem) iterator.next());    			
    			editorPresentation.close(presentablePart);                
            }
        }
    }
}