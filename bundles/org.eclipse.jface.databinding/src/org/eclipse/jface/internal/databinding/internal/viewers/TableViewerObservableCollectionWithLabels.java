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

package org.eclipse.jface.internal.databinding.internal.viewers;

import org.eclipse.jface.internal.databinding.provisional.conversion.IConverter;
import org.eclipse.jface.internal.databinding.provisional.observable.mapping.IMultiMapping;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * @since 3.2
 * 
 */
public class TableViewerObservableCollectionWithLabels extends
		StructuredViewerObservableCollectionWithLabels {

	private TabelLabelProvider labelProvider = new TabelLabelProvider();

	private IMultiMapping labelMapping;
	
	private IConverter[] modelToTargetConverters;

	private class TabelLabelProvider implements ITableLabelProvider,
			ITableColorProvider, ITableFontProvider {

		ListenerList listeners = new ListenerList();

		private Object getColumnValue(Object element, int columnIndex) {			
			Object object = labelMapping.getMappingValues(element,
					new int[] { columnIndex })[0];
			return convertColumnValue(object, columnIndex);
		}
		
		/**
		 * @param object
		 * @param columnIndex
		 * @return converted value
		 */
		private Object convertColumnValue(Object object, int columnIndex) {
			if (modelToTargetConverters[0] != null) {
				if (modelToTargetConverters.length == 1) {
					return modelToTargetConverters[0].convert(object);
				}

				if (modelToTargetConverters.length >= columnIndex) {
					if (modelToTargetConverters[columnIndex] != null) {
						return modelToTargetConverters[columnIndex].convert(object);
					}
					return object;
				}
				throw new IllegalStateException("A converter was not specified for column index " + columnIndex); //$NON-NLS-1$
			}
			return object;
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

	/**
	 * @param tableViewer
	 */
	public TableViewerObservableCollectionWithLabels(TableViewer tableViewer) {
		super(tableViewer);
	}

	public void init(IMultiMapping labelMapping) {
		this.labelMapping = labelMapping;
		getViewer().setLabelProvider(labelProvider);
	}

	public void updateElements(Object[] elements) {
		labelProvider.fireLabelsChanged(elements);
	}

	public void dispose() {
		super.dispose();
		labelProvider = null;
		labelMapping = null;
	}

	protected void addToViewer(Object element) {
		((TableViewer) getViewer()).add(element);
	}

	protected void addToViewer(Object[] elements) {
		((TableViewer) getViewer()).add(elements);
	}

	protected void removeFromViewer(Object element) {
		((TableViewer) getViewer()).remove(element);
	}

	protected void removeFromViewer(Object[] elements) {
		((TableViewer) getViewer()).remove(elements);
	}

	protected void addToViewer(int index, Object element) {
		((TableViewer) getViewer()).insert(element, index);
	}

	/**
	 * @param converters
	 */
	public void setModelToTargetConverters(IConverter[] converters) {
		this.modelToTargetConverters = converters;
	}
}
