/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.common;

import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.ui.internal.workbench.E4XMIResource;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

@SuppressWarnings("restriction")
public class CommandToStringConverter extends Converter {
	private final Messages Messages;

	public CommandToStringConverter(Messages Messages) {
		super(MCommand.class, String.class);
		this.Messages = Messages;
	}

	@Override
	public Object convert(Object fromObject) {
		final MCommand cmd = (MCommand) fromObject;
		String elementId = null;
		if (cmd != null && cmd.getElementId() != null && cmd.getElementId().trim().length() > 0) {
			elementId = cmd.getElementId();
		}
		if (cmd == null) {
			return Messages.CommandToStringConverter_None;
		} else if (cmd.getCommandName() != null && cmd.getCommandName().trim().length() > 0) {
			return cmd.getCommandName() + (elementId != null ? " - " + elementId : ""); //$NON-NLS-1$//$NON-NLS-2$
		} else if (elementId != null) {
			return elementId;
		} else {
			final Resource res = ((EObject) cmd).eResource();
			if (res instanceof E4XMIResource) {
				final String v = ((E4XMIResource) res).getID((EObject) cmd);
				if (v != null && v.trim().length() > 0) {
					return v;
				}
			}
			return cmd.getClass().getSimpleName() + "@" + cmd.hashCode(); //$NON-NLS-1$
		}
	}
}