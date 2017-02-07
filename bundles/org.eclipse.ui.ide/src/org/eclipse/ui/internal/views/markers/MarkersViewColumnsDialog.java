/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
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
import org.eclipse.ui.internal.views.markers.MarkersViewColumnsDialog.FieldEntry;
import org.eclipse.ui.views.markers.MarkerField;

/**
 * MarkersViewSettingDialog is the dialog for showing marker preferences.
 *
 *
 * @since 3.7
 * @author Hitesh Soliwal
 *
 */
public class MarkersViewColumnsDialog extends ViewerColumnsDialog<FieldEntry> {

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
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(JFaceResources.getString("ConfigureColumnsDialog_Title")); //$NON-NLS-1$
	}

	@Override
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.RESIZE;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Control control = super.createDialogArea(parent);
		return control;
	}

	@Override
	protected void okPressed() {
		extendedView.setVisibleFields(getVisibleFields(), getNewWidths());
		super.okPressed();
	}

	private int[] getNewWidths() {
		List<FieldEntry> visible = getVisible();
		int[] widths = new int[visible.size()];
		int i = 0;
		Iterator<FieldEntry> iterator = visible.iterator();
		while (iterator.hasNext()) {
			widths[i] = iterator.next().width;
			i++;
		}
		return widths;
	}

	private Collection<MarkerField> getVisibleFields() {
		List<FieldEntry> visible = getVisible();
		ArrayList<MarkerField> list = new ArrayList<>(visible.size());
		Iterator<FieldEntry> iterator = visible.iterator();
		while (iterator.hasNext()) {
			list.add(iterator.next().field);
		}
		return list;
	}

	@Override
	protected void performDefaults() {
		initialize(true);
		super.performDefaults();
	}

	void initialize(boolean defaultWidths) {
		MarkerField[] allFields = extendedView.getBuilder().getGenerator().getAllFields();
		MarkerField[] visibleFields = null;
		if (defaultWidths) {
			visibleFields = extendedView.getBuilder().getGenerator().getInitialVisible();
		} else {
			visibleFields = extendedView.getBuilder().getGenerator().getVisibleFields();
		}
		List<FieldEntry> visible = getVisible();
		List<FieldEntry> nonVisible = getNonVisible();
		visible.clear();
		nonVisible.clear();
		FieldEntry entry = null;
		for (int i = 0; i < allFields.length; i++) {
			if (!contains(visibleFields, allFields[i])) {
				entry = new FieldEntry(allFields[i], -1);
				entry.width = extendedView.getFieldWidth(entry.field, defaultWidths ? 0 : -1, !defaultWidths);
				entry.visible = false;
				nonVisible.add(entry);
			}
		}
		for (MarkerField visibleField : visibleFields) {
			entry = new FieldEntry(visibleField, -1);
			entry.width = extendedView.getFieldWidth(entry.field, defaultWidths ? 0 : -1, !defaultWidths);
			entry.visible = true;
			visible.add(entry);
		}
	}

	private boolean contains(MarkerField[] visibleFields, MarkerField field) {
		for (MarkerField visibleField : visibleFields) {
			if (visibleField.equals(field)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected ITableLabelProvider getLabelProvider() {
		return new TableLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((FieldEntry) element).field.getName();
			}
		};
	}

	@Override
	protected IColumnInfoProvider<FieldEntry> getColumnInfoProvider() {

		return new IColumnInfoProvider<FieldEntry>() {
			@Override
			public int getColumnIndex(FieldEntry columnObj) {
				return getVisible().indexOf(columnObj);
			}

			@Override
			public int getColumnWidth(FieldEntry columnObj) {
				FieldEntry field = columnObj;
				if (field.width <= 0) {
					field.width = extendedView.getFieldWidth(field.field, field.width, false);
				}
				return field.width;
			}

			@Override
			public boolean isColumnVisible(FieldEntry columnObj) {
				return columnObj.visible;
			}

			@Override
			public boolean isColumnMovable(FieldEntry columnObj) {
				return true;
			}

			@Override
			public boolean isColumnResizable(FieldEntry columnObj) {
				return true;
			}
		};
	}

	@Override
	protected IColumnUpdater<FieldEntry> getColumnUpdater() {

		return new IColumnUpdater<FieldEntry>() {
			@Override
			public void setColumnVisible(FieldEntry columnObj, boolean visible) {
				columnObj.visible = visible;
			}

			@Override
			public void setColumnMovable(FieldEntry columnObj, boolean movable) {
				// not implemented
			}

			@Override
			public void setColumnIndex(FieldEntry columnObj, int index) {
				// ignore
			}

			@Override
			public void setColumnResizable(FieldEntry columnObj, boolean resizable) {
				// ignore
			}

			@Override
			public void setColumnWidth(FieldEntry columnObj, int newWidth) {
				columnObj.width = newWidth;
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((field == null) ? 0 : field.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
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
