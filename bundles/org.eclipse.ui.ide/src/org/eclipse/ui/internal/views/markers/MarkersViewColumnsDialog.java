/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.views.markers.MarkerField;

/**
 * MarkersViewSettingDialog is the dialog for showing marker preferences.
 * 
 * 
 * @since 3.7
 * @author Hitesh Soliwal
 * 
 */
public class MarkersViewColumnsDialog extends ViewerColumnsDialog {

	private ExtendedMarkersView extendedView;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param view
	 *            - the view this is being launched from
	 */
	public MarkersViewColumnsDialog(ExtendedMarkersView view) {
		super(view.getSite().getShell());
		this.extendedView = view;
		initialize(false);
		setLimitValue(IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.getInt(IDEInternalPreferences.MARKER_LIMITS_VALUE));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets
	 * .Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(JFaceResources.getString("ConfigureColumnsDialog_Title")); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#getShellStyle()
	 */
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.RESIZE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets
	 * .Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Control control = super.createDialogArea(parent);
		return control;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		IDEWorkbenchPlugin
				.getDefault()
				.getPreferenceStore()
				.setValue(IDEInternalPreferences.MARKER_LIMITS_VALUE,
						getLimitValue());
		IDEWorkbenchPlugin.getDefault().savePluginPreferences();
		extendedView.setVisibleFields(getVisibleFields(), getNewWidths());
		super.okPressed();
	}

	/**
	 */
	private int[] getNewWidths() {
		List visible = getVisible();
		int[] widths = new int[visible.size()];
		int i = 0;
		Iterator iterator = visible.iterator();
		while (iterator.hasNext()) {
			widths[i] = ((FieldEntry) iterator.next()).width;
			i++;
		}
		return widths;
	}

	private Collection getVisibleFields() {
		List visible = getVisible();
		List list = new ArrayList(visible.size());
		Iterator iterator = visible.iterator();
		while (iterator.hasNext()) {
			list.add(((FieldEntry) iterator.next()).field);
		}
		return list;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.preferences.ViewSettingsDialog#performDefaults()
	 */
	protected void performDefaults() {
		initialize(true);
		setLimitValue(IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.getDefaultInt(IDEInternalPreferences.MARKER_LIMITS_VALUE));
		super.performDefaults();
	}

	/**
	 */
	void initialize(boolean defaultWidths) {
		MarkerField[] allFields = extendedView.getBuilder().getGenerator()
				.getAllFields();
		MarkerField[] visibleFields = extendedView.getBuilder().getGenerator()
				.getVisibleFields();

		List visible = getVisible();
		List nonVisible = getNonVisible();
		visible.clear();
		nonVisible.clear();

		FieldEntry entry = null;
		for (int i = 0; i < allFields.length; i++) {
			if (!contains(visibleFields, allFields[i])) {
				entry = new FieldEntry(allFields[i], -1);
				if (defaultWidths) {
					entry.width = extendedView.getFieldWidth(entry.field, 0,
							true);
				} else {
					entry.width = extendedView.getFieldWidth(entry.field, -1,
							true);
				}
				entry.visible = false;
				nonVisible.add(entry);
			}
		}
		for (int i = 0; i < visibleFields.length; i++) {
			entry = new FieldEntry(visibleFields[i], -1);
			if (defaultWidths) {
				entry.width = extendedView.getFieldWidth(entry.field, 0, true);
			} else {
				entry.width = extendedView.getFieldWidth(entry.field, -1, true);
			}
			entry.visible = true;
			visible.add(entry);
		}

	}

	/**
	 * @param visibleFields
	 * @param field
	 */
	private boolean contains(MarkerField[] visibleFields, MarkerField field) {
		for (int i = 0; i < visibleFields.length; i++) {
			if (visibleFields[i].equals(field)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.views.markers.ViewerColumnsDialog#getLabelProvider
	 * ()
	 */
	protected ITableLabelProvider getLabelProvider() {
		return new TableLabelProvider() {
			public String getText(Object element) {
				return ((FieldEntry) element).field.getName();
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.views.markers.ViewerColumnsDialog#
	 * getColumnInfoProvider()
	 */
	protected IColumnInfoProvider getColumnInfoProvider() {

		return new IColumnInfoProvider() {
			public int getColumnIndex(Object columnObj) {
				return getVisible().indexOf(columnObj);
			}

			public int getColumnWidth(Object columnObj) {
				FieldEntry field = (FieldEntry) columnObj;
				return extendedView.getFieldWidth(field.field, field.width,
						true);
			}

			public boolean isColumnVisible(Object columnObj) {
				return ((FieldEntry) columnObj).visible;
			}

			public boolean isColumnMovable(Object columnObj) {
				return true;
			}

			public boolean isColumnResizable(Object columnObj) {
				return true;
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.views.markers.ViewerColumnsDialog#getColumnUpdater
	 * ()
	 */
	protected IColumnUpdater getColumnUpdater() {

		return new IColumnUpdater() {
			public void setColumnVisible(Object columnObj, boolean visible) {
				((FieldEntry) columnObj).visible = visible;
			}

			public void setColumnMovable(Object columnObj, boolean movable) {
				// not implemented
			}

			public void setColumnIndex(Object columnObj, int index) {
				// ignore
			}

			public void setColumnResizable(Object columnObj, boolean resizable) {
				// ignore
			}

			public void setColumnWidth(Object columnObj, int newWidth) {
				((FieldEntry) columnObj).width = newWidth;
			}
		};
	}

	static class FieldEntry {
		final MarkerField field;
		int width;
		boolean visible;

		FieldEntry(MarkerField field, int width) {
			this.field = field;
			this.width = width;
			visible = false;
		}
	}
}
