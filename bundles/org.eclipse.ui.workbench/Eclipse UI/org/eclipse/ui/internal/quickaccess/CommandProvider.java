/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
import java.util.HashSet;
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
import org.eclipse.ui.internal.handlers.HandlerService;

/**
 * @since 3.3
 * 
 */
public class CommandProvider extends QuickAccessProvider {

	private Map idToElement;
	private IEvaluationContext contextSnapshot;
	private HandlerService realHandlerService;
	
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
			final Collection commandSet = new HashSet();
			final Iterator commandIdItr = commandIds.iterator();
			while (commandIdItr.hasNext()) {
				final String currentCommandId = (String) commandIdItr.next();
				final Command command = commandService
						.getCommand(currentCommandId);
				if (command != null && command.isHandled()
						&& command.isEnabled()) {
					try {
						commandSet.addAll(ParameterizedCommand
								.generateCombinations(command));
					} catch (final NotDefinedException e) {
						// It is safe to just ignore undefined commands.
					}
				}
			}
			ParameterizedCommand[] commands = (ParameterizedCommand[]) commandSet
					.toArray(new ParameterizedCommand[commandSet.size()]);
			for (int i = 0; i < commands.length; i++) {
				CommandElement commandElement = new CommandElement(commands[i],
						this);
				idToElement.put(commandElement.getId(), commandElement);
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
		realHandlerService = (HandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
		contextSnapshot = realHandlerService.getContextSnapshot();
	}
	
	HandlerService getRealHandlerService() {
		return realHandlerService;
	}
	
	IEvaluationContext getContextSnapshot() {
		return contextSnapshot;
	}
}
