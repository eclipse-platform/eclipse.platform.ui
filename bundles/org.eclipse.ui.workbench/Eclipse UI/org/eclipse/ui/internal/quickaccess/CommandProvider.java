/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 440810
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
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.e4.core.contexts.IEclipseContext;
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

	private IEvaluationContext currentSnapshot;

	void setSnapshot(IEvaluationContext c) {
		reset();
		currentSnapshot = c;
	}

	private Map idToElement;
	private IHandlerService handlerService;
	private ICommandService commandService;
	private EHandlerService ehandlerService;

	public CommandProvider() {
	}

	@Override
	public String getId() {
		return "org.eclipse.ui.commands"; //$NON-NLS-1$
	}

	@Override
	public QuickAccessElement getElementForId(String id) {
		getElements();
		return (CommandElement) idToElement.get(id);
	}

	@Override
	public QuickAccessElement[] getElements() {
		if (idToElement == null) {
			idToElement = new HashMap();
			ICommandService commandService = getCommandService();
			EHandlerService ehandlerService = getEHandlerService();
			final Collection commandIds = commandService.getDefinedCommandIds();
			final Iterator commandIdItr = commandIds.iterator();
			while (commandIdItr.hasNext()) {
				final String currentCommandId = (String) commandIdItr.next();
				final Command command = commandService
						.getCommand(currentCommandId);
				ParameterizedCommand pcmd = new ParameterizedCommand(command, null);
				if (command != null && ehandlerService.canExecute(pcmd)) {
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

	@Override
	public ImageDescriptor getImageDescriptor() {
		return WorkbenchImages
				.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJ_NODE);
	}

	@Override
	public String getName() {
		return QuickAccessMessages.QuickAccess_Commands;
	}

	EHandlerService getEHandlerService() {
		if (ehandlerService == null) {
			if (currentSnapshot instanceof ExpressionContext) {
				IEclipseContext ctx = ((ExpressionContext) currentSnapshot).eclipseContext;
				ehandlerService = ctx.get(EHandlerService.class);
			} else {
				ehandlerService = PlatformUI.getWorkbench().getService(
						EHandlerService.class);
			}
		}
		return ehandlerService;
	}

	ICommandService getCommandService() {
		if (commandService == null) {
			if (currentSnapshot instanceof ExpressionContext) {
				IEclipseContext ctx = ((ExpressionContext) currentSnapshot).eclipseContext;
				commandService = ctx.get(ICommandService.class);
			} else {
				commandService = PlatformUI.getWorkbench().getService(
						ICommandService.class);
			}
		}
		return commandService;
	}

	IHandlerService getHandlerService() {
		if (handlerService == null) {
			if (currentSnapshot instanceof ExpressionContext) {
				IEclipseContext ctx = ((ExpressionContext) currentSnapshot).eclipseContext;
				handlerService = ctx.get(IHandlerService.class);
			} else {
				handlerService = PlatformUI.getWorkbench().getService(
						IHandlerService.class);
			}
		}
		return handlerService;
	}

	IEvaluationContext getContextSnapshot() {
		return currentSnapshot;
	}

	@Override
	protected void doReset() {
		idToElement = null;
		if (currentSnapshot instanceof ExpressionContext) {
			((ExpressionContext) currentSnapshot).eclipseContext.dispose();
		}
		currentSnapshot = null;
	}
}
