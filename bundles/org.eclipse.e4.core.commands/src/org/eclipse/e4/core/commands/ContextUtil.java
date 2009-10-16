/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.core.commands;

import org.eclipse.core.commands.CommandManager;
import org.eclipse.e4.core.commands.internal.CommandServiceImpl;
import org.eclipse.e4.core.commands.internal.HandlerServiceCreationFunction;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;

/**
 * Utility methods for setting up an IEclipseContext with support for commands.
 */
public class ContextUtil {
	/**
	 * This should be called once on the application context.
	 * 
	 * @param context
	 */
	public static void commandSetup(IEclipseContext context) {
		CommandManager commandManager = new CommandManager();
		context.set(CommandManager.class.getName(), commandManager);
		CommandServiceImpl csi = new CommandServiceImpl();
		ContextInjectionFactory.inject(csi, context);
		context.set(ECommandService.class.getName(), csi);
	}

	/**
	 * This should be called once on the application context.
	 * 
	 * @param context
	 */
	public static void handlerSetup(IEclipseContext context) {
		context.set(EHandlerService.class.getName(), new HandlerServiceCreationFunction());
	}
}
