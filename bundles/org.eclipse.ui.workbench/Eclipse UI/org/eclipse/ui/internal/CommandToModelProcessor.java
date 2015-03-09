/*******************************************************************************
 * Copyright (c) 2010, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.core.commands.internal.HandlerServiceImpl;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.internal.workbench.addons.CommandProcessingAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCategory;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.ui.internal.commands.CommandPersistence;

/**
 * @since 3.5
 *
 */
public class CommandToModelProcessor {

	private Map<String, MCategory> categories = new HashMap<String, MCategory>();

	private Map<String, MCommand> commands = new HashMap<String, MCommand>();

	private EModelService modelService;

	@Execute
	void process(MApplication application, IEclipseContext context, EModelService modelService) {
		this.modelService = modelService;
		for (MCategory catModel : application.getCategories()) {
			categories.put(catModel.getElementId(), catModel);
		}

		for (MCommand cmdModel : application.getCommands()) {
			commands.put(cmdModel.getElementId(), cmdModel);
		}
		CommandManager commandManager = context.get(CommandManager.class);
		if (commandManager == null) {
			HandlerServiceImpl.handlerGenerator = new ContextFunction() {
				@Override
				public Object compute(IEclipseContext context, String contextKey) {
					return new WorkbenchHandlerServiceHandler(contextKey);
				}
			};
			commandManager = new CommandManager();
			// setCommandFireEvents(commandManager, false);
			context.set(CommandManager.class, commandManager);
		}

		CommandPersistence cp = new CommandPersistence(commandManager);
		cp.reRead();
		generateCategories(application, commandManager);
		generateCommands(application, commandManager);
		cp.dispose();
	}

	/**
	 * @param application
	 * @param commandManager
	 */
	private void generateCommands(MApplication application, CommandManager commandManager) {
		for (Command cmd : commandManager.getDefinedCommands()) {
			if (commands.containsKey(cmd.getId())) {
				continue;
			}
			try {
				final MCategory categoryModel = categories.get(cmd.getCategory().getId());

				MCommand command = CommandProcessingAddon.createCommand(cmd, modelService,
						categoryModel);

				application.getCommands().add(command);
				commands.put(command.getElementId(), command);
			} catch (NotDefinedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}



	/**
	 * @param commandManager
	 */
	private void generateCategories(MApplication application, CommandManager commandManager) {
		for (Category cat : commandManager.getDefinedCategories()) {
			if (categories.containsKey(cat.getId())) {
				continue;
			}
			try {
				MCategory catModel = modelService.createModelElement(MCategory.class);
				catModel.setElementId(cat.getId());
				catModel.setName(cat.getName());
				catModel.setDescription(cat.getDescription());
				application.getCategories().add(catModel);
				categories.put(catModel.getElementId(), catModel);
			} catch (NotDefinedException e) {
				// Since we asked for defined commands, this shouldn't be an
				// issue
				e.printStackTrace();
			}
		}
	}

	void setCommandFireEvents(CommandManager manager, boolean b) {
		try {
			Field f = CommandManager.class.getDeclaredField("shouldCommandFireEvents"); //$NON-NLS-1$
			f.setAccessible(true);
			f.set(manager, Boolean.valueOf(b));
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
