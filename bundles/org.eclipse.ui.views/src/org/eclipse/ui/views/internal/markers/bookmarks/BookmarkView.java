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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.CellEditorActionHandler;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.internal.markers.CreationTime;
import org.eclipse.ui.views.internal.markers.FiltersDialog;
import org.eclipse.ui.views.internal.markers.Folder;
import org.eclipse.ui.views.internal.markers.IField;
import org.eclipse.ui.views.internal.markers.LineNumber;
import org.eclipse.ui.views.internal.markers.MarkerFilter;
import org.eclipse.ui.views.internal.markers.MarkerRegistry;
import org.eclipse.ui.views.internal.markers.MarkerView;
import org.eclipse.ui.views.internal.markers.Message;
import org.eclipse.ui.views.internal.markers.Resource;


public class BookmarkView extends MarkerView {
	
	private static final String TAG_DIALOG_SECTION = "org.eclipse.ui.views.bookmarkexplorer"; //$NON-NLS-1$
	
	private static final ColumnLayoutData[] DEFAULT_COLUMN_LAYOUTS =
		{
			new ColumnWeightData(200),
			new ColumnWeightData(75),
			new ColumnWeightData(150),
			new ColumnWeightData(60)};
			
	private static String[] tableColumnProperties =
		{
			IMarker.MESSAGE,
			"", //$NON-NLS-1$
			"", //$NON-NLS-1$
			""  }; //$NON-NLS-1$

	private ICellModifier cellModifier = new ICellModifier() {
		public Object getValue(Object element, String property) {
			if (element instanceof IMarker && property.equals(IMarker.MESSAGE)) {
				return ((IMarker) element).getAttribute(IMarker.MESSAGE, ""); //$NON-NLS-1$
			}
			return null;
		}
		public boolean canModify(Object element, String property) {
			return true;
		}
		/**
		 * Modifies a marker as a result of a successfully completed direct editing.
		 */
		public void modify(Object element, String property, Object value) {
			Item item = (Item) element;
			IMarker marker = (IMarker) item.getData();
			setMarkerMessage(marker, property, value);
		}
	};
	
	private CellEditorActionHandler editorActionHandler;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerView#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		
		TableViewer viewer = getViewer();
		CellEditor editors[] = new CellEditor[viewer.getTable().getColumnCount()];
		CellEditor descriptionEditor = new TextCellEditor(viewer.getTable());
		editors[0] = descriptionEditor;
		viewer.setCellEditors(editors);
		viewer.setCellModifier(cellModifier);
		viewer.setColumnProperties(tableColumnProperties);
		
		//Add global action handlers.	
		editorActionHandler =
			new CellEditorActionHandler(getViewSite().getActionBars());
		editorActionHandler.addCellEditor(descriptionEditor);
		editorActionHandler.setCopyAction(copyAction);
		editorActionHandler.setPasteAction(pasteAction);
		editorActionHandler.setDeleteAction(deleteAction);
		editorActionHandler.setSelectAllAction(selectAllAction);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerView#dispose()
	 */
	public void dispose() {
		super.dispose();
		if (editorActionHandler != null) {
			editorActionHandler.dispose();
			editorActionHandler = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerView#getRootTypes()
	 */
	protected String[] getRootTypes() {
		return new String[] {IMarker.BOOKMARK};
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerView#getDialogSettings()
	 */
	protected IDialogSettings getDialogSettings() {
		AbstractUIPlugin plugin = (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
		IDialogSettings workbenchSettings = plugin.getDialogSettings();
		IDialogSettings settings = workbenchSettings.getSection(TAG_DIALOG_SECTION);
		if (settings == null) {
			settings = workbenchSettings.addNewSection(TAG_DIALOG_SECTION);
		}
		return settings;
	}
	
	protected IField[] getVisibleFields() {
		return new IField[] {new Message(), new Resource(), new Folder(), new LineNumber()
		};
	}
	
	protected IField[] getHiddenFields() {
		return new IField[] {new CreationTime()
		};
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerView#getRegistry()
	 */
	protected MarkerRegistry getRegistry() {
		return BookmarkRegistry.getInstance();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerView#getDefaultColumnLayouts()
	 */
	protected ColumnLayoutData[] getDefaultColumnLayouts() {
		return DEFAULT_COLUMN_LAYOUTS;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerView#getFilter()
	 */
	protected MarkerFilter getFilter() {
		if (filter == null) {
			filter = new BookmarkFilter();
			filter.restoreState(getDialogSettings());
		}
		return filter;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerView#getFiltersDialog()
	 */
	protected FiltersDialog getFiltersDialog() {
		if (getFilter() != null && getFilter() instanceof BookmarkFilter) {
			return new BookmarkFiltersDialog(getSite().getShell(), (BookmarkFilter) getFilter());
		}
		return super.getFiltersDialog(); 
	}

	/**
	 * Sets the property on a marker to the given value.
	 *
	 * @exception CoreException if an error occurs setting the value
	 */
	protected void setMarkerMessage(IMarker marker, String property, Object value) {
		try {
			if (marker.getAttribute(property).equals(value)) {
				return;
			}
			if (property.equals(IMarker.MESSAGE)) { // Description
				marker.setAttribute(IMarker.MESSAGE, value);
			}
			MarkerFilter filter = getFilter();
			if (filter != null && !filter.select(marker)) {
				filtersChanged();
			}
		} catch (CoreException e) {
			String msg = Messages.getString("errorModifyingBookmark"); //$NON-NLS-1$
			ErrorDialog.openError(
				getSite().getShell(),
				msg,
				null,
				e.getStatus());
		}
	}

}
