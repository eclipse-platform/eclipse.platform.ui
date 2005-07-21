/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.ISourceProviderListener;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.internal.misc.Policy;

/**
 * <p>
 * A central authority for resolving conflicts between handlers. This authority
 * listens to a variety of incoming sources, and updates the underlying commands
 * if changes in the active handlers occur.
 * </p>
 * <p>
 * This authority encapsulates all of the handler conflict resolution mechanisms
 * for the workbench. A conflict occurs if two or more handlers are assigned to
 * the same command identifier. To resolve this conflict, the authority
 * considers which source the handler came from.
 * </p>
 * 
 * @since 3.1
 */
final class HandlerAuthority implements ISourceProviderListener {

	/**
	 * Whether the workbench command support should kick into debugging mode.
	 * This causes the unresolvable handler conflicts to be printed to the
	 * console.
	 */
	private static final boolean DEBUG = Policy.DEBUG_HANDLERS;

	/**
	 * Whether the workbench command support should kick into verbose debugging
	 * mode. This causes the resolvable handler conflicts to be printed to the
	 * console.
	 */
	private static final boolean DEBUG_VERBOSE = Policy.DEBUG_HANDLERS
			&& Policy.DEBUG_HANDLERS_VERBOSE;

	/**
	 * The command identifier to which the verbose output should be restricted.
	 */
	private static final String DEBUG_VERBOSE_COMMAND_ID = Policy.DEBUG_HANDLERS_VERBOSE_COMMAND_ID;

	/**
	 * A bucket sort of the handler activations based on source priority. Each
	 * activation will appear only once per set, but may appear in multiple
	 * sets. If no activations are defined for a particular priority level, then
	 * the array at that index will only contain <code>null</code>.
	 */
	private final Set[] activationsBySourcePriority = new Set[33];

	/**
	 * The command manager that should be updated when the handlers are
	 * changing.
	 */
	private final CommandManager commandManager;

	/**
	 * The evaluation context instance to use when evaluating handler
	 * activations. This context is shared, and so all calls into
	 * <code>sourceChanged</code> must happen on the event thread.
	 */
	private final IEvaluationContext context;

	/**
	 * This is a map of handler activations (<code>Collection</code> of
	 * <code>IHandlerActivation</code>) sorted by command identifier (<code>String</code>).
	 * If there is only one handler activation for a command, then the
	 * <code>Collection</code> is replaced by a
	 * <code>IHandlerActivation</code>. If there is no activation, the entry
	 * should be removed entirely.
	 */
	private final Map handlerActivationsByCommandId = new HashMap();

	/**
	 * The collection of source providers used by this authority. This
	 * collection is consulted whenever a handler is activated. This collection
	 * only contains instances of <code>ISourceProvider</code>.
	 */
	private final Collection providers = new ArrayList();

	/**
	 * Constructs a new instance of <code>HandlerAuthority</code>.
	 * 
	 * @param commandManager
	 *            The command manager from which commands can be retrieved (to
	 *            update their handlers); must not be <code>null</code>.
	 */
	HandlerAuthority(final CommandManager commandManager) {
		if (commandManager == null) {
			throw new NullPointerException(
					"The handler authority needs a command manager"); //$NON-NLS-1$
		}

		this.commandManager = commandManager;
		this.context = new EvaluationContext(null, this);
	}

	/**
	 * Activates a handler on the workbench. This will add it to a master list.
	 * If conflicts exist, they will be resolved based on the source priority.
	 * If conflicts still exist, then no handler becomes active.
	 * 
	 * @param activation
	 *            The activation; must not be <code>null</code>.
	 */
	final void activateHandler(final IHandlerActivation activation) {
		// First we update the handlerActivationsByCommandId map.
		final String commandId = activation.getCommandId();
		final Object value = handlerActivationsByCommandId.get(commandId);
		if (value instanceof Collection) {
			final Collection handlerActivations = (Collection) value;
			if (!handlerActivations.contains(activation)) {
				handlerActivations.add(activation);
				updateCurrentState();
				updateCommand(commandId, resolveConflicts(commandId,
						handlerActivations));
			}
		} else if (value instanceof IHandlerActivation) {
			if (value != activation) {
				final Collection handlerActivations = new ArrayList(2);
				handlerActivations.add(value);
				handlerActivations.add(activation);
				handlerActivationsByCommandId
						.put(commandId, handlerActivations);
				updateCurrentState();
				updateCommand(commandId, resolveConflicts(commandId,
						handlerActivations));
			}
		} else {
			handlerActivationsByCommandId.put(commandId, activation);
			updateCurrentState();
			updateCommand(commandId, (activation.isActive(context) ? activation
					: null));
		}

		// Next we update the source priority bucket sort of activations.
		final int sourcePriority = activation.getSourcePriority();
		for (int i = 1; i <= 32; i++) {
			if ((sourcePriority & (1 << i)) != 0) {
				Set activations = activationsBySourcePriority[i];
				if (activations == null) {
					activations = new HashSet(1);
					activationsBySourcePriority[i] = activations;
				}
				activations.add(activation);
			}
		}
	}

