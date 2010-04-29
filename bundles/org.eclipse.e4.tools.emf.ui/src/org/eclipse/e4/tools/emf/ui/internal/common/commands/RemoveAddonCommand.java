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

import java.util.List;

import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.domain.EditingDomain;

public class RemoveAddonCommand {
	public void execute(EditingDomain editingDomain, List<MAddon> addons) {
		EObject parent = ((EObject)addons.get(0)).eContainer();
		for( MAddon a : addons ) {
			if( parent != ((EObject)a).eContainer() ) {
				throw new IllegalArgumentException("The addons all have to belong to the same parent container");
			}
		}
		Command cmd = RemoveCommand.create(editingDomain, parent, ApplicationPackageImpl.Literals.APPLICATION__ADDONS, addons);
		if( cmd.canExecute() ) {
			editingDomain.getCommandStack().execute(cmd);
		}
	}
}
