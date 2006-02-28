/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.nonapi.viewers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.internal.databinding.api.observable.mapping.IMapping;
import org.eclipse.jface.internal.databinding.api.observable.set.IObservableSetWithLabels;
import org.eclipse.jface.internal.databinding.api.observable.set.ObservableSet;
import org.eclipse.jface.internal.databinding.api.observable.set.SetDiff;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * @since 3.2
 * 
 */
public class TableViewerObservableSetWithLabels extends ObservableSet implements
		IObservableSetWithLabels {

	private TableViewer tableViewer;

	private ContentProvider contentProvider = new ContentProvider();

	private TabelLabelProvider labelProvider = new TabelLabelProvider();

	private IMapping labelMapping;

	private class ContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			return TableViewerObservableSetWithLabels.this.toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

	private class TabelLabelProvider implements ITableLabelProvider,
			ITableColorProvider, ITableFontProvider {

		ListenerList listeners = new ListenerList();

		private Object getColumnValue(Object element, int columnIndex) {
			Object mappingValue = labelMapping.getMappingValue(element);
			Object[] mappingValues;
			if (mappingValue instanceof Object[]) {
				mappingValues = (Object[]) mappingValue;
			} else {
				mappingValues = new Object[] { mappingValue };
			}
			if (columnIndex < mappingValues.length) {
				return mappingValues[columnIndex];
			}
			return null;
		}

		private ViewerLabel getColumnValueAsViewerLabel(Object element,
				int columnIndex) {
			Object columnValue = getColumnValue(element, columnIndex);
			if (columnValue instanceof ViewerLabel) {
				return (ViewerLabel) columnValue;
			}
			return null;
		}

		public Image getColumnImage(Object element, int columnIndex) {
			ViewerLabel columnValue = getColumnValueAsViewerLabel(element,
					columnIndex);
			return columnValue == null ? null : columnValue.getImage();
		}

		public String getColumnText(Object element, int columnIndex) {
			Object columnValue = getColumnValue(element, columnIndex);
			if (columnValue instanceof ViewerLabel) {
				return ((ViewerLabel) columnValue).getText();
			} else if (columnValue != null) {
				return columnValue.toString();
			}
			return ""; //$NON-NLS-1$
		}

		public void addListener(ILabelProviderListener listener) {
			listeners.add(listener);
		}

		public void dispose() {
			listeners.clear();
		}

		public boolean isLabelProperty(Object element, String property) {
			return true;
		}

		public void removeListener(ILabelProviderListener listener) {
			listeners.remove(listener);
		}

		private void fireLabelsChanged(Object[] elements) {
			LabelProviderChangedEvent event = new LabelProviderChangedEvent(
					this, elements);
			Object[] listenerArray = listeners.getListeners();
			for (int i = 0; i < listenerArray.length; i++) {
				ILabelProviderListener listener = (ILabelProviderListener) listenerArray[i];
				listener.labelProviderChanged(event);
			}
		}

		public Color getForeground(Object element, int columnIndex) {
			ViewerLabel columnValue = getColumnValueAsViewerLabel(element,
					columnIndex);
			return columnValue == null ? null : columnValue.getForeground();
		}

		public Color getBackground(Object element, int columnIndex) {
			ViewerLabel columnValue = getColumnValueAsViewerLabel(element,
					columnIndex);
			return columnValue == null ? null : columnValue.getBackground();
		}

		public Font getFont(Object element, int columnIndex) {
			ViewerLabel columnValue = getColumnValueAsViewerLabel(element,
					columnIndex);
			return columnValue == null ? null : columnValue.getFont();
		}

	}

	TableViewerObservableSetWithLabels(TableViewer tableViewer) {
		super(new HashSet());
		this.tableViewer = tableViewer;
	}

	public void init(IMapping labelMapping) {
		this.labelMapping = labelMapping;
		tableViewer.setContentProvider(contentProvider);
		tableViewer.setLabelProvider(labelProvider);
		tableViewer.setInput(this);
	}

	public void updateElements(Object[] elements) {
		labelProvider.fireLabelsChanged(elements);
	}

	public boolean add(Object o) {
		boolean added = wrappedSet.add(o);
		if (added) {
			fireSetChange(new SetDiff(Collections.singleton(o),
					Collections.EMPTY_SET));
			// add to table after firing
			tableViewer.add(o);
		}
		return added;
	}

	public boolean addAll(Collection c) {
		Set adds = new HashSet();
		Iterator it = c.iterator();
		while (it.hasNext()) {
			Object element = it.next();
			if (wrappedSet.add(element)) {
				adds.add(element);
			}
		}
		if (adds.size() > 0) {
			fireSetChange(new SetDiff(adds, Collections.EMPTY_SET));
			// add to table after firing
			tableViewer.add(adds.toArray());
			return true;
		}
		return false;
	}

	public boolean remove(Object o) {
		boolean removed = wrappedSet.remove(o);
		if (removed) {
			// remove from table before firing
			tableViewer.remove(o);
			fireSetChange(new SetDiff(Collections.EMPTY_SET, Collections
					.singleton(o)));
		}
		return removed;
	}

	public boolean removeAll(Collection c) {
		Set removes = new HashSet();
		Iterator it = c.iterator();
		while (it.hasNext()) {
			Object element = it.next();
			if (wrappedSet.remove(element)) {
				removes.add(element);
			}
		}
		if (removes.size() > 0) {
			// remove from table before firing
			tableViewer.remove(removes.toArray());
			fireSetChange(new SetDiff(Collections.EMPTY_SET, removes));
			return true;
		}
		return false;
	}

	public boolean retainAll(Collection c) {
		Set removes = new HashSet();
		Iterator it = wrappedSet.iterator();
		while (it.hasNext()) {
			Object element = it.next();
			if (!c.contains(element)) {
				it.remove();
				removes.add(element);
			}
		}
		if (removes.size() > 0) {
			// remove from table before firing
			tableViewer.remove(removes.toArray());
			fireSetChange(new SetDiff(Collections.EMPTY_SET, removes));
			return true;
		}
		return false;
	}

	public void clear() {
		Set removes = new HashSet(wrappedSet);
		wrappedSet.clear();
		// refresh before firing
		tableViewer.refresh();
		fireSetChange(new SetDiff(Collections.EMPTY_SET, removes));
	}

	public void dispose() {
		super.dispose();
		wrappedSet.clear();
		tableViewer = null;
		contentProvider = null;
		labelProvider = null;
		labelMapping = null;
	}

}
