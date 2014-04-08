/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	private IProject project;
	private MApplicationElement element;
	private EStructuralFeature feature;
	private EditingDomain editingDomain;
	// private Map<IFile, Image> icons = Collections.synchronizedMap(new
	// HashMap<IFile, Image>());

	protected Messages Messages;

	public AbstractIconDialog(Shell parentShell, IEclipseContext context, IProject project, EditingDomain editingDomain, MApplicationElement element, EStructuralFeature feature, Messages Messages) {
		super(parentShell, context);
		this.editingDomain = editingDomain;
		this.element = element;
		this.feature = feature;
		this.project = project;
		this.Messages = Messages;
		context.set(Messages.class, Messages);
	}

	@Override
	protected String getFilterTextMessage() {
		return "Type to start search";
	}

	@Override
	protected String getResourceNameText() {
		return "Icon Name";
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
