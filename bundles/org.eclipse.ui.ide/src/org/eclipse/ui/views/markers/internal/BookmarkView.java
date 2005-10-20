/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.part.CellEditorActionHandler;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The BookmarkView is the marker view for bookmarks.
 *
 */
public class BookmarkView extends MarkerView {

	private final ColumnPixelData[] DEFAULT_COLUMN_LAYOUTS = {
			new ColumnPixelData(200), new ColumnPixelData(75),
			new ColumnPixelData(150), new ColumnPixelData(60) };

	private final IField[] HIDDEN_FIELDS = { new FieldCreationTime() };

	private final static String[] ROOT_TYPES = { IMarker.BOOKMARK };

	private final static String[] TABLE_COLUMN_PROPERTIES = { IMarker.MESSAGE,
			"", //$NON-NLS-1$
			"", //$NON-NLS-1$
			"" //$NON-NLS-1$
	};

	private final static String TAG_DIALOG_SECTION = "org.eclipse.ui.views.bookmark"; //$NON-NLS-1$

	private final IField[] VISIBLE_FIELDS = { new FieldMessage(),
			new FieldResource(), new FieldFolder(), new FieldLineNumber() };

	private ICellModifier cellModifier = new ICellModifier() {
		public Object getValue(Object element, String property) {
			if (element instanceof ConcreteMarker
					&& IMarker.MESSAGE.equals(property))
				return ((ConcreteMarker) element).getDescription();
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

				if (data instanceof ConcreteMarker) {
					IMarker marker = ((ConcreteMarker) data).getMarker();

					try {
						if (!marker.getAttribute(property).equals(value)) {
							if (IMarker.MESSAGE.equals(property))
								marker.setAttribute(IMarker.MESSAGE, value);
						}
					} catch (CoreException e) {
						ErrorDialog
								.openError(
										getSite().getShell(),
										MarkerMessages.errorModifyingBookmark, null, e.getStatus()); 
					}
				}
			}
		}
	};

	private CellEditorActionHandler cellEditorActionHandler;

	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		// TODO: Check for possible reliance on IMarker
		TreeViewer treeViewer = getViewer();
		CellEditor cellEditors[] = new CellEditor[treeViewer.getTree()
				.getColumnCount()];
		CellEditor descriptionCellEditor = new TextCellEditor(treeViewer
				.getTree());
		cellEditors[0] = descriptionCellEditor;
		treeViewer.setCellEditors(cellEditors);
		treeViewer.setCellModifier(cellModifier);
		treeViewer.setColumnProperties(TABLE_COLUMN_PROPERTIES);

		cellEditorActionHandler = new CellEditorActionHandler(getViewSite()
				.getActionBars());
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

	protected ColumnPixelData[] getDefaultColumnLayouts() {
		return DEFAULT_COLUMN_LAYOUTS;
	}

	protected IDialogSettings getDialogSettings() {
		AbstractUIPlugin plugin = (AbstractUIPlugin) Platform
				.getPlugin(PlatformUI.PLUGIN_ID);
		IDialogSettings workbenchSettings = plugin.getDialogSettings();
		IDialogSettings settings = workbenchSettings
				.getSection(TAG_DIALOG_SECTION);

		if (settings == null)
			settings = workbenchSettings.addNewSection(TAG_DIALOG_SECTION);

		return settings;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.TableView#getSortingFields()
	 */
	protected IField[] getSortingFields() {
		IField[] visible = getVisibleFields();
		IField[] all = new IField[visible.length + HIDDEN_FIELDS.length];
		
		System.arraycopy(visible, 0, all, 0, visible.length);
		System.arraycopy(HIDDEN_FIELDS, 0, all, visible.length, HIDDEN_FIELDS.length);
		
		return all;
	}

	protected String[] getRootTypes() {
		return ROOT_TYPES;
	}

	protected IField[] getVisibleFields() {
		return VISIBLE_FIELDS;
	}

	public void setSelection(IStructuredSelection structuredSelection,
			boolean reveal) {
		// TODO: added because nick doesn't like public API inherited from
		// internal classes
		super.setSelection(structuredSelection, reveal);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getMarkerTypes()
	 */
	protected String[] getMarkerTypes() {
		return new String[] { IMarker.BOOKMARK };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#createFiltersDialog()
	 */
	protected DialogMarkerFilter createFiltersDialog() {

		MarkerFilter[] filters = getUserFilters();
		BookmarkFilter[] bookmarkFilters = new BookmarkFilter[filters.length];
		System.arraycopy(filters, 0, bookmarkFilters, 0, filters.length);
		return new DialogBookmarkFilter(getSite().getShell(), bookmarkFilters);
	}

	protected String getStaticContextId() {
		return PlatformUI.PLUGIN_ID + ".bookmark_view_context"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#createFilter(java.lang.String)
	 */
	protected MarkerFilter createFilter(String name) {
		return new BookmarkFilter(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getSectionTag()
	 */
	protected String getSectionTag() {
		return TAG_DIALOG_SECTION;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#fillContextMenuAdditions(org.eclipse.jface.action.IMenuManager)
	 */
	void fillContextMenuAdditions(IMenuManager manager) {
		//Do nothing in this view
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getMarkerEnablementPreferenceName()
	 */
	String getMarkerEnablementPreferenceName() {
		return IDEInternalPreferences.LIMIT_BOOKMARKS;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getMarkerLimitPreferenceName()
	 */
	String getMarkerLimitPreferenceName() {
		return IDEInternalPreferences.BOOKMARKS_LIMIT;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getFiltersPreferenceName()
	 */
	String getFiltersPreferenceName() {
		return IDEInternalPreferences.BOOKMARKS_FILTERS;
	}
	
}
