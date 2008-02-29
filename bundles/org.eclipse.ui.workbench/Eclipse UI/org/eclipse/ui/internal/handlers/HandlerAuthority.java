/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.handlers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.util.Tracing;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.Policy;
import org.eclipse.ui.internal.services.EvaluationResultCacheComparator;
import org.eclipse.ui.internal.services.ExpressionAuthority;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.services.ISourceProviderService;

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
final class HandlerAuthority extends ExpressionAuthority {

	/**
	 * The default size of the set containing the activations to recompute. This
	 * is more than enough to cover the average case.
	 */
	private static final int ACTIVATIONS_BY_SOURCE_SIZE = 256;

	/**
	 * The default size of the set containing the activations to recompute. This
	 * is more than enough to cover the average case.
	 */
	private static final int ACTIVATIONS_TO_RECOMPUTE_SIZE = 1024;

	/**
	 * Whether the workbench command support should kick into debugging mode.
	 * This causes the unresolvable handler conflicts to be printed to the
	 * console.
	 */
	private static final boolean DEBUG = Policy.DEBUG_HANDLERS;

	/**
	 * Whether the performance information should be printed about the
	 * performance of the handler authority.
	 */
	private static final boolean DEBUG_PERFORMANCE = Policy.DEBUG_HANDLERS_PERFORMANCE;

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
	 * The component name to print when displaying tracing information.
	 */
	private static final String TRACING_COMPONENT = "HANDLERS"; //$NON-NLS-1$

	private static final String[] SELECTION_VARIABLES = {
			ISources.ACTIVE_CURRENT_SELECTION_NAME,
			ISources.ACTIVE_FOCUS_CONTROL_ID_NAME,
			ISources.ACTIVE_FOCUS_CONTROL_NAME,
			ISources.ACTIVE_MENU_EDITOR_INPUT_NAME, ISources.ACTIVE_MENU_NAME,
			ISources.ACTIVE_MENU_SELECTION_NAME };

	/**
	 * A bucket sort of the handler activations based on source priority of its
	 * expression. Each expression will appear only once per set, but may appear
	 * in multiple sets. If no activations are defined for a particular priority
	 * level, then the array at that index will only contain <code>null</code>.
	 * This is an array of {@link Map}, where the maps contain instances of
	 * {@link Collection} containing instances of {@link IHandlerActivation}
	 * indexed by instances of {@link Expression}.
	 */
	private final Map[] activationsByExpressionBySourcePriority = new Map[33];

	/**
	 * The command service that should be updated when the handlers are
	 * changing. This value is never <code>null</code>.
	 */
	private final ICommandService commandService;

	/**
	 * This is a map of handler activations (<code>SortedSet</code> of
	 * <code>IHandlerActivation</code>) sorted by command identifier (<code>String</code>).
	 * If there is only one handler activation for a command, then the
	 * <code>SortedSet</code> is replaced by a <code>IHandlerActivation</code>.
	 * If there is no activation, the entry should be removed entirely.
	 */
	private final Map handlerActivationsByCommandId = new HashMap();

	private Set previousLogs = new HashSet();

	private IServiceLocator locator;

