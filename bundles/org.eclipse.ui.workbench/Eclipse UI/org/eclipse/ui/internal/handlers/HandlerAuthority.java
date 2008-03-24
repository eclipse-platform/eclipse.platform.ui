/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.Policy;
import org.eclipse.ui.internal.services.EvaluationResultCacheComparator;
import org.eclipse.ui.internal.services.EvaluationService;
import org.eclipse.ui.internal.services.IEvaluationResultCache;
import org.eclipse.ui.services.IEvaluationService;
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
final class HandlerAuthority {

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

	private Collection changedCommandIds = new HashSet();

	private IPropertyChangeListener serviceListener;

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

	private IEvaluationService evalService = null;

	private IEvaluationService getEvaluationService() {
		if (evalService == null) {
			evalService = (IEvaluationService) locator
					.getService(IEvaluationService.class);
			evalService.addServiceListener(getServiceListener());
		}
		return evalService;
	}

	private IPropertyChangeListener getServiceListener() {
		if (serviceListener == null) {
			serviceListener = new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					if (IEvaluationService.PROP_NOTIFYING.equals(event
							.getProperty())) {
						if (event.getNewValue() instanceof Boolean) {
							boolean startNotifying = ((Boolean) event
									.getNewValue()).booleanValue();
							if (startNotifying) {
								changedCommandIds.clear();
							} else {
								processChangedCommands();
							}
						}
					}
				}
			};
		}
		return serviceListener;
	}

	public void dispose() {
		if (serviceListener != null) {
			getEvaluationService().removeServiceListener(serviceListener);
			serviceListener = null;
		}
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
		final HandlerActivation handler = (HandlerActivation) activation;

		// First we update the handlerActivationsByCommandId map.
		final String commandId = handler.getCommandId();
		MultiStatus conflicts = new MultiStatus("org.eclipse.ui.workbench", 0, //$NON-NLS-1$
				"A handler conflict occurred.  This may disable some commands.", //$NON-NLS-1$
				null);
		final Object value = handlerActivationsByCommandId.get(commandId);
		if (value instanceof SortedSet) {
			final SortedSet handlerActivations = (SortedSet) value;
			if (!handlerActivations.contains(handler)) {
				handlerActivations.add(handler);
				if (handler.getExpression() != null) {
					HandlerPropertyListener l = new HandlerPropertyListener(
							handler);
					handler.setReference(getEvaluationService()
							.addEvaluationListener(handler.getExpression(), l,
									handler.getCommandId()));
				}
				updateCommand(commandId, resolveConflicts(commandId,
						handlerActivations, conflicts));
			}
		} else if (value instanceof IHandlerActivation) {
			if (value != handler) {
				final SortedSet handlerActivations = new TreeSet(
						new EvaluationResultCacheComparator());
				handlerActivations.add(value);
				handlerActivations.add(handler);
				if (handler.getExpression() != null) {
					HandlerPropertyListener l = new HandlerPropertyListener(
							handler);
					handler.setReference(getEvaluationService()
							.addEvaluationListener(handler.getExpression(), l,
									handler.getCommandId()));
				}
				handlerActivationsByCommandId
						.put(commandId, handlerActivations);
				updateCommand(commandId, resolveConflicts(commandId,
						handlerActivations, conflicts));
			}
		} else {
			handlerActivationsByCommandId.put(commandId, handler);
			if (handler.getExpression() != null) {
				HandlerPropertyListener l = new HandlerPropertyListener(handler);
				handler.setReference(getEvaluationService()
						.addEvaluationListener(handler.getExpression(), l,
								handler.getCommandId()));
			}
			updateCommand(commandId, (evaluate(handler) ? handler : null));
		}

		if (conflicts.getSeverity() != IStatus.OK) {
			WorkbenchPlugin.log(conflicts);
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
		final HandlerActivation handler = (HandlerActivation) activation;

		// First we update the handlerActivationsByCommandId map.
		final String commandId = handler.getCommandId();
		MultiStatus conflicts = new MultiStatus("org.eclipse.ui.workbench", 0, //$NON-NLS-1$
				"A handler conflict occurred.  This may disable some commands.", //$NON-NLS-1$
				null);
		final Object value = handlerActivationsByCommandId.get(commandId);
		if (value instanceof SortedSet) {
			final SortedSet handlerActivations = (SortedSet) value;
			if (handlerActivations.contains(handler)) {
				handlerActivations.remove(handler);
				if (handler.getReference() != null) {
					getEvaluationService().removeEvaluationListener(
							handler.getReference());
					handler.setReference(null);
					handler.setListener(null);
				}
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
			if (value == handler) {
				if (handler.getReference() != null) {
					getEvaluationService().removeEvaluationListener(
							handler.getReference());
					handler.setReference(null);
					handler.setListener(null);
				}

				handlerActivationsByCommandId.remove(commandId);
				updateCommand(commandId, null);
			}
		}
		if (conflicts.getSeverity() != IStatus.OK) {
			WorkbenchPlugin.log(conflicts);
		}
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
		// we don't do this anymore ... we just keep walking.
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

	private class HandlerPropertyListener implements IPropertyChangeListener {
		private HandlerActivation handler;

		public HandlerPropertyListener(final HandlerActivation activation) {
			handler = activation;
			handler.setListener(this);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent event) {
			if (handler.getCommandId().equals(event.getProperty())) {
				boolean val = false;
				if (event.getNewValue() instanceof Boolean) {
					val = ((Boolean) event.getNewValue()).booleanValue();
				}
				handler.setResult(val);
				changedCommandIds.add(handler.getCommandId());
			}
		}
	}

	private void processChangedCommands() {
		// If tracing, then track how long it takes to process the activations.
		long startTime = 0L;
		if (DEBUG_PERFORMANCE) {
			startTime = System.currentTimeMillis();
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

	protected final boolean evaluate(final IEvaluationResultCache expression) {
		final IEvaluationContext contextWithDefaultVariable = getCurrentState();
		return expression.evaluate(contextWithDefaultVariable);
	}

	public final IEvaluationContext getCurrentState() {
		return getEvaluationService().getCurrentState();
	}

	public void updateShellKludge() {
		((EvaluationService) getEvaluationService()).updateShellKludge();
	}

	public void updateShellKludge(Shell shell) {
		((EvaluationService) getEvaluationService()).updateShellKludge(shell);
	}
}
