/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Steven Spungin <steven@spungin.tv> - Bug 404136, Bug 424730
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.swt.widgets.Shell;

public abstract class AbstractIconDialog extends AbstractIconDialogWithScopeAndFilter {
	private final MApplicationElement element;
	private final EStructuralFeature feature;
	private final EditingDomain editingDomain;
	// private Map<IFile, Image> icons = Collections.synchronizedMap(new
	// HashMap<IFile, Image>());

	protected Messages Messages;

	public AbstractIconDialog(Shell parentShell, IEclipseContext context, IProject project, EditingDomain editingDomain, MApplicationElement element, EStructuralFeature feature, Messages Messages) {
		super(parentShell, context);
		this.editingDomain = editingDomain;
		this.element = element;
		this.feature = feature;
		this.Messages = Messages;
		context.set(Messages.class, Messages);
	}

	@Override
	protected String getFilterTextMessage() {
		return Messages.AbstractIconDialog_Type_To_Start_Search;
	}

	@Override
	protected String getResourceNameText() {
		return Messages.AbstractIconDialog_Icon_Name;
	}

	@Override
	protected void okPressed() {
		super.okPressed();
		String uri = getValue();
		if (uri != null) {
			Command cmd = SetCommand.create(editingDomain, element, feature, uri);
			if (cmd.canExecute()) {
				editingDomain.getCommandStack().execute(cmd);
			}
		}
	}
}
