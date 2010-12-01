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

package org.eclipse.ui.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsFactoryImpl;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.ui.internal.commands.CommandPersistence;
import org.eclipse.ui.internal.contexts.ContextPersistence;
import org.eclipse.ui.internal.keys.BindingPersistence;

/**
 * 
 *
 */
public class BindingToModelProcessor {

	private Map<String, MBindingContext> contexts = new HashMap<String, MBindingContext>();
	private Map<String, MCommand> commands = new HashMap<String, MCommand>();
	private Map<String, MBindingTable> tables = new HashMap<String, MBindingTable>();


	@Execute
	void process(final MApplication application) {
		gatherContexts(application.getRootContext());
		gatherCommands(application.getCommands());
		gatherTables(application.getBindingTables());

		CommandManager commandManager = new CommandManager();
		CommandPersistence commandPersistence = new CommandPersistence(commandManager);
		commandPersistence.reRead();
		ContextManager contextManager = new ContextManager();
		ContextPersistence contextPersistence = new ContextPersistence(contextManager);
		contextPersistence.reRead();
		BindingManager bindingManager = new BindingManager(contextManager, commandManager);
		BindingPersistence persistence = new BindingPersistence(bindingManager, commandManager);
		persistence.read();
		Iterator i = bindingManager.getActiveBindingsDisregardingContextFlat().iterator();
		while (i.hasNext()) {
			Binding binding = (Binding) i.next();
			addBinding(application, binding);
		}
		persistence.dispose();
	}

	/**
	 * @param bindingTables
	 */
	private void gatherTables(List<MBindingTable> bindingTables) {
		for (MBindingTable table : bindingTables) {
			tables.put(table.getBindingContextId(), table);
		}
	}

	public final void addBinding(final MApplication application, final Binding binding) {
		MBindingTable table = tables.get(binding.getContextId());
		if (table == null) {
			table = createTable(application, binding.getContextId());

		}
		final MKeyBinding keyBinding = CommandsFactoryImpl.eINSTANCE.createKeyBinding();
		ParameterizedCommand parmCmd = binding.getParameterizedCommand();

		MCommand cmd = commands.get(parmCmd.getId());
		if (cmd == null) {
			return;
		}
		keyBinding.setCommand(cmd);
		keyBinding.setKeySequence(binding.getTriggerSequence().format());
		for (Object obj : parmCmd.getParameterMap().entrySet()) {
			Map.Entry entry = (Map.Entry) obj;
			MParameter p = CommandsFactoryImpl.eINSTANCE.createParameter();
			p.setElementId((String) entry.getKey());
			p.setName((String) entry.getKey());
			p.setValue((String) entry.getValue());
			keyBinding.getParameters().add(p);
		}
		table.getBindings().add(keyBinding);
	}

	/**
	 * @param contextId
	 * @return
	 */
	private MBindingTable createTable(MApplication application, String contextId) {
		MBindingTable table = CommandsFactoryImpl.eINSTANCE.createBindingTable();
		table.setBindingContextId(contextId);
		table.setElementId(contextId);
		application.getBindingTables().add(table);
		tables.put(table.getBindingContextId(), table);
		return table;
	}

	/**
	 * @param commands
	 */
	private void gatherCommands(List<MCommand> commandList) {
		for (MCommand cmd : commandList) {
			commands.put(cmd.getElementId(), cmd);
		}
	}

	private void gatherContexts(List<MBindingContext> contextList) {
		for (MBindingContext ctx : contextList) {
			gatherContexts(ctx);
		}
	}

	/**
	 * @param ctx
	 */
	private void gatherContexts(MBindingContext ctx) {
		if (ctx == null) {
			return;
		}
		contexts.put(ctx.getElementId(), ctx);
		gatherContexts(ctx.getChildren());
	}
}
