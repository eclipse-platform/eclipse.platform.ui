/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.ContextResolver;
import org.eclipse.jface.action.IContextResolver;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.commands.ICommandManagerEvent;
import org.eclipse.ui.commands.ICommandManagerListener;
import org.eclipse.ui.contexts.IContextManagerEvent;
import org.eclipse.ui.contexts.IContextManagerListener;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.contexts.ContextManager;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.KeyStroke;

/**
 * <p>
 * Manages the relationship between the context and command managers, as well as 
 * filtering all incoming key strokes before allowing them to reach the widget
 * hierarchy.  This is the magic glue that makes key bindings work, and keeps
 * everyone agreeing about the state of the active contexts.
 * </p>
 * <p>
 * If an incoming key matches one of the active key bindings, then it dispatches
 * the event to the appropriate handler.  Otherwise, the key is allowed to
 * propagate normally through the widget hierarchy.
 * </p>
 */
public final class CommandAndContextController implements IContextResolver {

	private final ICommandManagerListener commandManagerListener = new ICommandManagerListener() {
		public final void commandManagerChanged(final ICommandManagerEvent commandManagerEvent) {
			update();
		}
	};
	
	private final IContextManagerListener contextManagerListener = new IContextManagerListener() {
		public final void contextManagerChanged(final IContextManagerEvent contextManagerEvent) {
			update();
		}
	};

	private final StatusLineContributionItem statusLineContributionItem = new StatusLineContributionItem("ModeContributionItem"); //$NON-NLS-1$

	public CommandAndContextController() {
		ContextResolver.getInstance().setContextResolver(this);
		CommandManager.getInstance().addCommandManagerListener(commandManagerListener);
		ContextManager.getInstance().addContextManagerListener(contextManagerListener);
		update();
	}

	public final void clear() {
		CommandManager.getInstance().setMode(KeySequence.getInstance());
		statusLineContributionItem.setText(""); //$NON-NLS-1$ 
		update();
	}

	public IContributionItem getModeContributionItem() {
		return statusLineContributionItem;
	}

	public final boolean inContext(final String commandId) {
		if (commandId != null) {
			final ICommandManager commandManager = CommandManager.getInstance();
			final ICommand command = commandManager.getCommand(commandId);

			if (command != null)
				return command.isDefined() && command.isActive();
		}

		return true;
	}

	// TODO remove event parameter once key-modified actions are removed
	public final boolean press(KeyStroke keyStroke, Event event) {
		boolean handled = false;
		final CommandManager commandManager = CommandManager.getInstance();
		final List keyStrokes = new ArrayList(commandManager.getMode().getKeyStrokes());
		keyStrokes.add(keyStroke);
		final KeySequence childMode = KeySequence.getInstance(keyStrokes);
		final Map matchesByKeySequenceForMode = commandManager.getMatchesByKeySequenceForMode();
		commandManager.setMode(childMode);
		final Map childMatchesByKeySequenceForMode = commandManager.getMatchesByKeySequenceForMode();

		if (childMatchesByKeySequenceForMode.isEmpty()) {
			clear();
			final Match match = (Match) matchesByKeySequenceForMode.get(childMode);

			if (match != null) {
				final String commandId = match.getCommandId();
				final Map actionsById = commandManager.getActionsById();
				org.eclipse.ui.commands.IAction action = (org.eclipse.ui.commands.IAction) actionsById.get(commandId);

				if ((action != null) && (action.isEnabled())) {
					try {
						action.execute(event);
					} catch (final Exception e) {
						// TODO 						
					}
					
					handled = true;
				}
			}
		} else {
			statusLineContributionItem.setText(childMode.format());
			update();
			handled = true;
		}
		
		return handled;
	}

	public final void update() {
		final List activeContextIds = new ArrayList(ContextManager.getInstance().getActiveContextIds());
		activeContextIds.add(IWorkbenchConstants.DEFAULT_ACCELERATOR_SCOPE_ID);
        CommandManager.getInstance().setActiveContextIds(activeContextIds);
	}
}
