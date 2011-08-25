/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.handlers.IHandlerService;

public class MakeHandlersGo extends AbstractHandler {

	private IWorkbench workbench;

	public MakeHandlersGo(IWorkbench wb) {
		workbench = wb;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands
	 * .ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		WorkbenchPlugin.log("Calling \"" + event.getCommand() //$NON-NLS-1$
				+ "\" command directly"); //$NON-NLS-1$
		Object obj = event.getApplicationContext();
		if (obj instanceof IEvaluationContext) {
			IHandlerService hs = (IHandlerService) workbench.getService(IHandlerService.class);
			if (hs != null) {
				ParameterizedCommand pcmd = ParameterizedCommand.generateCommand(
						event.getCommand(), event.getParameters());
				if (pcmd != null) {
					Event e = null;
					if (event.getTrigger() instanceof Event) {
						e = (Event) event.getTrigger();
					}
					try {
						return hs.executeCommandInContext(pcmd, e, (IEvaluationContext) obj);
					} catch (NotDefinedException e1) {
						// Because of the expectations of 3.x, this should
						// go nowhere
					} catch (NotEnabledException e1) {
						// Because of the expectations of 3.x, this should
						// go nowhere
					} catch (NotHandledException e1) {
						// Because of the expectations of 3.x, this should
						// go nowhere
					}
				}
			}
		}
		return null;
	}

}