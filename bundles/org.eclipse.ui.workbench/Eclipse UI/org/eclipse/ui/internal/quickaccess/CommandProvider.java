/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.quickaccess;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;

/**
 * @since 3.3
 * 
 */
public class CommandProvider extends QuickAccessProvider {

	private Map idToElement;
	private IEvaluationContext contextSnapshot;
	private IHandlerService handlerService;
	
	public CommandProvider() {
		// initialize eagerly
		saveApplicationContext();
		getElements();
	}

	public String getId() {
		return "org.eclipse.ui.commands"; //$NON-NLS-1$
	}

	public QuickAccessElement getElementForId(String id) {
		getElements();
		return (CommandElement) idToElement.get(id);
	}

	public QuickAccessElement[] getElements() {
		if (idToElement == null) {
			idToElement = new HashMap();
			ICommandService commandService = (ICommandService) PlatformUI
					.getWorkbench().getService(ICommandService.class);
			final Collection commandIds = commandService.getDefinedCommandIds();
			final Iterator commandIdItr = commandIds.iterator();
			while (commandIdItr.hasNext()) {
				final String currentCommandId = (String) commandIdItr.next();
				final Command command = commandService
						.getCommand(currentCommandId);
				if (command != null && command.isHandled()
						&& command.isEnabled()) {
					try {
						Collection combinations = ParameterizedCommand
								.generateCombinations(command);
						for (Iterator it = combinations.iterator(); it
								.hasNext();) {
							ParameterizedCommand pc = (ParameterizedCommand) it.next();
							String id = pc.serialize();
							idToElement.put(id,
									new CommandElement(pc, id, this));
						}
					} catch (final NotDefinedException e) {
						// It is safe to just ignore undefined commands.
					}
				}
			}
		}
		return (QuickAccessElement[]) idToElement.values().toArray(
				new QuickAccessElement[idToElement.values().size()]);
	}

	public ImageDescriptor getImageDescriptor() {
		return WorkbenchImages
				.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJ_NODE);
	}

	public String getName() {
		return QuickAccessMessages.QuickAccess_Commands;
	}
	
	private void saveApplicationContext() {
		handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
		contextSnapshot = handlerService.createContextSnapshot(true);
	}
	
	IHandlerService getHandlerService() {
		return handlerService;
	}
	
	IEvaluationContext getContextSnapshot() {
		return contextSnapshot;
	}
}
