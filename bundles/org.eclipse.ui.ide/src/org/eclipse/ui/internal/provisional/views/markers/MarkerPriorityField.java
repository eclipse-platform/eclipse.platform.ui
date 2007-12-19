/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.provisional.views.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.ide.StatusUtil;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerField;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerItem;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerSupportConstants;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * MarkerPriorityField is the field for task priority.
 * 
 * @since 3.4
 * 
 */
public class MarkerPriorityField extends MarkerField {

	static final String HIGH_PRIORITY_IMAGE_PATH = "$nl$/icons/full/obj16/hprio_tsk.gif"; //$NON-NLS-1$

	static final String LOW_PRIORITY_IMAGE_PATH = "$nl$/icons/full/obj16/lprio_tsk.gif"; //$NON-NLS-1$

	private static String[] priorities = new String[] {
			MarkerMessages.priority_low, MarkerMessages.priority_normal,
			MarkerMessages.priority_high };

	private class PriorityEditingSupport extends EditingSupport {

		private ComboBoxCellEditor editor;

		/**
		 * Create a new instance of the receiver.
		 * 
		 * @param viewer
		 */
		public PriorityEditingSupport(ColumnViewer viewer) {
			super(viewer);
			this.editor = new ComboBoxCellEditor((Composite) viewer
					.getControl(), priorities, SWT.READ_ONLY);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.EditingSupport#canEdit(java.lang.Object)
		 */
		protected boolean canEdit(Object element) {
			if (element instanceof MarkerEntry)
				return ((MarkerEntry) element).getAttributeValue(
						IMarker.USER_EDITABLE, false);
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.EditingSupport#getCellEditor(java.lang.Object)
		 */
		protected CellEditor getCellEditor(Object element) {
			return editor;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.EditingSupport#getValue(java.lang.Object)
		 */
		protected Object getValue(Object element) {
			return new Integer(((MarkerEntry) element).getAttributeValue(
					IMarker.PRIORITY, IMarker.PRIORITY_NORMAL));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.EditingSupport#setValue(java.lang.Object,
		 *      java.lang.Object)
		 */
		protected void setValue(Object element, Object value) {
			MarkerEntry entry = (MarkerEntry) element;
			Integer integerValue = (Integer) value;
			try {
				entry.getMarker().setAttribute(IMarker.PRIORITY,
						integerValue.intValue());
			} catch (CoreException e) {
				StatusManager.getManager().handle(StatusUtil.newStatus(e));
			}

		}
	}

	/**
	 * Return a new priority field.
	 */
	public MarkerPriorityField() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerField#getColumnHeaderText()
	 */
	public String getColumnHeaderText() {
		return MarkerSupportConstants.EMPTY_STRING;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.api.MarkerField#getColumnTooltipText()
	 */
	public String getColumnTooltipText() {
		return MarkerMessages.priority_description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.api.MarkerField#getDefaultColumnWidth(org.eclipse.swt.widgets.Control)
	 */
	public int getDefaultColumnWidth(Control control) {
		return getHighPriorityImage().getBounds().width;
	}

	/**
	 * Get the image for high priority
	 * 
	 * @return Image
	 */
	private Image getHighPriorityImage() {
		return MarkerSupportInternalUtilities
				.createImage(HIGH_PRIORITY_IMAGE_PATH);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerField#getImage(org.eclipse.ui.internal.provisional.views.markers.MarkerItem)
	 */
	public Image getImage(MarkerItem item) {
		try {
			int priority = item.getAttributeValue(IMarker.PRIORITY,
					IMarker.PRIORITY_NORMAL);
			if (priority == IMarker.PRIORITY_HIGH) {
				return getHighPriorityImage();
			}
			if (priority == IMarker.PRIORITY_LOW) {
				return MarkerSupportInternalUtilities
						.createImage(LOW_PRIORITY_IMAGE_PATH);
			}
		} catch (NumberFormatException e) {
			return null;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerField#getValue(org.eclipse.ui.internal.provisional.views.markers.MarkerItem)
	 */
	public String getValue(MarkerItem item) {
		return MarkerSupportConstants.EMPTY_STRING;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.api.MarkerField#compare(org.eclipse.ui.internal.provisional.views.markers.api.MarkerItem,
	 *      org.eclipse.ui.internal.provisional.views.markers.api.MarkerItem)
	 */
	public int compare(MarkerItem item1, MarkerItem item2) {
		return item2.getAttributeValue(IMarker.PRIORITY,
				IMarker.PRIORITY_NORMAL)
				- item1.getAttributeValue(IMarker.PRIORITY,
						IMarker.PRIORITY_NORMAL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.api.MarkerField#getEditingSupport(org.eclipse.jface.viewers.ColumnViewer)
	 */
	public EditingSupport getEditingSupport(ColumnViewer viewer) {
		return new PriorityEditingSupport(viewer);
	}
}
