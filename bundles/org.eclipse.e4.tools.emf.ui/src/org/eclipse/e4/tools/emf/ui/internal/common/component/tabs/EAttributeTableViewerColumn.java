/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.component.tabs;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * A table viewer column for editing an EMF EAttribute If the object does not
 * have an attribute with the given ID, the field is disabled.
 * 
 * @author Steven Spungin
 *
 */
public class EAttributeTableViewerColumn {

	public EAttributeTableViewerColumn(final TableViewer tvResults, String label, final String attName, final IEclipseContext context) {
		TableViewerColumn tlc = new TableViewerColumn(tvResults, SWT.NONE);

		tlc.getColumn().setText(label);
		tlc.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				EObject eObject = (EObject) element;
				EAttribute eAtt = EmfUtil.getAttribute(eObject, attName);
				Object value;
				if (eAtt != null) {
					value = eObject.eGet(eAtt);
				} else {
					value = ""; //$NON-NLS-1$
				}
				return super.getText(value);
			}

			@Override
			public Color getBackground(Object element) {
				EObject eObject = (EObject) element;
				EAttribute eAtt = EmfUtil.getAttribute(eObject, attName);
				if (eAtt == null) {
					return tvResults.getTable().getDisplay().getSystemColor(SWT.COLOR_GRAY);
				} else {
					return super.getBackground(element);
				}
			}
		});
		tlc.setEditingSupport(new EAttributeEditingSupport(tvResults, attName, context));
	}

}