	/**
	 * Adds a source provider to a list of providers to check when updating.
	 * This also attaches this authority as a listener to the provider.
	 * 
	 * @param provider
	 *            The provider to add; must not be <code>null</code>.
	 */
	final void addSourceProvider(final ISourceProvider provider) {
		provider.addSourceProviderListener(this);
		providers.add(provider);
		updateCurrentState();
	}

	/**
	 * Removes an activation for a handler on the workbench. This will remove it
	 * from the master list, and update the appropriate command, if necessary.
	 * 
	 * @param activation
	 *            The activation; must not be <code>null</code>.
	 */
	final void deactivateHandler(final IHandlerActivation activation) {
		// First we update the handlerActivationsByCommandId map.
		final String commandId = activation.getCommandId();
		final Object value = handlerActivationsByCommandId.get(commandId);
		if (value instanceof Collection) {
			final Collection handlerActivations = (Collection) value;
			if (handlerActivations.contains(activation)) {
				handlerActivations.remove(activation);
				if (handlerActivations.isEmpty()) {
					handlerActivationsByCommandId.remove(commandId);
					updateCurrentState();
					updateCommand(commandId, null);

				} else if (handlerActivations.size() == 1) {
					final IHandlerActivation remainingActivation = (IHandlerActivation) handlerActivations
							.iterator().next();
					handlerActivationsByCommandId.put(commandId,
							remainingActivation);
					updateCurrentState();
					updateCommand(commandId, (remainingActivation
							.isActive(context) ? remainingActivation : null));

				} else {
					updateCurrentState();
					updateCommand(commandId, resolveConflicts(commandId,
							handlerActivations));
				}
			}
		} else if (value instanceof IHandlerActivation) {
			if (value == activation) {
				handlerActivationsByCommandId.remove(commandId);
				updateCurrentState();
				updateCommand(commandId, null);
			}
		}

		// Next we update the source priority bucket sort of activations.
		final int sourcePriority = activation.getSourcePriority();
		for (int i = 1; i <= 32; i++) {
			if ((sourcePriority & (1 << i)) != 0) {
				final Set activations = activationsBySourcePriority[i];
				if (activations == null) {
					continue;
				}
				activations.remove(activation);
				if (activations.isEmpty()) {
					activationsBySourcePriority[i] = null;
				}
			}
		}
	}

	/**
	 * Queries the source providers, and fills in the variables for the given
	 * evaluation context.
	 * 
	 * @param context
	 *            The context to fill in; must not be <code>null</code>.
	 */
	private final void fillInCurrentState(final IEvaluationContext context) {
		final Iterator providerItr = providers.iterator();
		while (providerItr.hasNext()) {
			final ISourceProvider provider = (ISourceProvider) providerItr
					.next();
			final Map currentState = provider.getCurrentState();
			final Iterator variableItr = currentState.entrySet().iterator();
			while (variableItr.hasNext()) {
				final Map.Entry entry = (Map.Entry) variableItr.next();
				final String variableName = (String) entry.getKey();
				final Object variableValue = entry.getValue();
				if (variableName != null) {
					if (variableValue == null) {
						context.removeVariable(variableName);
					} else {
						context.addVariable(variableName, variableValue);
					}
				}
			}
		}
	}
	
	/**
	 * Returns the currently active shell.
	 * 
	 * @return The currently active shell; may be <code>null</code>.
	 */
	final Shell getActiveShell() {
		return (Shell) context.getVariable(ISources.ACTIVE_SHELL_NAME);
	}

