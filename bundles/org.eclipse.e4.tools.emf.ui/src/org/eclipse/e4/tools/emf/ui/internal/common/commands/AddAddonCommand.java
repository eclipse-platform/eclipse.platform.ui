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
package org.eclipse.e4.tools.emf.ui.internal.common.commands;

import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.domain.EditingDomain;

public class AddAddonCommand {
	public void execute(EditingDomain editingDomain, MApplication parent) {
		MAddon command = MApplicationFactory.INSTANCE.createAddon();
		Command cmd = AddCommand.create(editingDomain, parent, ApplicationPackageImpl.Literals.APPLICATION__ADDONS, command);
		
		if( cmd.canExecute() ) {
			editingDomain.getCommandStack().execute(cmd);
		}
	}
}
