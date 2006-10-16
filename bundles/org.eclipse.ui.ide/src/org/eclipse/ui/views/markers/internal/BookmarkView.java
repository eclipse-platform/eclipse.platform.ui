/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - Fix for
 *     		Bug 154289 [Viewers] - NPE in TreeEditorImpl.activateCellEditor
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.UpdateMarkersOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.part.CellEditorActionHandler;

/**
 * The BookmarkView is the marker view for bookmarks.
 *
 */
public class BookmarkView extends MarkerView {

	private final IField[] HIDDEN_FIELDS = { new FieldCreationTime() };

	private final static String[] ROOT_TYPES = { IMarker.BOOKMARK };

	private final static String[] TABLE_COLUMN_PROPERTIES = {IMarker.MESSAGE,
		Util.EMPTY_STRING,
		Util.EMPTY_STRING,
		Util.EMPTY_STRING
	};

	private final static String TAG_DIALOG_SECTION = "org.eclipse.ui.views.bookmark"; //$NON-NLS-1$

	private final IField[] VISIBLE_FIELDS = {new FieldMessage(),
			new FieldResource(), new FieldFolder(), new FieldLineNumber() };

	private ICellModifier cellModifier = new ICellModifier() {
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
		 */
		public Object getValue(Object element, String property) {
			if (element instanceof ConcreteMarker
					&& IMarker.MESSAGE.equals(property)) {
				return ((ConcreteMarker) element).getDescription();
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
		 */
		public boolean canModify(Object element, String property) {
			return element instanceof ConcreteMarker && IMarker.MESSAGE.equals(property);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
		 */
		public void modify(Object element, String property, Object value) {
			if (element instanceof Item) {
				Item item = (Item) element;
				Object data = item.getData();

				if (data instanceof ConcreteMarker) {
					IMarker marker = ((ConcreteMarker) data).getMarker();

					try {
						if (!marker.getAttribute(property).equals(value)) {
							if (IMarker.MESSAGE.equals(property)) {
								Map attrs = new HashMap();
								attrs.put(IMarker.MESSAGE, value);
								IUndoableOperation op = new UpdateMarkersOperation(marker, attrs, MarkerMessages.modifyBookmark_title, true);
						           PlatformUI.getWorkbench().getOperationSupport().getOperationHistory().execute(
						        		   op, null, WorkspaceUndoUtil.getUIInfoAdapter(getSite().getShell()));
							}
						}
					} catch (ExecutionException e) {
						if (e.getCause() instanceof CoreException) {
							ErrorDialog.openError(
									getSite().getShell(),
									MarkerMessages.errorModifyingBookmark, null, ((CoreException)e.getCause()).getStatus()); 
						} else {
							// something rather unexpected occurred.
							IDEWorkbenchPlugin.log(MarkerMessages.errorModifyingBookmark, e); 
						}
					} catch (CoreException e) {
						ErrorDialog.openError(
								getSite().getShell(),
								MarkerMessages.errorModifyingBookmark, null, e.getStatus()); 
					}
				}
			}
		}
	};

	private CellEditorActionHandler cellEditorActionHandler;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

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
		cellEditorActionHandler.setUndoAction(undoAction);
		cellEditorActionHandler.setRedoAction(redoAction);
	}

	public void dispose() {
		if (cellEditorActionHandler != null) {
			cellEditorActionHandler.dispose();
		}

		super.dispose();
	}

	protected IDialogSettings getDialogSettings() {
		IDialogSettings workbenchSettings = IDEWorkbenchPlugin.getDefault().getDialogSettings();
		IDialogSettings settings = workbenchSettings
				.getSection(TAG_DIALOG_SECTION);

		if (settings == null) {
			settings = workbenchSettings.addNewSection(TAG_DIALOG_SECTION);
		}

		return settings;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.TableView#getSortingFields()
	 */
	protected IField[] getSortingFields() {
		IField[] all = new IField[VISIBLE_FIELDS.length + HIDDEN_FIELDS.length];
		
		System.arraycopy(VISIBLE_FIELDS, 0, all, 0, VISIBLE_FIELDS.length);
		System.arraycopy(HIDDEN_FIELDS, 0, all, VISIBLE_FIELDS.length, HIDDEN_FIELDS.length);
		
		return all;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.TableView#getAllFields()
	 */
	protected IField[] getAllFields() {
		return getSortingFields();
	}

	protected String[] getRootTypes() {
		return ROOT_TYPES;
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
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getMarkerName()
	 */
	protected String getMarkerName() {
		return MarkerMessages.bookmark_title;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getUndoContext()
	 */
	protected IUndoContext getUndoContext() {
		return WorkspaceUndoUtil.getBookmarksUndoContext();
	}
	
}
