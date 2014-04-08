/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 432555
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.component.tabs;

import org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.dialogs.UriDialogType;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;

// Restrictions: Only handles boolean and String types
class EAttributeEditingSupport extends EditingSupport {

	private String attName;
	private TableViewer tableViewer;
	private boolean wasNull;
	private IEclipseContext context;

	static public enum ATT_TYPE {
		STRING, BOOLEAN, INTEGER, NOT_AN_ATTRIBUTE, OTHER;
	}

	public EAttributeEditingSupport(TableViewer tableViewer, String attName, IEclipseContext context) {
		super(tableViewer);
		this.tableViewer = tableViewer;
		this.attName = attName;
		this.context = context;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		switch (getAttributeType(element, attName)) {
		case BOOLEAN:
			return new CheckboxCellEditor(tableViewer.getTable(), SWT.CHECK);
		case STRING:
		case INTEGER:
		default:
			if ("contributionURI".equals(attName)) { //$NON-NLS-1$
				return new ContributionUriCellEditor(tableViewer.getTable(), SWT.NONE, context, UriDialogType.BUNDLECLASS);
			}
			if ("iconURI".equals(attName)) { //$NON-NLS-1$
				return new ContributionUriCellEditor(tableViewer.getTable(), SWT.NONE, context, UriDialogType.ICON);
			}
			return new TextCellEditor(tableViewer.getTable(), SWT.NONE);
		}
	}

	static public ATT_TYPE getAttributeType(Object element, String attName) {
		EAttribute att = EmfUtil.getAttribute((EObject) element, attName);
		if (att == null) {
			return ATT_TYPE.NOT_AN_ATTRIBUTE;
		}
		String instanceTypeName = att.getEType().getInstanceTypeName();
		if (instanceTypeName.equals(String.class.getName())) {
			return ATT_TYPE.STRING;
		} else if (instanceTypeName.equals(boolean.class.getName())) {
			return ATT_TYPE.BOOLEAN;
		} else if (instanceTypeName.equals(int.class.getName())) {
			return ATT_TYPE.INTEGER;
		} else {
			return ATT_TYPE.OTHER;
		}
	}

	@Override
	protected boolean canEdit(Object element) {
		EAttribute att = EmfUtil.getAttribute((EObject) element, attName);
		if (att == null) {
			return false;
		} else {
			String instanceTypeName = att.getEType().getInstanceTypeName();
			if (instanceTypeName.equals(String.class.getName())) {
				return true;
			} else if (instanceTypeName.equals(boolean.class.getName())) {
				return true;
			} else if (instanceTypeName.equals(int.class.getName())) {
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	protected Object getValue(Object element) {
		EObject eObject = (EObject) element;
		Object value = eObject.eGet(EmfUtil.getAttribute(eObject, attName));
		switch (getAttributeType(element, attName)) {
		case BOOLEAN:
			wasNull = false;
			break;
		case INTEGER:
			if (value == null) {
				value = ""; //$NON-NLS-1$
				wasNull = true;
			} else {
				return Integer.toString((Integer) value);
			}
			break;
		case STRING:
		default:
			if (value == null) {
				value = ""; //$NON-NLS-1$
				wasNull = true;
			} else {
				wasNull = false;
			}
			break;
		}
		return value;
	}

	@Override
	protected void setValue(Object element, Object value) {
		EObject eObject = (EObject) element;
		switch (getAttributeType(element, attName)) {
		case INTEGER:
			if (value.equals("")) { //$NON-NLS-1$
				value = null;
			} else {
				try {
					value = Integer.parseInt(value.toString());
				} catch (Exception e) {
					return;
				}
			}
			break;
		case BOOLEAN:
		case STRING:
		default:
			if (value.equals("") && wasNull) { //$NON-NLS-1$
				value = null;
			}
			break;
		}
		Command cmd = SetCommand.create(context.get(EditingDomain.class), eObject, EmfUtil.getAttribute(eObject, attName), value);
		context.get(EditingDomain.class).getCommandStack().execute(cmd);
		TableViewerUtil.updateAndPack(tableViewer, eObject);
	}

	static public TableViewerColumn getTableViewerColumn(TableViewer viewer, int index) {
		return (TableViewerColumn) viewer.getTable().getColumn(index).getData(Policy.JFACE + ".columnViewer"); //$NON-NLS-1$

	}
}