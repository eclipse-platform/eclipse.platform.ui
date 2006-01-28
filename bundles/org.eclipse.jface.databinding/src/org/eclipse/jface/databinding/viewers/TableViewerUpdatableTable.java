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

package org.eclipse.jface.databinding.viewers;

import java.util.Collection;

import org.eclipse.jface.databinding.ICellProvider;
import org.eclipse.jface.databinding.IUpdatableTable;
import org.eclipse.jface.databinding.updatables.WritableSet;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.graphics.Image;

/**
 * @since 3.2
 *
 */
public class TableViewerUpdatableTable extends WritableSet implements
		IUpdatableTable {
	
	private final class LabelProvider extends ViewerLabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			Object cellValue = cellProvider.getCellValue(element, columnIndex);
			if (cellValue instanceof ViewerLabel) {
				return ((ViewerLabel)cellValue).getImage();
			}
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			Object cellValue = cellProvider.getCellValue(element, columnIndex);
			if (cellValue instanceof ViewerLabel) {
				return ((ViewerLabel)cellValue).getText();
			}
			return cellValue.toString();
		}
	}

	private final TableViewer tableViewer;
	private UpdatableSetContentProvider contentProvider;
	private ICellProvider cellProvider;

	public TableViewerUpdatableTable(TableViewer tableViewer) {
		this.tableViewer = tableViewer;
		contentProvider = new UpdatableSetContentProvider();
		tableViewer.setContentProvider(contentProvider);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.ITableThing#setCellProvider(org.eclipse.jface.databinding.ICellProvider)
	 */
	public void setCellProvider(ICellProvider cellProvider) {
		this.cellProvider = cellProvider;
		tableViewer.setLabelProvider(new LabelProvider());
		// TODO set cell editor, cell modifier etc. to make table editable
		tableViewer.setInput(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.updatables.WritableSet#addAll(java.util.Collection)
	 */
	public void addAll(Collection toAdd) {
		super.addAll(toAdd);
		tableViewer.add(toAdd.toArray());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.updatables.WritableSet#addAll(java.util.Collection)
	 */
	public void removeAll(Collection toRemove) {
		super.removeAll(toRemove);
		tableViewer.remove(toRemove.toArray());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.ITableThing#updateElement(java.lang.Object)
	 */
	public void updateElements(Object[] elements) {
		tableViewer.update(elements, null);
	}}
