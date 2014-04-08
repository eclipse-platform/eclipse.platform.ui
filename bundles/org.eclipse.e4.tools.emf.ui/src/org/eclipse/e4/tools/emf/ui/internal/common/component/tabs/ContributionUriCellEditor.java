/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 432555
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.component.tabs;

import org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.dialogs.BundleClassDialog;
import org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.dialogs.IconDialog;
import org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.dialogs.UriDialog;
import org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.dialogs.UriDialogType;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A cell editor that when selected offers the option to edit, view, or goto the
 * item.
 *
 * @author Steven Spungin
 *
 */
public class ContributionUriCellEditor extends DialogCellEditor {

	private UriDialog dlg;

	public ContributionUriCellEditor(Composite parent, int style, IEclipseContext context, UriDialogType dialogType) {
		super(parent, style);
		switch (dialogType) {
		case ICON:
			dlg = new IconDialog(parent.getShell(), context);
			break;
		case BUNDLECLASS:
		default:
			dlg = new BundleClassDialog(parent.getShell(), context);
			break;
		}
	}

	@Override
	protected Object openDialogBox(Control cellEditorWindow) {
		dlg.setUri((String) getValue());
		if (dlg.open() == Dialog.OK) {
			return dlg.getUri();
		} else {
			return getValue();
		}
	}
}
