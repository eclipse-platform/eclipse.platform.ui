/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.views.markers;

import java.text.CollationKey;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.ide.Policy;
import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.MarkerItem;

/**
 * MarkerDescriptionField is the field for showing the description of a marker.
 *
 * @since 3.4
 */
public class MarkerDescriptionField extends MarkerField {

	private static class DescriptionEditingSupport extends EditingSupport {

		private TextCellEditor editor;

		/**
		 * Create a new instance of the receiver.
		 */
		public DescriptionEditingSupport(ColumnViewer viewer) {
			super(viewer);
			this.editor = new TextCellEditor((Composite) viewer.getControl());
		}

		@Override
		protected boolean canEdit(Object element) {
			if (element instanceof MarkerEntry) {

				MarkerEntry entry = (MarkerEntry) element;
				// Bookmarks are a special case
				try {
					if (entry.getMarker() != null
							&& entry.getMarker().isSubtypeOf(IMarker.BOOKMARK))
						return true;
				} catch (CoreException e) {
					Policy.handle(e);
					return false;
				}
				return entry.getAttributeValue(IMarker.USER_EDITABLE, false);
			}
			return false;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return editor;
		}

		@Override
		protected Object getValue(Object element) {
			return ((MarkerEntry) element).getAttributeValue(IMarker.MESSAGE,
					MarkerSupportInternalUtilities.EMPTY_STRING);
		}

		@Override
		protected void setValue(Object element, Object value) {
			MarkerEntry entry = (MarkerEntry) element;
			try {
				entry.getMarker().setAttribute(IMarker.MESSAGE, value);
			} catch (CoreException e) {
				Policy.handle(e);
			}

		}

	}

	/**
	 * Create a new instance of the receiver.
	 */
	public MarkerDescriptionField() {
		super();
	}

	@Override
	public int compare(MarkerItem item1, MarkerItem item2) {
		return getDescriptionKey(item1).compareTo(getDescriptionKey(item2));
	}

	@Override
	public int getDefaultColumnWidth(Control control) {
		return 50 * MarkerSupportInternalUtilities.getFontWidth(control);
	}

	/**
	 * Return the collation key for the description.
	 *
	 * @return CollationKey
	 */
	private CollationKey getDescriptionKey(Object element) {
		if (element instanceof MarkerEntry)
			return ((MarkerEntry) element).getCollationKey(IMarker.MESSAGE,
					MarkerSupportInternalUtilities.UNKNOWN_ATRRIBTE_VALUE_STRING);
		return MarkerSupportInternalUtilities.EMPTY_COLLATION_KEY;
	}

	@Override
	public String getValue(MarkerItem item) {
		return item.getAttributeValue(IMarker.MESSAGE,
				MarkerSupportInternalUtilities.UNKNOWN_ATRRIBTE_VALUE_STRING);
	}

	@Override
	public EditingSupport getEditingSupport(ColumnViewer viewer) {
		return new DescriptionEditingSupport(viewer);
	}
}
