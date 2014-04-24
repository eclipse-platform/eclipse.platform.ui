/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Steven Spungin <steven@spungin.tv> - Bug 433408
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.swt.widgets.Shell;

public class AreaIconDialogEditor extends AbstractIconDialog {

	public AreaIconDialogEditor(Shell parentShell, IProject project, EditingDomain editingDomain, MApplicationElement element, Messages Messages) {
		super(parentShell, project, editingDomain, element, UiPackageImpl.Literals.UI_LABEL__ICON_URI, Messages);
	}

	@Override
	protected String getShellTitle() {
		return Messages.AreaIconDialogEditor_ShellTitle;
	}

	@Override
	protected String getDialogTitle() {
		return Messages.AreaIconDialogEditor_DialogTitle;
	}

	@Override
	protected String getDialogMessage() {
		return Messages.AreaIconDialogEditor_DialogMessage;
	}

}
