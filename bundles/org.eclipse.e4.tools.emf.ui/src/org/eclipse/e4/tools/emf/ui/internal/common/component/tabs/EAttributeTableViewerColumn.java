/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 432555, Bug 436889
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.component.tabs;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.services.IResourcePool;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

/**
 * A table viewer column for editing an EMF EAttribute If the object does not
 * have an attribute with the given ID, the field is disabled.
 *
 * @author Steven Spungin
 *
 */
public class EAttributeTableViewerColumn {

	private TableViewerColumn tableViewerColumn;
	private IResourcePool resourcePool;
	private String attName;
	private TableViewer tvResults;

	public EAttributeTableViewerColumn(final TableViewer tvResults, String label, final String attName,
		final IEclipseContext context) {
		this.tvResults = tvResults;
		this.attName = attName;
		tableViewerColumn = new TableViewerColumn(tvResults, SWT.NONE);

		// CAN be null. Used for checkbox icon.
		resourcePool = context.get(IResourcePool.class);

		tableViewerColumn.getColumn().setText(label);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				final EObject eObject = (EObject) element;
				final EAttribute eAtt = EmfUtil.getAttribute(eObject, attName);
				Object value;
				if (eAtt != null) {
					value = eObject.eGet(eAtt);
				} else {
					value = ""; //$NON-NLS-1$
				}
				switch (EAttributeEditingSupport.getAttributeType(element, attName)) {
				case BOOLEAN:
					// if no icons provided, use text instead of checkbox
					return resourcePool == null && (Boolean) value ? "X" : ""; //$NON-NLS-1$ //$NON-NLS-2$
				case STRING:
				case NOT_AN_ATTRIBUTE:
				case OTHER:
				default:
					return super.getText(value);
				}
			}

			@Override
			public Image getImage(Object element) {
				switch (EAttributeEditingSupport.getAttributeType(element, attName)) {
				case BOOLEAN:
					if (resourcePool != null) {
						final Object value = EmfUtil.getAttributeValue((EObject) element, attName);
						if (value != null && value.equals(true)) {
							return resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_checkbox_obj);
						}
					}
					// fall through
					//$FALL-THROUGH$
				case STRING:
				case NOT_AN_ATTRIBUTE:
				case OTHER:
				default:
					return super.getImage(element);
				}
			}

			@Override
			public Color getBackground(Object element) {
				return EAttributeTableViewerColumn.this.getBackground(element);
			}
		});
		tableViewerColumn.setEditingSupport(new EAttributeEditingSupport(tvResults, attName, context));
	}

	public Color getBackground(Object element) {
		final EObject eObject = (EObject) element;
		final EAttribute eAtt = EmfUtil.getAttribute(eObject, attName);
		if (eAtt == null) {
			return tvResults.getTable().getDisplay().getSystemColor(SWT.COLOR_GRAY);
		}
		return null;
	}

	public void dispose() {
		tableViewerColumn.getColumn().dispose();
	}

	public TableViewerColumn getTableViewerColumn() {
		return tableViewerColumn;
	}

}
