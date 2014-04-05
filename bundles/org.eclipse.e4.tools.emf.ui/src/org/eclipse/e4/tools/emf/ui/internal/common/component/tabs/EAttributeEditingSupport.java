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
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;

class EAttributeEditingSupport extends EditingSupport {

	private String attName;
	private TextCellEditor editor;
	private TableViewer tableViewer;
	private boolean wasNull;
	private IEclipseContext context;

	public EAttributeEditingSupport(TableViewer tableViewer, String attName, IEclipseContext context) {
		super(tableViewer);
		this.tableViewer = tableViewer;
		this.attName = attName;
		this.context = context;
		editor = new TextCellEditor(tableViewer.getTable(), SWT.NONE);
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return editor;
	}

	@Override
	protected boolean canEdit(Object element) {
		return EmfUtil.getAttribute((EObject) element, attName) != null;
	}

	@Override
	protected Object getValue(Object element) {
		EObject eObject = (EObject) element;
		Object value = eObject.eGet(EmfUtil.getAttribute(eObject, attName));
		if (value == null) {
			value = ""; //$NON-NLS-1$
			wasNull = true;
		} else {
			wasNull = false;
		}
		return value;
	}

	@Override
	protected void setValue(Object element, Object value) {
		EObject eObject = (EObject) element;
		if (value.equals("") && wasNull) { //$NON-NLS-1$
			value = null;
		}
		Command cmd = SetCommand.create(context.get(EditingDomain.class), eObject, EmfUtil.getAttribute(eObject, attName), value);
		context.get(EditingDomain.class).getCommandStack().execute(cmd);
		TableViewerUtil.updateAndPack(tableViewer, eObject);
	}

}