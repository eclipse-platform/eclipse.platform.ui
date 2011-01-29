/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs;

import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.swt.widgets.Shell;

public class KeyBindingCommandSelectionDialog extends AbstractCommandSelectionDialog {
	private MKeyBinding binding;

	public KeyBindingCommandSelectionDialog(Shell parentShell, MKeyBinding binding, IModelResource resource, Messages Messages) {
		super(parentShell, resource, Messages);
		this.binding = binding;
	}

	@Override
	protected String getShellTitle() {
		return Messages.KeyBindingCommandSelectionDialog_ShellTitle;
	}

	@Override
	protected String getDialogTitle() {
		return Messages.KeyBindingCommandSelectionDialog_DialogTitle;
	}

	@Override
	protected String getDialogMessage() {
		return Messages.KeyBindingCommandSelectionDialog_DialogMessage;
	}

	@Override
	protected Command createStoreCommand(EditingDomain editingDomain, MCommand command) {
		return SetCommand.create(editingDomain, binding, CommandsPackageImpl.Literals.KEY_BINDING__COMMAND, command);
	}
}