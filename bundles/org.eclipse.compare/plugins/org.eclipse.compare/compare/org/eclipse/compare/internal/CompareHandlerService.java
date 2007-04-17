/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.compare.ICompareContainer;
import org.eclipse.core.expressions.Expression;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ActiveShellExpression;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.services.IServiceLocator;

public class CompareHandlerService {
	
	private final List fActivations = new ArrayList();
	private IHandlerService fHandlerService;
	private final Expression fExpression;

	public static CompareHandlerService createFor(ICompareContainer container, Shell shell) {
		IServiceLocator serviceLocator = container.getServiceLocator();
		if (serviceLocator != null) {
			IHandlerService service = (IHandlerService)serviceLocator.getService(IHandlerService.class);
			if (service != null)
				return new CompareHandlerService(service, null);
		}
		if (container.getWorkbenchPart() == null && shell != null) {
			// We're in a dialog so we can use an active shell expression
			IHandlerService service = (IHandlerService)PlatformUI.getWorkbench().getService(IHandlerService.class);
			if (service != null) {
				Expression e = new ActiveShellExpression(shell);
				return new CompareHandlerService(service, e);
			}
		}
		return new CompareHandlerService(null, null);
	}
	
	private CompareHandlerService(IHandlerService handlerService,
			Expression expression) {
		fHandlerService = handlerService;
		fExpression = expression;
	}
	
	public void registerAction(IAction action, String commandId) {
		if (fHandlerService == null)
			return;
		action.setActionDefinitionId(commandId);
		IHandlerActivation activation;
		if (fExpression == null) {
			activation = fHandlerService.activateHandler(commandId, new ActionHandler(action));
		} else {
			activation = fHandlerService.activateHandler(commandId, new ActionHandler(action), fExpression);
		}
		if (activation != null) {
			fActivations .add(activation);
		}
	}
	
	public void dispose() {
		if (fHandlerService == null)
			return;
		fHandlerService.deactivateHandlers(fActivations);
		fActivations.clear();
		fHandlerService = null;
	}
}
