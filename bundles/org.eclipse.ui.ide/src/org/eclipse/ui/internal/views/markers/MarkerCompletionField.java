/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 430694
 *******************************************************************************/
package org.eclipse.ui.internal.views.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.ide.Policy;
import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.MarkerItem;

/**
 * MarkerCompletionField is the class that specifies the completion entry.
 *
 * @since 3.4
 *
 */
public class MarkerCompletionField extends MarkerField {

	private class CompletionEditingSupport extends EditingSupport {

		private CheckboxCellEditor editor;

		/**
		 * Create a new instance of the receiver.
		 *
		 * @param viewer
		 */
		public CompletionEditingSupport(ColumnViewer viewer) {
			super(viewer);
			this.editor = new CheckboxCellEditor((Composite) viewer
					.getControl());
		}

		@Override
		protected boolean canEdit(Object element) {
			if (element instanceof MarkerEntry)
				return ((MarkerEntry) element).getAttributeValue(
						IMarker.USER_EDITABLE, false);
			return false;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return editor;
		}

		@Override
		protected Object getValue(Object element) {
			return Boolean.valueOf(((MarkerEntry) element).getAttributeValue(
					IMarker.DONE, false));
		}

		@Override
		protected void setValue(Object element, Object value) {
			MarkerEntry entry = (MarkerEntry) element;
			Boolean booleanValue = (Boolean) value;
			try {
				entry.getMarker().setAttribute(IMarker.DONE,
						booleanValue.booleanValue());
			} catch (CoreException e) {
				Policy.handle(e);
			}

		}
	}

	static final String COMPLETE_IMAGE_PATH = "$nl$/icons/full/obj16/complete_tsk.png"; //$NON-NLS-1$

	static final String INCOMPLETE_IMAGE_PATH = "$nl$/icons/full/obj16/incomplete_tsk.png"; //$NON-NLS-1$
	private static final int DONE = 2;
	private static final int NOT_DONE = 1;

	private static final int UNDEFINED = 0;

	/**
	 * Create a new instance of the receiver.
	 */
	public MarkerCompletionField() {
		super();
	}

	@Override
	public int compare(MarkerItem item1, MarkerItem item2) {
		return getDoneConstant(item2) - getDoneConstant(item1);
	}

	@Override
	public String getColumnHeaderText() {
		return MarkerSupportInternalUtilities.EMPTY_STRING;
	}

	@Override
	public String getColumnTooltipText() {
		return getName();
	}

	/**
	 * Return the image for task completion.
	 *
	 * @return Image
	 */
	private Image getCompleteImage() {
		return MarkerSupportInternalUtilities.createImage(COMPLETE_IMAGE_PATH,
				getImageManager());
	}

	@Override
	public int getDefaultColumnWidth(Control control) {
		return 40;
	}

	/**
	 * Return the constant that indicates whether or not the receiver is done
	 *
	 * @param item
	 * @return 1 if it is done, 0 if it not and -1 if it cannot be determined.
	 */
	private int getDoneConstant(MarkerItem item) {

		int done = UNDEFINED;

		if (item.getMarker() != null
				&& item.getAttributeValue(IMarker.USER_EDITABLE, true)) {
			done = NOT_DONE;
			if (item.getAttributeValue(IMarker.DONE, false)) {
				done = DONE;
			}
		}
		return done;
	}

	@Override
	public EditingSupport getEditingSupport(ColumnViewer viewer) {

		return new CompletionEditingSupport(viewer);
	}

	/**
	 * Get the image for item.
	 *
	 * @param element
	 * @return Image
	 */
	private Image getImage(Object element) {

		switch (getDoneConstant((MarkerItem) element)) {
		case DONE:
			return getCompleteImage();
		case NOT_DONE:
			return MarkerSupportInternalUtilities
					.createImage(INCOMPLETE_IMAGE_PATH,getImageManager());
		default:
			return null;
		}
	}

	@Override
	public String getValue(MarkerItem item) {
		return MarkerSupportInternalUtilities.EMPTY_STRING;
	}

	@Override
	public void update(ViewerCell cell) {
		super.update(cell);
		cell.setImage(getImage(cell.getElement()));
	}
}
