/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.handlers;

import java.util.Map;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.e4.core.commands.internal.HandlerServiceImpl;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.workbench.ui.internal.Activator;
import org.eclipse.e4.workbench.ui.internal.Policy;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.services.LegacyEvalContext;

/**
 * @since 3.5
 * 
 */
public class E4HandlerProxy {
	public HandlerActivation activation = null;
	private Command command;
	private IHandler handler;

	public E4HandlerProxy(Command command, IHandler handler) {
		this.command = command;
		this.handler = handler;
	}

	@CanExecute
	public boolean canExecute(IEclipseContext context) {
		return handler.isEnabled();
	}

	@Execute
	public void execute(IEclipseContext context) {
		Activator.trace(Policy.DEBUG_CMDS, "execute " + command + " and " //$NON-NLS-1$ //$NON-NLS-2$
				+ handler + " with: " + context, null); //$NON-NLS-1$
		LegacyEvalContext legacy = new LegacyEvalContext(context);
		ExecutionEvent event = new ExecutionEvent(command, (Map) context
				.get(HandlerServiceImpl.PARM_MAP), null, legacy);
		try {
			handler.execute(event);
		} catch (ExecutionException e) {
			WorkbenchPlugin.log("Failure during execution of " + command.getId(), e); //$NON-NLS-1$
		}
	}

	public IHandler getHandler() {
		return handler;
	}
}
