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

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * MarkersViewSettingDialog is the dialog for showing marker preferences.
 * 
 * 
 * @since 3.7
 * @author Hitesh Soliwal
 * 
 */
public class MarkersViewSettingDialog extends ViewerColumnsDialog {

	private ExtendedMarkersView extendedView;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param view
	 *            - the view this is being launched from
	 */
	public MarkersViewSettingDialog(ExtendedMarkersView view) {
		super(view.getSite().getShell());
		this.extendedView = view;
		initialize(convert(extendedView.getBuilder().getGenerator()
				.getVisibleFields()), convert(extendedView.getBuilder()
				.getGenerator().getAllFields()), false);
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
		newShell.setText(MarkerMessages.MarkerPreferences_DialogTitle);
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
			widths[i++] = ((FieldEntry) iterator.next()).width;
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
		initialize(convert(extendedView.getBuilder().getGenerator()
				.getInitialVisible()), convert(extendedView.getBuilder()
				.getGenerator().getAllFields()), true);
		setLimitValue(IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.getDefaultInt(IDEInternalPreferences.MARKER_LIMITS_VALUE));
		super.performDefaults();
	}

	/**
	 * @param visibleFields
	 * @param allFields
	 */
	void initialize(FieldEntry[] visibleFields, FieldEntry[] allFields,
			boolean defaultWidths) {
		List visible = getVisible();
		List nonVisible = getNonVisible();
		visible.clear();
		nonVisible.clear();
		for (int i = 0; i < allFields.length; i++) {
			nonVisible.add(allFields[i]);
		}
		for (int i = 0; i < visibleFields.length; i++) {
			nonVisible.remove(visibleFields[i]);
			visible.add(visibleFields[i]);
			visibleFields[i].visible = true;
			if (defaultWidths) {
				visibleFields[i].width = extendedView.getFieldWidth(
						visibleFields[i].field, 0);
			} else {
				// from a persistent store
				visibleFields[i].width = extendedView.getFieldWidth(
						visibleFields[i].field, -1);
			}
		}
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
				return extendedView.getFieldWidth(field.field, field.width);
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

	private static FieldEntry[] convert(Object[] fields) {
		FieldEntry[] entries = new FieldEntry[fields.length];
		for (int i = 0; i < entries.length; i++) {
			entries[i] = new FieldEntry((MarkerField) fields[i], -1);
		}
		return entries;
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			// TODO Auto-generated method stub
			return super.toString();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((field == null) ? 0 : field.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof FieldEntry)) {
				return false;
			}
			FieldEntry other = (FieldEntry) obj;
			if (field == null) {
				if (other.field != null) {
					return false;
				}
			} else if (!field.equals(other.field)) {
				return false;
			}
			return true;
		}
	}
}
