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

package org.eclipse.ui.views.internal.markers.bookmarks;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.CellEditorActionHandler;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.internal.markers.CreationTime;
import org.eclipse.ui.views.internal.markers.Folder;
import org.eclipse.ui.views.internal.markers.IField;
import org.eclipse.ui.views.internal.markers.IFilter;
import org.eclipse.ui.views.internal.markers.LineNumber;
import org.eclipse.ui.views.internal.markers.MarkerRegistry;
import org.eclipse.ui.views.internal.markers.MarkerView;
import org.eclipse.ui.views.internal.markers.Message;
import org.eclipse.ui.views.internal.markers.Resource;

public class BookmarkView extends MarkerView {

	private final static ColumnLayoutData[] DEFAULT_COLUMN_LAYOUTS = { 
		new ColumnWeightData(200),
		new ColumnWeightData(75),
		new ColumnWeightData(150),
		new ColumnWeightData(60)
	};
	
	private final static IField[] HIDDEN_FIELDS = { 
		new CreationTime() 
	};
	
	private final static String[] ROOT_TYPES = { 
		IMarker.BOOKMARK
	};

	private final static String[] TABLE_COLUMN_PROPERTIES = {
		IMarker.MESSAGE,
		"", //$NON-NLS-1$
		"", //$NON-NLS-1$
		"" //$NON-NLS-1$
	}; 

	private final static String TAG_DIALOG_SECTION = "org.eclipse.ui.views.bookmark"; //$NON-NLS-1$

	private final static IField[] VISIBLE_FIELDS = { 
		new Message(), 
		new Resource(), 
		new Folder(), 
		new LineNumber() 
	};

	private ICellModifier cellModifier = new ICellModifier() {
		public Object getValue(Object element, String property) {
			if (element instanceof IMarker && IMarker.MESSAGE.equals(property))
				return ((IMarker) element).getAttribute(IMarker.MESSAGE, ""); //$NON-NLS-1$
			else 
				return null;
		}

		public boolean canModify(Object element, String property) {
			return true;
		}

		public void modify(Object element, String property, Object value) {
			if (element instanceof Item) {
				Item item = (Item) element;
				Object data = item.getData();
				
				if (data instanceof IMarker) {				
					IMarker marker = (IMarker) data;
	
					try {
						if (!marker.getAttribute(property).equals(value)) {
							if (IMarker.MESSAGE.equals(property))
								marker.setAttribute(IMarker.MESSAGE, value);
					
							if (bookmarkFilter != null && !bookmarkFilter.select(marker))
								filtersChanged();
						}
					} catch (CoreException e) {
						ErrorDialog.openError(getSite().getShell(), Messages.getString("errorModifyingBookmark") , null, e.getStatus()); //$NON-NLS-1$
					}
				}
			}
		}
	};

	private CellEditorActionHandler cellEditorActionHandler;
	private BookmarkFilter bookmarkFilter;
	private BookmarkRegistry bookmarkRegistry;

	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		
		TableViewer tableViewer = getViewer();
		CellEditor cellEditors[] = new CellEditor[tableViewer.getTable().getColumnCount()];
		CellEditor descriptionCellEditor = new TextCellEditor(tableViewer.getTable());
		cellEditors[0] = descriptionCellEditor;
		tableViewer.setCellEditors(cellEditors);
		tableViewer.setCellModifier(cellModifier);
		tableViewer.setColumnProperties(TABLE_COLUMN_PROPERTIES);
		
		cellEditorActionHandler = new CellEditorActionHandler(getViewSite().getActionBars());
		cellEditorActionHandler.addCellEditor(descriptionCellEditor);
		cellEditorActionHandler.setCopyAction(copyAction);
		cellEditorActionHandler.setPasteAction(pasteAction);
		cellEditorActionHandler.setDeleteAction(deleteAction);
		cellEditorActionHandler.setSelectAllAction(selectAllAction);
	}

	public void dispose() {
		if (cellEditorActionHandler != null)
			cellEditorActionHandler.dispose();
		
		super.dispose();
	}

	public void init(IViewSite viewSite, IMemento memento) throws PartInitException {
		super.init(viewSite, memento);
		bookmarkFilter = new BookmarkFilter();
		IDialogSettings dialogSettings = getDialogSettings();
		
		if (bookmarkFilter != null)
			bookmarkFilter.restoreState(dialogSettings);
			
		bookmarkRegistry = BookmarkRegistry.getInstance();
		bookmarkRegistry.setFilter(bookmarkFilter);
		bookmarkRegistry.setInput((IResource) getViewerInput());
	}

	public void saveState(IMemento memento) {
		IDialogSettings dialogSettings = getDialogSettings();
		
		if (bookmarkFilter != null)
			bookmarkFilter.saveState(dialogSettings);
		
		super.saveState(memento);	
	}

	protected ColumnLayoutData[] getDefaultColumnLayouts() {
		return DEFAULT_COLUMN_LAYOUTS;
	}

	protected IDialogSettings getDialogSettings() {
		AbstractUIPlugin plugin = (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
		IDialogSettings workbenchSettings = plugin.getDialogSettings();
		IDialogSettings settings = workbenchSettings.getSection(TAG_DIALOG_SECTION);
		
		if (settings == null)
			settings = workbenchSettings.addNewSection(TAG_DIALOG_SECTION);

		return settings;
	}
	
	protected IFilter getFilter() {
		return bookmarkFilter;
	}
	
	protected Dialog getFiltersDialog() {
		return new BookmarkFiltersDialog(getSite().getShell(), bookmarkFilter);
	}
	
	protected IField[] getHiddenFields() {
		return HIDDEN_FIELDS;
	}

	protected MarkerRegistry getRegistry() {
		return bookmarkRegistry;
	}

	protected String[] getRootTypes() {
		return ROOT_TYPES;
	}

	protected Object getViewerInput() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
	protected IField[] getVisibleFields() {
		return VISIBLE_FIELDS;
	}
}