	final IEvaluationContext getCurrentState() {
		final IEvaluationContext context = new EvaluationContext(null, this);
		fillInCurrentState(context);
		return context;
	}

	/**
	 * Removes this source provider from the list, and detaches this authority
	 * as a listener.
	 * 
	 * @param provider
	 *            The provider to remove; must not be <code>null</code>.
	 */
	final void removeSourceProvider(final ISourceProvider provider) {
		provider.removeSourceProviderListener(this);
		providers.remove(provider);
		updateCurrentState();
	}

	/**
	 * Resolves conflicts between multiple handlers for the same command
	 * identifier. This tries to select the best activation based on the source
	 * priority. For the sake of comparison, activations with the same handler
	 * are considered equivalent (i.e., non-conflicting).
	 * 
	 * @param commandId
	 *            The identifier of the command for which the conflicts should
	 *            be detected; must not be <code>null</code>. This is only
	 *            used for debugging purposes.
	 * @param handlerActivations
	 *            All of the possible handler activations for the given command
	 *            identifier; must not be <code>null</code>.
	 * @return The best matching handler activation. If none can be found (e.g.,
	 *         because of unresolvable conflicts), then this returns
	 *         <code>null</code>.
	 */
	private final IHandlerActivation resolveConflicts(final String commandId,
			final Collection handlerActivations) {
		// Create a collection with only the activation that are active.
		final Collection activations = trimInactive(handlerActivations);

		// If we don't have any, then there is no match.
		if (activations.isEmpty()) {
			return null;
		}

		/*
		 * Prime the best activation pointer with the first element in the
		 * collection.
		 */
		final Iterator activationItr = activations.iterator();
		IHandlerActivation bestActivation = (IHandlerActivation) activationItr
				.next();
		int bestSourcePriority = bestActivation.getSourcePriority();
		boolean conflict = false;

		// Cycle over the activations, remembered the current best.
		while (activationItr.hasNext()) {
			final IHandlerActivation currentActivation = (IHandlerActivation) activationItr
					.next();
			if (currentActivation.getSourcePriority() > bestSourcePriority) {
				bestActivation = currentActivation;
				bestSourcePriority = bestActivation.getSourcePriority();
				conflict = false;

			} else if (currentActivation.getSourcePriority() == bestSourcePriority) {
				if (currentActivation.getHandler() != bestActivation
						.getHandler()) {
					conflict = true;
				}

			}
		}

		// If we are logging information, now is the time to do it.
		if (DEBUG) {
			if (conflict) {
				System.out
						.println("HANDLERS >>> Unresolved conflict detected for '" //$NON-NLS-1$
								+ commandId + '\'');
			} else if ((DEBUG_VERBOSE)
					&& ((DEBUG_VERBOSE_COMMAND_ID == null) || (DEBUG_VERBOSE_COMMAND_ID
							.equals(commandId)))) {
				System.out
						.println("HANDLERS >>> Resolved conflict detected.  The following activation won: "); //$NON-NLS-1$
				System.out.println("HANDLERS >>>     " + bestActivation); //$NON-NLS-1$
			}
		}

		// Return the current best.
		if (conflict) {
			return null;
		}
		return bestActivation;
	}

