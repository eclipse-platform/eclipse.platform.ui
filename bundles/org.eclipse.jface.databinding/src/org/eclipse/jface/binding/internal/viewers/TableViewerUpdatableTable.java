/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.binding.internal.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.binding.ChangeEvent;
import org.eclipse.jface.binding.IUpdatableTable;
import org.eclipse.jface.binding.Updatable;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TableItem;

/**
 * @since 3.2
 * 
 */
public class TableViewerUpdatableTable extends Updatable implements
		IUpdatableTable {

	// TODO column names
	// TODO cell types other than strings - how can this be supported?
	// (might boil down to: how to customize which cell editor is being used)

	private final class CellModifier implements ICellModifier {
		public boolean canModify(Object element, String property) {
			return true;
		}

		public Object getValue(Object element, String property) {
			int column = Integer.parseInt(property);
			// TODO need to get rid of this linear search
			int row = elements.indexOf(element);
			return ((String[]) labels.get(row))[column];
		}

		public void modify(Object element, String property, Object value) {
			if (element instanceof TableItem) {
				TableItem tableItem = (TableItem) element;
				element = tableItem.getData();
			}
			int column = Integer.parseInt(property);
			// TODO need to get rid of this linear search
			int row = elements.indexOf(element);
			String[] strings = (String[]) labels.get(row);
			String oldValue = strings[column];
			strings[column] = (String) value;
			fireChangeEvent(ChangeEvent.CHANGE, oldValue, value, row);
			viewer.update(element, null);
		}
	}

	private final class LabelProvider implements ITableLabelProvider {
		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return true;
		}

		public void removeListener(ILabelProviderListener listener) {
		}

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			return ((String[]) labels.get(elements.indexOf(element)))[columnIndex];
		}
	}

	private final class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return elements.toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private final TableViewer viewer;

	private List elements = new ArrayList();

	/* list of arrays of labels */
	private List labels = new ArrayList();

	/**
	 * @param viewer
	 */
	public TableViewerUpdatableTable(TableViewer viewer) {
		this.viewer = viewer;
		viewer.setContentProvider(new ContentProvider());
		viewer.setLabelProvider(new LabelProvider());
		int columnCount = getColumnCount();
		CellEditor[] cellEditors = new CellEditor[columnCount];
		String[] columnProperties = new String[columnCount];
		TextCellEditor textCellEditor = new TextCellEditor(viewer.getTable());
		for (int i = 0; i < columnCount; i++) {
			cellEditors[i] = textCellEditor;
			columnProperties[i] = Integer.toString(i);
		}
		viewer.setCellEditors(cellEditors);
		viewer.setColumnProperties(columnProperties);
		viewer.setCellModifier(new CellModifier());
		viewer.setInput(this);
	}

	public int getSize() {
		return elements.size();
	}

	public int addElement(Object element, int index) {
		int position = doAddElementAndValues(element, index,
				createEmptyStringArray(getColumnCount()));
		viewer.add(element);
		return position;
	}

	private int getColumnCount() {
		return viewer.getTable().getColumnCount();
	}

	private String[] createEmptyStringArray(int columnCount) {
		String[] result = new String[columnCount];
		for (int i = 0; i < result.length; i++) {
			result[i] = ""; //$NON-NLS-1$
		}
		return null;
	}

	private int doAddElementAndValues(Object element, int index, String[] values) {
		int position = elements.size();
		if (index < 0 || index > elements.size()) {
			position = elements.size();
			elements.add(element);
			labels.add(values);
		} else {
			elements.add(index, element);
			labels.add(index, values);
			position = index;
		}
		return position;
	}

	public void removeElement(int index) {
		Object element = elements.remove(index);
		viewer.remove(element);
		labels.remove(index);
	}

	public void setElement(int index, Object element) {
		if (elements.get(index).equals(element)) {
			viewer.update(element, null);
		} else {
			removeElement(index);
			addElement(element, index);
		}
	}

	public Object getElement(int index) {
		return elements.get(index);
	}

	public Class getElementType() {
		return Object.class;
	}

	public Class[] getColumnTypes() {
		Class[] result = new Class[getColumnCount()];
		for (int i = 0; i < result.length; i++) {
			result[i] = String.class;
		}
		return result;
	}

	public Object[] getValues(int index) {
		return (String[]) labels.get(index);
	}

	public void setElementAndValues(int index, Object element, Object[] values) {
		if (elements.get(index).equals(element)) {
			setValues(index, values);
		} else {
			removeElement(index);
			addElementWithValues(index, element, values);
		}
	}

	public int addElementWithValues(int index, Object element, Object[] values) {
		String[] stringValues = new String[values.length];
		System.arraycopy(values, 0, stringValues, 0, values.length);
		int position = doAddElementAndValues(element, index, stringValues);
		viewer.add(element);
		return position;
	}

	public void setValues(int index, Object[] values) {
		String[] stringValues = new String[values.length];
		System.arraycopy(values, 0, stringValues, 0, values.length);
		labels.set(index, stringValues);
		viewer.update(elements.get(index), null);
	}

}