	/**
	 * Constructs a new instance of <code>HandlerAuthority</code>.
	 * 
	 * @param commandService
	 *            The command service from which commands can be retrieved (to
	 *            update their handlers); must not be <code>null</code>.
	 * @param locator
	 *            the appropriate service locator
	 */
	HandlerAuthority(final ICommandService commandService,
			final IServiceLocator locator) {
		if (commandService == null) {
			throw new NullPointerException(
					"The handler authority needs a command service"); //$NON-NLS-1$
		}

		this.commandService = commandService;
		this.locator = locator;
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
		MultiStatus conflicts = new MultiStatus("org.eclipse.ui.workbench", 0, //$NON-NLS-1$
				"A handler conflict occurred.  This may disable some commands.", //$NON-NLS-1$
				null);
		final Object value = handlerActivationsByCommandId.get(commandId);
		if (value instanceof SortedSet) {
			final SortedSet handlerActivations = (SortedSet) value;
			if (!handlerActivations.contains(activation)) {
				handlerActivations.add(activation);
				updateCommand(commandId, resolveConflicts(commandId,
						handlerActivations, conflicts));
			}
		} else if (value instanceof IHandlerActivation) {
			if (value != activation) {
				final SortedSet handlerActivations = new TreeSet(
						new EvaluationResultCacheComparator());
				handlerActivations.add(value);
				handlerActivations.add(activation);
				handlerActivationsByCommandId
						.put(commandId, handlerActivations);
				updateCommand(commandId, resolveConflicts(commandId,
						handlerActivations, conflicts));
			}
		} else {
			handlerActivationsByCommandId.put(commandId, activation);
			updateCommand(commandId, (evaluate(activation) ? activation : null));
		}

		if (conflicts.getSeverity() != IStatus.OK) {
			WorkbenchPlugin.log(conflicts);
		}

		// Next we update the source priority bucket sort of activations.
		final int sourcePriority = activation.getSourcePriority();
		for (int i = 1; i <= 32; i++) {
			if ((sourcePriority & (1 << i)) != 0) {
				Map activationsByExpression = activationsByExpressionBySourcePriority[i];
				if (activationsByExpression == null) {
					activationsByExpression = new HashMap(
							ACTIVATIONS_BY_SOURCE_SIZE);
					activationsByExpressionBySourcePriority[i] = activationsByExpression;
				}

				final Expression expression = activation.getExpression();
				Collection activations = (Collection) activationsByExpression
						.get(expression);
				if (activations == null) {
					activations = new HashSet();
					activationsByExpression.put(expression, activations);
				}
				activations.add(activation);
			}
		}
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
		MultiStatus conflicts = new MultiStatus("org.eclipse.ui.workbench", 0, //$NON-NLS-1$
				"A handler conflict occurred.  This may disable some commands.", //$NON-NLS-1$
				null);
		final Object value = handlerActivationsByCommandId.get(commandId);
		if (value instanceof SortedSet) {
			final SortedSet handlerActivations = (SortedSet) value;
			if (handlerActivations.contains(activation)) {
				handlerActivations.remove(activation);
				if (handlerActivations.isEmpty()) {
					handlerActivationsByCommandId.remove(commandId);
					updateCommand(commandId, null);

				} else if (handlerActivations.size() == 1) {
					final IHandlerActivation remainingActivation = (IHandlerActivation) handlerActivations
							.iterator().next();
					handlerActivationsByCommandId.put(commandId,
							remainingActivation);
					updateCommand(
							commandId,
							(evaluate(remainingActivation) ? remainingActivation
									: null));

				} else {
					updateCommand(commandId, resolveConflicts(commandId,
							handlerActivations, conflicts));
				}
			}
		} else if (value instanceof IHandlerActivation) {
			if (value == activation) {
				handlerActivationsByCommandId.remove(commandId);
				updateCommand(commandId, null);
			}
		}
		if (conflicts.getSeverity() != IStatus.OK) {
			WorkbenchPlugin.log(conflicts);
		}

		// Next we update the source priority bucket sort of activations.
		final int sourcePriority = activation.getSourcePriority();
		for (int i = 1; i <= 32; i++) {
			if ((sourcePriority & (1 << i)) != 0) {
				final Map activationsByExpression = activationsByExpressionBySourcePriority[i];
				if (activationsByExpression == null) {
					continue;
				}

				final Expression expression = activation.getExpression();
				final Collection activations = (Collection) activationsByExpression
						.get(expression);
				activations.remove(activation);
				if (activations.isEmpty()) {
					activationsByExpression.remove(expression);
				}

				if (activationsByExpression.isEmpty()) {
					activationsByExpressionBySourcePriority[i] = null;
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
		return (Shell) getVariable(ISources.ACTIVE_SHELL_NAME);
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
	 * @param activations
	 *            All of the possible handler activations for the given command
	 *            identifier; must not be <code>null</code>.
	 * @return The best matching handler activation. If none can be found (e.g.,
	 *         because of unresolvable conflicts), then this returns
	 *         <code>null</code>.
	 */
	private final IHandlerActivation resolveConflicts(final String commandId,
			final SortedSet activations, MultiStatus conflicts) {
		// If we don't have any, then there is no match.
		if (activations.isEmpty()) {
			return null;
		}

		// Cycle over the activations, remembered the current best.
		final Iterator activationItr = activations.iterator();
		IHandlerActivation bestActivation = null;
		IHandlerActivation currentActivation = null;
		boolean conflict = false;
		while (activationItr.hasNext()) {
			currentActivation = (IHandlerActivation) activationItr.next();
			if (!evaluate(currentActivation)) {
				continue; // only consider potentially active handlers
			}

			// Check to see if we haven't found a potentially active handler yet
			if ((DEBUG_VERBOSE)
					&& ((DEBUG_VERBOSE_COMMAND_ID == null) || (DEBUG_VERBOSE_COMMAND_ID
							.equals(commandId)))) {
				Tracing.printTrace(TRACING_COMPONENT,
						"    resolveConflicts: eval: " + currentActivation); //$NON-NLS-1$
			}
			if (bestActivation == null) {
				bestActivation = currentActivation;
				conflict = false;
				continue;
			}

			// Compare the two handlers.
			final int comparison = bestActivation.compareTo(currentActivation);
			if (comparison < 0) {
				bestActivation = currentActivation;
				conflict = false;

			} else if (comparison == 0) {
				if (currentActivation.getHandler() != bestActivation
						.getHandler()) {
					conflict = true;
					break;
				}

			} else {
				break;
			}
		}

		// If we are logging information, now is the time to do it.
		if (DEBUG) {
			if (conflict) {
				Tracing.printTrace(TRACING_COMPONENT,
						"Unresolved conflict detected for '" //$NON-NLS-1$
								+ commandId + '\'');
			} else if ((bestActivation != null)
					&& (DEBUG_VERBOSE)
					&& ((DEBUG_VERBOSE_COMMAND_ID == null) || (DEBUG_VERBOSE_COMMAND_ID
							.equals(commandId)))) {
				Tracing
						.printTrace(TRACING_COMPONENT,
								"Resolved conflict detected.  The following activation won: "); //$NON-NLS-1$
				Tracing.printTrace(TRACING_COMPONENT, "    " + bestActivation); //$NON-NLS-1$
			}
		}

		// Return the current best.
		if (conflict) {
			if (previousLogs.add(commandId)) {
				final StringWriter sw = new StringWriter();
				final BufferedWriter buffer = new BufferedWriter(sw);
				try {
					buffer.write("Conflict for \'"); //$NON-NLS-1$
					buffer.write(commandId);
					buffer.write("\':"); //$NON-NLS-1$
					buffer.newLine();
					buffer.write(bestActivation.toString());
					buffer.newLine();
					buffer.write(currentActivation.toString());
					buffer.flush();
				} catch (IOException e) {
					// should never get this.
				}

				IStatus s = new Status(IStatus.WARNING,
						"org.eclipse.ui.workbench", //$NON-NLS-1$
						sw.toString());
				conflicts.add(s);
			}
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
	protected final void sourceChanged(final int sourcePriority) {
		// If tracing, then track how long it takes to process the activations.
		long startTime = 0L;
		if (DEBUG_PERFORMANCE) {
			startTime = System.currentTimeMillis();
		}

		/*
		 * In this first phase, we cycle through all of the activations that
		 * could have potentially changed. Each such activation is added to a
		 * set for future processing. We add it to a set so that we avoid
		 * handling any individual activation more than once.
		 */
		final Collection changedCommandIds = new HashSet(
				ACTIVATIONS_TO_RECOMPUTE_SIZE);
		for (int i = 1; i <= 32; i++) {
			if ((sourcePriority & (1 << i)) != 0) {
				final Map activationsByExpression = activationsByExpressionBySourcePriority[i];
				if (activationsByExpression != null) {
					final Iterator activationByExpressionItr = activationsByExpression
							.values().iterator();
					while (activationByExpressionItr.hasNext()) {
						final Collection activations = (Collection) activationByExpressionItr
								.next();
						final Iterator activationItr = activations.iterator();

						// Check the first activation to see if it has changed.
						if (activationItr.hasNext()) {
							IHandlerActivation activation = (IHandlerActivation) activationItr
									.next();
							final boolean currentActive = evaluate(activation);
							activation.clearResult();
							final boolean newActive = evaluate(activation);
							if (newActive != currentActive) {
								changedCommandIds
										.add(activation.getCommandId());

								// Then add every other activation as well.
								while (activationItr.hasNext()) {
									activation = (IHandlerActivation) activationItr
											.next();
									activation.setResult(newActive);

									changedCommandIds.add(activation
											.getCommandId());
								}
							} else {
								while (activationItr.hasNext()) {
									activation = (IHandlerActivation) activationItr
											.next();
									// if for some reason another activation
									// doesn't match the new result, update and
									// mark as changed. It's not as expensive
									// as it looks :-)
									if (newActive != evaluate(activation)) {
										activation.setResult(newActive);
										changedCommandIds.add(activation
												.getCommandId());
									}
								}
							}
						}
					}
				}
			}
		}

		MultiStatus conflicts = new MultiStatus("org.eclipse.ui.workbench", 0, //$NON-NLS-1$
				"A handler conflict occurred.  This may disable some commands.", //$NON-NLS-1$
				null);

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
				updateCommand(commandId, (evaluate(activation) ? activation
						: null));
			} else if (value instanceof SortedSet) {
				final IHandlerActivation activation = resolveConflicts(
						commandId, (SortedSet) value, conflicts);
				updateCommand(commandId, activation);
			} else {
				updateCommand(commandId, null);
			}
		}
		if (conflicts.getSeverity() != IStatus.OK) {
			WorkbenchPlugin.log(conflicts);
		}

		// If tracing performance, then print the results.
		if (DEBUG_PERFORMANCE) {
			final long elapsedTime = System.currentTimeMillis() - startTime;
			final int size = changedCommandIds.size();
			if (size > 0) {
				Tracing.printTrace(TRACING_COMPONENT, size
						+ " command ids changed in " + elapsedTime + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
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
		final Command command = commandService.getCommand(commandId);
		if (activation == null) {
			command.setHandler(null);
		} else {
			command.setHandler(activation.getHandler());
			commandService.refreshElements(commandId, null);
		}
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

	/**
	 * Currently this is a an internal method to help locate a handler.
	 * <p>
	 * DO NOT CALL THIS METHOD.
	 * </p>
	 * 
	 * @param commandId
	 *            the command id to check
	 * @param context
	 *            the context to use for activations
	 * @since 3.3
	 */
	public final IHandler findHandler(String commandId,
			IEvaluationContext context) {
		Object o = handlerActivationsByCommandId.get(commandId);
		if (o instanceof IHandlerActivation) {
			IHandlerActivation activation = (IHandlerActivation) o;
			try {
				if (eval(context, activation)) {
					return activation.getHandler();
				}
			} catch (CoreException e) {
				// the evalution failed
			}
		} else if (o instanceof SortedSet) {
			SortedSet activations = (SortedSet) o;
			IHandlerActivation lastActivation = null;
			IHandlerActivation currentActivation = null;
			Iterator i = activations.iterator();
			while (i.hasNext() && lastActivation == null) {
				IHandlerActivation activation = (IHandlerActivation) i.next();
				try {
					if (eval(context, activation)) {
						lastActivation = currentActivation;
						currentActivation = activation;
					}
				} catch (CoreException e) {
					// OK, this one is out of the running
				}
			}
			if (currentActivation != null) {
				if (lastActivation == null) {
					return currentActivation.getHandler();
				}
				if (lastActivation.getSourcePriority() != currentActivation
						.getSourcePriority()) {
					return lastActivation.getHandler();
				}
			}
		}
		return null;
	}

	/**
	 * Evaluate the expression for the handler and bypass the result cache.
	 * <p>
	 * DO NOT CALL THIS METHOD.
	 * </p>
	 * 
	 * @param context
	 * @param activation
	 * @return <code>true</code> if the handler expression can evaluate to
	 *         true.
	 * @throws CoreException
	 * @since 3.3
	 */
	private boolean eval(IEvaluationContext context,
			IHandlerActivation activation) throws CoreException {
		Expression expression = activation.getExpression();
		if (expression == null) {
			return true;
		}
		return expression.evaluate(context) == EvaluationResult.TRUE;
	}

	public IEvaluationContext createContextSnapshot(boolean includeSelection) {
		IEvaluationContext tmpContext = getCurrentState();

		EvaluationContext context = null;
		if (includeSelection) {
			context = new EvaluationContext(null, tmpContext
					.getDefaultVariable());
			for (int i = 0; i < SELECTION_VARIABLES.length; i++) {
				copyVariable(context, tmpContext, SELECTION_VARIABLES[i]);
			}
		} else {
			context = new EvaluationContext(null, Collections.EMPTY_LIST);
		}

		ISourceProviderService sp = (ISourceProviderService) locator
				.getService(ISourceProviderService.class);
		ISourceProvider[] providers = sp.getSourceProviders();
		for (int i = 0; i < providers.length; i++) {
			String[] names = providers[i].getProvidedSourceNames();
			for (int j = 0; j < names.length; j++) {
				if (!isSelectionVariable(names[j])) {
					copyVariable(context, tmpContext, names[j]);
				}
			}
		}
		
		return context;
	}
	
	private boolean isSelectionVariable(String name) {
		for (int i = 0; i < SELECTION_VARIABLES.length; i++) {
			if (SELECTION_VARIABLES[i].equals(name)) {
				return true;
			}
		}
		return false;
	}

	private void copyVariable(IEvaluationContext context,
			IEvaluationContext tmpContext, String var) {
		Object o = tmpContext.getVariable(var);
		if (o != null) {
			context.addVariable(var, o);
		}
	}
}
