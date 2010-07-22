/*******************************************************************************
 * Copyright (c) 2010 Brian de Alwis, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brian de Alwis - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt.cocoa;

import javax.inject.Inject;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsFactoryImpl;

/**
 * A hack to create an IStartup-like equivalent using ModelProcessor. The actual
 * handling is done by {@link CocoaUIHandler}. But as the context provided to a
 * {@code ModelProcessor} is destroyed after the processor has executed, we
 * create a new context.
 */
public class CocoaUIProcessor {
	private static final String HOST_ID = "org.eclipse.e4.ui.workbench.renderers.swt"; //$NON-NLS-1$

	private final String CLOSE_DIALOG_COMMAND = "org.eclipse.ui.cocoa.closeDialog"; //$NON-NLS-1$

	private static final String DIALOG_CONTEXT_ID = "org.eclipse.ui.contexts.dialog"; //$NON-NLS-1$

	private static final String CLOSE_DIALOG_KEYSEQUENCE = "M1+W"; //$NON-NLS-1$

	@Inject
	protected MApplication app;
	@Inject
	protected IEclipseContext context;

	protected MCommand closeDialogCommand;

	@Execute
	public void execute() {
		// first create the menu handler for the special MacOSX
		// Quit, About, and Preferences menus
		IEclipseContext ctxt = context.getParent().createChild(
				CocoaUIHandler.class.getName());
		CocoaUIHandler uiHandler = ContextInjectionFactory.make(
				CocoaUIHandler.class, ctxt);
		ContextInjectionFactory.invoke(uiHandler, Execute.class, ctxt);

		// Now add the special Cmd-W dialog helper
		addCloseDialogCommand();
		addCloseDialogHandler();
		addCloseDialogBinding();
	}

	private void addCloseDialogCommand() {
		for (MCommand cmd : app.getCommands()) {
			if (cmd.getElementId() != null
					&& CLOSE_DIALOG_COMMAND.equals(cmd.getElementId())) {
				closeDialogCommand = cmd;
				return;
			}
		}
		closeDialogCommand = CommandsFactoryImpl.eINSTANCE.createCommand();
		closeDialogCommand.setElementId(CLOSE_DIALOG_COMMAND);
		closeDialogCommand.setCommandName(CLOSE_DIALOG_COMMAND);
		app.getCommands().add(closeDialogCommand);
	}

	private void addCloseDialogHandler() {
		for (MHandler handler : app.getHandlers()) {
			if (handler.getCommand() == closeDialogCommand) {
				return;
			}
		}
		MHandler handler = CommandsFactoryImpl.eINSTANCE.createHandler();
		handler.setCommand(closeDialogCommand);
		handler.setContributionURI("platform:/plugin/" + HOST_ID + "/" //$NON-NLS-1$ //$NON-NLS-2$
				+ CloseDialogHandler.class.getName());
		app.getHandlers().add(handler);
	}

	private void addCloseDialogBinding() {
		MBindingTable bt = findBindingTable(DIALOG_CONTEXT_ID);
		for (MKeyBinding kb : bt.getBindings()) {
			if (kb.getCommand() == closeDialogCommand) {
				return;
			}
		}
		MKeyBinding kb = CommandsFactoryImpl.eINSTANCE.createKeyBinding();
		kb.setCommand(closeDialogCommand);
		kb.setKeySequence(CLOSE_DIALOG_KEYSEQUENCE);
		bt.getBindings().add(kb);
	}

	/**
	 * Find or create a binding table for the provided {@code contextId}
	 * 
	 * @param contextId
	 * @return
	 */
	private MBindingTable findBindingTable(String contextId) {
		for (MBindingTable bt : app.getBindingTables()) {
			if (bt.getBindingContextId() != null
					&& bt.getBindingContextId().equals(contextId)) {
				return bt;
			}
		}
		MBindingTable bt = CommandsFactoryImpl.eINSTANCE.createBindingTable();
		bt.setBindingContextId(contextId);
		app.getBindingTables().add(bt);
		return bt;
	}
}
