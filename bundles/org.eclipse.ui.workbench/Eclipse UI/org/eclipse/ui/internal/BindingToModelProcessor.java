/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsFactoryImpl;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.ui.internal.keys.BindingPersistence;
import org.eclipse.ui.internal.keys.BindingService;

public class BindingToModelProcessor {

	private Map<String, MBindingContext> contexts = new HashMap<String, MBindingContext>();
	private Map<String, MCommand> commands = new HashMap<String, MCommand>();
	private Map<String, MBindingTable> tables = new HashMap<String, MBindingTable>();
	private Set<MKeyBinding> keys = new HashSet<MKeyBinding>();

	@Execute
	void process(final MApplication application, IEclipseContext context) {
		gatherContexts(application.getRootContext());
		gatherCommands(application.getCommands());
		gatherTables(application.getBindingTables());

		CommandManager commandManager = context.get(CommandManager.class);
		if (commandManager == null) {
			WorkbenchPlugin
					.log("Command manager was null in org.eclipse.ui.internal.BindingToModelProcessor"); //$NON-NLS-1$
		}
		ContextManager contextManager = context.get(ContextManager.class);
		if (contextManager == null) {
			WorkbenchPlugin
					.log("Context manager was null in org.eclipse.ui.internal.BindingToModelProcessor"); //$NON-NLS-1$
		}
		BindingManager bindingManager = new BindingManager(contextManager, commandManager);
		context.set(BindingManager.class, bindingManager);
		BindingPersistence persistence = new BindingPersistence(bindingManager, commandManager);
		persistence.read();

		// we'll make this available, although I doubt we have a use for it
		application.getTags().add(
				EBindingService.ACTIVE_SCHEME_TAG + ':' + bindingManager.getActiveScheme().getId());

		Collection<?> activeBindingsForScheme = bindingManager
				.getActiveBindingsDisregardingContextFlat();

		for (Object obj : activeBindingsForScheme) {
			Binding binding = (Binding) obj;
			addBinding(application, binding);
		}

		removeBindings();

		persistence.dispose();
		contexts.clear();
		commands.clear();
		tables.clear();
		keys.clear();
	}

	private void gatherTables(List<MBindingTable> bindingTables) {
		for (MBindingTable table : bindingTables) {
			tables.put(table.getBindingContext().getElementId(), table);
			keys.addAll(table.getBindings());
		}
	}

	public final void addBinding(final MApplication application, final Binding binding) {


		MBindingTable table = tables.get(binding.getContextId());
		if (table == null) {
			table = createTable(application, binding.getContextId());
		}
		MKeyBinding model = BindingService.createORupdateMKeyBinding(application, table, binding);
		keys.remove(model);
	}

	private void removeBindings() {
		for (MKeyBinding key : keys) {
			if (!key.getTags().contains("type:user")) { //$NON-NLS-1$
				EObject obj = ((EObject) key).eContainer();
				if (obj instanceof MBindingTable) {
					MBindingTable table = (MBindingTable) obj;
					table.getBindings().remove(key);
				}
			}
		}
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

	private MBindingTable createTable(MApplication application, String contextId) {
		MBindingTable table = CommandsFactoryImpl.eINSTANCE.createBindingTable();
		table.setBindingContext(getBindingContext(application, contextId));
		table.setElementId(contextId);
		application.getBindingTables().add(table);
		tables.put(table.getBindingContext().getElementId(), table);
		return table;
	}

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

	private void gatherContexts(MBindingContext ctx) {
		if (ctx == null) {
			return;
		}
		contexts.put(ctx.getElementId(), ctx);
		gatherContexts(ctx.getChildren());
	}
}
