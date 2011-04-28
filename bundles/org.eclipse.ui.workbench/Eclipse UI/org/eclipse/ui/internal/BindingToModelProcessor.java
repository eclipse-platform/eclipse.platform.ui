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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
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

		// we'll make this available, although I doubt we have a use for it
		application.getTags().add(
				EBindingService.ACTIVE_SCHEME_TAG + ':' + bindingManager.getActiveScheme().getId());

		Collection activeBindingsForScheme = bindingManager
				.getActiveBindingsDisregardingContextFlat();

		for (Object obj : activeBindingsForScheme) {
			Binding binding = (Binding) obj;
			addBinding(application, binding);
		}

		persistence.dispose();
	}

	/**
	 * @param bindingTables
	 */
	private void gatherTables(List<MBindingTable> bindingTables) {
		for (MBindingTable table : bindingTables) {
			tables.put(table.getBindingContext().getElementId(), table);
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

		List<String> tags = keyBinding.getTags();
		// just add the 'schemeId' tag if it's anything other than the default
		// scheme id
		if (binding.getSchemeId() != null
				&& !binding.getSchemeId().equals(
						org.eclipse.ui.keys.IBindingService.DEFAULT_DEFAULT_ACTIVE_SCHEME_ID)) {
			tags.add(EBindingService.SCHEME_ID_ATTR_TAG + ":" + binding.getSchemeId()); //$NON-NLS-1$
		}
		if (binding.getLocale() != null) {
			tags.add(EBindingService.LOCALE_ATTR_TAG + ":" + binding.getLocale()); //$NON-NLS-1$
		}
		if (binding.getPlatform() != null) {
			tags.add(EBindingService.PLATFORM_ATTR_TAG + ":" + binding.getPlatform()); //$NON-NLS-1$
		}
		// just add the 'type' tag if it's a user binding
		if (binding.getType() == Binding.USER) {
			tags.add(EBindingService.TYPE_ATTR_TAG + ":user"); //$NON-NLS-1$
		}

		keyBinding.getTransientData().put(EBindingService.MODEL_TO_BINDING_KEY, binding);
		table.getBindings().add(keyBinding);
	}

	public MBindingContext getBindingContext(MApplication application, String id) {
		// cache
		MBindingContext result = contexts.get(id);
		if (result == null) {
			// search
			result = searchContexts(id, application.getRootContext());
			if (result == null) {
				// create
				result = MCommandsFactory.INSTANCE.createBindingContext();
				result.setElementId(id);
				result.setName("Auto::" + id); //$NON-NLS-1$
				application.getRootContext().add(result);
			}
			if (result != null) {
				contexts.put(id, result);
			}
		}
		return result;
	}

	/**
	 * @param id
	 * @param rootContext
	 * @return
	 */
	private MBindingContext searchContexts(String id, List<MBindingContext> rootContext) {
		for (MBindingContext context : rootContext) {
			if (context.getElementId().equals(id)) {
				return context;
			}
			MBindingContext result = searchContexts(id, context.getChildren());
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	/**
	 * @param contextId
	 * @return
	 */
	private MBindingTable createTable(MApplication application, String contextId) {
		MBindingTable table = CommandsFactoryImpl.eINSTANCE.createBindingTable();
		table.setBindingContext(getBindingContext(application, contextId));
		table.setElementId(contextId);
		application.getBindingTables().add(table);
		tables.put(table.getBindingContext().getElementId(), table);
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
