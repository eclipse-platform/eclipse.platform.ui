/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.internal.ui.preferences;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.Team;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.SWTUtils;


public class FileTypeTable implements ICellModifier, IStructuredContentProvider, ITableLabelProvider {
    
    private final static int COMBO_INDEX_BINARY= 0;
    private final static int COMBO_INDEX_TEXT= 1;
    private final static String [] MODES_TEXT= { Policy.bind("FileTypeTable.0"), Policy.bind("FileTypeTable.1") }; //$NON-NLS-1$ //$NON-NLS-2$
    
    private final static int COMBO_INDEX_SAVE= 0;
    private final static int COMBO_INDEX_DONT_SAVE= 1;
    private static final String [] SAVE_TEXT= { Policy.bind("FileTypeTable.2"), Policy.bind("FileTypeTable.3") }; //$NON-NLS-1$ //$NON-NLS-2$

    public interface PixelConverter	{
        
        int convertWidthInCharsToPixels(int chars);
        
    }
    
    public abstract static class Item {
        public final String name;
        public boolean save;
        public int mode;
        
        public Item(String name) { this.name= name; save= true; mode= Team.BINARY; }
    }
    
    public static class Extension extends Item {
        public Extension(String name) { super(name); }

    }

    public static class Name extends Item {
        public Name(String name) { super(name); }
    }
    
    private final static int SMALL_COLUMN= 15;
    private final static int LARGE_COLUMN= 30;
    
    protected static final String ITEM = "item"; //$NON-NLS-1$
    protected static final String PROPERTY_MODE= "mode"; //$NON-NLS-1$
    protected static final String PROPERTY_SAVE= "save"; //$NON-NLS-1$

    private final TableViewer fTableViewer;
    private final List fItems;
    private final boolean fShowSaveColumn;
    
	public FileTypeTable(Composite composite, PixelConverter converter, List items, boolean showSaveColumn) {
	    
	    fShowSaveColumn= showSaveColumn;
	    
	    fItems= items;

		/**
		 * Create a table.
		 */
		final Table table = new Table(composite, SWT.V_SCROLL | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		table.setLayoutData(SWTUtils.createHVFillGridData());
		table.setLinesVisible(false);
		table.setHeaderVisible(true);
		
		/**
		 * Create a viewer for the table.
		 */
		fTableViewer = new TableViewer(table);
		fTableViewer.setContentProvider(this);
		fTableViewer.setLabelProvider(this);
		fTableViewer.setSorter(new ViewerSorter() {
		    public int category(Object element) {
		        if (element instanceof Extension)
		            return 0;
		        if (element instanceof Name) {
		            return 1;
		        }
		        return 2;
		    }
		    
		});

		/**
		 * The 'Extension' column
		 */
		final TableColumn fileColumn = new TableColumn(table, SWT.NONE, 0);
		fileColumn.setWidth(converter.convertWidthInCharsToPixels(LARGE_COLUMN));
		fileColumn.setText(Policy.bind("FileTypeTable.4")); //$NON-NLS-1$
		
		/**
		 * The 'Mode' column
		 */
		final TableColumn modeColumn = new TableColumn(table, SWT.NONE, 1);
		modeColumn.setWidth(converter.convertWidthInCharsToPixels(SMALL_COLUMN));
		modeColumn.setText(Policy.bind("FileTypeTable.5")); //$NON-NLS-1$

		/**
		 * The 'Save' column
		 */
		if (fShowSaveColumn) {
		    final TableColumn saveColumn = new TableColumn(table, SWT.NONE, 2);
		    saveColumn.setWidth(converter.convertWidthInCharsToPixels(LARGE_COLUMN));
		    saveColumn.setText(Policy.bind("FileTypeTable.6")); //$NON-NLS-1$
		}
		
		/**
		 * Add a cell editor in the Keyword Substitution Mode column
		 */
		new TableEditor(table);
		
		final CellEditor modeEditor = new ComboBoxCellEditor(table, MODES_TEXT, SWT.READ_ONLY);
		final CellEditor saveEditor= new ComboBoxCellEditor(table, SAVE_TEXT, SWT.READ_ONLY);
		
		if (fShowSaveColumn) {
		    fTableViewer.setCellEditors(new CellEditor[] { null, modeEditor, saveEditor });
		    fTableViewer.setColumnProperties(new String [] { ITEM, PROPERTY_MODE, PROPERTY_SAVE });
		} else { 
		    fTableViewer.setCellEditors(new CellEditor [] { null, modeEditor });
		    fTableViewer.setColumnProperties(new String [] { ITEM, PROPERTY_MODE });
		}
		    
		fTableViewer.setCellModifier(this);

		fTableViewer.setInput(fItems);
	}
	

    public Object getValue(Object element, String property) {
        
        final Item item= (Item)element;
        
        if (PROPERTY_MODE.equals(property)) {
            if (item.mode == Team.BINARY)
                return new Integer(COMBO_INDEX_BINARY);
            if (item.mode == Team.TEXT) 
                return new Integer(COMBO_INDEX_TEXT);
        }
        
        if (fShowSaveColumn && PROPERTY_SAVE.equals(property)) {
            return new Integer(item.save ? COMBO_INDEX_SAVE : COMBO_INDEX_DONT_SAVE);
        }
        return null;
    }

    public boolean canModify(Object element, String property) {
    	return PROPERTY_MODE.equals(property) || (fShowSaveColumn && PROPERTY_SAVE.equals(property));
    }

    public void modify(Object element, String property, Object value) {
        
        final IStructuredSelection selection = (IStructuredSelection)fTableViewer.getSelection();
        final Item item= (Item)selection.getFirstElement();

        final int comboIndex = ((Integer)value).intValue();
        
        if (PROPERTY_MODE.equals(property)) {
    	    if (comboIndex == COMBO_INDEX_BINARY)
    	        item.mode= Team.BINARY;
    	    if (comboIndex == COMBO_INDEX_TEXT)
    	        item.mode= Team.TEXT;
        }
    	    
        if (fShowSaveColumn && PROPERTY_SAVE.equals(property)) {
    	    item.save= COMBO_INDEX_SAVE == comboIndex;
        }
        fTableViewer.refresh(item);
    }

    public Image getColumnImage(Object element, int columnIndex) {
    	return null;
    }

    public String getColumnText(Object element, int columnIndex) {

        final Item item= (Item) element;
        
        if (columnIndex == 0) { 
            return (item instanceof Extension ? "*." : "") + item.name; //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        if (columnIndex == 1) {
            if (item.mode == Team.BINARY) {
                return MODES_TEXT[COMBO_INDEX_BINARY];
            } else if (item.mode == Team.TEXT) {
                return MODES_TEXT[COMBO_INDEX_TEXT];
            }
        }
        
        if (columnIndex == 2) {
            if (fShowSaveColumn) return SAVE_TEXT[item.save ? COMBO_INDEX_SAVE : COMBO_INDEX_DONT_SAVE];
        }
        
    	return null;
    }

    public void addListener(ILabelProviderListener listener) {}

    public void dispose() {}

    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    public void removeListener(ILabelProviderListener listener) {}

    public Object[] getElements(Object inputElement) {	
    	return ((Collection)inputElement).toArray();
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
    
    public IStructuredSelection getSelection() {
        return (IStructuredSelection)fTableViewer.getSelection();
    }
    
    public void setInput(List items) {
        fItems.clear();
        fItems.addAll(items);
        fTableViewer.refresh();
    }
    
    public TableViewer getViewer() {
        return fTableViewer;
    }
}