	/**
	 * Carries out the actual source change notification. It assumed that by the
	 * time this method is called, <code>context</code> is up-to-date with the
	 * current state of the application.
	 * 
	 * @param sourcePriority
	 *            A bit mask of all the source priorities that have changed.
	 */
	private final void sourceChanged(final int sourcePriority) {
		/*
		 * In this first phase, we cycle through all of the activations that
		 * could have potentially changed. Each such activation is added to a
		 * set for future processing. We add it to a set so that we avoid
		 * handling any individual activation more than once.
		 */
		final Set activationsToRecompute = new HashSet();
		for (int i = 1; i <= 32; i++) {
			if ((sourcePriority & (1 << i)) != 0) {
				final Collection activations = activationsBySourcePriority[i];
				if (activations != null) {
					final Iterator activationItr = activations.iterator();
					while (activationItr.hasNext()) {
						activationsToRecompute.add(activationItr.next());
					}
				}
			}
		}

		/*
		 * For every activation, we recompute its active state, and check
		 * whether it has changed. If it has changed, then we take note of the
		 * command identifier so we can update the command later.
		 */
		final Collection changedCommandIds = new ArrayList(
				activationsToRecompute.size());
		final Iterator activationItr = activationsToRecompute.iterator();
		while (activationItr.hasNext()) {
			final IHandlerActivation activation = (IHandlerActivation) activationItr
					.next();
			final boolean currentActive = activation.isActive(context);
			activation.clearActive();
			final boolean newActive = activation.isActive(context);
			if (newActive != currentActive) {
				changedCommandIds.add(activation.getCommandId());
			}
		}

		/*
		 * For every command identifier with a changed activation, we resolve
		 * conflicts and trigger an update.
		 */
		final Iterator changedCommandIdItr = changedCommandIds.iterator();
		while (changedCommandIdItr.hasNext()) {
			final String commandId = (String) changedCommandIdItr.next();
			final Object value = handlerActivationsByCommandId.get(commandId);
			if (value instanceof IHandlerActivation) {
				final IHandlerActivation activation = (IHandlerActivation) value;
				updateCommand(commandId,
						(activation.isActive(context) ? activation : null));
			} else if (value instanceof Collection) {
				final IHandlerActivation activation = resolveConflicts(
						commandId, (Collection) value);
				updateCommand(commandId, activation);
			} else {
				updateCommand(commandId, null);
			}
		}
	}

	public final void sourceChanged(final int sourcePriority,
			final Map sourceValuesByName) {
		final Iterator entryItr = sourceValuesByName.entrySet().iterator();
		while (entryItr.hasNext()) {
			final Map.Entry entry = (Map.Entry) entryItr.next();
			final String sourceName = (String) entry.getKey();
			final Object sourceValue = entry.getValue();
			if (sourceName != null) {
				if (sourceValue == null) {
					context.removeVariable(sourceName);
				} else {
					context.addVariable(sourceName, sourceValue);
				}
			}
		}
		sourceChanged(sourcePriority);
	}

	public final void sourceChanged(final int sourcePriority,
			final String sourceName, final Object sourceValue) {
		if (sourceName != null) {
			if (sourceValue == null) {
				context.removeVariable(sourceName);
			} else {
				context.addVariable(sourceName, sourceValue);
			}
		}
		sourceChanged(sourcePriority);
	}

	/**
	 * Returns a subset of the given <code>activations</code> containing only
	 * those that are active
	 * 
	 * @param activations
	 *            The activations to trim; must not be <code>null</code>, but
	 *            may be empty.
	 * @return A collection containing only those activations that are actually
	 *         active; never <code>null</code>.
	 */
	private final Collection trimInactive(final Collection activations) {
		final Collection trimmed = new ArrayList(activations.size());

		final Iterator activationItr = activations.iterator();
		while (activationItr.hasNext()) {
			final IHandlerActivation activation = (IHandlerActivation) activationItr
					.next();
			if (activation.isActive(context)) {
				trimmed.add(activation);
			}
		}

		return trimmed;
	}

	/**
	 * Updates the command with the given handler activation.
	 * 
	 * @param commandId
	 *            The identifier of the command which should be updated; must
	 *            not be <code>null</code>.
	 * @param activation
	 *            The activation to use; may be <code>null</code> if the
	 *            command should have a <code>null</code> handler.
	 */
	private final void updateCommand(final String commandId,
			final IHandlerActivation activation) {
		final Command command = commandManager.getCommand(commandId);
		if (activation == null) {
			command.setHandler(null);
		} else {
			command.setHandler(activation.getHandler());
		}
	}

	/**
	 * Updates the evaluation context with the current state from all of the
	 * source providers.
	 */
	private final void updateCurrentState() {
		fillInCurrentState(context);
	}
    
    /**
     * <p>
     * Bug 95792. A mechanism by which the key binding architecture can force an
     * update of the handlers (based on the active shell) before trying to
     * execute a command. This mechanism is required for GTK+ only.
     * </p>
     * <p>
     * DO NOT CALL THIS METHOD.
     * </p>
     */
    final void updateShellKludge() {
        updateCurrentState();
        sourceChanged(ISources.ACTIVE_SHELL);
    }
}
