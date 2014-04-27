/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.help;

import java.util.Map;
import java.util.WeakHashMap;
import javax.inject.Inject;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.internal.commands.util.Util;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.internal.HandlerServiceImpl;
import org.eclipse.e4.core.commands.internal.ICommandHelpService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.ui.internal.handlers.E4HandlerProxy;

/**
 * @since 3.5
 */
@SuppressWarnings("restriction")
public class CommandHelpServiceImpl implements ICommandHelpService {

	@Inject
	private ECommandService commandService;

	@Inject
	@Optional
	private Logger logger;

	private Map<IHandler, String> helpContextIdsByHandler = new WeakHashMap<IHandler, String>();

	@Override
	public String getHelpContextId(String commandId, IEclipseContext context) {
		if (commandId == null || context == null) {
			return null;
		}

		Command command = commandService.getCommand(commandId);
		if (!command.isDefined()) {
			if (logger != null) {
				logger.error("The command " + commandId //$NON-NLS-1$
						+ " is not defined. Help context ID cannot be determined."); //$NON-NLS-1$
			}
			return null;
		}

		IHandler handler = null;
		Object obj = HandlerServiceImpl.lookUpHandler(context, commandId);
		if (obj instanceof IHandler) {
			handler = (IHandler) obj;
		}
		if (handler instanceof E4HandlerProxy) {
			handler = ((E4HandlerProxy) handler).getHandler();
		}
		String contextId = null;
		if (handler != null) {
			contextId = helpContextIdsByHandler.get(handler);
		}
		if (contextId == null) {
			contextId = Util.getHelpContextId(command);
		}
		return contextId;
	}

	@Override
	public void setHelpContextId(IHandler handler, String contextId) {
		helpContextIdsByHandler.put(handler, contextId);
	}
}
