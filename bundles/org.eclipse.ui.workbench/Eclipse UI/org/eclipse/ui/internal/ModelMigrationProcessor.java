/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;

public class ModelMigrationProcessor {
	// remove e4 commands from existing IDE models
	// Bug 411602 - CTRL+Q keyboard shortcut in any dialog closes the workbench
	// without option to save
	private static final String MIGRATION_001 = "ModelMigrationProcessor.001"; //$NON-NLS-1$

	@Execute
	public void process(MApplication application, IEclipseContext context) {
		if (!application.getTags().contains(MIGRATION_001)) {
			application.getTags().add(MIGRATION_001);
			removeE4CommandsFromIDE(application);
		}
	}

	/**
	 * @param application
	 */
	private void removeE4CommandsFromIDE(MApplication application) {
		List<MCommand> commands = application.getCommands();
		Set<MCommand> toBeRemoved = new HashSet<MCommand>();
		for (MCommand command : commands) {
			final String elementId = command.getElementId();
			if ("e4.exit".equals(elementId)) { //$NON-NLS-1$
				toBeRemoved.add(command);
			} else if ("e4.show.view".equals(elementId)) { //$NON-NLS-1$
				toBeRemoved.add(command);
			} else if ("org.eclipse.e4.ui.saveCommands".equals(elementId)) { //$NON-NLS-1$
				toBeRemoved.add(command);
			} else if ("org.eclipse.e4.ui.saveAllCommands".equals(elementId)) { //$NON-NLS-1$
				toBeRemoved.add(command);
			}
			if (toBeRemoved.size() > 3) {
				break;
			}
		}
		if (toBeRemoved.size() == 0) {
			return;
		}
		List<MHandler> handlers = application.getHandlers();
		Iterator<MHandler> i = handlers.iterator();
		int removed = 0;
		while (i.hasNext() && removed < 4) {
			MHandler handler = i.next();
			if (toBeRemoved.contains(handler.getCommand())) {
				i.remove();
				removed++;
			}
		}
		List<MBindingContext> bindingContexts = application.getBindingContexts();
		MBindingContext dialogAndWindow = null;
		for (MBindingContext c : bindingContexts) {
			if ("org.eclipse.ui.contexts.dialogAndWindow".equals(c.getElementId())) { //$NON-NLS-1$
				dialogAndWindow = c;
				break;
			}
		}

		if (dialogAndWindow != null) {
			List<MBindingTable> bindingTables = application.getBindingTables();
			MBindingTable dAWTable = null;
			for (MBindingTable table : bindingTables) {
				if (dialogAndWindow.equals(table.getBindingContext())) {
					dAWTable = table;
					break;
				}
			}
			if (dAWTable != null) {
				List<MKeyBinding> bindings = dAWTable.getBindings();
				Iterator<MKeyBinding> j = bindings.iterator();
				removed = 0;
				while (j.hasNext() && removed < 3) {
					MKeyBinding binding = j.next();
					if (toBeRemoved.contains(binding.getCommand())) {
						j.remove();
						removed++;
					}
				}
			}
		}
		commands.removeAll(toBeRemoved);
	}

}
